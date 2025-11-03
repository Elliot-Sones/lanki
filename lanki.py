import os
import sys
import json
import argparse
import requests
from datetime import datetime

LEETCODE_GRAPHQL_URL = "https://leetcode.com/graphql"

# ----------------------------------------------------------------------
# Authentication: use env or a local .env (untracked). Do not hardcode tokens.
# Expected env vars:
#   - LEETCODE_SESSION (cookie value only)
#   - CSRFTOKEN or csrftoken (cookie value only)
# ----------------------------------------------------------------------


def load_env_file(path: str = ".env") -> None:
    """Minimal .env loader. Lines like KEY=VALUE; ignores comments/blank lines.
    Does not overwrite existing os.environ values.
    """
    if not os.path.exists(path):
        return
    try:
        with open(path, "r", encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if not line or line.startswith("#"):
                    continue
                if "=" not in line:
                    continue
                key, val = line.split("=", 1)
                key = key.strip()
                # Strip optional surrounding quotes
                val = val.strip().strip("\"'\"")
                if key and key not in os.environ:
                    os.environ[key] = val
    except Exception:
        # Non-fatal; just skip if .env unreadable
        pass


def _build_session(session_cookie: str, csrf_cookie: str) -> requests.Session:
    """Creates an authenticated requests session using the provided cookies."""
    s = requests.Session()
    s.headers.update(
        {
            "Content-Type": "application/json",
            "Origin": "https://leetcode.com",
            "Referer": "https://leetcode.com/",
            "x-csrftoken": csrf_cookie,  # Sent as a header
            "User-Agent": (
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) "
                "AppleWebKit/537.36 (KHTML, like Gecko) "
                "Chrome/119.0.0.0 Safari/537.36"
            ),
        }
    )
    # Sent as cookies
    s.cookies.set("LEETCODE_SESSION", session_cookie, domain="leetcode.com")
    s.cookies.set("csrftoken", csrf_cookie, domain="leetcode.com")
    return s


def execute_graphql_query(
    s: requests.Session, query_str: str, variables: dict | None = None, operation_name: str | None = None
) -> dict:
    """Helper to execute any authenticated GraphQL query."""
    if variables is None:
        variables = {}

    query = {
        "query": query_str,
        "variables": variables,
        "operationName": operation_name,
    }
    resp = s.post(LEETCODE_GRAPHQL_URL, data=json.dumps(query), timeout=20)
    try:
        resp.raise_for_status()
    except requests.exceptions.HTTPError as e:
        # Try to surface GraphQL errors if present even on 4xx
        try:
            payload = resp.json()
            if payload.get("errors"):
                raise RuntimeError(f"GraphQL errors: {payload['errors']}") from e
        except Exception:
            pass
        raise
    payload = resp.json()

    if "errors" in payload and payload["errors"]:
        raise RuntimeError(f"GraphQL errors: {payload['errors']}")
    if "data" not in payload or not payload["data"]:
        raise RuntimeError(
            "Empty GraphQL response data or blocked by Cloudflare. Try updating your cookies."
        )
    return payload["data"]


def query_user_status(s: requests.Session) -> dict:
    """Checks the user's basic signed-in status to confirm authentication."""
    query_str = "query { userStatus { username isSignedIn } }"
    data = execute_graphql_query(s, query_str)
    return data["userStatus"]


# Query minimal fields for latest accepted submission(s)
LATEST_SUBMISSION_QUERY = """
query latestAcSubmission($username: String!, $limit: Int!) {
  recentAcSubmissionList(username: $username, limit: $limit) {
    id
    title
    titleSlug
    timestamp
    statusDisplay
    lang
  }
}
"""


def query_latest_submission(s: requests.Session, username: str) -> dict | None:
    variables = {"username": username, "limit": 1}
    data = execute_graphql_query(
        s,
        LATEST_SUBMISSION_QUERY,
        variables=variables,
        operation_name="latestAcSubmission",
    )

    if data["recentAcSubmissionList"]:
        return data["recentAcSubmissionList"][0]
    return None


def iterate_submissions(s: requests.Session, page_size: int = 20, max_pages: int | None = None):
    """Yield all submissions using the legacy REST pagination API.

    Typical response:
      - submissions_dump: list[submission]
      - has_next: bool
      - last_key: str  (pass as 'lastkey' query param)
    """
    base_url = "https://leetcode.com/api/submissions/"
    last_key: str | None = None
    page = 0

    while True:
        params = {"offset": 0, "limit": page_size}
        if last_key:
            params["lastkey"] = last_key
        resp = s.get(base_url, params=params, timeout=30)
        try:
            resp.raise_for_status()
        except requests.exceptions.HTTPError as e:
            try:
                detail = resp.json()
            except Exception:
                detail = None
            raise RuntimeError(f"Submissions fetch failed: {e.response.status_code} {detail}") from e

        payload = resp.json()
        items = payload.get("submissions_dump", [])
        for sub in items:
            yield sub

        if not payload.get("has_next"):
            break
        last_key = payload.get("last_key") or payload.get("lastKey") or last_key
        page += 1
        if max_pages is not None and page >= max_pages:
            break


def fetch_all_submissions(s: requests.Session, accepted_only: bool | None = None) -> list[dict]:
    subs: list[dict] = []
    for sub in iterate_submissions(s):
        status = sub.get("status_display")
        if accepted_only is True and status != "Accepted":
            continue
        if accepted_only is False and status == "Accepted":
            continue
        subs.append(sub)

    subs.sort(key=lambda x: int(x.get("timestamp", 0)))
    return subs


def dedupe_by_problem_latest(submissions: list[dict]) -> list[dict]:
    latest: dict[str, dict] = {}
    for sub in submissions:
        slug = sub.get("title_slug") or sub.get("titleSlug")
        if not slug:
            slug = f"{sub.get('title')}".strip()
        ts = int(sub.get("timestamp", 0))
        cur = latest.get(slug)
        if not cur or ts >= int(cur.get("timestamp", 0)):
            latest[slug] = sub
    result = list(latest.values())
    result.sort(key=lambda x: int(x.get("timestamp", 0)))
    return result


def print_submission_list(submissions: list[dict]):
    print(f"\n--- Submissions ({len(submissions)}) ---")
    for sub in submissions:
        title = sub.get("title")
        slug = sub.get("title_slug") or sub.get("titleSlug")
        status = sub.get("status_display") or sub.get("statusDisplay")
        lang = sub.get("lang") or sub.get("lang_name")
        ts = int(sub.get("timestamp", 0))
        dt = datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S') if ts else ""
        print(f"{dt} | {status:9} | {lang:8} | {title} ({slug})")


def process_submission_data(submission: dict):
    submission_time = datetime.fromtimestamp(int(submission['timestamp']))

    print("\n--- NEW SUBMISSION METRICS ---")
    print(f"Problem Title: {submission['title']} ({submission['titleSlug']})")
    print(f"Last Review Date: {submission_time.strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"Status: {submission['statusDisplay']}")
    print(f"Language: {submission['lang']}")


def main() -> int:
    try:
        parser = argparse.ArgumentParser(description="LeetCode helper")
        subparsers = parser.add_subparsers(dest="cmd")

        p_list = subparsers.add_parser("list", help="List your submissions")
        p_list.add_argument("--accepted-only", action="store_true", help="Only Accepted submissions")
        p_list.add_argument("--unique", action="store_true", help="Only latest per problem")
        p_list.add_argument("--page-size", type=int, default=20, help="Pagination size for REST API")

        args = parser.parse_args()

        # Load env from .env if present (non-destructive)
        load_env_file()

        # Require env cookies
        session_cookie = os.environ.get("LEETCODE_SESSION", "").strip()
        csrf_cookie = (os.environ.get("CSRFTOKEN") or os.environ.get("csrftoken") or "").strip()
        if not session_cookie or not csrf_cookie:
            print("Missing LEETCODE_SESSION and/or CSRFTOKEN env vars. Set them or add to .env.")
            return 2

        s = _build_session(session_cookie, csrf_cookie)

        # 1. Test authentication
        status = query_user_status(s)

        if status.get("isSignedIn"):
            username = status.get('username') or ""
            if not username:
                # Fallback: cannot infer username reliably without userStatus
                print("Authenticated but username not returned. Proceeding without it.")
            print(f"✅ Successfully connected as: {username or 'unknown-user'}")

            if args.cmd == "list":
                all_subs = list(iterate_submissions(s, page_size=args.page_size))
                if args.accepted_only:
                    all_subs = [x for x in all_subs if x.get("status_display") == "Accepted"]
                if args.unique:
                    all_subs = dedupe_by_problem_latest(all_subs)
                all_subs.sort(key=lambda x: int(x.get("timestamp", 0)))
                print_submission_list(all_subs)
                return 0

            # Default behavior: show latest accepted submission
            if username:
                latest_submission = query_latest_submission(s, username)
                if latest_submission:
                    process_submission_data(latest_submission)
                else:
                    print("⚠️ Found no recent accepted submissions to track.")
            else:
                print("⚠️ Username unknown; run with 'list' to verify submissions.")
            return 0

        print("❌ Connected but not signed in. Check your cookies (they may have expired).")
        return 1

    except requests.exceptions.HTTPError as e:
        print(f"❌ Authentication failed: HTTP Error {e.response.status_code}. Tokens may be expired.")
        return 2
    except Exception as e:
        print(f"❌ Authentication failed: {type(e).__name__}: {e}")
        return 2


if __name__ == "__main__":
    sys.exit(main())


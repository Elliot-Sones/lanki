# Lanki - Quick Start Guide

## Get Started in 3 Steps

### Step 1: Start the Backend (5 minutes)

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

✅ Backend running on http://localhost:8080

### Step 2: Start the Frontend (2 minutes)

```bash
cd frontend
npm install
npm run dev
```

✅ Frontend running on http://localhost:5173

### Step 3: Try It Out!

1. Open http://localhost:5173 in your browser
2. Navigate to "All Problems"
3. Click on any problem (e.g., "Two Sum")
4. Modal appears → Click "Yes, Start Problem"
5. LeetCode opens in new tab + timer starts
6. Solve problem on LeetCode and submit
7. Return to Lanki → you'll see completion notification!

## How It Works

### The Flow

```
[Click Problem]
    → [Start Confirmation Modal]
    → [Timer Starts + LeetCode Opens]
    → [Solve on LeetCode]
    → [Submit Solution]
    → [Lanki Detects Submission (polls every 5s)]
    → [Completion Modal: "You finished in X minutes!"]
    → [Start Interview (future)]
    → [SM-2 updates next review date]
```

### Key Features

1. **Daily Problems**: Homepage shows problems due today based on spaced repetition
2. **All Problems**: Browse all 150 NeetCode problems by category
3. **Automatic Detection**: No manual input needed - Lanki watches for your submissions
4. **Smart Scheduling**: SM-2 algorithm ensures optimal review timing

## Configure LeetCode Integration (Optional)

For automatic submission detection to work, add your LeetCode cookies:

1. Login to leetcode.com
2. Open DevTools → Application → Cookies
3. Copy `LEETCODE_SESSION` and `csrftoken` values
4. Save to database or via API (see SETUP_GUIDE.md)

**Note**: For MVP testing, you can skip this and manually test the UI flow.

## Troubleshooting

**Problem: Frontend shows "Failed to load problems"**
- Solution: Make sure backend is running on port 8080

**Problem: Submission not detected**
- Solution: LeetCode credentials not configured (see SETUP_GUIDE.md)

**Problem: Port 8080 already in use**
- Solution: Change port in `backend/src/main/resources/application.yml`

## What's Next?

- See [SETUP_GUIDE.md](./SETUP_GUIDE.md) for detailed setup
- Check [README.md](./README.md) for project overview
- Browse code in `backend/` and `frontend/` directories

## Tech Stack

- **Backend**: Spring Boot 3.2 + H2 Database + Spring Data JPA
- **Frontend**: React 18 + Vite + Tailwind CSS
- **Integration**: LeetCode GraphQL API
- **Algorithm**: SuperMemo 2 (SM-2) for spaced repetition

Enjoy mastering LeetCode with Lanki! 🚀

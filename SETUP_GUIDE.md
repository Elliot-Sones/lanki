# Lanki - Setup Guide

## Overview

Lanki is a spaced repetition system for LeetCode problems, inspired by Anki. It helps you master coding problems through:

- **Spaced Repetition (SM-2 Algorithm)**: Automatically schedules problem reviews based on your performance
- **LeetCode Integration**: Detects your submissions automatically via GraphQL API
- **AI Interview System**: Evaluates your understanding through conversational questions (coming soon)
- **Progress Tracking**: Visualize your progress across 150 NeetCode problems

## Architecture

```
lanki/
├── backend/          # Spring Boot REST API
│   ├── src/main/java/com/lanki/
│   │   ├── model/           # JPA entities
│   │   ├── repository/      # Data access layer
│   │   ├── service/         # Business logic
│   │   ├── controller/      # REST endpoints
│   │   └── config/          # Configuration
│   └── src/main/resources/
│       ├── application.yml  # App configuration
│       └── data.sql         # 150 NeetCode problems seed data
└── frontend/         # React + Vite
    └── src/
        ├── components/      # React components
        ├── pages/           # Page components
        └── services/        # API client
```

## Prerequisites

- **Java 17+** (for Spring Boot backend)
- **Maven** (for building the backend)
- **Node.js 18+** and **npm** (for React frontend)
- **LeetCode Account** with session cookies (for submission tracking)

## Installation

### 1. Backend Setup

```bash
# Navigate to backend directory
cd backend

# Install dependencies and build
mvn clean install

# Run the Spring Boot application
mvn spring-boot:run
```

The backend will start on **http://localhost:8080**

You can access the H2 database console at **http://localhost:8080/h2-console**:
- JDBC URL: `jdbc:h2:mem:lankidb`
- Username: `sa`
- Password: (leave empty)

### 2. Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

The frontend will start on **http://localhost:5173**

## User Flow

### 1. Daily Problems Page (Homepage)

- Shows problems due for review based on SM-2 algorithm
- Click on any problem card to start

### 2. Starting a Problem

1. Click on a problem card
2. Modal appears: "Ready to start the LeetCode problem?"
3. Click "Yes, Start Problem"
4. **Timer starts** and LeetCode opens in a new tab
5. Frontend polls backend **every 5 seconds** for submission

### 3. Solving on LeetCode

- Solve the problem on LeetCode
- Submit your solution
- Lanki automatically detects your submission via GraphQL API

### 4. Submission Detection

- When submission is detected:
  - Timer stops
  - Completion modal appears: "You finished the problem in X minutes!"
  - Shows submission status (Accepted, etc.)

### 5. AI Interview (Future Feature)

- Click "Start Interview" to explain your solution
- AI asks questions about approach, complexity, edge cases
- Score determines next review date via SM-2 algorithm

### 6. All Problems Page

- Browse all 150 NeetCode problems grouped by category
- Each category shows progress (completed/in-progress)
- Collapsible sections for easy navigation

## LeetCode Integration Setup

To enable automatic submission detection, you need to provide your LeetCode session cookies:

### 1. Get Your LeetCode Cookies

1. Log in to [leetcode.com](https://leetcode.com)
2. Open browser DevTools (F12)
3. Go to **Application** > **Cookies** > `https://leetcode.com`
4. Copy the values for:
   - `LEETCODE_SESSION`
   - `csrftoken`

### 2. Configure in Lanki

For now (MVP), you'll need to manually add these to the database or use the API:

**Option 1: Via API (Coming Soon)**
```bash
POST http://localhost:8080/api/user/leetcode-credentials
{
  "sessionToken": "your_LEETCODE_SESSION_value",
  "csrfToken": "your_csrftoken_value",
  "username": "your-leetcode-username"
}
```

**Option 2: Update Python Script**
You can use the existing `lanki.py` script to test LeetCode integration:
```bash
python lanki.py list --accepted-only
```

## Database Schema

### Key Tables

**problems**: 150 NeetCode problems with LeetCode URLs
**users**: User accounts with LeetCode credentials
**problem_sessions**: Active problem-solving sessions with timer
**submissions**: LeetCode submissions detected via API
**spaced_repetition_cards**: SM-2 algorithm data per problem
**user_progress**: Overall progress tracking (completed, in-progress)
**interviews**: AI interview transcripts and scores (future)

## API Endpoints

### Problems
- `GET /api/problems` - Get all problems grouped by category
- `GET /api/problems/daily` - Get today's recommended problems
- `GET /api/problems/{id}` - Get specific problem details

### Sessions
- `POST /api/sessions/start` - Start problem session (begins timer)
- `GET /api/sessions/{id}/check` - Check for submission (poll every 5s)
- `GET /api/sessions/{id}` - Get session details
- `POST /api/sessions/{id}/abandon` - Abandon session

### User
- `GET /api/user/profile` - Get user profile
- `POST /api/user/leetcode-credentials` - Save LeetCode credentials

## Spaced Repetition (SM-2) Algorithm

Lanki uses the SuperMemo 2 algorithm to schedule reviews:

1. **First Review**: 1 day after initial completion
2. **Second Review**: 6 days after first review
3. **Subsequent Reviews**: Interval increases based on performance

**Quality Rating (0-5)**:
- 5: Perfect response (90%+ overall score)
- 4: Good (80-89%)
- 3: Acceptable (70-79%)
- 2: Hesitant (50-69%)
- 1: Difficult (30-49%)
- 0: Total failure (<30%)

The algorithm adjusts the ease factor based on your performance, ensuring you review problems at optimal intervals.

## Technologies Used

**Backend**:
- Spring Boot 3.2
- Spring Data JPA
- H2 Database (dev) / PostgreSQL (prod)
- Spring Security
- Jackson for JSON
- RestTemplate for LeetCode API

**Frontend**:
- React 18
- Vite (build tool)
- React Router (navigation)
- Axios (HTTP client)
- Tailwind CSS (styling)

## Troubleshooting

### Backend won't start
- Ensure Java 17+ is installed: `java --version`
- Check if port 8080 is available
- Check logs for database errors

### Frontend can't connect to backend
- Verify backend is running on port 8080
- Check browser console for CORS errors
- Ensure API URLs in `frontend/src/services/api.js` are correct

### LeetCode submissions not detected
- Verify your session cookies are valid (they expire)
- Check that LeetCode username matches your account
- Test GraphQL query manually using the Python script

### H2 Database data lost on restart
- This is expected with in-memory H2 database
- For persistence, switch to PostgreSQL in `application.yml`

## Next Steps

1. **Add Authentication**: Implement JWT-based user authentication
2. **AI Interview**: Integrate OpenAI GPT-4 for interview questions
3. **LeetCode OAuth**: Replace manual cookie entry with OAuth flow
4. **Analytics Dashboard**: Visualize learning progress over time
5. **Mobile App**: React Native version
6. **Problem Recommendations**: ML-based suggestions beyond SM-2

## Contributing

This is a personal project, but suggestions and improvements are welcome!

## License

MIT License - feel free to use and modify as needed.

import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Problem APIs
export const getAllProblems = () => api.get('/problems');

export const getDailyProblems = () => api.get('/problems/daily');

export const getProblem = (id) => api.get(`/problems/${id}`);

// Session APIs
export const startSession = (problemId) =>
  api.post('/sessions/start', { problemId });

export const checkSession = (sessionId) =>
  api.get(`/sessions/${sessionId}/check`);

export const getSession = (sessionId) =>
  api.get(`/sessions/${sessionId}`);

export const abandonSession = (sessionId) =>
  api.post(`/sessions/${sessionId}/abandon`);

export const getActiveSessions = () =>
  api.get('/sessions/active');

// User APIs
export const getUserProfile = () => api.get('/user/profile');

export const saveLeetCodeCredentials = (credentials) =>
  api.post('/user/leetcode-credentials', credentials);

export default api;

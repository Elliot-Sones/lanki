import { useState, useEffect } from 'react';
import { getDailyProblems } from '../services/api';
import ProblemCard from '../components/ProblemCard';

function HomePage() {
  const [dailyProblems, setDailyProblems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadDailyProblems();
  }, []);

  const loadDailyProblems = async () => {
    try {
      setLoading(true);
      const response = await getDailyProblems();
      setDailyProblems(response.data);
      setError(null);
    } catch (err) {
      console.error('Error loading daily problems:', err);
      setError('Failed to load daily problems. Make sure the backend is running on port 8080.');
    } finally {
      setLoading(false);
    }
  };

  const calculateDaysOverdue = (problem) => {
    if (!problem.isOverdue) return 0;
    return problem.daysSinceLastReview || 0;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-accent"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-2xl mx-auto mt-8">
        <div className="bg-red-900 bg-opacity-30 border border-red-700 rounded-lg p-4 text-red-200">
          <p className="font-semibold mb-2">Error</p>
          <p>{error}</p>
          <button
            onClick={loadDailyProblems}
            className="mt-4 px-4 py-2 bg-red-700 hover:bg-red-600 rounded text-white text-sm"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  return (
    <div>
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-white mb-2">Daily Practice</h1>
        <p className="text-gray-400">
          {dailyProblems.length} problem{dailyProblems.length !== 1 ? 's' : ''} due for review today
        </p>
      </div>

      {/* Problems List */}
      {dailyProblems.length === 0 ? (
        <div className="bg-secondary rounded-lg p-8 text-center">
          <svg
            className="mx-auto h-16 w-16 text-gray-600 mb-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          <h3 className="text-xl font-semibold text-white mb-2">
            All caught up!
          </h3>
          <p className="text-gray-400 mb-4">
            No problems due for review today. Check back tomorrow or practice from all problems.
          </p>
          <a
            href="/problems"
            className="inline-block px-4 py-2 bg-accent text-white rounded-lg hover:bg-orange-600 transition-colors"
          >
            Browse All Problems
          </a>
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {dailyProblems.map((dailyProblem) => (
            <ProblemCard
              key={dailyProblem.problem.id}
              problem={dailyProblem.problem}
              isDailyProblem={true}
              daysOverdue={calculateDaysOverdue(dailyProblem)}
            />
          ))}
        </div>
      )}

      {/* Stats Section */}
      {dailyProblems.length > 0 && (
        <div className="mt-8 grid grid-cols-3 gap-4">
          <div className="bg-secondary rounded-lg p-4 text-center">
            <div className="text-2xl font-bold text-accent">
              {dailyProblems.length}
            </div>
            <div className="text-sm text-gray-400">Due Today</div>
          </div>
          <div className="bg-secondary rounded-lg p-4 text-center">
            <div className="text-2xl font-bold text-green-500">
              {dailyProblems.filter(p => !p.isOverdue).length}
            </div>
            <div className="text-sm text-gray-400">On Schedule</div>
          </div>
          <div className="bg-secondary rounded-lg p-4 text-center">
            <div className="text-2xl font-bold text-red-500">
              {dailyProblems.filter(p => p.isOverdue).length}
            </div>
            <div className="text-sm text-gray-400">Overdue</div>
          </div>
        </div>
      )}
    </div>
  );
}

export default HomePage;

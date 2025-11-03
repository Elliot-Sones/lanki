import { useState, useEffect } from 'react';
import { getAllProblems } from '../services/api';
import ProblemCard from '../components/ProblemCard';

function AllProblemsPage() {
  const [problemsByCategory, setProblemsByCategory] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [expandedCategories, setExpandedCategories] = useState({});

  useEffect(() => {
    loadProblems();
  }, []);

  const loadProblems = async () => {
    try {
      setLoading(true);
      const response = await getAllProblems();
      const problems = response.data;

      // Initialize all categories as expanded
      const initialExpanded = {};
      Object.keys(problems).forEach(category => {
        initialExpanded[category] = true;
      });
      setExpandedCategories(initialExpanded);

      setProblemsByCategory(problems);
      setError(null);
    } catch (err) {
      console.error('Error loading problems:', err);
      setError('Failed to load problems. Make sure the backend is running on port 8080.');
    } finally {
      setLoading(false);
    }
  };

  const toggleCategory = (category) => {
    setExpandedCategories(prev => ({
      ...prev,
      [category]: !prev[category]
    }));
  };

  const getCategoryStats = (problems) => {
    const total = problems.length;
    const completed = problems.filter(p => p.progressStatus === 'COMPLETED').length;
    const inProgress = problems.filter(p => p.progressStatus === 'IN_PROGRESS').length;
    return { total, completed, inProgress };
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
            onClick={loadProblems}
            className="mt-4 px-4 py-2 bg-red-700 hover:bg-red-600 rounded text-white text-sm"
          >
            Retry
          </button>
        </div>
      </div>
    );
  }

  const totalProblems = Object.values(problemsByCategory).reduce(
    (sum, problems) => sum + problems.length, 0
  );

  return (
    <div>
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-white mb-2">
          All Problems
        </h1>
        <p className="text-gray-400">
          {totalProblems} NeetCode problems organized by topic
        </p>
      </div>

      {/* Categories */}
      <div className="space-y-6">
        {Object.entries(problemsByCategory).map(([category, problems]) => {
          const { total, completed, inProgress } = getCategoryStats(problems);
          const isExpanded = expandedCategories[category];

          return (
            <div key={category} className="bg-secondary rounded-lg border border-gray-700">
              {/* Category Header */}
              <button
                onClick={() => toggleCategory(category)}
                className="w-full px-6 py-4 flex items-center justify-between hover:bg-gray-700 transition-colors rounded-lg"
              >
                <div className="flex items-center space-x-4">
                  <svg
                    className={`w-5 h-5 text-gray-400 transition-transform ${
                      isExpanded ? 'transform rotate-90' : ''
                    }`}
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M9 5l7 7-7 7"
                    />
                  </svg>
                  <h2 className="text-xl font-semibold text-white">
                    {category}
                  </h2>
                  <span className="text-sm text-gray-400">
                    {total} problems
                  </span>
                </div>
                <div className="flex items-center space-x-4 text-sm">
                  {completed > 0 && (
                    <span className="text-green-500">
                      {completed} completed
                    </span>
                  )}
                  {inProgress > 0 && (
                    <span className="text-yellow-500">
                      {inProgress} in progress
                    </span>
                  )}
                  <div className="w-32 bg-gray-700 rounded-full h-2">
                    <div
                      className="bg-green-500 h-2 rounded-full"
                      style={{ width: `${(completed / total) * 100}%` }}
                    ></div>
                  </div>
                </div>
              </button>

              {/* Problems Grid */}
              {isExpanded && (
                <div className="px-6 pb-6 grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                  {problems.map(problem => (
                    <ProblemCard
                      key={problem.id}
                      problem={problem}
                    />
                  ))}
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default AllProblemsPage;

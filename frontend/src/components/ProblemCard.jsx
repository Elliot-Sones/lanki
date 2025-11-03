import { useState } from 'react';
import StartProblemModal from './StartProblemModal';
import CompletionModal from './CompletionModal';
import { startSession, checkSession } from '../services/api';

function ProblemCard({ problem, isDailyProblem = false, daysOverdue = null }) {
  const [showStartModal, setShowStartModal] = useState(false);
  const [showCompletionModal, setShowCompletionModal] = useState(false);
  const [session, setSession] = useState(null);
  const [isPolling, setIsPolling] = useState(false);

  const difficultyColor = {
    'EASY': 'text-easy',
    'MEDIUM': 'text-medium',
    'HARD': 'text-hard',
  }[problem.difficulty] || 'text-gray-400';

  const statusColor = {
    'COMPLETED': 'text-green-500',
    'IN_PROGRESS': 'text-yellow-500',
    'NOT_STARTED': 'text-gray-500',
  }[problem.progressStatus] || 'text-gray-500';

  const handleCardClick = () => {
    setShowStartModal(true);
  };

  const handleStartProblem = async () => {
    try {
      const response = await startSession(problem.id);
      setSession(response.data);
      setShowStartModal(false);
      setIsPolling(true);

      // Open LeetCode in new tab
      window.open(problem.leetcodeUrl, '_blank');

      // Start polling for submission
      startPolling(response.data.id);
    } catch (error) {
      console.error('Error starting session:', error);
      alert('Failed to start session. Please try again.');
    }
  };

  const startPolling = (sessionId) => {
    const pollInterval = setInterval(async () => {
      try {
        const response = await checkSession(sessionId);
        const updatedSession = response.data;

        if (updatedSession.status === 'COMPLETED') {
          // Submission detected!
          clearInterval(pollInterval);
          setSession(updatedSession);
          setIsPolling(false);
          setShowCompletionModal(true);
        }
      } catch (error) {
        console.error('Error checking session:', error);
        clearInterval(pollInterval);
        setIsPolling(false);
      }
    }, 5000); // Poll every 5 seconds

    // Cleanup: stop polling after 2 hours
    setTimeout(() => {
      clearInterval(pollInterval);
      setIsPolling(false);
    }, 2 * 60 * 60 * 1000);
  };

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <>
      <div
        onClick={handleCardClick}
        className="bg-secondary rounded-lg p-4 hover:bg-gray-700 cursor-pointer transition-all border border-gray-700 hover:border-accent"
      >
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <h3 className="text-lg font-medium text-white mb-2 flex items-center">
              {problem.title}
              <svg
                className="w-4 h-4 ml-2 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"
                />
              </svg>
            </h3>
            <div className="flex items-center space-x-4 text-sm">
              <span className={`font-medium ${difficultyColor}`}>
                {problem.difficulty}
              </span>
              <span className="text-gray-400">{problem.category}</span>
              {problem.progressStatus && (
                <span className={`${statusColor}`}>
                  {problem.progressStatus.replace('_', ' ')}
                </span>
              )}
            </div>
            {isDailyProblem && daysOverdue !== null && daysOverdue > 0 && (
              <div className="mt-2">
                <span className="text-xs bg-red-900 text-red-200 px-2 py-1 rounded">
                  Overdue by {daysOverdue} days
                </span>
              </div>
            )}
          </div>
        </div>

        {isPolling && (
          <div className="mt-3 p-2 bg-blue-900 bg-opacity-30 rounded text-sm text-blue-200 flex items-center">
            <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-400 mr-2"></div>
            Waiting for LeetCode submission...
          </div>
        )}
      </div>

      {showStartModal && (
        <StartProblemModal
          problem={problem}
          onStart={handleStartProblem}
          onCancel={() => setShowStartModal(false)}
        />
      )}

      {showCompletionModal && session && (
        <CompletionModal
          problem={problem}
          session={session}
          onClose={() => setShowCompletionModal(false)}
        />
      )}
    </>
  );
}

export default ProblemCard;

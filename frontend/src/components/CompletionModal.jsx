function CompletionModal({ problem, session, onClose }) {
  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    const hours = Math.floor(mins / 60);
    const remainingMins = mins % 60;

    if (hours > 0) {
      return `${hours}h ${remainingMins}m ${secs}s`;
    }
    return `${mins}m ${secs}s`;
  };

  const handleStartInterview = () => {
    // TODO: Implement interview flow
    alert('AI Interview feature coming soon!');
    onClose();
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50">
      <div className="bg-secondary rounded-lg p-6 max-w-md w-full mx-4 border border-green-700">
        <div className="text-center">
          {/* Celebration Icon */}
          <div className="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-green-900 bg-opacity-30 mb-4">
            <svg
              className="h-8 w-8 text-green-400"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M5 13l4 4L19 7"
              />
            </svg>
          </div>

          <h2 className="text-2xl font-bold text-white mb-2">
            Submission Detected!
          </h2>

          <p className="text-gray-300 mb-6">
            You finished <span className="font-semibold text-accent">{problem.title}</span> in
          </p>

          {/* Time Display */}
          <div className="bg-primary rounded-lg p-4 mb-6">
            <div className="text-4xl font-bold text-accent">
              {formatTime(session.elapsedTimeSeconds)}
            </div>
            <div className="text-sm text-gray-400 mt-1">
              Status: {session.submission?.status || 'Unknown'}
            </div>
            {session.submission?.language && (
              <div className="text-sm text-gray-400">
                Language: {session.submission.language}
              </div>
            )}
          </div>

          <p className="text-gray-300 mb-6">
            Ready to start the interview to explain your solution?
          </p>

          <div className="flex flex-col space-y-3">
            <button
              onClick={handleStartInterview}
              className="w-full bg-accent text-white px-4 py-3 rounded-lg font-medium hover:bg-orange-600 transition-colors"
            >
              Start Interview
            </button>
            <button
              onClick={onClose}
              className="w-full bg-gray-700 text-gray-300 px-4 py-2 rounded-lg font-medium hover:bg-gray-600 transition-colors"
            >
              Skip for Now
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default CompletionModal;

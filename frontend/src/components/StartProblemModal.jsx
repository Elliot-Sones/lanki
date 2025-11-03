function StartProblemModal({ problem, onStart, onCancel }) {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50">
      <div className="bg-secondary rounded-lg p-6 max-w-md w-full mx-4 border border-gray-700">
        <h2 className="text-2xl font-bold text-white mb-4">
          Ready to start?
        </h2>
        <p className="text-gray-300 mb-6">
          Clicking "Yes" will:
        </p>
        <ul className="text-gray-300 mb-6 space-y-2 list-disc list-inside">
          <li>Start the timer</li>
          <li>Open <span className="font-semibold">{problem.title}</span> on LeetCode in a new tab</li>
          <li>Monitor for your submission (checking every 5 seconds)</li>
        </ul>
        <p className="text-sm text-gray-400 mb-6">
          Once you submit on LeetCode, you'll see a completion notification with your time!
        </p>
        <div className="flex space-x-3">
          <button
            onClick={onStart}
            className="flex-1 bg-accent text-white px-4 py-2 rounded-lg font-medium hover:bg-orange-600 transition-colors"
          >
            Yes, Start Problem
          </button>
          <button
            onClick={onCancel}
            className="flex-1 bg-gray-700 text-gray-300 px-4 py-2 rounded-lg font-medium hover:bg-gray-600 transition-colors"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  );
}

export default StartProblemModal;

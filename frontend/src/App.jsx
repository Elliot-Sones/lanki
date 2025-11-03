import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import HomePage from './pages/HomePage';
import AllProblemsPage from './pages/AllProblemsPage';
import './App.css';

function App() {
  return (
    <Router>
      <div className="min-h-screen bg-primary">
        {/* Navigation */}
        <nav className="bg-secondary border-b border-gray-700">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
            <div className="flex items-center justify-between h-16">
              <div className="flex items-center">
                <Link to="/" className="text-2xl font-bold text-accent">
                  Lanki
                </Link>
                <span className="ml-3 text-sm text-gray-400">
                  Anki for LeetCode
                </span>
              </div>
              <div className="flex space-x-4">
                <Link
                  to="/"
                  className="px-3 py-2 rounded-md text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
                >
                  Daily Problems
                </Link>
                <Link
                  to="/problems"
                  className="px-3 py-2 rounded-md text-sm font-medium text-gray-300 hover:bg-gray-700 hover:text-white"
                >
                  All Problems
                </Link>
              </div>
            </div>
          </div>
        </nav>

        {/* Main Content */}
        <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <Routes>
            <Route path="/" element={<HomePage />} />
            <Route path="/problems" element={<AllProblemsPage />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;

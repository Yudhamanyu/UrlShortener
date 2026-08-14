import { Routes, Route } from 'react-router-dom';
import HomePage from './pages/HomePage';
import UrlResultPage from './pages/UrlResultPage';
import AnalyticsDashboardPage from './pages/AnalyticsDashboardPage';
import NotFoundPage from './pages/NotFoundPage';

function App() {
  return (
    <div className="app-container">
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/result/:shortCode" element={<UrlResultPage />} />
        <Route path="/analytics/:shortCode" element={<AnalyticsDashboardPage />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </div>
  );
}

export default App;


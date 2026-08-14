import { useParams, Link } from 'react-router-dom';
import Loader from '@/components/Loader';
import ErrorMessage from '@/components/ErrorMessage';
import AnalyticsTable from '@/components/AnalyticsTable';
import { useAnalytics } from '@/hooks/useAnalytics';
import { formatDateTime, formatDate } from '@/utils/formatters';

export default function AnalyticsDashboardPage() {
  const { shortCode } = useParams<{ shortCode: string }>();
  const { data, loading, error } = useAnalytics(shortCode);

  if (loading) {
    return (
      <div className="page">
        <Loader />
      </div>
    );
  }

  if (error || !data) {
    return (
      <div className="page">
        <ErrorMessage message={error ?? 'Analytics not found'} />
        <Link to="/" className="link">
          Back to Home
        </Link>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page-title">Analytics</div>
      <div className="page-subtitle">{data.originalUrl}</div>

      <div className="stats-grid">
        <div className="stat-box">
          <div className="stat-label">Total Clicks</div>
          <div className="stat-value">{data.totalClicks}</div>
        </div>
        <div className="stat-box">
          <div className="stat-label">First Visit</div>
          <div className="stat-value" style={{ fontSize: '14px' }}>
            {formatDateTime(data.firstVisit)}
          </div>
        </div>
        <div className="stat-box">
          <div className="stat-label">Last Visit</div>
          <div className="stat-value" style={{ fontSize: '14px' }}>
            {formatDateTime(data.lastVisit)}
          </div>
        </div>
      </div>

      <div className="card">
        <div className="section-title" style={{ marginTop: 0 }}>
          Daily Clicks
        </div>
        {data.dailyClicks.length === 0 ? (
          <div style={{ color: '#6b7280', fontSize: '14px' }}>No clicks recorded yet</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Clicks</th>
              </tr>
            </thead>
            <tbody>
              {data.dailyClicks.map((entry) => (
                <tr key={entry.date}>
                  <td>{formatDate(entry.date)}</td>
                  <td>{entry.clicks}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}

        <AnalyticsTable title="Browser Breakdown" data={data.browserBreakdown} />
        <AnalyticsTable title="OS Breakdown" data={data.osBreakdown} />
        <AnalyticsTable title="Device Breakdown" data={data.deviceBreakdown} />
        <AnalyticsTable title="Referrer Breakdown" data={data.referrerBreakdown} />
      </div>

      <Link to="/" className="link">
        Shorten another URL
      </Link>
    </div>
  );
}


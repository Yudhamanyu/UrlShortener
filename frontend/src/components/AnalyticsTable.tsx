interface AnalyticsTableProps {
  title: string;
  data: Record<string, number>;
}

export default function AnalyticsTable({ title, data }: AnalyticsTableProps) {
  const entries = Object.entries(data);

  return (
    <div>
      <div className="section-title">{title}</div>
      {entries.length === 0 ? (
        <div style={{ color: '#6b7280', fontSize: '14px' }}>No data yet</div>
      ) : (
        <table className="data-table">
          <thead>
            <tr>
              <th>Name</th>
              <th>Clicks</th>
            </tr>
          </thead>
          <tbody>
            {entries.map(([key, value]) => (
              <tr key={key}>
                <td>{key}</td>
                <td>{value}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}


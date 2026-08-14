import { useState } from 'react';
import { Link } from 'react-router-dom';
import { UrlResponse } from '@/types/url.types';
import { formatDateTime } from '@/utils/formatters';

interface UrlResultCardProps {
  url: UrlResponse;
}

export default function UrlResultCard({ url }: UrlResultCardProps) {
  const [copied, setCopied] = useState(false);

  const handleCopy = async () => {
    await navigator.clipboard.writeText(url.shortUrl);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="card">
      <div className="short-url-display">
        <a href={url.shortUrl} target="_blank" rel="noopener noreferrer" className="link">
          {url.shortUrl}
        </a>
        <button className="btn btn-secondary" onClick={handleCopy}>
          {copied ? 'Copied!' : 'Copy'}
        </button>
      </div>

      <div style={{ marginTop: '16px' }}>
        <div className="form-label">Original URL</div>
        <div style={{ wordBreak: 'break-all', color: '#6b7280', fontSize: '14px' }}>
          {url.originalUrl}
        </div>
      </div>

      <div style={{ marginTop: '16px', display: 'flex', gap: '24px', fontSize: '13px', color: '#6b7280' }}>
        <div>Created: {formatDateTime(url.createdAt)}</div>
        <div>Status: {url.isActive ? 'Active' : 'Inactive'}</div>
        {url.expirationDate && <div>Expires: {formatDateTime(url.expirationDate)}</div>}
      </div>

      <div style={{ marginTop: '20px' }}>
        <Link to={`/analytics/${url.shortCode}`} className="btn btn-primary">
          View Analytics
        </Link>
      </div>
    </div>
  );
}


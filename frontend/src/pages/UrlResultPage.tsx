import { useEffect, useState } from 'react';
import { useParams, useLocation, Link } from 'react-router-dom';
import UrlResultCard from '@/components/UrlResultCard';
import Loader from '@/components/Loader';
import ErrorMessage from '@/components/ErrorMessage';
import { urlService } from '@/services/urlService';
import { extractErrorMessage } from '@/services/api';
import { UrlResponse } from '@/types/url.types';

interface LocationState {
  url?: UrlResponse;
}

export default function UrlResultPage() {
  const { shortCode } = useParams<{ shortCode: string }>();
  const location = useLocation();
  const state = location.state as LocationState;

  const [url, setUrl] = useState<UrlResponse | null>(state?.url ?? null);
  const [loading, setLoading] = useState(!state?.url);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (url || !shortCode) {
      return;
    }
    setLoading(true);
    urlService
      .getAllUrls(0, 100)
      .then((paged) => {
        const found = paged.content.find((u) => u.shortCode === shortCode);
        if (found) {
          setUrl(found);
        } else {
          setError('Short URL not found');
        }
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [shortCode, url]);

  if (loading) {
    return (
      <div className="page">
        <Loader />
      </div>
    );
  }

  if (error || !url) {
    return (
      <div className="page">
        <ErrorMessage message={error ?? 'Short URL not found'} />
        <Link to="/" className="link">
          Back to Home
        </Link>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page-title">Your Short URL is Ready</div>
      <div className="page-subtitle">Share it or check its performance below.</div>
      <UrlResultCard url={url} />
      <Link to="/" className="link">
        Shorten another URL
      </Link>
    </div>
  );
}


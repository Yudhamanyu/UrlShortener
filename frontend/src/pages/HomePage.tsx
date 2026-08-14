import { useNavigate } from 'react-router-dom';
import UrlShortenerForm from '@/components/UrlShortenerForm';
import ErrorMessage from '@/components/ErrorMessage';
import { useUrlShortener } from '@/hooks/useUrlShortener';
import { CreateUrlRequest } from '@/types/url.types';

export default function HomePage() {
  const { loading, error, createShortUrl } = useUrlShortener();
  const navigate = useNavigate();

  const handleSubmit = async (request: CreateUrlRequest) => {
    try {
      const result = await createShortUrl(request);
      navigate(`/result/${result.shortCode}`, { state: { url: result } });
    } catch {
      // error is already captured in hook state
    }
  };

  return (
    <div className="page">
      <div className="page-title">URL Shortener</div>
      <div className="page-subtitle">
        Paste a long URL below to generate a short, trackable link.
      </div>
      {error && <ErrorMessage message={error} />}
      <UrlShortenerForm onSubmit={handleSubmit} loading={loading} />
    </div>
  );
}


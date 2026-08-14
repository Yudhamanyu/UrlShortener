import { useState, FormEvent } from 'react';
import { isValidUrl, isValidCustomAlias } from '@/utils/validators';
import { CreateUrlRequest } from '@/types/url.types';

interface UrlShortenerFormProps {
  onSubmit: (request: CreateUrlRequest) => void;
  loading: boolean;
}

interface FormErrors {
  originalUrl?: string;
  customAlias?: string;
}

export default function UrlShortenerForm({ onSubmit, loading }: UrlShortenerFormProps) {
  const [originalUrl, setOriginalUrl] = useState('');
  const [customAlias, setCustomAlias] = useState('');
  const [expirationDate, setExpirationDate] = useState('');
  const [errors, setErrors] = useState<FormErrors>({});

  const validate = (): boolean => {
    const newErrors: FormErrors = {};
    if (!isValidUrl(originalUrl)) {
      newErrors.originalUrl = 'Please enter a valid http or https URL';
    }
    if (customAlias && !isValidCustomAlias(customAlias)) {
      newErrors.customAlias = 'Alias must be 3-50 characters (letters, digits, - or _)';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    if (!validate()) {
      return;
    }
    const request: CreateUrlRequest = { originalUrl };
    if (customAlias) {
      request.customAlias = customAlias;
    }
    if (expirationDate) {
      request.expirationDate = new Date(expirationDate).toISOString();
    }
    onSubmit(request);
  };

  return (
    <form onSubmit={handleSubmit} className="card">
      <div className="form-group">
        <label className="form-label" htmlFor="originalUrl">
          Long URL
        </label>
        <input
          id="originalUrl"
          type="text"
          className="form-input"
          placeholder="https://example.com/very/long/path"
          value={originalUrl}
          onChange={(e) => setOriginalUrl(e.target.value)}
        />
        {errors.originalUrl && <div className="form-error">{errors.originalUrl}</div>}
      </div>

      <div className="form-group">
        <label className="form-label" htmlFor="customAlias">
          Custom Alias (optional)
        </label>
        <input
          id="customAlias"
          type="text"
          className="form-input"
          placeholder="my-custom-link"
          value={customAlias}
          onChange={(e) => setCustomAlias(e.target.value)}
        />
        {errors.customAlias && <div className="form-error">{errors.customAlias}</div>}
      </div>

      <div className="form-group">
        <label className="form-label" htmlFor="expirationDate">
          Expiration Date (optional)
        </label>
        <input
          id="expirationDate"
          type="datetime-local"
          className="form-input"
          value={expirationDate}
          onChange={(e) => setExpirationDate(e.target.value)}
        />
      </div>

      <button type="submit" className="btn btn-primary" disabled={loading}>
        {loading ? 'Shortening...' : 'Shorten URL'}
      </button>
    </form>
  );
}


import { useState, useCallback } from 'react';
import { urlService } from '@/services/urlService';
import { extractErrorMessage } from '@/services/api';
import { CreateUrlRequest, UrlResponse } from '@/types/url.types';

interface UseUrlShortenerState {
  data: UrlResponse | null;
  loading: boolean;
  error: string | null;
}

export function useUrlShortener() {
  const [state, setState] = useState<UseUrlShortenerState>({
    data: null,
    loading: false,
    error: null,
  });

  const createShortUrl = useCallback(async (request: CreateUrlRequest) => {
    setState({ data: null, loading: true, error: null });
    try {
      const result = await urlService.createShortUrl(request);
      setState({ data: result, loading: false, error: null });
      return result;
    } catch (err) {
      const message = extractErrorMessage(err);
      setState({ data: null, loading: false, error: message });
      throw err;
    }
  }, []);

  return { ...state, createShortUrl };
}


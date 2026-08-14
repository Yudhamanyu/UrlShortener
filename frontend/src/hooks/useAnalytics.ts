import { useState, useEffect, useCallback } from 'react';
import { analyticsService } from '@/services/analyticsService';
import { extractErrorMessage } from '@/services/api';
import { AnalyticsResponse } from '@/types/analytics.types';

interface UseAnalyticsState {
  data: AnalyticsResponse | null;
  loading: boolean;
  error: string | null;
}

export function useAnalytics(shortCode: string | undefined) {
  const [state, setState] = useState<UseAnalyticsState>({
    data: null,
    loading: true,
    error: null,
  });

  const fetchAnalytics = useCallback(async () => {
    if (!shortCode) {
      return;
    }
    setState((prev) => ({ ...prev, loading: true, error: null }));
    try {
      const result = await analyticsService.getAnalytics(shortCode);
      setState({ data: result, loading: false, error: null });
    } catch (err) {
      const message = extractErrorMessage(err);
      setState({ data: null, loading: false, error: message });
    }
  }, [shortCode]);

  useEffect(() => {
    fetchAnalytics();
  }, [fetchAnalytics]);

  return { ...state, refetch: fetchAnalytics };
}


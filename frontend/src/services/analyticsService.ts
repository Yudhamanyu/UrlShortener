import { apiClient } from './api';
import { AnalyticsResponse } from '@/types/analytics.types';

export const analyticsService = {
  async getAnalytics(shortCode: string): Promise<AnalyticsResponse> {
    const response = await apiClient.get<AnalyticsResponse>(`/api/analytics/${shortCode}`);
    return response.data;
  },
};


import { apiClient } from './api';
import { CreateUrlRequest, UpdateUrlRequest, UrlResponse, PagedResponse } from '@/types/url.types';

export const urlService = {
  async createShortUrl(request: CreateUrlRequest): Promise<UrlResponse> {
    const response = await apiClient.post<UrlResponse>('/api/urls', request);
    return response.data;
  },

  async getUrlById(id: number): Promise<UrlResponse> {
    const response = await apiClient.get<UrlResponse>(`/api/urls/${id}`);
    return response.data;
  },

  async updateUrl(id: number, request: UpdateUrlRequest): Promise<UrlResponse> {
    const response = await apiClient.put<UrlResponse>(`/api/urls/${id}`, request);
    return response.data;
  },

  async deleteUrl(id: number): Promise<void> {
    await apiClient.delete(`/api/urls/${id}`);
  },

  async getAllUrls(page = 0, size = 20): Promise<PagedResponse<UrlResponse>> {
    const response = await apiClient.get<PagedResponse<UrlResponse>>('/api/urls', {
      params: { page, size },
    });
    return response.data;
  },
};


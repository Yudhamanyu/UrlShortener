export interface CreateUrlRequest {
  originalUrl: string;
  customAlias?: string;
  expirationDate?: string;
}

export interface UpdateUrlRequest {
  originalUrl?: string;
  expirationDate?: string;
  isActive?: boolean;
}

export interface UrlResponse {
  id: number;
  shortCode: string;
  shortUrl: string;
  originalUrl: string;
  customAlias: string | null;
  expirationDate: string | null;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PagedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}


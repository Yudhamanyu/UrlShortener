export interface DailyClickResponse {
  date: string;
  clicks: number;
}

export interface AnalyticsResponse {
  shortCode: string;
  originalUrl: string;
  totalClicks: number;
  firstVisit: string | null;
  lastVisit: string | null;
  dailyClicks: DailyClickResponse[];
  browserBreakdown: Record<string, number>;
  osBreakdown: Record<string, number>;
  deviceBreakdown: Record<string, number>;
  referrerBreakdown: Record<string, number>;
}


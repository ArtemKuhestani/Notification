import axios from 'axios';

const API_BASE = '/api/v1';

const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Types
export interface SendNotificationRequest {
  channel: 'EMAIL' | 'TELEGRAM' | 'SMS' | 'WHATSAPP';
  recipient: string;
  subject?: string;
  message: string;
  priority?: 'HIGH' | 'NORMAL' | 'LOW';
  idempotencyKey?: string;
  callbackUrl?: string;
  metadata?: Record<string, unknown>;
}

export interface NotificationDto {
  notificationId: string;
  clientId: number;
  clientName: string;
  channelType: string;
  recipient: string;
  subject: string;
  messageBody: string;
  status: string;
  priority: string;
  retryCount: number;
  maxRetries: number;
  nextRetryAt: string;
  errorMessage: string;
  errorCode: string;
  providerMessageId: string;
  idempotencyKey: string;
  callbackUrl: string;
  metadata: Record<string, unknown>;
  createdAt: string;
  updatedAt: string;
  sentAt: string;
  expiresAt: string;
}

export interface DashboardStats {
  totalSent: number;
  totalFailed: number;
  totalPending: number;
  successRate: number;
  byChannel: Record<string, number>;
  byStatus: Record<string, number>;
  hourlyStats: { hour: string; count: number }[];
  recentErrors: NotificationDto[];
  generatedAt: string;
}

export interface AuditLogDto {
  logId: number;
  adminId: number;
  adminEmail: string;
  actionType: string;
  entityType: string;
  entityId: string;
  oldValue: Record<string, unknown>;
  newValue: Record<string, unknown>;
  ipAddress: string;
  userAgent: string;
  createdAt: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// API calls
export const sendNotification = (data: SendNotificationRequest) =>
  api.post<ApiResponse<{ notificationId: string; status: string; createdAt: string }>>('/send', data);

export const getNotificationStatus = (id: string) =>
  api.get<ApiResponse<NotificationDto>>(`/status/${id}`);

export const getNotifications = (page = 0, size = 20, status?: string, channel?: string) => {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (status) params.append('status', status);
  if (channel) params.append('channel', channel);
  return api.get<ApiResponse<Page<NotificationDto>>>(`/admin/notifications?${params}`);
};

export const retryNotification = (id: string) =>
  api.post<ApiResponse<NotificationDto>>(`/admin/notifications/${id}/retry`);

export const getDashboardStats = () =>
  api.get<ApiResponse<DashboardStats>>('/admin/stats/dashboard');

export const getAuditLogs = (page = 0, size = 50) =>
  api.get<ApiResponse<Page<AuditLogDto>>>(`/admin/audit?page=${page}&size=${size}`);

export const getHealth = () =>
  api.get<ApiResponse<{ status: string; service: string; version: string }>>('/health');

export default api;

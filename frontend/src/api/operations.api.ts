import { apiClient } from './client';
import type { ApiResponse } from '../types/api';
import type {
  Complaint,
  ComplaintInput,
  FoodFeedback,
  FoodFeedbackInput,
  FoodMenu,
  FoodMenuInput,
  MaintenanceTask,
  MaintenanceTaskInput,
  Notice,
  NoticeInput,
  Notification,
  VisitorEntry,
  VisitorInput
} from '../types/operations';

const actionBody = (remarks?: string) => ({ remarks: remarks || undefined });

export const complaintsApi = {
  async list() {
    const response = await apiClient.get<ApiResponse<Complaint[]>>('/users/complaints');
    return response.data.data ?? [];
  },
  async create(input: ComplaintInput) {
    const response = await apiClient.post<ApiResponse<Complaint>>('/users/complaints', input);
    return response.data.data!;
  },
  async get(id: number) {
    const response = await apiClient.get<ApiResponse<Complaint>>(`/users/complaints/${id}`);
    return response.data.data!;
  },
  async cancel(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<Complaint>>(`/users/complaints/${id}/cancel`, actionBody(remarks));
    return response.data.data!;
  },
  async reopen(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<Complaint>>(`/users/complaints/${id}/reopen`, actionBody(remarks));
    return response.data.data!;
  },
  async close(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<Complaint>>(`/users/complaints/${id}/close`, actionBody(remarks));
    return response.data.data!;
  },
  async comment(id: number, comment: string) {
    const response = await apiClient.post<ApiResponse<Complaint>>(`/users/complaints/${id}/comments`, { comment });
    return response.data.data!;
  }
};

export const ownerComplaintsApi = {
  async list() {
    const response = await apiClient.get<ApiResponse<Complaint[]>>('/owner/complaints');
    return response.data.data ?? [];
  },
  async get(id: number) {
    const response = await apiClient.get<ApiResponse<Complaint>>(`/owner/complaints/${id}`);
    return response.data.data!;
  },
  async acknowledge(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<Complaint>>(`/owner/complaints/${id}/acknowledge`, actionBody(remarks));
    return response.data.data!;
  },
  async start(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<Complaint>>(`/owner/complaints/${id}/start`, actionBody(remarks));
    return response.data.data!;
  },
  async resolve(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<Complaint>>(`/owner/complaints/${id}/resolve`, actionBody(remarks));
    return response.data.data!;
  },
  async close(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<Complaint>>(`/owner/complaints/${id}/close`, actionBody(remarks));
    return response.data.data!;
  },
  async comment(id: number, comment: string) {
    const response = await apiClient.post<ApiResponse<Complaint>>(`/owner/complaints/${id}/comments`, { comment });
    return response.data.data!;
  }
};

export const ownerMaintenanceApi = {
  async list() {
    const response = await apiClient.get<ApiResponse<MaintenanceTask[]>>('/owner/maintenance');
    return response.data.data ?? [];
  },
  async create(input: MaintenanceTaskInput) {
    const response = await apiClient.post<ApiResponse<MaintenanceTask>>('/owner/maintenance', input);
    return response.data.data!;
  },
  async get(id: number) {
    const response = await apiClient.get<ApiResponse<MaintenanceTask>>(`/owner/maintenance/${id}`);
    return response.data.data!;
  },
  async update(id: number, input: MaintenanceTaskInput) {
    const response = await apiClient.patch<ApiResponse<MaintenanceTask>>(`/owner/maintenance/${id}`, input);
    return response.data.data!;
  },
  async start(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<MaintenanceTask>>(`/owner/maintenance/${id}/start`, actionBody(remarks));
    return response.data.data!;
  },
  async complete(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<MaintenanceTask>>(`/owner/maintenance/${id}/complete`, actionBody(remarks));
    return response.data.data!;
  },
  async cancel(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<MaintenanceTask>>(`/owner/maintenance/${id}/cancel`, actionBody(remarks));
    return response.data.data!;
  }
};

export const noticesApi = {
  async list() {
    const response = await apiClient.get<ApiResponse<Notice[]>>('/users/notices');
    return response.data.data ?? [];
  },
  async get(id: number) {
    const response = await apiClient.get<ApiResponse<Notice>>(`/users/notices/${id}`);
    return response.data.data!;
  }
};

export const ownerNoticesApi = {
  async list() {
    const response = await apiClient.get<ApiResponse<Notice[]>>('/owner/notices');
    return response.data.data ?? [];
  },
  async create(input: NoticeInput) {
    const response = await apiClient.post<ApiResponse<Notice>>('/owner/notices', input);
    return response.data.data!;
  },
  async get(id: number) {
    const response = await apiClient.get<ApiResponse<Notice>>(`/owner/notices/${id}`);
    return response.data.data!;
  },
  async update(id: number, input: NoticeInput) {
    const response = await apiClient.patch<ApiResponse<Notice>>(`/owner/notices/${id}`, input);
    return response.data.data!;
  },
  async publish(id: number) {
    const response = await apiClient.patch<ApiResponse<Notice>>(`/owner/notices/${id}/publish`);
    return response.data.data!;
  },
  async archive(id: number) {
    const response = await apiClient.patch<ApiResponse<Notice>>(`/owner/notices/${id}/archive`);
    return response.data.data!;
  }
};

export const foodApi = {
  async today() {
    const response = await apiClient.get<ApiResponse<FoodMenu[]>>('/users/food-menus/today');
    return response.data.data ?? [];
  },
  async byDate(date: string) {
    const response = await apiClient.get<ApiResponse<FoodMenu[]>>('/users/food-menus', { params: { date } });
    return response.data.data ?? [];
  },
  async feedback(input: FoodFeedbackInput) {
    const response = await apiClient.post<ApiResponse<FoodFeedback>>('/users/food-feedback', input);
    return response.data.data!;
  }
};

export const ownerFoodApi = {
  async menus() {
    const response = await apiClient.get<ApiResponse<FoodMenu[]>>('/owner/food-menus');
    return response.data.data ?? [];
  },
  async createMenu(input: FoodMenuInput) {
    const response = await apiClient.post<ApiResponse<FoodMenu>>('/owner/food-menus', input);
    return response.data.data!;
  },
  async updateMenu(id: number, input: FoodMenuInput) {
    const response = await apiClient.patch<ApiResponse<FoodMenu>>(`/owner/food-menus/${id}`, input);
    return response.data.data!;
  },
  async feedback(propertyId?: number) {
    const response = await apiClient.get<ApiResponse<FoodFeedback[]>>('/owner/food-feedback', { params: propertyId ? { propertyId } : undefined });
    return response.data.data ?? [];
  }
};

export const visitorsApi = {
  async list() {
    const response = await apiClient.get<ApiResponse<VisitorEntry[]>>('/users/visitors');
    return response.data.data ?? [];
  },
  async create(input: VisitorInput) {
    const response = await apiClient.post<ApiResponse<VisitorEntry>>('/users/visitors', input);
    return response.data.data!;
  },
  async get(id: number) {
    const response = await apiClient.get<ApiResponse<VisitorEntry>>(`/users/visitors/${id}`);
    return response.data.data!;
  },
  async cancel(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<VisitorEntry>>(`/users/visitors/${id}/cancel`, actionBody(remarks));
    return response.data.data!;
  }
};

export const ownerVisitorsApi = {
  async list() {
    const response = await apiClient.get<ApiResponse<VisitorEntry[]>>('/owner/visitors');
    return response.data.data ?? [];
  },
  async get(id: number) {
    const response = await apiClient.get<ApiResponse<VisitorEntry>>(`/owner/visitors/${id}`);
    return response.data.data!;
  },
  async approve(id: number) {
    const response = await apiClient.patch<ApiResponse<VisitorEntry>>(`/owner/visitors/${id}/approve`);
    return response.data.data!;
  },
  async reject(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<VisitorEntry>>(`/owner/visitors/${id}/reject`, actionBody(remarks));
    return response.data.data!;
  },
  async checkIn(id: number) {
    const response = await apiClient.patch<ApiResponse<VisitorEntry>>(`/owner/visitors/${id}/check-in`);
    return response.data.data!;
  },
  async checkOut(id: number) {
    const response = await apiClient.patch<ApiResponse<VisitorEntry>>(`/owner/visitors/${id}/check-out`);
    return response.data.data!;
  }
};

export const notificationsApi = {
  async list() {
    const response = await apiClient.get<ApiResponse<Notification[]>>('/notifications');
    return response.data.data ?? [];
  },
  async unreadCount() {
    const response = await apiClient.get<ApiResponse<{ unreadCount: number }>>('/notifications/unread-count');
    return response.data.data?.unreadCount ?? 0;
  },
  async markRead(id: number) {
    const response = await apiClient.patch<ApiResponse<Notification>>(`/notifications/${id}/read`);
    return response.data.data!;
  },
  async markAllRead() {
    await apiClient.patch('/notifications/read-all');
  }
};

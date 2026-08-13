import { apiClient } from './client';
import type { ApiResponse } from '../types/api';
import type { OwnerDetail, OwnerProfile, OwnerVerificationStatus } from '../types/owner';
import type { AdminPropertyDetails, AdminPropertySummary } from '../types/property';
import type { User } from '../types/user';

export const adminApi = {
  async users() {
    const response = await apiClient.get<ApiResponse<User[]>>('/admin/users');
    return response.data.data ?? [];
  },
  async owners(status?: OwnerVerificationStatus | 'ALL') {
    const params = status && status !== 'ALL' ? { status } : undefined;
    const response = await apiClient.get<ApiResponse<OwnerProfile[]>>('/admin/owners', { params });
    return response.data.data ?? [];
  },
  async pendingOwners() {
    const response = await apiClient.get<ApiResponse<OwnerProfile[]>>('/admin/owners/pending');
    return response.data.data ?? [];
  },
  async owner(id: string) {
    const response = await apiClient.get<ApiResponse<OwnerDetail>>(`/admin/owners/${id}`);
    return response.data.data!;
  },
  async verifyOwner(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<OwnerProfile>>(`/admin/owners/${id}/verify`, { remarks });
    return response.data.data!;
  },
  async rejectOwner(id: number, reason: string) {
    const response = await apiClient.patch<ApiResponse<OwnerProfile>>(`/admin/owners/${id}/reject`, { reason });
    return response.data.data!;
  },
  async suspendOwner(id: number, reason: string) {
    const response = await apiClient.patch<ApiResponse<OwnerProfile>>(`/admin/owners/${id}/suspend`, { reason });
    return response.data.data!;
  },
  async pgs() {
    const response = await apiClient.get<ApiResponse<AdminPropertySummary[]>>('/admin/pgs');
    return response.data.data ?? [];
  },
  async pendingPgs() {
    const response = await apiClient.get<ApiResponse<AdminPropertySummary[]>>('/admin/pgs/pending-verification');
    return response.data.data ?? [];
  },
  async pg(id: number) {
    const response = await apiClient.get<ApiResponse<AdminPropertyDetails>>(`/admin/pgs/${id}`);
    return response.data.data!;
  },
  async startPgReview(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<AdminPropertyDetails>>(`/admin/pgs/${id}/review`, { remarks });
    return response.data.data!;
  },
  async verifyPg(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<AdminPropertyDetails>>(`/admin/pgs/${id}/verify`, { remarks });
    return response.data.data!;
  },
  async rejectPg(id: number, remarks: string) {
    const response = await apiClient.patch<ApiResponse<AdminPropertyDetails>>(`/admin/pgs/${id}/reject`, { remarks });
    return response.data.data!;
  },
  async requestPgChanges(id: number, remarks: string) {
    const response = await apiClient.patch<ApiResponse<AdminPropertyDetails>>(`/admin/pgs/${id}/request-changes`, { remarks });
    return response.data.data!;
  }
};

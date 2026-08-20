import { apiClient } from './client';
import type { ApiResponse } from '../types/api';
import type { Booking, BookingStatus } from '../types/booking';

export type CreateBookingInput = {
  propertyId: number;
  roomId: number;
  bedId: number;
  moveInDate: string;
  expectedMoveOutDate: string;
  remarks?: string;
};

export const bookingApi = {
  async create(input: CreateBookingInput) {
    const response = await apiClient.post<ApiResponse<Booking>>('/users/bookings', input);
    return response.data.data!;
  },

  async rooms(propertyId: number) {
    const response = await apiClient.get<ApiResponse<any[]>>(`/public/pgs/${propertyId}/rooms`);
    return response.data.data ?? [];
  },

  async list() {
    const response = await apiClient.get<ApiResponse<Booking[]>>('/users/bookings');
    return response.data.data ?? [];
  },

  async get(id: number) {
    const response = await apiClient.get<ApiResponse<Booking>>(`/users/bookings/${id}`);
    return response.data.data!;
  },

  async cancel(id: number) {
    await apiClient.patch(`/users/bookings/${id}/cancel`);
  },

  async ownerList() {
    const response = await apiClient.get<ApiResponse<Booking[]>>('/owner/bookings');
    return response.data.data ?? [];
  },

  async ownerGet(id: number) {
    const response = await apiClient.get<ApiResponse<Booking>>(`/owner/bookings/${id}`);
    return response.data.data!;
  },

  async ownerApprove(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<Booking>>(`/owner/bookings/${id}/approve`, null, {
      params: { remarks }
    });
    return response.data.data!;
  },

  async ownerReject(id: number, reason: string) {
    const response = await apiClient.patch<ApiResponse<Booking>>(`/owner/bookings/${id}/reject`, null, {
      params: { reason }
    });
    return response.data.data!;
  },

  async ownerCheckIn(id: number) {
    const response = await apiClient.patch<ApiResponse<Booking>>(`/owner/bookings/${id}/check-in`);
    return response.data.data!;
  }
};
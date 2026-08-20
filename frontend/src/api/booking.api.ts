import { apiClient } from './client';
import type { ApiResponse } from '../types/api';
import type {
  Booking,
  CreateBookingInput,
  DocumentType,
  PaymentMethod,
  RentalAgreement,
  SecurityDeposit,
  TenantDocument,
  TenantProfile
} from '../types/booking';

export const bookingApi = {
  async create(input: CreateBookingInput) {
    const response = await apiClient.post<ApiResponse<Booking>>('/users/bookings', input);
    return response.data.data!;
  },
  async listMine() {
    const response = await apiClient.get<ApiResponse<Booking[]>>('/users/bookings');
    return response.data.data ?? [];
  },
  async getMine(id: number) {
    const response = await apiClient.get<ApiResponse<Booking>>(`/users/bookings/${id}`);
    return response.data.data!;
  },
  async cancel(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<Booking>>(`/users/bookings/${id}/cancel`, { remarks });
    return response.data.data!;
  },
  async uploadDocument(id: number, input: { documentType: DocumentType; documentNumber?: string; file: File }) {
    const form = new FormData();
    form.append('documentType', input.documentType);
    if (input.documentNumber) form.append('documentNumber', input.documentNumber);
    form.append('file', input.file);
    const response = await apiClient.post<ApiResponse<TenantDocument>>(`/users/bookings/${id}/documents`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    return response.data.data!;
  },
  async deleteDocument(id: number, documentId: number) {
    await apiClient.delete(`/users/bookings/${id}/documents/${documentId}`);
  },
  async acceptAgreement(id: number) {
    const response = await apiClient.patch<ApiResponse<RentalAgreement>>(`/users/bookings/${id}/agreement/accept`);
    return response.data.data!;
  },
  async myPg() {
    const response = await apiClient.get<ApiResponse<Booking>>('/users/my-pg');
    return response.data.data!;
  }
};

export const ownerBookingApi = {
  async list() {
    const response = await apiClient.get<ApiResponse<Booking[]>>('/owner/bookings');
    return response.data.data ?? [];
  },
  async get(id: number) {
    const response = await apiClient.get<ApiResponse<Booking>>(`/owner/bookings/${id}`);
    return response.data.data!;
  },
  async approve(id: number, remarks?: string) {
    const response = await apiClient.patch<ApiResponse<Booking>>(`/owner/bookings/${id}/approve`, { remarks });
    return response.data.data!;
  },
  async reject(id: number, remarks: string) {
    const response = await apiClient.patch<ApiResponse<Booking>>(`/owner/bookings/${id}/reject`, { remarks });
    return response.data.data!;
  },
  async verifyDocument(id: number, documentId: number) {
    const response = await apiClient.patch<ApiResponse<TenantDocument>>(`/owner/bookings/${id}/documents/${documentId}/verify`);
    return response.data.data!;
  },
  async rejectDocument(id: number, documentId: number, remarks: string) {
    const response = await apiClient.patch<ApiResponse<TenantDocument>>(`/owner/bookings/${id}/documents/${documentId}/reject`, { remarks });
    return response.data.data!;
  },
  async recordDeposit(id: number, input: { amount: number; paymentMethod: PaymentMethod; paymentReference?: string; remarks?: string }) {
    const response = await apiClient.post<ApiResponse<SecurityDeposit>>(`/owner/bookings/${id}/deposit`, input);
    return response.data.data!;
  },
  async issueAgreement(id: number, input: { endDate?: string; terms?: string; file?: File }) {
    const form = new FormData();
    if (input.endDate) form.append('endDate', input.endDate);
    if (input.terms) form.append('terms', input.terms);
    if (input.file) form.append('file', input.file);
    const response = await apiClient.post<ApiResponse<RentalAgreement>>(`/owner/bookings/${id}/agreement`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    return response.data.data!;
  },
  async checkIn(id: number) {
    const response = await apiClient.patch<ApiResponse<Booking>>(`/owner/bookings/${id}/check-in`);
    return response.data.data!;
  }
};

export const ownerTenantApi = {
  async list() {
    const response = await apiClient.get<ApiResponse<TenantProfile[]>>('/owner/tenants');
    return response.data.data ?? [];
  },
  async get(id: number) {
    const response = await apiClient.get<ApiResponse<TenantProfile>>(`/owner/tenants/${id}`);
    return response.data.data!;
  }
};

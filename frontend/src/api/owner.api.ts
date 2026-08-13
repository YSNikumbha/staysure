import { apiClient } from './client';
import type { ApiResponse } from '../types/api';
import type {
  DocumentType,
  OwnerApplicationInput,
  OwnerDashboard,
  OwnerDocument,
  OwnerProfile
} from '../types/owner';

export const ownerApi = {
  async apply(input: OwnerApplicationInput) {
    const response = await apiClient.post<ApiResponse<OwnerProfile>>('/owners/apply', input);
    return response.data.data!;
  },
  async me() {
    const response = await apiClient.get<ApiResponse<OwnerProfile>>('/owners/me');
    return response.data.data!;
  },
  async updateMe(input: OwnerApplicationInput) {
    const response = await apiClient.put<ApiResponse<OwnerProfile>>('/owners/me', input);
    return response.data.data!;
  },
  async dashboard() {
    const response = await apiClient.get<ApiResponse<OwnerDashboard>>('/owners/dashboard');
    return response.data.data!;
  },
  async documents() {
    const response = await apiClient.get<ApiResponse<OwnerDocument[]>>('/owners/me/documents');
    return response.data.data ?? [];
  },
  async uploadDocument(input: { documentType: DocumentType; documentNumber?: string; file: File }) {
    const form = new FormData();
    form.append('documentType', input.documentType);
    if (input.documentNumber) {
      form.append('documentNumber', input.documentNumber);
    }
    form.append('file', input.file);
    const response = await apiClient.post<ApiResponse<OwnerDocument>>('/owners/me/documents', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    return response.data.data!;
  },
  async deleteDocument(id: number) {
    await apiClient.delete<ApiResponse<void>>(`/owners/me/documents/${id}`);
  }
};

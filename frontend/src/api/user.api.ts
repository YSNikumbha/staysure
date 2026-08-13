import { apiClient } from './client';
import type { ApiResponse } from '../types/api';
import type { ChangePasswordInput, UpdateProfileInput, User } from '../types/user';

export const userApi = {
  async me() {
    const response = await apiClient.get<ApiResponse<User>>('/users/me');
    return response.data.data!;
  },
  async updateMe(input: UpdateProfileInput) {
    const response = await apiClient.put<ApiResponse<User>>('/users/me', input);
    return response.data.data!;
  },
  async changePassword(input: ChangePasswordInput) {
    await apiClient.put<ApiResponse<void>>('/users/me/change-password', input);
  }
};

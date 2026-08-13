import { apiClient } from './client';
import type { ApiResponse } from '../types/api';
import type {
  AuthPayload,
  ForgotPasswordInput,
  LoginInput,
  RegisterInput,
  ResetPasswordInput
} from '../types/auth';

export const authApi = {
  async register(input: RegisterInput) {
    const response = await apiClient.post<ApiResponse<AuthPayload>>('/auth/register', input);
    return response.data.data!;
  },
  async login(input: LoginInput) {
    const response = await apiClient.post<ApiResponse<AuthPayload>>('/auth/login', input);
    return response.data.data!;
  },
  async logout(refreshToken: string) {
    await apiClient.post<ApiResponse<void>>('/auth/logout', { refreshToken });
  },
  async forgotPassword(input: ForgotPasswordInput) {
    await apiClient.post<ApiResponse<void>>('/auth/forgot-password', input);
  },
  async resetPassword(input: ResetPasswordInput) {
    await apiClient.post<ApiResponse<void>>('/auth/reset-password', input);
  }
};

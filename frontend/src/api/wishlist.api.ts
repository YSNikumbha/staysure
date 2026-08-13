import { apiClient } from './client';
import type { ApiResponse } from '../types/api';
import type { WishlistItem } from '../types/property';

export const wishlistApi = {
  async list() {
    const response = await apiClient.get<ApiResponse<WishlistItem[]>>('/users/wishlist');
    return response.data.data ?? [];
  },
  async add(propertyId: number) {
    const response = await apiClient.post<ApiResponse<WishlistItem>>(`/users/wishlist/${propertyId}`);
    return response.data.data!;
  },
  async remove(propertyId: number) {
    await apiClient.delete<ApiResponse<void>>(`/users/wishlist/${propertyId}`);
  }
};

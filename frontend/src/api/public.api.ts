import { apiClient } from './client';
import type { ApiResponse } from '../types/api';
import type {
  Amenity,
  GenderType,
  PaginationResponse,
  PropertyType,
  PublicPgCard,
  PublicPgDetails,
  SharingType
} from '../types/property';

export type PublicPgSearchParams = {
  page?: number;
  size?: number;
  search?: string;
  city?: string;
  area?: string;
  minRent?: number;
  maxRent?: number;
  genderType?: GenderType;
  propertyType?: PropertyType;
  sharingType?: SharingType;
  foodAvailable?: boolean;
  amenityIds?: number[];
  availableOnly?: boolean;
  sort?: 'latest' | 'price_low_to_high' | 'price_high_to_low' | 'availability';
};

export const publicApi = {
  async searchPgs(params: PublicPgSearchParams) {
    const response = await apiClient.get<ApiResponse<PaginationResponse<PublicPgCard>>>('/public/pgs', {
      params: {
        ...params,
        amenityIds: params.amenityIds?.join(',')
      }
    });
    return response.data.data!;
  },
  async pg(slug: string) {
    const response = await apiClient.get<ApiResponse<PublicPgDetails>>(`/public/pgs/${slug}`);
    return response.data.data!;
  },
  async amenities() {
    const response = await apiClient.get<ApiResponse<Amenity[]>>('/public/amenities');
    return response.data.data ?? [];
  }
};

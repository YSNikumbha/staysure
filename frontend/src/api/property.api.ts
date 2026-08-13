import { apiClient } from './client';
import type { ApiResponse } from '../types/api';
import type {
  Amenity,
  Bed,
  BedInput,
  Floor,
  FloorInput,
  ImageCategory,
  PgImage,
  PgPropertyInput,
  PropertyDetails,
  PropertyStatus,
  PropertySummary,
  VerificationHistory,
  Room,
  RoomInput
} from '../types/property';

export const propertyApi = {
  async listProperties() {
    const response = await apiClient.get<ApiResponse<PropertySummary[]>>('/owner/pgs');
    return response.data.data ?? [];
  },
  async property(id: number) {
    const response = await apiClient.get<ApiResponse<PropertyDetails>>(`/owner/pgs/${id}`);
    return response.data.data!;
  },
  async createProperty(input: PgPropertyInput) {
    const response = await apiClient.post<ApiResponse<PropertyDetails>>('/owner/pgs', input);
    return response.data.data!;
  },
  async updateProperty(id: number, input: PgPropertyInput) {
    const response = await apiClient.put<ApiResponse<PropertyDetails>>(`/owner/pgs/${id}`, input);
    return response.data.data!;
  },
  async updatePropertyStatus(id: number, status: PropertyStatus) {
    const response = await apiClient.patch<ApiResponse<PropertyDetails>>(`/owner/pgs/${id}/status`, { status });
    return response.data.data!;
  },
  async archiveProperty(id: number) {
    await apiClient.delete<ApiResponse<void>>(`/owner/pgs/${id}`);
  },
  async submitVerification(id: number) {
    const response = await apiClient.post<ApiResponse<{ propertyId: number; verificationStatus: string; submittedForVerificationAt: string; missingItems: string[] }>>(`/owner/pgs/${id}/submit-verification`);
    return response.data.data!;
  },
  async createFloor(pgId: number, input: FloorInput) {
    const response = await apiClient.post<ApiResponse<Floor>>(`/owner/pgs/${pgId}/floors`, input);
    return response.data.data!;
  },
  async updateFloor(pgId: number, floorId: number, input: FloorInput) {
    const response = await apiClient.put<ApiResponse<Floor>>(`/owner/pgs/${pgId}/floors/${floorId}`, input);
    return response.data.data!;
  },
  async archiveFloor(pgId: number, floorId: number) {
    await apiClient.delete<ApiResponse<void>>(`/owner/pgs/${pgId}/floors/${floorId}`);
  },
  async createRoom(pgId: number, floorId: number, input: RoomInput) {
    const response = await apiClient.post<ApiResponse<Room>>(`/owner/pgs/${pgId}/floors/${floorId}/rooms`, input);
    return response.data.data!;
  },
  async updateRoom(pgId: number, floorId: number, roomId: number, input: RoomInput) {
    const response = await apiClient.put<ApiResponse<Room>>(`/owner/pgs/${pgId}/floors/${floorId}/rooms/${roomId}`, input);
    return response.data.data!;
  },
  async archiveRoom(pgId: number, floorId: number, roomId: number) {
    await apiClient.delete<ApiResponse<void>>(`/owner/pgs/${pgId}/floors/${floorId}/rooms/${roomId}`);
  },
  async createBed(pgId: number, floorId: number, roomId: number, input: BedInput) {
    const response = await apiClient.post<ApiResponse<Bed>>(`/owner/pgs/${pgId}/floors/${floorId}/rooms/${roomId}/beds`, input);
    return response.data.data!;
  },
  async updateBed(pgId: number, floorId: number, roomId: number, bedId: number, input: BedInput) {
    const response = await apiClient.put<ApiResponse<Bed>>(`/owner/pgs/${pgId}/floors/${floorId}/rooms/${roomId}/beds/${bedId}`, input);
    return response.data.data!;
  },
  async archiveBed(pgId: number, floorId: number, roomId: number, bedId: number) {
    await apiClient.delete<ApiResponse<void>>(`/owner/pgs/${pgId}/floors/${floorId}/rooms/${roomId}/beds/${bedId}`);
  },
  async amenities() {
    const response = await apiClient.get<ApiResponse<Amenity[]>>('/owner/amenities');
    return response.data.data ?? [];
  },
  async updateAmenities(pgId: number, amenityIds: number[]) {
    const response = await apiClient.put<ApiResponse<Amenity[]>>(`/owner/pgs/${pgId}/amenities`, { amenityIds });
    return response.data.data ?? [];
  },
  async uploadImage(input: { pgId: number; category: ImageCategory; coverImage: boolean; sortOrder?: number; file: File }) {
    const form = new FormData();
    form.append('category', input.category);
    form.append('coverImage', String(input.coverImage));
    if (input.sortOrder !== undefined) {
      form.append('sortOrder', String(input.sortOrder));
    }
    form.append('file', input.file);
    const response = await apiClient.post<ApiResponse<PgImage>>(`/owner/pgs/${input.pgId}/images`, form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    return response.data.data!;
  },
  async setCoverImage(pgId: number, imageId: number) {
    const response = await apiClient.patch<ApiResponse<PgImage>>(`/owner/pgs/${pgId}/images/${imageId}/cover`);
    return response.data.data!;
  },
  async reorderImages(pgId: number, order: Array<{ imageId: number; sortOrder: number }>) {
    const response = await apiClient.put<ApiResponse<PgImage[]>>(`/owner/pgs/${pgId}/images/reorder`, order);
    return response.data.data ?? [];
  },
  async deleteImage(pgId: number, imageId: number) {
    await apiClient.delete<ApiResponse<void>>(`/owner/pgs/${pgId}/images/${imageId}`);
  }
};

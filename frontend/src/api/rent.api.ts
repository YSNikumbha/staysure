import { apiClient } from './client';
import type { ApiResponse } from '../types/api';
import type {
  GenerateRentInput,
  GenerateRentResult,
  RecordRentPaymentInput,
  RentDashboard,
  RentInvoiceDetail,
  RentPayment,
  UpdateRentChargesInput
} from '../types/rent';

function downloadBlob(blob: Blob, filename: string) {
  const href = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = href;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(href);
}

export const ownerRentApi = {
  async list(propertyId?: number) {
    const response = await apiClient.get<ApiResponse<RentDashboard>>('/owner/rent', {
      params: propertyId ? { propertyId } : undefined
    });
    return response.data.data!;
  },
  async generate(input: GenerateRentInput) {
    const response = await apiClient.post<ApiResponse<GenerateRentResult>>('/owner/rent/generate', input);
    return response.data.data!;
  },
  async get(invoiceId: number) {
    const response = await apiClient.get<ApiResponse<RentInvoiceDetail>>(`/owner/rent/${invoiceId}`);
    return response.data.data!;
  },
  async updateCharges(invoiceId: number, input: UpdateRentChargesInput) {
    const response = await apiClient.patch<ApiResponse<RentInvoiceDetail>>(`/owner/rent/${invoiceId}/charges`, input);
    return response.data.data!;
  },
  async recordPayment(invoiceId: number, input: RecordRentPaymentInput) {
    const response = await apiClient.post<ApiResponse<RentInvoiceDetail>>(`/owner/rent/${invoiceId}/payments`, input);
    return response.data.data!;
  },
  async payments(invoiceId: number) {
    const response = await apiClient.get<ApiResponse<RentPayment[]>>(`/owner/rent/${invoiceId}/payments`);
    return response.data.data ?? [];
  },
  async downloadReceipt(paymentId: number) {
    const response = await apiClient.get<Blob>(`/owner/rent/payments/${paymentId}/receipt`, { responseType: 'blob' });
    downloadBlob(response.data, `rent-receipt-${paymentId}.txt`);
  }
};

export const userRentApi = {
  async list() {
    const response = await apiClient.get<ApiResponse<RentDashboard>>('/users/rent');
    return response.data.data!;
  },
  async get(invoiceId: number) {
    const response = await apiClient.get<ApiResponse<RentInvoiceDetail>>(`/users/rent/${invoiceId}`);
    return response.data.data!;
  },
  async payments(invoiceId: number) {
    const response = await apiClient.get<ApiResponse<RentPayment[]>>(`/users/rent/${invoiceId}/payments`);
    return response.data.data ?? [];
  },
  async downloadReceipt(paymentId: number) {
    const response = await apiClient.get<Blob>(`/users/rent/payments/${paymentId}/receipt`, { responseType: 'blob' });
    downloadBlob(response.data, `rent-receipt-${paymentId}.txt`);
  }
};

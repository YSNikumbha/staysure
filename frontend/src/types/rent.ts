import type { PaymentMethod, SecurityDeposit } from './booking';

export type RentInvoiceStatus = 'PENDING' | 'PARTIALLY_PAID' | 'PAID' | 'OVERDUE' | 'CANCELLED';

export type RentSummary = {
  totalRent: number;
  collected: number;
  outstanding: number;
  overdueAmount: number;
  pendingInvoices: number;
  overdueInvoices: number;
  securityDeposit?: SecurityDeposit | null;
};

export type RentInvoiceSummary = {
  id: number;
  invoiceNumber: string;
  tenantProfileId: number;
  tenantName: string;
  propertyId: number;
  propertyName: string;
  roomNumber: string;
  bedLabel: string;
  billingMonth: number;
  billingYear: number;
  totalAmount: number;
  paidAmount: number;
  balanceAmount: number;
  dueDate: string;
  status: RentInvoiceStatus;
};

export type RentPayment = {
  id: number;
  paymentNumber: string;
  invoiceId: number;
  invoiceNumber: string;
  amount: number;
  paymentMethod: PaymentMethod;
  paymentReference?: string | null;
  paymentDate: string;
  remarks?: string | null;
  createdAt: string;
};

export type RentInvoiceDetail = RentInvoiceSummary & {
  baseRent: number;
  maintenanceCharge: number;
  electricityCharge: number;
  otherCharge: number;
  lateFee: number;
  notes?: string | null;
  generatedAt: string;
  securityDeposit?: SecurityDeposit | null;
  payments: RentPayment[];
};

export type RentDashboard = {
  summary: RentSummary;
  invoices: RentInvoiceSummary[];
};

export type GenerateRentInput = {
  propertyId: number;
  billingMonth: number;
  billingYear: number;
};

export type GenerateRentResult = {
  propertyId: number;
  billingMonth: number;
  billingYear: number;
  generatedCount: number;
  alreadyGeneratedCount: number;
  skippedCount: number;
  invoices: RentInvoiceSummary[];
};

export type UpdateRentChargesInput = {
  maintenanceCharge?: number;
  electricityCharge?: number;
  otherCharge?: number;
  lateFee?: number;
  notes?: string;
};

export type RecordRentPaymentInput = {
  amount: number;
  paymentMethod: PaymentMethod;
  paymentReference?: string;
  paymentDate: string;
  remarks?: string;
};

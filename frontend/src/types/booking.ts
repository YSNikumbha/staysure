export type BookingStatus = 'REQUESTED' | 'APPROVED' | 'AWAITING_KYC' | 'KYC_VERIFICATION' | 'AWAITING_DEPOSIT' | 'AWAITING_AGREEMENT' | 'CONFIRMED' | 'REJECTED' | 'CANCELLED' | 'CHECKED_IN' | 'CHECKED_OUT';

export type TenantStatus = 'UPCOMING' | 'ACTIVE' | 'NOTICE_PERIOD' | 'CHECKED_OUT' | 'ARCHIVED';

export type DocumentType = 'AADHAAR' | 'PAN' | 'PASSPORT' | 'DRIVING_LICENSE' | 'COLLEGE_ID' | 'EMPLOYEE_ID' | 'PHOTO' | 'OTHER';

export type DocumentVerificationStatus = 'PENDING' | 'VERIFIED' | 'REJECTED';

export type DepositStatus = 'PENDING' | 'PARTIALLY_PAID' | 'PAID' | 'REFUND_PENDING' | 'REFUNDED';

export type AgreementStatus = 'DRAFT' | 'ISSUED' | 'ACCEPTED' | 'CANCELLED';

export type PaymentMethod = 'CASH' | 'UPI' | 'BANK_TRANSFER' | 'OTHER';

export type Booking = {
  id: number;
  bookingNumber: string;
  userId: number;
  propertyId: number;
  propertyName: string;
  propertyCity: string;
  propertyStatus: string;
  propertyVerificationStatus: string;
  roomId: number;
  roomNumber: string;
  bedId: number;
  bedNumber: string;
  moveInDate: string;
  expectedMoveOutDate: string;
  monthlyRent: number;
  securityDepositAmount: number;
  status: BookingStatus;
  userRemarks?: string | null;
  ownerRemarks?: string | null;
  approvedAt?: string | null;
  rejectedAt?: string | null;
  rejectionReason?: string | null;
  confirmedAt?: string | null;
  checkedInAt?: string | null;
  cancelledAt?: string | null;
  createdAt: string;
};

export type BookingStatusHistory = {
  id: number;
  bookingId: number;
  previousStatus?: BookingStatus | null;
  newStatus: BookingStatus;
  remarks?: string | null;
  changedBy?: number | null;
  createdAt: string;
};

export type TenantProfile = {
  id: number;
  userId: number;
  bookingId: number;
  propertyId: number;
  roomId: number;
  bedId: number;
  status: TenantStatus;
  joiningDate?: string | null;
  expectedCheckoutDate?: string | null;
  emergencyContactName?: string | null;
  emergencyContactPhone?: string | null;
  collegeOrCompany?: string | null;
  occupationType?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type TenantDocument = {
  id: number;
  bookingId: number;
  userId: number;
  documentType: DocumentType;
  documentNumber?: string | null;
  documentUrl: string;
  verificationStatus: DocumentVerificationStatus;
  rejectionReason?: string | null;
  verifiedBy?: number | null;
  verifiedAt?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type SecurityDeposit = {
  id: number;
  bookingId: number;
  tenantId?: number | null;
  propertyId: number;
  userId: number;
  requiredAmount: number;
  paidAmount: number;
  paymentMethod?: PaymentMethod | null;
  paymentReference?: string | null;
  paidAt?: string | null;
  status: DepositStatus;
  remarks?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type RentalAgreement = {
  id: number;
  bookingId: number;
  propertyId: number;
  userId: number;
  agreementNumber: string;
  startDate: string;
  endDate: string;
  monthlyRent: number;
  securityDeposit: number;
  noticePeriodDays: number;
  lockInMonths: number;
  termsAndConditions?: string | null;
  agreementFileUrl?: string | null;
  status: AgreementStatus;
  createdAt: string;
  updatedAt: string;
};
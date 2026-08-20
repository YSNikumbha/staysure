import type { BedStatus, FurnishingType, SharingType } from './property';

export type BookingStatus =
  | 'REQUESTED'
  | 'AWAITING_KYC'
  | 'KYC_VERIFICATION'
  | 'AWAITING_DEPOSIT'
  | 'AWAITING_AGREEMENT'
  | 'CONFIRMED'
  | 'CHECKED_IN'
  | 'REJECTED'
  | 'CANCELLED';

export type DocumentType =
  | 'AADHAAR'
  | 'PAN'
  | 'PASSPORT'
  | 'DRIVING_LICENSE'
  | 'PHOTO'
  | 'BUSINESS_REGISTRATION'
  | 'ADDRESS_PROOF'
  | 'OTHER';

export type DocumentVerificationStatus = 'PENDING' | 'VERIFIED' | 'REJECTED';
export type DepositStatus = 'PENDING' | 'PARTIALLY_PAID' | 'PAID';
export type PaymentMethod = 'CASH' | 'UPI' | 'BANK_TRANSFER' | 'OTHER';
export type AgreementStatus = 'ISSUED' | 'ACCEPTED';
export type TenantStatus = 'UPCOMING' | 'ACTIVE';

export type BookingUserSummary = {
  id: number;
  firstName: string;
  lastName: string;
  phone: string;
  email: string;
};

export type BookingPropertySummary = {
  id: number;
  slug: string;
  name: string;
  area: string;
  city: string;
  addressLine1: string;
};

export type BookingRoomSummary = {
  id: number;
  roomNumber: string;
  sharingType: SharingType;
  monthlyRent: number;
  securityDeposit: number;
  capacity: number;
  acAvailable: boolean;
  attachedBathroom: boolean;
  furnishingType: FurnishingType;
};

export type BookingBedSummary = {
  id: number;
  bedNumber: string;
  bedLabel?: string | null;
  status: BedStatus;
};

export type TenantDocument = {
  id: number;
  bookingId: number;
  documentType: DocumentType;
  documentNumber?: string | null;
  documentUrl: string;
  originalFileName?: string | null;
  contentType?: string | null;
  sizeBytes?: number | null;
  verificationStatus: DocumentVerificationStatus;
  rejectionReason?: string | null;
  verifiedBy?: number | null;
  verifiedAt?: string | null;
  createdAt: string;
};

export type SecurityDeposit = {
  id: number;
  bookingId: number;
  requiredAmount: number;
  paidAmount: number;
  remainingAmount: number;
  status: DepositStatus;
  lastPaymentMethod?: PaymentMethod | null;
  lastPaymentReference?: string | null;
  remarks?: string | null;
  paidAt?: string | null;
};

export type RentalAgreement = {
  id: number;
  bookingId: number;
  agreementNumber: string;
  status: AgreementStatus;
  documentUrl?: string | null;
  originalFileName?: string | null;
  terms?: string | null;
  startDate: string;
  endDate?: string | null;
  monthlyRent: number;
  securityDeposit: number;
  noticePeriodDays: number;
  lockInMonths: number;
  issuedAt: string;
  acceptedAt?: string | null;
};

export type TenantProfile = {
  id: number;
  bookingId: number;
  user: BookingUserSummary;
  property: BookingPropertySummary;
  room: BookingRoomSummary;
  bed: BookingBedSummary;
  status: TenantStatus;
  joiningDate?: string | null;
  expectedCheckoutDate?: string | null;
  createdAt: string;
};

export type BookingStatusHistory = {
  id: number;
  previousStatus?: BookingStatus | null;
  newStatus: BookingStatus;
  remarks?: string | null;
  actionBy?: number | null;
  createdAt: string;
};

export type Booking = {
  id: number;
  bookingNumber: string;
  status: BookingStatus;
  user: BookingUserSummary;
  property: BookingPropertySummary;
  room: BookingRoomSummary;
  bed: BookingBedSummary;
  moveInDate: string;
  expectedMoveOutDate?: string | null;
  monthlyRent: number;
  securityDeposit: number;
  requestedAt: string;
  approvedAt?: string | null;
  rejectedAt?: string | null;
  cancelledAt?: string | null;
  confirmedAt?: string | null;
  checkedInAt?: string | null;
  rejectionReason?: string | null;
  cancellationReason?: string | null;
  remarks?: string | null;
  documents: TenantDocument[];
  deposit?: SecurityDeposit | null;
  agreement?: RentalAgreement | null;
  tenant?: TenantProfile | null;
  history: BookingStatusHistory[];
};

export type CreateBookingInput = {
  propertyId: number;
  roomId: number;
  bedId: number;
  moveInDate: string;
  expectedMoveOutDate?: string;
  remarks?: string;
};

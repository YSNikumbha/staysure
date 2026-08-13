import type { User } from './user';

export type OwnerVerificationStatus = 'PENDING' | 'UNDER_REVIEW' | 'VERIFIED' | 'REJECTED' | 'SUSPENDED';
export type DocumentType = 'AADHAAR' | 'PAN' | 'BUSINESS_REGISTRATION' | 'ADDRESS_PROOF' | 'OTHER';
export type DocumentVerificationStatus = 'PENDING' | 'VERIFIED' | 'REJECTED';

export type OwnerApplicationInput = {
  businessName: string;
  alternatePhone?: string;
  businessEmail?: string;
  experienceYears?: number;
  description?: string;
};

export type OwnerProfile = {
  id: number;
  user: User;
  businessName: string;
  alternatePhone?: string | null;
  businessEmail?: string | null;
  experienceYears?: number | null;
  description?: string | null;
  verificationStatus: OwnerVerificationStatus;
  verificationRemarks?: string | null;
  verifiedAt?: string | null;
  verifiedBy?: number | null;
  createdAt: string;
  updatedAt: string;
};

export type OwnerDocument = {
  id: number;
  documentType: DocumentType;
  documentNumber?: string | null;
  documentUrl: string;
  originalFileName?: string | null;
  contentType?: string | null;
  sizeBytes?: number | null;
  verificationStatus: DocumentVerificationStatus;
  rejectionReason?: string | null;
  createdAt: string;
};

export type OwnerDetail = {
  profile: OwnerProfile;
  documents: OwnerDocument[];
};

export type OwnerDashboard = {
  ownerId: number;
  businessName: string;
  verificationStatus: OwnerVerificationStatus;
  totalPgs: number;
  activePgs: number;
  totalRooms: number;
  totalBeds: number;
  availableBeds: number;
};

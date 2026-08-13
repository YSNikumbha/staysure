export type GenderType = 'MALE' | 'FEMALE' | 'COED';
export type PropertyType = 'PG' | 'HOSTEL' | 'CO_LIVING' | 'APARTMENT';
export type PropertyStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';
export type PropertyVerificationStatus = 'NOT_SUBMITTED' | 'PENDING' | 'UNDER_REVIEW' | 'VERIFIED' | 'REJECTED' | 'CHANGES_REQUESTED';
export type FloorStatus = 'ACTIVE' | 'INACTIVE' | 'ARCHIVED';
export type SharingType = 'SINGLE' | 'DOUBLE' | 'TRIPLE' | 'FOUR_SHARING' | 'DORMITORY';
export type FurnishingType = 'UNFURNISHED' | 'SEMI_FURNISHED' | 'FULLY_FURNISHED';
export type RoomStatus = 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' | 'ARCHIVED';
export type BedStatus = 'AVAILABLE' | 'RESERVED' | 'OCCUPIED' | 'MAINTENANCE' | 'INACTIVE' | 'ARCHIVED';
export type OwnerEditableBedStatus = 'AVAILABLE' | 'MAINTENANCE' | 'INACTIVE';
export type ImageCategory = 'BUILDING' | 'ROOM' | 'BATHROOM' | 'KITCHEN' | 'DINING' | 'COMMON_AREA' | 'EXTERIOR' | 'PARKING' | 'OTHER';

export type PropertyRuleInput = {
  visitorAllowed: boolean;
  smokingAllowed: boolean;
  alcoholAllowed: boolean;
  cookingAllowed: boolean;
  gateClosingTime?: string;
  lateEntryAllowed: boolean;
  noticePeriodDays?: number;
  additionalRules?: string;
};

export type PgPropertyInput = {
  name: string;
  description?: string;
  genderType: GenderType;
  propertyType: PropertyType;
  addressLine1: string;
  addressLine2?: string;
  area: string;
  city: string;
  state: string;
  pincode: string;
  latitude?: number;
  longitude?: number;
  startingRent: number;
  securityDeposit: number;
  noticePeriodDays: number;
  lockInMonths: number;
  entryTime?: string;
  foodAvailable: boolean;
  status?: Exclude<PropertyStatus, 'ARCHIVED'>;
  rules?: PropertyRuleInput;
};

export type PgProperty = {
  id: number;
  ownerId: number;
  name: string;
  slug: string;
  description?: string | null;
  genderType: GenderType;
  propertyType: PropertyType;
  addressLine1: string;
  addressLine2?: string | null;
  area: string;
  city: string;
  state: string;
  pincode: string;
  latitude?: number | null;
  longitude?: number | null;
  startingRent: number;
  securityDeposit: number;
  noticePeriodDays: number;
  lockInMonths: number;
  entryTime?: string | null;
  foodAvailable: boolean;
  status: PropertyStatus;
  verificationStatus: PropertyVerificationStatus;
  submittedForVerificationAt?: string | null;
  verifiedAt?: string | null;
  verifiedBy?: number | null;
  verificationRemarks?: string | null;
  rejectionReason?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type PropertyRule = {
  id: number;
  propertyId: number;
  visitorAllowed: boolean;
  smokingAllowed: boolean;
  alcoholAllowed: boolean;
  cookingAllowed: boolean;
  gateClosingTime?: string | null;
  lateEntryAllowed: boolean;
  noticePeriodDays: number;
  additionalRules?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type PropertyInventoryCounts = {
  totalFloors: number;
  totalRooms: number;
  totalBeds: number;
  availableBeds: number;
  maintenanceBeds: number;
  inactiveBeds: number;
};

export type PropertySummary = {
  id: number;
  name: string;
  slug: string;
  area: string;
  city: string;
  state: string;
  status: PropertyStatus;
  verificationStatus: PropertyVerificationStatus;
  coverImageUrl?: string | null;
  roomCount: number;
  bedCount: number;
  availableBedCount: number;
  createdAt: string;
};

export type FloorInput = {
  name: string;
  floorNumber: number;
  description?: string;
  status?: Exclude<FloorStatus, 'ARCHIVED'>;
};

export type Floor = {
  id: number;
  propertyId: number;
  name: string;
  floorNumber: number;
  description?: string | null;
  status: FloorStatus;
  roomCount: number;
  bedCount: number;
  rooms: Room[];
  createdAt: string;
  updatedAt: string;
};

export type RoomInput = {
  roomNumber: string;
  roomName?: string;
  sharingType: SharingType;
  capacity: number;
  monthlyRent: number;
  securityDeposit: number;
  acAvailable: boolean;
  attachedBathroom: boolean;
  furnishingType: FurnishingType;
  status?: Exclude<RoomStatus, 'ARCHIVED'>;
  description?: string;
};

export type Room = {
  id: number;
  floorId: number;
  propertyId: number;
  roomNumber: string;
  roomName?: string | null;
  sharingType: SharingType;
  capacity: number;
  monthlyRent: number;
  securityDeposit: number;
  acAvailable: boolean;
  attachedBathroom: boolean;
  furnishingType: FurnishingType;
  status: RoomStatus;
  description?: string | null;
  bedCount: number;
  beds: Bed[];
  createdAt: string;
  updatedAt: string;
};

export type BedInput = {
  bedNumber: string;
  bedLabel?: string;
  status?: OwnerEditableBedStatus;
};

export type Bed = {
  id: number;
  roomId: number;
  propertyId: number;
  bedNumber: string;
  bedLabel?: string | null;
  status: BedStatus;
  createdAt: string;
  updatedAt: string;
};

export type Amenity = {
  id: number;
  name: string;
  code: string;
  icon?: string | null;
  description?: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
};

export type PgImage = {
  id: number;
  propertyId: number;
  imageUrl: string;
  category: ImageCategory;
  coverImage: boolean;
  sortOrder: number;
  createdAt: string;
};

export type PropertyDetails = {
  property: PgProperty;
  rules?: PropertyRule | null;
  amenities: Amenity[];
  images: PgImage[];
  floors: Floor[];
  roomCount: number;
  bedCount: number;
  availableBedCount: number;
  counts: PropertyInventoryCounts;
};

export type PaginationResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type PublicPgCard = {
  id: number;
  slug: string;
  name: string;
  coverImage?: string | null;
  area: string;
  city: string;
  genderType: GenderType;
  propertyType: PropertyType;
  startingRent: number;
  securityDeposit: number;
  foodAvailable: boolean;
  averageRating?: number | null;
  totalBeds: number;
  availableBeds: number;
  verificationStatus: PropertyVerificationStatus;
  amenities: Amenity[];
};

export type PublicRoomAvailability = {
  roomId: number;
  roomNumber: string;
  sharingType: SharingType;
  monthlyRent: number;
  securityDeposit: number;
  capacity: number;
  availableBeds: number;
  acAvailable: boolean;
  attachedBathroom: boolean;
  furnishingType: FurnishingType;
};

export type PublicPgDetails = {
  id: number;
  slug: string;
  name: string;
  description?: string | null;
  genderType: GenderType;
  propertyType: PropertyType;
  addressLine1: string;
  addressLine2?: string | null;
  area: string;
  city: string;
  state: string;
  pincode: string;
  latitude?: number | null;
  longitude?: number | null;
  startingRent: number;
  securityDeposit: number;
  noticePeriodDays: number;
  lockInMonths: number;
  entryTime?: string | null;
  foodAvailable: boolean;
  rules?: PropertyRule | null;
  amenities: Amenity[];
  gallery: PgImage[];
  availableRooms: PublicRoomAvailability[];
  availableBedCount: number;
  totalBedCount: number;
  owner?: {
    businessName: string;
    experienceYears?: number | null;
  } | null;
};

export type VerificationHistory = {
  id: number;
  propertyId: number;
  previousStatus?: PropertyVerificationStatus | null;
  newStatus: PropertyVerificationStatus;
  remarks?: string | null;
  actionBy?: number | null;
  createdAt: string;
};

export type AdminPropertySummary = {
  id: number;
  name: string;
  ownerName: string;
  ownerId: number;
  city: string;
  submittedForVerificationAt?: string | null;
  verificationStatus: PropertyVerificationStatus;
  status: PropertyStatus;
  roomCount: number;
  bedCount: number;
};

export type AdminPropertyDetails = {
  owner: import('./owner').OwnerProfile;
  propertyDetails: PropertyDetails;
  verificationHistory: VerificationHistory[];
};

export type WishlistItem = {
  id: number;
  property: PublicPgCard;
  createdAt: string;
};

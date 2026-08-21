export type OperationalPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT';
export type ComplaintCategory = 'ELECTRICAL' | 'PLUMBING' | 'CLEANING' | 'INTERNET' | 'FURNITURE' | 'APPLIANCE' | 'SECURITY' | 'FOOD' | 'ROOM' | 'OTHER';
export type ComplaintStatus = 'OPEN' | 'ACKNOWLEDGED' | 'IN_PROGRESS' | 'RESOLVED' | 'CLOSED' | 'REOPENED' | 'CANCELLED';
export type MaintenanceStatus = 'PENDING' | 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
export type NoticeType = 'GENERAL' | 'MAINTENANCE' | 'PAYMENT' | 'EVENT' | 'EMERGENCY' | 'FOOD' | 'RULE' | 'OTHER';
export type NoticeStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
export type MealType = 'BREAKFAST' | 'LUNCH' | 'SNACKS' | 'DINNER';
export type VisitorStatus = 'REQUESTED' | 'APPROVED' | 'REJECTED' | 'CHECKED_IN' | 'CHECKED_OUT' | 'CANCELLED';
export type NotificationType =
  | 'COMPLAINT_CREATED'
  | 'COMPLAINT_ACKNOWLEDGED'
  | 'COMPLAINT_IN_PROGRESS'
  | 'COMPLAINT_RESOLVED'
  | 'COMPLAINT_COMMENT_ADDED'
  | 'COMPLAINT_REOPENED'
  | 'COMPLAINT_CANCELLED'
  | 'NOTICE_PUBLISHED'
  | 'VISITOR_REQUESTED'
  | 'VISITOR_APPROVED'
  | 'VISITOR_REJECTED'
  | 'RENT_INVOICE_GENERATED'
  | 'RENT_PAYMENT_RECORDED'
  | 'BOOKING_APPROVED'
  | 'BOOKING_REJECTED'
  | 'BOOKING_CONFIRMED'
  | 'KYC_REJECTED';

export type ComplaintComment = {
  id: number;
  authorUserId: number;
  authorName: string;
  comment: string;
  createdAt: string;
};

export type ComplaintHistory = {
  id: number;
  previousStatus?: ComplaintStatus | null;
  newStatus: ComplaintStatus;
  remarks?: string | null;
  changedBy?: number | null;
  createdAt: string;
};

export type Complaint = {
  id: number;
  complaintNumber: string;
  tenantProfileId: number;
  tenantName: string;
  propertyId: number;
  propertyName: string;
  roomId?: number | null;
  roomNumber?: string | null;
  category: ComplaintCategory;
  title: string;
  description: string;
  priority: OperationalPriority;
  status: ComplaintStatus;
  resolvedAt?: string | null;
  closedAt?: string | null;
  createdAt: string;
  updatedAt: string;
  comments: ComplaintComment[];
  history: ComplaintHistory[];
};

export type ComplaintInput = {
  category: ComplaintCategory;
  title: string;
  description: string;
  priority?: OperationalPriority;
};

export type MaintenanceTask = {
  id: number;
  taskNumber: string;
  complaintId?: number | null;
  complaintNumber?: string | null;
  propertyId: number;
  propertyName: string;
  roomId?: number | null;
  roomNumber?: string | null;
  title: string;
  description: string;
  priority: OperationalPriority;
  status: MaintenanceStatus;
  assignedToText?: string | null;
  scheduledDate?: string | null;
  completedAt?: string | null;
  remarks?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type MaintenanceTaskInput = {
  propertyId: number;
  complaintId?: number;
  title: string;
  description: string;
  priority?: OperationalPriority;
  assignedToText?: string;
  scheduledDate?: string;
  remarks?: string;
};

export type Notice = {
  id: number;
  propertyId: number;
  propertyName: string;
  title: string;
  content: string;
  noticeType: NoticeType;
  priority: OperationalPriority;
  status: NoticeStatus;
  publishedAt?: string | null;
  expiresAt?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type NoticeInput = {
  propertyId: number;
  title: string;
  content: string;
  noticeType?: NoticeType;
  priority?: OperationalPriority;
  expiresAt?: string;
};

export type FoodMenu = {
  id: number;
  propertyId: number;
  propertyName: string;
  menuDate: string;
  mealType: MealType;
  items: string;
  notes?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type FoodMenuInput = {
  propertyId: number;
  menuDate: string;
  mealType: MealType;
  items: string;
  notes?: string;
};

export type FoodFeedback = {
  id: number;
  tenantProfileId: number;
  tenantName: string;
  propertyId: number;
  propertyName: string;
  menuDate: string;
  mealType: MealType;
  rating: number;
  comment?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type FoodFeedbackInput = {
  menuDate: string;
  mealType: MealType;
  rating: number;
  comment?: string;
};

export type VisitorEntry = {
  id: number;
  visitorNumber: string;
  tenantProfileId: number;
  tenantName: string;
  propertyId: number;
  propertyName: string;
  visitorName: string;
  visitorPhone: string;
  relationship: string;
  visitDate: string;
  expectedArrivalTime: string;
  expectedDepartureTime: string;
  actualArrivalTime?: string | null;
  actualDepartureTime?: string | null;
  purpose: string;
  status: VisitorStatus;
  rejectionReason?: string | null;
  approvedAt?: string | null;
  createdAt: string;
  updatedAt: string;
};

export type VisitorInput = {
  visitorName: string;
  visitorPhone: string;
  relationship: string;
  visitDate: string;
  expectedArrivalTime: string;
  expectedDepartureTime: string;
  purpose: string;
};

export type Notification = {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  referenceType?: string | null;
  referenceId?: number | null;
  readAt?: string | null;
  createdAt: string;
};

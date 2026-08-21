type StatusTone = 'neutral' | 'success' | 'warning' | 'danger';

const toneByStatus: Record<string, StatusTone> = {
  ACTIVE: 'success',
  AVAILABLE: 'success',
  COVER: 'success',
  VERIFIED: 'success',
  PENDING: 'warning',
  UNDER_REVIEW: 'warning',
  CHANGES_REQUESTED: 'warning',
  DRAFT: 'warning',
  OPEN: 'warning',
  ACKNOWLEDGED: 'warning',
  IN_PROGRESS: 'warning',
  REOPENED: 'warning',
  SCHEDULED: 'warning',
  PUBLISHED: 'success',
  APPROVED: 'success',
  COMPLETED: 'success',
  UNREAD: 'warning',
  READ: 'neutral',
  NOT_SUBMITTED: 'neutral',
  MAINTENANCE: 'warning',
  RESERVED: 'warning',
  REQUESTED: 'warning',
  AWAITING_KYC: 'warning',
  KYC_VERIFICATION: 'warning',
  AWAITING_DEPOSIT: 'warning',
  AWAITING_AGREEMENT: 'warning',
  PARTIALLY_PAID: 'warning',
  OVERDUE: 'danger',
  ISSUED: 'warning',
  CONFIRMED: 'success',
  CHECKED_IN: 'success',
  PAID: 'success',
  ACCEPTED: 'success',
  RESOLVED: 'success',
  CLOSED: 'success',
  UPCOMING: 'warning',
  REJECTED: 'danger',
  CANCELLED: 'danger',
  URGENT: 'danger',
  HIGH: 'danger',
  MEDIUM: 'warning',
  LOW: 'neutral',
  SUSPENDED: 'danger',
  OCCUPIED: 'danger',
  MALE: 'neutral',
  FEMALE: 'neutral',
  COED: 'neutral',
  PG: 'neutral',
  HOSTEL: 'neutral',
  CO_LIVING: 'neutral',
  APARTMENT: 'neutral',
  AC: 'neutral',
  BATHROOM: 'neutral',
  FULLY_FURNISHED: 'neutral',
  SEMI_FURNISHED: 'neutral',
  UNFURNISHED: 'neutral',
  INACTIVE: 'neutral',
  ARCHIVED: 'neutral'
};

type StatusBadgeProps = {
  status: string;
};

export function StatusBadge({ status }: StatusBadgeProps) {
  const tone = toneByStatus[status] ?? 'neutral';
  return <span className={`status-badge status-badge--${tone}`}>{status.replaceAll('_', ' ')}</span>;
}

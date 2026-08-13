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
  NOT_SUBMITTED: 'neutral',
  MAINTENANCE: 'warning',
  RESERVED: 'warning',
  REJECTED: 'danger',
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

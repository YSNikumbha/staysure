import type { ReactNode } from 'react';

type AdminReviewDialogProps = {
  title: string;
  description: string;
  actionLabel: string;
  tone?: 'primary' | 'warning' | 'danger';
  icon?: ReactNode;
  remarksLabel?: string;
  remarksValue: string;
  remarksRequired?: boolean;
  isPending?: boolean;
  error?: string | null;
  onRemarksChange: (value: string) => void;
  onCancel: () => void;
  onConfirm: () => void;
};

export function AdminReviewDialog({
  title,
  description,
  actionLabel,
  tone = 'primary',
  icon,
  remarksLabel = 'Remarks',
  remarksValue,
  remarksRequired = false,
  isPending = false,
  error,
  onRemarksChange,
  onCancel,
  onConfirm
}: AdminReviewDialogProps) {
  const actionClass = tone === 'danger' ? 'danger-button' : tone === 'warning' ? 'warning-button' : 'primary-button';

  return (
    <div className="modal-backdrop admin-modal-backdrop" role="presentation">
      <section className="modal-panel admin-review-dialog" role="dialog" aria-modal="true" aria-labelledby="admin-review-dialog-title">
        <div className="admin-review-dialog__header">
          <span>{icon}</span>
          <div>
            <h2 id="admin-review-dialog-title">{title}</h2>
            <p>{description}</p>
          </div>
        </div>
        <label>
          {remarksLabel}{remarksRequired ? ' *' : ''}
          <textarea
            rows={4}
            value={remarksValue}
            onChange={(event) => onRemarksChange(event.target.value)}
            placeholder={remarksRequired ? 'Enter a clear reason for this decision' : 'Optional remarks'}
          />
        </label>
        {error ? <p className="form-message form-message--error">{error}</p> : null}
        <div className="admin-review-dialog__actions">
          <button className="secondary-button" type="button" onClick={onCancel} disabled={isPending}>Cancel</button>
          <button className={actionClass} type="button" onClick={onConfirm} disabled={isPending}>
            {actionLabel}
          </button>
        </div>
      </section>
    </div>
  );
}

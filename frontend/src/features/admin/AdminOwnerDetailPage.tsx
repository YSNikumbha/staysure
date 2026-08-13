import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Ban, CheckCircle2, CircleSlash } from 'lucide-react';
import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { adminApi } from '../../api/admin.api';
import { FormMessage } from '../../components/FormMessage';
import { PageHeader } from '../../components/PageHeader';
import { StatusBadge } from '../../components/StatusBadge';
import { getApiErrorMessage } from '../../utils/apiError';
import { toAssetUrl } from '../../utils/assets';

export function AdminOwnerDetailPage() {
  const { id } = useParams();
  const queryClient = useQueryClient();
  const [reason, setReason] = useState('');
  const [remarks, setRemarks] = useState('');
  const [actionError, setActionError] = useState<string | null>(null);

  const ownerQuery = useQuery({
    queryKey: ['admin-owner', id],
    queryFn: () => adminApi.owner(id!),
    enabled: Boolean(id)
  });

  const invalidate = async () => {
    await queryClient.invalidateQueries({ queryKey: ['admin-owner', id] });
    await queryClient.invalidateQueries({ queryKey: ['admin-owners'] });
  };

  const verifyMutation = useMutation({
    mutationFn: () => adminApi.verifyOwner(Number(id), remarks || undefined),
    onSuccess: invalidate,
    onError: (err) => setActionError(getApiErrorMessage(err, 'Unable to verify owner'))
  });

  const rejectMutation = useMutation({
    mutationFn: () => adminApi.rejectOwner(Number(id), reason),
    onSuccess: invalidate,
    onError: (err) => setActionError(getApiErrorMessage(err, 'Unable to reject owner'))
  });

  const suspendMutation = useMutation({
    mutationFn: () => adminApi.suspendOwner(Number(id), reason),
    onSuccess: invalidate,
    onError: (err) => setActionError(getApiErrorMessage(err, 'Unable to suspend owner'))
  });

  if (ownerQuery.isLoading) {
    return <div className="route-state">Loading</div>;
  }

  if (ownerQuery.isError || !ownerQuery.data) {
    return <div className="route-state">{getApiErrorMessage(ownerQuery.error, 'Unable to load owner application')}</div>;
  }

  const { profile, documents } = ownerQuery.data;
  const actionPending = verifyMutation.isPending || rejectMutation.isPending || suspendMutation.isPending;

  const confirmReject = () => {
    if (!reason.trim()) {
      setActionError('Reason is required.');
      return;
    }
    if (window.confirm('Reject this owner application?')) {
      rejectMutation.mutate();
    }
  };

  const confirmSuspend = () => {
    if (!reason.trim()) {
      setActionError('Reason is required.');
      return;
    }
    if (window.confirm('Suspend this owner profile?')) {
      suspendMutation.mutate();
    }
  };

  const confirmVerify = () => {
    if (window.confirm('Verify this owner and assign PG_OWNER role?')) {
      verifyMutation.mutate();
    }
  };

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Owner review"
        title={profile.businessName}
        actions={<Link className="secondary-link" to="/admin/owners">Back</Link>}
      />

      <section className="surface status-surface">
        <div>
          <h2>{profile.user.firstName} {profile.user.lastName}</h2>
          <p>{profile.user.email} · {profile.user.phone}</p>
        </div>
        <StatusBadge status={profile.verificationStatus} />
      </section>

      <section className="surface detail-grid">
        <div>
          <span>Business email</span>
          <strong>{profile.businessEmail || 'Not provided'}</strong>
        </div>
        <div>
          <span>Alternate phone</span>
          <strong>{profile.alternatePhone || 'Not provided'}</strong>
        </div>
        <div>
          <span>Experience</span>
          <strong>{profile.experienceYears ?? 0} years</strong>
        </div>
        <div>
          <span>Remarks</span>
          <strong>{profile.verificationRemarks || 'None'}</strong>
        </div>
      </section>

      <section className="surface">
        <h2>KYC Documents</h2>
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Type</th>
                <th>Number</th>
                <th>File</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {documents.map((document) => (
                <tr key={document.id}>
                  <td>{document.documentType.replaceAll('_', ' ')}</td>
                  <td>{document.documentNumber || 'Not provided'}</td>
                  <td>
                    <a href={toAssetUrl(document.documentUrl)} target="_blank" rel="noreferrer">
                      {document.originalFileName ?? 'Document'}
                    </a>
                  </td>
                  <td><StatusBadge status={document.verificationStatus} /></td>
                </tr>
              ))}
              {documents.length === 0 ? (
                <tr>
                  <td colSpan={4}>No documents uploaded.</td>
                </tr>
              ) : null}
            </tbody>
          </table>
        </div>
      </section>

      <section className="surface">
        <h2>Review Action</h2>
        <div className="form-grid">
          <label>
            Verification remarks
            <textarea rows={3} value={remarks} onChange={(event) => setRemarks(event.target.value)} />
          </label>
          <label>
            Rejection or suspension reason
            <textarea rows={3} value={reason} onChange={(event) => setReason(event.target.value)} />
          </label>
          <FormMessage message={actionError} />
          <div className="action-row">
            <button className="primary-button" type="button" onClick={confirmVerify} disabled={actionPending}>
              <CheckCircle2 size={18} />
              Verify
            </button>
            <button className="warning-button" type="button" onClick={confirmReject} disabled={actionPending}>
              <Ban size={18} />
              Reject
            </button>
            <button className="danger-button" type="button" onClick={confirmSuspend} disabled={actionPending}>
              <CircleSlash size={18} />
              Suspend
            </button>
          </div>
        </div>
      </section>
    </div>
  );
}

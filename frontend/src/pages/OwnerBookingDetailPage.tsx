import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ownerBookingApi } from '../api/booking.api';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import type { PaymentMethod } from '../types/booking';
import { getApiErrorMessage } from '../utils/apiError';
import { toAssetUrl } from '../utils/assets';

const paymentMethods: PaymentMethod[] = ['CASH', 'UPI', 'BANK_TRANSFER', 'OTHER'];

export default function OwnerBookingDetailPage() {
  const { id } = useParams();
  const bookingId = Number(id);
  const queryClient = useQueryClient();
  const [remarks, setRemarks] = useState('');
  const [depositAmount, setDepositAmount] = useState('');
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('UPI');
  const [paymentReference, setPaymentReference] = useState('');
  const [agreementTerms, setAgreementTerms] = useState('');
  const [agreementEndDate, setAgreementEndDate] = useState('');
  const [agreementFile, setAgreementFile] = useState<File | null>(null);
  const query = useQuery({ queryKey: ['owner-booking', bookingId], queryFn: () => ownerBookingApi.get(bookingId), enabled: Number.isFinite(bookingId) });
  const refresh = async () => queryClient.invalidateQueries({ queryKey: ['owner-booking', bookingId] });
  const approve = useMutation({ mutationFn: () => ownerBookingApi.approve(bookingId, remarks || undefined), onSuccess: refresh });
  const reject = useMutation({ mutationFn: () => ownerBookingApi.reject(bookingId, remarks), onSuccess: refresh });
  const verifyDoc = useMutation({ mutationFn: (docId: number) => ownerBookingApi.verifyDocument(bookingId, docId), onSuccess: refresh });
  const rejectDoc = useMutation({ mutationFn: (docId: number) => ownerBookingApi.rejectDocument(bookingId, docId, remarks), onSuccess: refresh });
  const recordDeposit = useMutation({
    mutationFn: () => ownerBookingApi.recordDeposit(bookingId, {
      amount: Number(depositAmount),
      paymentMethod,
      paymentReference: paymentReference || undefined,
      remarks: remarks || undefined
    }),
    onSuccess: async () => {
      setDepositAmount('');
      await refresh();
    }
  });
  const issueAgreement = useMutation({
    mutationFn: () => ownerBookingApi.issueAgreement(bookingId, {
      endDate: agreementEndDate || undefined,
      terms: agreementTerms || undefined,
      file: agreementFile ?? undefined
    }),
    onSuccess: async () => {
      setAgreementFile(null);
      await refresh();
    }
  });
  const checkIn = useMutation({ mutationFn: () => ownerBookingApi.checkIn(bookingId), onSuccess: refresh });

  if (query.isLoading) return <div className="route-state">Loading booking</div>;
  if (query.isError || !query.data) return <div className="route-state">{getApiErrorMessage(query.error, 'Unable to load booking')}</div>;

  const booking = query.data;
  const error = approve.error ?? reject.error ?? verifyDoc.error ?? rejectDoc.error ?? recordDeposit.error ?? issueAgreement.error ?? checkIn.error;

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Owner booking"
        title={booking.bookingNumber}
        actions={<div className="action-row"><Link className="secondary-link" to="/owner/bookings">Back</Link><StatusBadge status={booking.status} /></div>}
      />
      <FormMessage message={error ? getApiErrorMessage(error, 'Unable to update booking') : null} />

      <section className="surface detail-grid">
        <div><span>Tenant</span><strong>{booking.user.firstName} {booking.user.lastName}</strong></div>
        <div><span>Phone</span><strong>{booking.user.phone}</strong></div>
        <div><span>PG</span><strong>{booking.property.name}</strong></div>
        <div><span>Room / Bed</span><strong>{booking.room.roomNumber} / {booking.bed.bedLabel || booking.bed.bedNumber}</strong></div>
        <div><span>Move-in</span><strong>{booking.moveInDate}</strong></div>
        <div><span>Rent</span><strong>Rs {Number(booking.monthlyRent).toLocaleString()}</strong></div>
        <div><span>Deposit</span><strong>Rs {Number(booking.securityDeposit).toLocaleString()}</strong></div>
        <div><span>Tenant Profile</span><strong>{booking.tenant?.status ?? 'Not created'}</strong></div>
      </section>

      <section className="surface">
        <h2>Actions</h2>
        <label>
          Remarks
          <textarea rows={3} value={remarks} onChange={(event) => setRemarks(event.target.value)} />
        </label>
        <div className="action-row action-row-spaced">
          {booking.status === 'REQUESTED' ? (
            <>
              <button className="primary-button" type="button" onClick={() => approve.mutate()}>Approve</button>
              <button className="danger-button" type="button" onClick={() => reject.mutate()} disabled={!remarks.trim()}>Reject</button>
            </>
          ) : null}
          {booking.status === 'CONFIRMED' ? <button className="primary-button" type="button" onClick={() => checkIn.mutate()}>Check In</button> : null}
        </div>
      </section>

      <section className="surface">
        <div className="section-heading"><h2>KYC Documents</h2><span className="muted-copy">Requires verified government ID + PHOTO</span></div>
        {booking.documents.length === 0 ? <div className="empty-state">No KYC documents uploaded yet.</div> : (
          <div className="table-wrap">
            <table>
              <thead><tr><th>Type</th><th>File</th><th>Status</th><th>Reason</th><th className="table-actions">Actions</th></tr></thead>
              <tbody>
                {booking.documents.map((doc) => (
                  <tr key={doc.id}>
                    <td>{doc.documentType.replaceAll('_', ' ')}</td>
                    <td><a href={toAssetUrl(doc.documentUrl)} target="_blank" rel="noreferrer">{doc.originalFileName ?? 'View'}</a></td>
                    <td><StatusBadge status={doc.verificationStatus} /></td>
                    <td>{doc.rejectionReason ?? '-'}</td>
                    <td className="table-actions">
                      {doc.verificationStatus === 'PENDING' ? (
                        <div className="action-row">
                          <button className="primary-button compact-button" type="button" onClick={() => verifyDoc.mutate(doc.id)}>Verify</button>
                          <button className="danger-button compact-button" type="button" onClick={() => rejectDoc.mutate(doc.id)} disabled={!remarks.trim()}>Reject</button>
                        </div>
                      ) : null}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {booking.status === 'AWAITING_DEPOSIT' || booking.deposit ? (
        <section className="surface">
          <div className="section-heading"><h2>Security Deposit</h2><StatusBadge status={booking.deposit?.status ?? 'PENDING'} /></div>
          <div className="detail-grid">
            <div><span>Required</span><strong>Rs {Number(booking.deposit?.requiredAmount ?? booking.securityDeposit).toLocaleString()}</strong></div>
            <div><span>Paid</span><strong>Rs {Number(booking.deposit?.paidAmount ?? 0).toLocaleString()}</strong></div>
            <div><span>Remaining</span><strong>Rs {Number(booking.deposit?.remainingAmount ?? booking.securityDeposit).toLocaleString()}</strong></div>
            <div><span>Reference</span><strong>{booking.deposit?.lastPaymentReference ?? '-'}</strong></div>
          </div>
          {booking.status === 'AWAITING_DEPOSIT' ? (
            <form className="inline-form three-column" onSubmit={(event) => { event.preventDefault(); recordDeposit.mutate(); }}>
              <label>Amount<input type="number" min="1" value={depositAmount} onChange={(event) => setDepositAmount(event.target.value)} required /></label>
              <label>Method<select value={paymentMethod} onChange={(event) => setPaymentMethod(event.target.value as PaymentMethod)}>{paymentMethods.map((item) => <option key={item}>{item}</option>)}</select></label>
              <label>Reference<input value={paymentReference} onChange={(event) => setPaymentReference(event.target.value)} /></label>
              <button className="primary-button" type="submit">Record Deposit</button>
            </form>
          ) : null}
        </section>
      ) : null}

      {booking.status === 'AWAITING_AGREEMENT' || booking.agreement ? (
        <section className="surface">
          <div className="section-heading"><h2>Rental Agreement</h2>{booking.agreement ? <StatusBadge status={booking.agreement.status} /> : null}</div>
          {booking.agreement ? (
            <div className="detail-grid">
              <div><span>Number</span><strong>{booking.agreement.agreementNumber}</strong></div>
              <div><span>Start</span><strong>{booking.agreement.startDate}</strong></div>
              <div><span>End</span><strong>{booking.agreement.endDate ?? 'Not set'}</strong></div>
              <div><span>Accepted</span><strong>{booking.agreement.acceptedAt ? new Date(booking.agreement.acceptedAt).toLocaleString() : '-'}</strong></div>
            </div>
          ) : null}
          {booking.status === 'AWAITING_AGREEMENT' ? (
            <form className="form-grid" onSubmit={(event) => { event.preventDefault(); issueAgreement.mutate(); }}>
              <label>End Date<input type="date" value={agreementEndDate} onChange={(event) => setAgreementEndDate(event.target.value)} /></label>
              <label className="form-span">Terms<textarea rows={4} value={agreementTerms} onChange={(event) => setAgreementTerms(event.target.value)} /></label>
              <label>PDF<input type="file" accept="application/pdf" onChange={(event) => setAgreementFile(event.target.files?.[0] ?? null)} /></label>
              <button className="primary-button" type="submit">Issue Agreement</button>
            </form>
          ) : null}
        </section>
      ) : null}
    </div>
  );
}

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Banknote, FileText, Receipt, ShieldCheck } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ownerRentApi } from '../api/rent.api';
import { FormMessage } from '../components/FormMessage';
import { StatusBadge } from '../components/StatusBadge';
import { OwnerShell } from '../features/owner/OwnerShell';
import type { PaymentMethod } from '../types/booking';
import { getApiErrorMessage } from '../utils/apiError';

const paymentMethods: PaymentMethod[] = ['CASH', 'UPI', 'BANK_TRANSFER', 'OTHER'];

export default function OwnerRentDetailPage() {
  const { id } = useParams();
  const invoiceId = Number(id);
  const queryClient = useQueryClient();
  const [maintenanceCharge, setMaintenanceCharge] = useState('');
  const [electricityCharge, setElectricityCharge] = useState('');
  const [otherCharge, setOtherCharge] = useState('');
  const [lateFee, setLateFee] = useState('');
  const [notes, setNotes] = useState('');
  const [paymentAmount, setPaymentAmount] = useState('');
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>('UPI');
  const [paymentReference, setPaymentReference] = useState('');
  const [paymentDate, setPaymentDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [paymentRemarks, setPaymentRemarks] = useState('');

  const query = useQuery({ queryKey: ['owner-rent-invoice', invoiceId], queryFn: () => ownerRentApi.get(invoiceId), enabled: Number.isFinite(invoiceId) });
  const refresh = async () => {
    await queryClient.invalidateQueries({ queryKey: ['owner-rent-invoice', invoiceId] });
    await queryClient.invalidateQueries({ queryKey: ['owner-rent'] });
  };
  const updateCharges = useMutation({
    mutationFn: () => ownerRentApi.updateCharges(invoiceId, {
      maintenanceCharge: toAmount(maintenanceCharge),
      electricityCharge: toAmount(electricityCharge),
      otherCharge: toAmount(otherCharge),
      lateFee: toAmount(lateFee),
      notes: notes || undefined
    }),
    onSuccess: refresh
  });
  const recordPayment = useMutation({
    mutationFn: () => ownerRentApi.recordPayment(invoiceId, {
      amount: Number(paymentAmount),
      paymentMethod,
      paymentReference: paymentReference || undefined,
      paymentDate,
      remarks: paymentRemarks || undefined
    }),
    onSuccess: async () => {
      setPaymentAmount('');
      setPaymentReference('');
      setPaymentRemarks('');
      await refresh();
    }
  });
  const downloadReceipt = useMutation({ mutationFn: (paymentId: number) => ownerRentApi.downloadReceipt(paymentId) });

  useEffect(() => {
    if (!query.data) return;
    setMaintenanceCharge(String(query.data.maintenanceCharge ?? 0));
    setElectricityCharge(String(query.data.electricityCharge ?? 0));
    setOtherCharge(String(query.data.otherCharge ?? 0));
    setLateFee(String(query.data.lateFee ?? 0));
    setNotes(query.data.notes ?? '');
  }, [query.data]);

  if (query.isLoading) {
    return (
      <OwnerShell title="Rent Invoice" eyebrow="Monthly rent">
        <div className="owner-stack">
          <div className="owner-skeleton-card" />
          <div className="owner-skeleton-card" />
        </div>
      </OwnerShell>
    );
  }

  if (query.isError || !query.data) {
    return (
      <OwnerShell title="Rent Invoice" eyebrow="Monthly rent">
        <div className="route-state">{getApiErrorMessage(query.error, 'Unable to load rent invoice')}</div>
      </OwnerShell>
    );
  }

  const invoice = query.data;
  const canEditCharges = invoice.status !== 'PAID' && invoice.status !== 'CANCELLED';
  const canRecordPayment = invoice.status !== 'PAID' && invoice.status !== 'CANCELLED' && Number(invoice.balanceAmount) > 0;
  const paymentTooHigh = Number(paymentAmount || 0) > Number(invoice.balanceAmount);
  const mutationError = updateCharges.error ?? recordPayment.error ?? downloadReceipt.error;

  return (
    <OwnerShell
      title={invoice.invoiceNumber}
      eyebrow="Rent invoice"
      actions={<div className="action-row"><Link className="secondary-link compact-button" to="/owner/rent">Back</Link><StatusBadge status={invoice.status} /></div>}
    >
      <div className="owner-stack">
        <FormMessage message={mutationError ? getApiErrorMessage(mutationError, 'Unable to update rent invoice') : null} />
        <FormMessage tone="success" message={updateCharges.isSuccess ? 'Charges updated.' : null} />
        <FormMessage tone="success" message={recordPayment.isSuccess ? 'Payment recorded.' : null} />

        <section className="owner-hero-card owner-hero-card--compact">
          <div>
            <p className="eyebrow">{monthName(invoice.billingMonth)} {invoice.billingYear}</p>
            <h2>{invoice.tenantName}</h2>
            <p>{invoice.propertyName} · Room {invoice.roomNumber} / {invoice.bedLabel}</p>
          </div>
          <Receipt size={28} />
        </section>

        <section className="surface owner-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Summary</p>
              <h2>Rent ledger</h2>
            </div>
          </div>
          <div className="detail-grid">
            <div><span>Base Rent</span><strong>{formatMoney(invoice.baseRent)}</strong></div>
            <div><span>Total</span><strong>{formatMoney(invoice.totalAmount)}</strong></div>
            <div><span>Paid</span><strong>{formatMoney(invoice.paidAmount)}</strong></div>
            <div><span>Balance</span><strong>{formatMoney(invoice.balanceAmount)}</strong></div>
            <div><span>Due Date</span><strong>{formatDate(invoice.dueDate)}</strong></div>
            <div><span>Generated</span><strong>{formatDateTime(invoice.generatedAt)}</strong></div>
            <div><span>Invoice Status</span><strong><StatusBadge status={invoice.status} /></strong></div>
            <div><span>Tenant Profile</span><strong>#{invoice.tenantProfileId}</strong></div>
          </div>
        </section>

        <section className="surface owner-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Charges</p>
              <h2>Adjust invoice charges</h2>
            </div>
            <FileText size={20} />
          </div>
          <form className="form-grid two-column" onSubmit={(event) => { event.preventDefault(); updateCharges.mutate(); }}>
            <label>Maintenance<input type="number" min="0" step="0.01" value={maintenanceCharge} onChange={(event) => setMaintenanceCharge(event.target.value)} disabled={!canEditCharges} /></label>
            <label>Electricity<input type="number" min="0" step="0.01" value={electricityCharge} onChange={(event) => setElectricityCharge(event.target.value)} disabled={!canEditCharges} /></label>
            <label>Other Charge<input type="number" min="0" step="0.01" value={otherCharge} onChange={(event) => setOtherCharge(event.target.value)} disabled={!canEditCharges} /></label>
            <label>Late Fee<input type="number" min="0" step="0.01" value={lateFee} onChange={(event) => setLateFee(event.target.value)} disabled={!canEditCharges} /></label>
            <label className="form-span">Notes<textarea rows={3} value={notes} onChange={(event) => setNotes(event.target.value)} disabled={!canEditCharges} /></label>
            <button className="primary-button" type="submit" disabled={!canEditCharges || updateCharges.isPending}>Update Charges</button>
          </form>
          {!canEditCharges ? <p className="muted-copy">Paid or cancelled invoices keep their financial history locked.</p> : null}
        </section>

        <section className="surface owner-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Payment</p>
              <h2>Record manual payment</h2>
            </div>
            <Banknote size={20} />
          </div>
          <form className="inline-form three-column" onSubmit={(event) => { event.preventDefault(); recordPayment.mutate(); }}>
            <label>Amount<input type="number" min="1" max={invoice.balanceAmount} step="0.01" value={paymentAmount} onChange={(event) => setPaymentAmount(event.target.value)} disabled={!canRecordPayment} required /></label>
            <label>Method<select value={paymentMethod} onChange={(event) => setPaymentMethod(event.target.value as PaymentMethod)} disabled={!canRecordPayment}>{paymentMethods.map((method) => <option value={method} key={method}>{method.replaceAll('_', ' ')}</option>)}</select></label>
            <label>Payment Date<input type="date" value={paymentDate} onChange={(event) => setPaymentDate(event.target.value)} disabled={!canRecordPayment} required /></label>
            <label>Reference<input value={paymentReference} onChange={(event) => setPaymentReference(event.target.value)} disabled={!canRecordPayment} /></label>
            <label>Remarks<input value={paymentRemarks} onChange={(event) => setPaymentRemarks(event.target.value)} disabled={!canRecordPayment} /></label>
            <button className="primary-button" type="submit" disabled={!canRecordPayment || !paymentAmount || paymentTooHigh || recordPayment.isPending}>Record Payment</button>
          </form>
          {paymentTooHigh ? <FormMessage message="Payment cannot exceed the outstanding balance." /> : null}
          {!canRecordPayment ? <p className="muted-copy">No payment can be recorded for this invoice status.</p> : null}
        </section>

        {invoice.securityDeposit ? (
          <section className="surface owner-panel">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Security deposit</p>
                <h2>Deposit visibility</h2>
              </div>
              <StatusBadge status={invoice.securityDeposit.status} />
            </div>
            <div className="detail-grid">
              <div><span>Required</span><strong>{formatMoney(invoice.securityDeposit.requiredAmount)}</strong></div>
              <div><span>Paid</span><strong>{formatMoney(invoice.securityDeposit.paidAmount)}</strong></div>
              <div><span>Remaining</span><strong>{formatMoney(invoice.securityDeposit.remainingAmount)}</strong></div>
              <div><span>Reference</span><strong>{invoice.securityDeposit.lastPaymentReference ?? '-'}</strong></div>
            </div>
          </section>
        ) : null}

        <section className="surface owner-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Payment history</p>
              <h2>{invoice.payments.length} payment{invoice.payments.length === 1 ? '' : 's'}</h2>
            </div>
            <ShieldCheck size={20} />
          </div>
          {invoice.payments.length === 0 ? (
            <p className="muted-copy">No rent payments recorded yet.</p>
          ) : (
            <div className="rent-payment-list">
              {invoice.payments.map((payment) => (
                <article className="rent-payment-row" key={payment.id}>
                  <div>
                    <p className="eyebrow">{payment.paymentNumber}</p>
                    <h3>{formatMoney(payment.amount)}</h3>
                    <p>{payment.paymentMethod.replaceAll('_', ' ')} · {payment.paymentReference ?? 'No reference'} · {formatDate(payment.paymentDate)}</p>
                  </div>
                  <button className="secondary-button compact-button" type="button" onClick={() => downloadReceipt.mutate(payment.id)} disabled={downloadReceipt.isPending}>
                    Download receipt
                  </button>
                </article>
              ))}
            </div>
          )}
        </section>
      </div>
    </OwnerShell>
  );
}

function toAmount(value: string) {
  return value === '' ? undefined : Number(value);
}

function monthName(month: number) {
  return new Date(2026, month - 1, 1).toLocaleString('en-IN', { month: 'long' });
}

function formatMoney(value: number) {
  return `Rs ${Number(value ?? 0).toLocaleString('en-IN')}`;
}

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('en-IN');
}

function formatDateTime(value: string) {
  return new Date(value).toLocaleString('en-IN');
}

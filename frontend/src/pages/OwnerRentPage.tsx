import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { AlertCircle, Banknote, CalendarDays, Receipt, Wallet } from 'lucide-react';
import type { ReactNode } from 'react';
import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ownerRentApi } from '../api/rent.api';
import { propertyApi } from '../api/property.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { StatusBadge } from '../components/StatusBadge';
import { OwnerShell } from '../features/owner/OwnerShell';
import type { RentInvoiceStatus } from '../types/rent';
import { getApiErrorMessage } from '../utils/apiError';

type RentFilter = 'ALL' | RentInvoiceStatus;

const statusFilters: RentFilter[] = ['ALL', 'PENDING', 'PARTIALLY_PAID', 'OVERDUE', 'PAID', 'CANCELLED'];
const monthOptions = Array.from({ length: 12 }, (_, index) => ({ value: index + 1, label: monthName(index + 1) }));

export default function OwnerRentPage() {
  const now = new Date();
  const queryClient = useQueryClient();
  const [propertyFilter, setPropertyFilter] = useState('');
  const [statusFilter, setStatusFilter] = useState<RentFilter>('ALL');
  const [search, setSearch] = useState('');
  const [generatePropertyId, setGeneratePropertyId] = useState('');
  const [generateMonth, setGenerateMonth] = useState(now.getMonth() + 1);
  const [generateYear, setGenerateYear] = useState(now.getFullYear());
  const selectedPropertyId = propertyFilter ? Number(propertyFilter) : undefined;

  const rentQuery = useQuery({
    queryKey: ['owner-rent', selectedPropertyId],
    queryFn: () => ownerRentApi.list(selectedPropertyId)
  });
  const propertiesQuery = useQuery({ queryKey: ['owner-properties'], queryFn: propertyApi.listProperties });
  const generateRent = useMutation({
    mutationFn: () => ownerRentApi.generate({
      propertyId: Number(generatePropertyId),
      billingMonth: generateMonth,
      billingYear: generateYear
    }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['owner-rent'] });
    }
  });

  const invoices = rentQuery.data?.invoices ?? [];
  const visibleInvoices = useMemo(() => {
    const trimmedSearch = search.trim().toLowerCase();
    return invoices.filter((invoice) => {
      const statusMatches = statusFilter === 'ALL' || invoice.status === statusFilter;
      const searchMatches = !trimmedSearch
        || invoice.invoiceNumber.toLowerCase().includes(trimmedSearch)
        || invoice.tenantName.toLowerCase().includes(trimmedSearch)
        || invoice.propertyName.toLowerCase().includes(trimmedSearch);
      return statusMatches && searchMatches;
    });
  }, [invoices, search, statusFilter]);

  return (
    <OwnerShell
      title="Rent"
      eyebrow="Monthly rent"
      actions={<Link className="secondary-link compact-button" to="/owner/tenants">Tenants</Link>}
    >
      <div className="owner-stack">
        <section className="owner-hero-card owner-hero-card--compact">
          <div>
            <p className="eyebrow">Rent operations</p>
            <h2>Generate monthly invoices and record manual payments</h2>
            <p>Invoices are created only for active tenants. Payment history and receipts come from stored rent payment records.</p>
          </div>
          <Wallet size={28} />
        </section>

        <section className="owner-kpi-grid owner-kpi-grid--tight">
          <RentMetric icon={<Receipt size={19} />} label="Billed" value={formatMoney(rentQuery.data?.summary.totalRent ?? 0)} />
          <RentMetric icon={<Banknote size={19} />} label="Collected" value={formatMoney(rentQuery.data?.summary.collected ?? 0)} />
          <RentMetric icon={<Wallet size={19} />} label="Outstanding" value={formatMoney(rentQuery.data?.summary.outstanding ?? 0)} />
          <RentMetric icon={<AlertCircle size={19} />} label="Overdue" value={formatMoney(rentQuery.data?.summary.overdueAmount ?? 0)} />
          <RentMetric icon={<CalendarDays size={19} />} label="Pending" value={String(rentQuery.data?.summary.pendingInvoices ?? 0)} />
          <RentMetric icon={<AlertCircle size={19} />} label="Overdue Invoices" value={String(rentQuery.data?.summary.overdueInvoices ?? 0)} />
        </section>

        <FormMessage message={rentQuery.isError ? getApiErrorMessage(rentQuery.error, 'Unable to load rent invoices') : null} />
        <FormMessage message={propertiesQuery.isError ? getApiErrorMessage(propertiesQuery.error, 'Unable to load properties') : null} />
        <FormMessage message={generateRent.isError ? getApiErrorMessage(generateRent.error, 'Unable to generate rent') : null} />
        <FormMessage
          tone="success"
          message={generateRent.data ? `Generated ${generateRent.data.generatedCount}, skipped ${generateRent.data.alreadyGeneratedCount} duplicate and ${generateRent.data.skippedCount} inactive/invalid invoice(s).` : null}
        />

        <section className="surface owner-panel">
          <div className="section-heading">
            <div>
              <p className="eyebrow">Generate</p>
              <h2>Create monthly rent invoices</h2>
            </div>
          </div>
          <form className="inline-form three-column" onSubmit={(event) => { event.preventDefault(); generateRent.mutate(); }}>
            <label>
              Property
              <select value={generatePropertyId} onChange={(event) => setGeneratePropertyId(event.target.value)} required>
                <option value="">Select property</option>
                {(propertiesQuery.data ?? []).map((property) => (
                  <option value={property.id} key={property.id}>{property.name}</option>
                ))}
              </select>
            </label>
            <label>
              Month
              <select value={generateMonth} onChange={(event) => setGenerateMonth(Number(event.target.value))}>
                {monthOptions.map((month) => <option value={month.value} key={month.value}>{month.label}</option>)}
              </select>
            </label>
            <label>
              Year
              <input type="number" min={2000} max={2100} value={generateYear} onChange={(event) => setGenerateYear(Number(event.target.value))} required />
            </label>
            <button className="primary-button" type="submit" disabled={!generatePropertyId || generateRent.isPending}>
              {generateRent.isPending ? 'Generating' : 'Generate Rent'}
            </button>
          </form>
        </section>

        <section className="surface owner-panel">
          <div className="owner-toolbar">
            <div>
              <p className="eyebrow">Invoices</p>
              <h2>{visibleInvoices.length} invoice{visibleInvoices.length === 1 ? '' : 's'}</h2>
            </div>
            <div className="action-row">
              <select aria-label="Filter property" value={propertyFilter} onChange={(event) => setPropertyFilter(event.target.value)}>
                <option value="">All properties</option>
                {(propertiesQuery.data ?? []).map((property) => (
                  <option value={property.id} key={property.id}>{property.name}</option>
                ))}
              </select>
              <input
                aria-label="Search invoices"
                placeholder="Search tenant, PG or invoice"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />
            </div>
          </div>

          <div className="segmented segmented--wrap">
            {statusFilters.map((status) => (
              <button className={statusFilter === status ? 'active' : ''} key={status} type="button" onClick={() => setStatusFilter(status)}>
                {status === 'ALL' ? 'All' : status.replaceAll('_', ' ')}
              </button>
            ))}
          </div>

          {rentQuery.isLoading ? (
            <div className="owner-card-grid">
              {Array.from({ length: 4 }).map((_, index) => <div className="owner-skeleton-card" key={index} />)}
            </div>
          ) : null}

          {!rentQuery.isLoading && visibleInvoices.length === 0 ? (
            <EmptyState
              title={invoices.length === 0 ? 'No rent invoices yet.' : 'No invoices match these filters.'}
              description={invoices.length === 0 ? 'Generate monthly rent for an active tenant property to start tracking payments.' : 'Try another status, property or search term.'}
            />
          ) : null}

          {visibleInvoices.length > 0 ? (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Invoice</th>
                    <th>Tenant</th>
                    <th>PG</th>
                    <th>Period</th>
                    <th>Total</th>
                    <th>Paid</th>
                    <th>Balance</th>
                    <th>Due</th>
                    <th>Status</th>
                    <th className="table-actions">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {visibleInvoices.map((invoice) => (
                    <tr key={invoice.id}>
                      <td>{invoice.invoiceNumber}</td>
                      <td>{invoice.tenantName}<br /><span className="muted-copy">Room {invoice.roomNumber} / {invoice.bedLabel}</span></td>
                      <td>{invoice.propertyName}</td>
                      <td>{monthName(invoice.billingMonth)} {invoice.billingYear}</td>
                      <td>{formatMoney(invoice.totalAmount)}</td>
                      <td>{formatMoney(invoice.paidAmount)}</td>
                      <td>{formatMoney(invoice.balanceAmount)}</td>
                      <td>{formatDate(invoice.dueDate)}</td>
                      <td><StatusBadge status={invoice.status} /></td>
                      <td className="table-actions"><Link to={`/owner/rent/${invoice.id}`}>View</Link></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
        </section>
      </div>
    </OwnerShell>
  );
}

function RentMetric({ icon, label, value }: { icon: ReactNode; label: string; value: string }) {
  return (
    <article className="owner-kpi-card">
      <span>{icon}</span>
      <div>
        <strong>{value}</strong>
        <p>{label}</p>
      </div>
    </article>
  );
}

function monthName(month: number) {
  return new Date(2026, month - 1, 1).toLocaleString('en-IN', { month: 'short' });
}

function formatMoney(value: number) {
  return `Rs ${Number(value ?? 0).toLocaleString('en-IN')}`;
}

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('en-IN');
}

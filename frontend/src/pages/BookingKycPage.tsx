import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { bookingApi } from '../api/booking.api';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import type { DocumentType } from '../types/booking';
import { getApiErrorMessage } from '../utils/apiError';
import { toAssetUrl } from '../utils/assets';

const documentTypes: DocumentType[] = ['AADHAAR', 'PAN', 'PASSPORT', 'DRIVING_LICENSE', 'PHOTO', 'OTHER'];

export default function BookingKycPage() {
  const { id } = useParams();
  const bookingId = Number(id);
  const queryClient = useQueryClient();
  const [documentType, setDocumentType] = useState<DocumentType>('AADHAAR');
  const [documentNumber, setDocumentNumber] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const query = useQuery({ queryKey: ['booking', bookingId], queryFn: () => bookingApi.getMine(bookingId), enabled: Number.isFinite(bookingId) });
  const upload = useMutation({
    mutationFn: () => {
      if (!file) throw new Error('Select a file');
      return bookingApi.uploadDocument(bookingId, { documentType, documentNumber: documentNumber || undefined, file });
    },
    onSuccess: async () => {
      setFile(null);
      setDocumentNumber('');
      await queryClient.invalidateQueries({ queryKey: ['booking', bookingId] });
    }
  });
  const remove = useMutation({
    mutationFn: (documentId: number) => bookingApi.deleteDocument(bookingId, documentId),
    onSuccess: async () => queryClient.invalidateQueries({ queryKey: ['booking', bookingId] })
  });

  if (query.isLoading) return <div className="route-state">Loading KYC</div>;
  if (query.isError || !query.data) return <div className="route-state">{getApiErrorMessage(query.error, 'Unable to load KYC')}</div>;
  const booking = query.data;

  return (
    <div className="stack">
      <PageHeader eyebrow="Tenant KYC" title={booking.bookingNumber} actions={<Link className="secondary-link" to={`/bookings/${booking.id}`}>Booking</Link>} />
      <section className="surface">
        <form className="document-upload" onSubmit={(event) => { event.preventDefault(); upload.mutate(); }}>
          <label>
            Document Type
            <select value={documentType} onChange={(event) => setDocumentType(event.target.value as DocumentType)}>
              {documentTypes.map((item) => <option key={item} value={item}>{item.replaceAll('_', ' ')}</option>)}
            </select>
          </label>
          <label>
            Document Number
            <input value={documentNumber} onChange={(event) => setDocumentNumber(event.target.value)} />
          </label>
          <label>
            File
            <input type="file" accept="image/png,image/jpeg,application/pdf" onChange={(event) => setFile(event.target.files?.[0] ?? null)} />
          </label>
          <button className="primary-button" type="submit" disabled={upload.isPending}>Upload</button>
        </form>
        <FormMessage message={upload.isError ? getApiErrorMessage(upload.error, 'Unable to upload document') : null} />
        <FormMessage message={remove.isError ? getApiErrorMessage(remove.error, 'Unable to delete document') : null} />
        {booking.documents.length === 0 ? <div className="empty-state">Upload one government ID and one photo.</div> : (
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
                      {doc.verificationStatus !== 'VERIFIED' ? <button className="danger-button compact-button" type="button" onClick={() => remove.mutate(doc.id)}>Delete</button> : null}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

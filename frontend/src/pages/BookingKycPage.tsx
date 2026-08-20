import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { bookingApi } from '../api/booking.api';
import type { TenantDocument, DocumentType, DocumentVerificationStatus } from '../types/booking';

const DOCUMENT_TYPES: DocumentType[] = ['AADHAAR', 'PAN', 'PASSPORT', 'DRIVING_LICENSE', 'COLLEGE_ID', 'EMPLOYEE_ID', 'PHOTO', 'OTHER'];

const DOCUMENT_LABELS: Record<DocumentType, string> = {
  AADHAAR: 'Aadhaar Card',
  PAN: 'PAN Card',
  PASSPORT: 'Passport',
  DRIVING_LICENSE: 'Driving License',
  COLLEGE_ID: 'College ID',
  EMPLOYEE_ID: 'Employee ID',
  PHOTO: 'Photo',
  OTHER: 'Other'
};

const STATUS_COLORS: Record<DocumentVerificationStatus, string> = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  VERIFIED: 'bg-green-100 text-green-800',
  REJECTED: 'bg-red-100 text-red-800'
};

const STATUS_LABELS: Record<DocumentVerificationStatus, string> = {
  PENDING: 'Pending',
  VERIFIED: 'Verified',
  REJECTED: 'Rejected'
};

export default function BookingKycPage() {
  const { id } = useParams<{ id: string }>();
  const [documents, setDocuments] = useState<TenantDocument[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [uploading, setUploading] = useState(false);
  const [documentType, setDocumentType] = useState<DocumentType>('AADHAAR');
  const [documentNumber, setDocumentNumber] = useState('');
  const [documentUrl, setDocumentUrl] = useState('');

  useEffect(() => {
    if (id) {
      loadDocuments(Number(id));
    }
  }, [id]);

  const loadDocuments = async (bookingId: number) => {
    try {
      setLoading(true);
      setError(null);
      const data = await bookingApi.get(bookingId);
      // In a real app, you'd have a separate endpoint for documents
      // For now, we'll just show the booking details
    } catch (err) {
      setError('Failed to load documents');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleUpload = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!id || !documentUrl.trim()) return;

    try {
      setUploading(true);
      // Note: The backend doesn't have a direct document upload endpoint in the current implementation
      // This would need to be implemented
      alert('Document upload would be implemented here');
      setDocumentUrl('');
      setDocumentNumber('');
    } catch (err) {
      alert('Failed to upload document');
      console.error(err);
    } finally {
      setUploading(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-lg">Loading documents...</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="mb-6">
        <Link to={`/bookings/${id}`} className="text-blue-600 hover:text-blue-900">
          ← Back to Booking
        </Link>
      </div>

      <div className="bg-white shadow-md rounded-lg p-6 mb-6">
        <h1 className="text-3xl font-bold mb-6">KYC Documents</h1>

        <div className="bg-blue-50 border-l-4 border-blue-400 p-4 mb-6">
          <p className="text-blue-700">Upload your KYC documents for verification. Please ensure all documents are clear and valid.</p>
        </div>

        <form onSubmit={handleUpload} className="mb-8 space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Document Type</label>
            <select
              value={documentType}
              onChange={(e) => setDocumentType(e.target.value as DocumentType)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {DOCUMENT_TYPES.map((type) => (
                <option key={type} value={type}>{DOCUMENT_LABELS[type]}</option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Document Number (Optional)</label>
            <input
              type="text"
              value={documentNumber}
              onChange={(e) => setDocumentNumber(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter document number"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">Document URL</label>
            <input
              type="text"
              value={documentUrl}
              onChange={(e) => setDocumentUrl(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="Enter document URL"
              required
            />
          </div>

          <button
            type="submit"
            disabled={uploading || !documentUrl.trim()}
            className="w-full px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
          >
            {uploading ? 'Uploading...' : 'Upload Document'}
          </button>
        </form>

        <div>
          <h2 className="text-xl font-semibold mb-4">Uploaded Documents</h2>
          {documents.length === 0 ? (
            <p className="text-gray-500">No documents uploaded yet</p>
          ) : (
            <div className="space-y-3">
              {documents.map((doc) => (
                <div key={doc.id} className="border rounded-lg p-4 flex justify-between items-center">
                  <div>
                    <p className="font-medium">{DOCUMENT_LABELS[doc.documentType]}</p>
                    <p className="text-sm text-gray-600">{doc.documentUrl}</p>
                    <span className={`inline-block mt-2 px-2 py-1 text-xs font-semibold rounded-full ${STATUS_COLORS[doc.verificationStatus]}`}>
                      {STATUS_LABELS[doc.verificationStatus]}
                    </span>
                    {doc.rejectionReason && (
                      <p className="text-sm text-red-600 mt-1">Reason: {doc.rejectionReason}</p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
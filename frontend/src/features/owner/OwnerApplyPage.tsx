import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { FileUp, Save, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { ownerApi } from '../../api/owner.api';
import { FormMessage } from '../../components/FormMessage';
import { PageHeader } from '../../components/PageHeader';
import { StatusBadge } from '../../components/StatusBadge';
import type { DocumentType, OwnerApplicationInput } from '../../types/owner';
import { getApiErrorMessage } from '../../utils/apiError';
import { toAssetUrl } from '../../utils/assets';

const ownerSchema = z.object({
  businessName: z.string().min(2, 'Business name is required').max(180),
  alternatePhone: z.string().max(30).optional(),
  businessEmail: z.string().email().optional().or(z.literal('')),
  experienceYears: z.number().min(0).max(60).optional(),
  description: z.string().max(3000).optional()
});

type OwnerForm = z.infer<typeof ownerSchema>;

const documentTypes: DocumentType[] = ['AADHAAR', 'PAN', 'BUSINESS_REGISTRATION', 'ADDRESS_PROOF', 'OTHER'];

export function OwnerApplyPage() {
  const queryClient = useQueryClient();
  const [formError, setFormError] = useState<string | null>(null);
  const [formMessage, setFormMessage] = useState<string | null>(null);
  const [documentType, setDocumentType] = useState<DocumentType>('AADHAAR');
  const [documentNumber, setDocumentNumber] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [documentError, setDocumentError] = useState<string | null>(null);

  const ownerQuery = useQuery({
    queryKey: ['owner-profile'],
    queryFn: ownerApi.me,
    retry: false
  });

  const documentsQuery = useQuery({
    queryKey: ['owner-documents'],
    queryFn: ownerApi.documents,
    enabled: Boolean(ownerQuery.data)
  });

  const form = useForm<OwnerForm>({
    resolver: zodResolver(ownerSchema),
    defaultValues: {
      businessName: '',
      alternatePhone: '',
      businessEmail: '',
      description: ''
    }
  });

  useEffect(() => {
    const owner = ownerQuery.data;
    if (owner) {
      form.reset({
        businessName: owner.businessName,
        alternatePhone: owner.alternatePhone ?? '',
        businessEmail: owner.businessEmail ?? '',
        experienceYears: owner.experienceYears ?? undefined,
        description: owner.description ?? ''
      });
    }
  }, [form, ownerQuery.data]);

  const saveApplication = useMutation({
    mutationFn: async (values: OwnerForm) => {
      const input: OwnerApplicationInput = {
        businessName: values.businessName,
        alternatePhone: values.alternatePhone || undefined,
        businessEmail: values.businessEmail || undefined,
        experienceYears: values.experienceYears,
        description: values.description || undefined
      };
      return ownerQuery.data ? ownerApi.updateMe(input) : ownerApi.apply(input);
    },
    onSuccess: async () => {
      setFormError(null);
      setFormMessage(ownerQuery.data ? 'Application updated.' : 'Application submitted.');
      await queryClient.invalidateQueries({ queryKey: ['owner-profile'] });
    },
    onError: (err) => {
      setFormMessage(null);
      setFormError(getApiErrorMessage(err, 'Unable to save application'));
    }
  });

  const uploadDocument = useMutation({
    mutationFn: () => {
      if (!file) {
        throw new Error('Choose a document file');
      }
      return ownerApi.uploadDocument({ documentType, documentNumber: documentNumber || undefined, file });
    },
    onSuccess: async () => {
      setFile(null);
      setDocumentNumber('');
      setDocumentError(null);
      await queryClient.invalidateQueries({ queryKey: ['owner-documents'] });
    },
    onError: (err) => setDocumentError(getApiErrorMessage(err, 'Unable to upload document'))
  });

  const deleteDocument = useMutation({
    mutationFn: ownerApi.deleteDocument,
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['owner-documents'] });
    }
  });

  const owner = ownerQuery.data;
  const documents = documentsQuery.data ?? [];

  return (
    <div className="stack">
      <PageHeader eyebrow="Owner onboarding" title="PG Owner Application" />

      {owner ? (
        <section className="surface status-surface">
          <div>
            <h2>{owner.businessName}</h2>
            {owner.verificationRemarks ? <p>{owner.verificationRemarks}</p> : null}
          </div>
          <StatusBadge status={owner.verificationStatus} />
        </section>
      ) : null}

      <section className="surface">
        <h2>{owner ? 'Application Details' : 'Apply as PG Owner'}</h2>
        <form className="form-grid two-column" onSubmit={form.handleSubmit((values) => saveApplication.mutate(values))}>
          <label>
            Business name
            <input {...form.register('businessName')} />
            <FormMessage message={form.formState.errors.businessName?.message} />
          </label>
          <label>
            Business email
            <input type="email" {...form.register('businessEmail')} />
            <FormMessage message={form.formState.errors.businessEmail?.message} />
          </label>
          <label>
            Alternate phone
            <input {...form.register('alternatePhone')} />
            <FormMessage message={form.formState.errors.alternatePhone?.message} />
          </label>
          <label>
            Experience years
            <input
              type="number"
              min="0"
              max="60"
              {...form.register('experienceYears', {
                setValueAs: (value) => (value === '' ? undefined : Number(value))
              })}
            />
            <FormMessage message={form.formState.errors.experienceYears?.message} />
          </label>
          <label className="form-span">
            Description
            <textarea rows={5} {...form.register('description')} />
            <FormMessage message={form.formState.errors.description?.message} />
          </label>
          <div className="form-span">
            <FormMessage message={formError} />
            <FormMessage message={formMessage} tone="success" />
            <button className="primary-button" type="submit" disabled={saveApplication.isPending}>
              <Save size={18} />
              {saveApplication.isPending ? 'Saving' : owner ? 'Update application' : 'Submit application'}
            </button>
          </div>
        </form>
      </section>

      {owner ? (
        <section className="surface">
          <h2>Owner Documents</h2>
          <div className="document-upload">
            <label>
              Document type
              <select value={documentType} onChange={(event) => setDocumentType(event.target.value as DocumentType)}>
                {documentTypes.map((type) => (
                  <option value={type} key={type}>{type.replaceAll('_', ' ')}</option>
                ))}
              </select>
            </label>
            <label>
              Document number
              <input value={documentNumber} onChange={(event) => setDocumentNumber(event.target.value)} />
            </label>
            <label>
              File
              <input
                type="file"
                accept="image/png,image/jpeg,application/pdf"
                onChange={(event) => setFile(event.target.files?.[0] ?? null)}
              />
            </label>
            <button className="secondary-button" type="button" onClick={() => uploadDocument.mutate()} disabled={uploadDocument.isPending}>
              <FileUp size={18} />
              {uploadDocument.isPending ? 'Uploading' : 'Upload'}
            </button>
          </div>
          <FormMessage message={documentError} />

          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Type</th>
                  <th>File</th>
                  <th>Status</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {documents.map((document) => (
                  <tr key={document.id}>
                    <td>{document.documentType.replaceAll('_', ' ')}</td>
                    <td>
                      <a href={toAssetUrl(document.documentUrl)} target="_blank" rel="noreferrer">
                        {document.originalFileName ?? 'Document'}
                      </a>
                    </td>
                    <td><StatusBadge status={document.verificationStatus} /></td>
                    <td className="table-actions">
                      <button
                        className="icon-button"
                        type="button"
                        title="Delete document"
                        aria-label="Delete document"
                        onClick={() => deleteDocument.mutate(document.id)}
                      >
                        <Trash2 size={16} />
                      </button>
                    </td>
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
      ) : null}
    </div>
  );
}

import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ownerNoticesApi } from '../api/operations.api';
import { propertyApi } from '../api/property.api';
import { FormMessage } from '../components/FormMessage';
import { OwnerShell } from '../features/owner/OwnerShell';
import type { NoticeType, OperationalPriority } from '../types/operations';
import { getApiErrorMessage } from '../utils/apiError';
import { label } from '../utils/operations';

const noticeTypes: NoticeType[] = ['GENERAL', 'MAINTENANCE', 'PAYMENT', 'EVENT', 'EMERGENCY', 'FOOD', 'RULE', 'OTHER'];
const priorities: OperationalPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];

export default function OwnerNoticeFormPage() {
  const { id } = useParams();
  const noticeId = id ? Number(id) : null;
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [propertyId, setPropertyId] = useState('');
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [noticeType, setNoticeType] = useState<NoticeType>('GENERAL');
  const [priority, setPriority] = useState<OperationalPriority>('MEDIUM');
  const [expiresAt, setExpiresAt] = useState('');
  const propertiesQuery = useQuery({ queryKey: ['owner-properties'], queryFn: propertyApi.listProperties });
  const noticeQuery = useQuery({ queryKey: ['owner-notice', noticeId], queryFn: () => ownerNoticesApi.get(noticeId!), enabled: noticeId !== null });
  useEffect(() => {
    if (!noticeQuery.data) return;
    setPropertyId(String(noticeQuery.data.propertyId));
    setTitle(noticeQuery.data.title);
    setContent(noticeQuery.data.content);
    setNoticeType(noticeQuery.data.noticeType);
    setPriority(noticeQuery.data.priority);
    setExpiresAt(noticeQuery.data.expiresAt ?? '');
  }, [noticeQuery.data]);
  const save = useMutation({
    mutationFn: () => {
      const input = { propertyId: Number(propertyId), title, content, noticeType, priority, expiresAt: expiresAt || undefined };
      return noticeId === null ? ownerNoticesApi.create(input) : ownerNoticesApi.update(noticeId, input);
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['owner-notices'] });
      navigate('/owner/notices');
    }
  });
  const error = propertiesQuery.error ?? noticeQuery.error ?? save.error;
  return (
    <OwnerShell title={noticeId ? 'Edit Notice' : 'New Notice'} eyebrow="Operations" actions={<Link className="secondary-link compact-button" to="/owner/notices">Back</Link>}>
      <div className="owner-stack">
        <section className="surface owner-panel">
          <FormMessage message={error ? getApiErrorMessage(error, 'Unable to save notice') : null} />
          <form className="form-grid two-column" onSubmit={(event) => { event.preventDefault(); save.mutate(); }}>
            <label>Property<select value={propertyId} onChange={(event) => setPropertyId(event.target.value)} required><option value="">Select property</option>{(propertiesQuery.data ?? []).map((property) => <option value={property.id} key={property.id}>{property.name}</option>)}</select></label>
            <label>Type<select value={noticeType} onChange={(event) => setNoticeType(event.target.value as NoticeType)}>{noticeTypes.map((item) => <option value={item} key={item}>{label(item)}</option>)}</select></label>
            <label>Priority<select value={priority} onChange={(event) => setPriority(event.target.value as OperationalPriority)}>{priorities.map((item) => <option value={item} key={item}>{label(item)}</option>)}</select></label>
            <label>Expires At<input type="date" value={expiresAt} onChange={(event) => setExpiresAt(event.target.value)} /></label>
            <label className="form-span">Title<input value={title} onChange={(event) => setTitle(event.target.value)} required /></label>
            <label className="form-span">Content<textarea rows={8} value={content} onChange={(event) => setContent(event.target.value)} required /></label>
            <button className="primary-button" type="submit" disabled={!propertyId || !title.trim() || !content.trim() || save.isPending}>Save Notice</button>
          </form>
        </section>
      </div>
    </OwnerShell>
  );
}

import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { complaintsApi } from '../api/operations.api';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import type { ComplaintCategory, OperationalPriority } from '../types/operations';
import { getApiErrorMessage } from '../utils/apiError';
import { label } from '../utils/operations';

const categories: ComplaintCategory[] = ['ELECTRICAL', 'PLUMBING', 'CLEANING', 'INTERNET', 'FURNITURE', 'APPLIANCE', 'SECURITY', 'FOOD', 'ROOM', 'OTHER'];
const priorities: OperationalPriority[] = ['LOW', 'MEDIUM', 'HIGH', 'URGENT'];

export default function CreateComplaintPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [category, setCategory] = useState<ComplaintCategory>('ROOM');
  const [priority, setPriority] = useState<OperationalPriority>('MEDIUM');
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const mutation = useMutation({
    mutationFn: () => complaintsApi.create({ category, priority, title, description }),
    onSuccess: async (complaint) => {
      await queryClient.invalidateQueries({ queryKey: ['complaints'] });
      navigate(`/complaints/${complaint.id}`);
    }
  });

  return (
    <div className="stack">
      <PageHeader eyebrow="My stay" title="Raise a Complaint" actions={<Link className="secondary-link" to="/complaints">Cancel</Link>} />
      <section className="surface">
        <FormMessage message={mutation.isError ? getApiErrorMessage(mutation.error, 'Unable to submit complaint') : null} />
        <form className="form-grid two-column" onSubmit={(event) => { event.preventDefault(); mutation.mutate(); }}>
          <label>Category<select value={category} onChange={(event) => setCategory(event.target.value as ComplaintCategory)}>{categories.map((item) => <option value={item} key={item}>{label(item)}</option>)}</select></label>
          <label>Priority<select value={priority} onChange={(event) => setPriority(event.target.value as OperationalPriority)}>{priorities.map((item) => <option value={item} key={item}>{label(item)}</option>)}</select></label>
          <label className="form-span">Title<input value={title} onChange={(event) => setTitle(event.target.value)} maxLength={180} required /></label>
          <label className="form-span">Description<textarea rows={6} value={description} onChange={(event) => setDescription(event.target.value)} required /></label>
          <button className="primary-button" type="submit" disabled={mutation.isPending || !title.trim() || !description.trim()}>Submit Complaint</button>
        </form>
      </section>
    </div>
  );
}

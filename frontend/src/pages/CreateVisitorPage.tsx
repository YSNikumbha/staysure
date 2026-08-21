import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { visitorsApi } from '../api/operations.api';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import { getApiErrorMessage } from '../utils/apiError';
import { todayIso } from '../utils/operations';

export default function CreateVisitorPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [visitorName, setVisitorName] = useState('');
  const [visitorPhone, setVisitorPhone] = useState('');
  const [relationship, setRelationship] = useState('');
  const [visitDate, setVisitDate] = useState(todayIso());
  const [expectedArrivalTime, setExpectedArrivalTime] = useState('10:00');
  const [expectedDepartureTime, setExpectedDepartureTime] = useState('18:00');
  const [purpose, setPurpose] = useState('');
  const mutation = useMutation({
    mutationFn: () => visitorsApi.create({ visitorName, visitorPhone, relationship, visitDate, expectedArrivalTime, expectedDepartureTime, purpose }),
    onSuccess: async (visitor) => {
      await queryClient.invalidateQueries({ queryKey: ['visitors'] });
      navigate(`/visitors/${visitor.id}`);
    }
  });
  return (
    <div className="stack">
      <PageHeader eyebrow="My PG" title="Request Visitor" actions={<Link className="secondary-link" to="/visitors">Cancel</Link>} />
      <section className="surface">
        <FormMessage message={mutation.isError ? getApiErrorMessage(mutation.error, 'Unable to request visitor') : null} />
        <form className="form-grid two-column" onSubmit={(event) => { event.preventDefault(); mutation.mutate(); }}>
          <label>Visitor Name<input value={visitorName} onChange={(event) => setVisitorName(event.target.value)} required /></label>
          <label>Visitor Phone<input value={visitorPhone} onChange={(event) => setVisitorPhone(event.target.value)} required /></label>
          <label>Relationship<input value={relationship} onChange={(event) => setRelationship(event.target.value)} required /></label>
          <label>Visit Date<input type="date" value={visitDate} min={todayIso()} onChange={(event) => setVisitDate(event.target.value)} required /></label>
          <label>Arrival<input type="time" value={expectedArrivalTime} onChange={(event) => setExpectedArrivalTime(event.target.value)} required /></label>
          <label>Departure<input type="time" value={expectedDepartureTime} onChange={(event) => setExpectedDepartureTime(event.target.value)} required /></label>
          <label className="form-span">Purpose<input value={purpose} onChange={(event) => setPurpose(event.target.value)} required /></label>
          <button className="primary-button" type="submit" disabled={mutation.isPending || !visitorName.trim() || !visitorPhone.trim() || !purpose.trim()}>Submit Request</button>
        </form>
      </section>
    </div>
  );
}

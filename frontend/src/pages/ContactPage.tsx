import { zodResolver } from '@hookform/resolvers/zod';
import { HelpCircle, Mail, MessageSquareText, Search } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router-dom';
import { z } from 'zod';
import { FormMessage } from '../components/FormMessage';
import { SectionHeader } from '../components/SectionHeader';

const contactSchema = z.object({
  name: z.string().min(2, 'Name is required').max(120),
  email: z.string().email(),
  subject: z.string().min(3, 'Subject is required').max(160),
  message: z.string().min(10, 'Message must be at least 10 characters').max(1000)
});

type ContactForm = z.infer<typeof contactSchema>;

export function ContactPage() {
  const [success, setSuccess] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm<ContactForm>({ resolver: zodResolver(contactSchema) });

  const onSubmit = async () => {
    setSuccess('Your enquiry is validated. Message delivery is not connected in this build.');
  };

  return (
    <div className="info-page">
      <section className="info-hero">
        <p className="eyebrow">Contact</p>
        <h1>Need help with PG discovery or owner listings?</h1>
        <p>
          Prepare an enquiry here. This frontend validates the message, and backend contact
          delivery can be connected later without changing the page flow.
        </p>
      </section>

      <section className="contact-layout">
        <form className="surface contact-form" onSubmit={handleSubmit(onSubmit)}>
          <SectionHeader eyebrow="Enquiry form" title="Contact StaySure" description="All fields are required." />
          <label>
            Name
            <input autoComplete="name" {...register('name')} />
            <FormMessage message={errors.name?.message} />
          </label>
          <label>
            Email
            <input type="email" autoComplete="email" {...register('email')} />
            <FormMessage message={errors.email?.message} />
          </label>
          <label>
            Subject
            <input {...register('subject')} />
            <FormMessage message={errors.subject?.message} />
          </label>
          <label>
            Message
            <textarea rows={6} {...register('message')} />
            <FormMessage message={errors.message?.message} />
          </label>
          <FormMessage message={success} tone="success" />
          <button className="primary-button" type="submit" disabled={isSubmitting}>
            <MessageSquareText size={18} />
            Prepare enquiry
          </button>
        </form>

        <aside className="contact-help">
          <article className="value-card">
            <span className="value-icon"><Search size={22} /></span>
            <h3>Finding a PG</h3>
            <p>Use filters on Find PG for location, budget, gender, sharing type, food and availability.</p>
            <Link to="/find-pg">Open Find PG</Link>
          </article>
          <article className="value-card">
            <span className="value-icon"><Mail size={22} /></span>
            <h3>Owner onboarding</h3>
            <p>Owners should create an account and complete the owner application before listing PGs.</p>
            <Link to="/for-owners">Learn for owners</Link>
          </article>
          <article className="value-card">
            <span className="value-icon"><HelpCircle size={22} /></span>
            <h3>Account access</h3>
            <p>Use forgot password if you need a reset token for an existing account.</p>
            <Link to="/forgot-password">Recover account</Link>
          </article>
        </aside>
      </section>
    </div>
  );
}

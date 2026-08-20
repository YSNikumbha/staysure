import { zodResolver } from '@hookform/resolvers/zod';
import { Mail } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link } from 'react-router-dom';
import { z } from 'zod';
import { authApi } from '../../api/auth.api';
import { AuthLayout } from '../../components/AuthLayout';
import { FormMessage } from '../../components/FormMessage';
import { getApiErrorMessage } from '../../utils/apiError';

const forgotSchema = z.object({
  email: z.string().email()
});

type ForgotForm = z.infer<typeof forgotSchema>;

export function ForgotPasswordPage() {
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm<ForgotForm>({ resolver: zodResolver(forgotSchema) });

  const onSubmit = async (values: ForgotForm) => {
    setError(null);
    setSuccess(null);
    try {
      await authApi.forgotPassword(values);
      setSuccess('If the email exists, a reset link will be sent.');
    } catch (err) {
      setError(getApiErrorMessage(err, 'Unable to process request'));
    }
  };

  return (
    <AuthLayout
      eyebrow="Account recovery"
      title="Reset access to your account"
      subtitle="Enter your registered email. If it exists, the backend will process the reset request."
      footer={(
        <>
          <Link to="/login">Back to login</Link>
          <Link to="/reset-password">I have a token</Link>
        </>
      )}
    >
      <form onSubmit={handleSubmit(onSubmit)} className="form-grid">
        <label>
          Email
          <input type="email" autoComplete="email" placeholder="you@example.com" {...register('email')} />
          <FormMessage message={errors.email?.message} />
        </label>
        <FormMessage message={error} />
        <FormMessage message={success} tone="success" />
        <button className="primary-button primary-button--full" type="submit" disabled={isSubmitting}>
          <Mail size={18} />
          {isSubmitting ? 'Sending' : 'Send reset link'}
        </button>
      </form>
    </AuthLayout>
  );
}

import { zodResolver } from '@hookform/resolvers/zod';
import { KeyRound } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { authApi } from '../../api/auth.api';
import { FormMessage } from '../../components/FormMessage';
import { getApiErrorMessage } from '../../utils/apiError';

const resetSchema = z
  .object({
    email: z.string().email(),
    token: z.string().min(1, 'Reset token is required'),
    newPassword: z.string().min(8, 'Password must be at least 8 characters').max(72),
    confirmPassword: z.string().min(1, 'Confirm your password')
  })
  .refine((value) => value.newPassword === value.confirmPassword, {
    path: ['confirmPassword'],
    message: 'Passwords do not match'
  });

type ResetForm = z.infer<typeof resetSchema>;

export function ResetPasswordPage() {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm<ResetForm>({ resolver: zodResolver(resetSchema) });

  const onSubmit = async (values: ResetForm) => {
    setError(null);
    try {
      await authApi.resetPassword(values);
      navigate('/login', { replace: true });
    } catch (err) {
      setError(getApiErrorMessage(err, 'Password reset failed'));
    }
  };

  return (
    <section className="auth-panel">
      <div className="auth-box">
        <p className="eyebrow">Account recovery</p>
        <h1>Reset Password</h1>
        <form onSubmit={handleSubmit(onSubmit)} className="form-grid">
          <label>
            Email
            <input type="email" autoComplete="email" {...register('email')} />
            <FormMessage message={errors.email?.message} />
          </label>
          <label>
            Reset token
            <input {...register('token')} />
            <FormMessage message={errors.token?.message} />
          </label>
          <label>
            New password
            <input type="password" autoComplete="new-password" {...register('newPassword')} />
            <FormMessage message={errors.newPassword?.message} />
          </label>
          <label>
            Confirm password
            <input type="password" autoComplete="new-password" {...register('confirmPassword')} />
            <FormMessage message={errors.confirmPassword?.message} />
          </label>
          <FormMessage message={error} />
          <button className="primary-button" type="submit" disabled={isSubmitting}>
            <KeyRound size={18} />
            {isSubmitting ? 'Resetting' : 'Reset password'}
          </button>
        </form>
        <div className="auth-links">
          <Link to="/login">Back to login</Link>
        </div>
      </div>
    </section>
  );
}

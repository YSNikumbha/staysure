import { zodResolver } from '@hookform/resolvers/zod';
import { Eye, EyeOff, KeyRound } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { authApi } from '../../api/auth.api';
import { AuthLayout } from '../../components/AuthLayout';
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
  const [showPassword, setShowPassword] = useState(false);
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
    <AuthLayout
      eyebrow="Account recovery"
      title="Create a new password"
      subtitle="Use the reset token from the recovery flow and choose a secure new password."
      footer={<Link to="/login">Back to login</Link>}
    >
      <form onSubmit={handleSubmit(onSubmit)} className="form-grid">
        <label>
          Email
          <input type="email" autoComplete="email" placeholder="you@example.com" {...register('email')} />
          <FormMessage message={errors.email?.message} />
        </label>
        <label>
          Reset token
          <input {...register('token')} />
          <FormMessage message={errors.token?.message} />
        </label>
        <label>
          New password
          <span className="password-field">
            <input type={showPassword ? 'text' : 'password'} autoComplete="new-password" {...register('newPassword')} />
            <button
              type="button"
              aria-label={showPassword ? 'Hide password' : 'Show password'}
              onClick={() => setShowPassword((value) => !value)}
            >
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </span>
          <FormMessage message={errors.newPassword?.message} />
        </label>
        <label>
          Confirm password
          <span className="password-field">
            <input type={showPassword ? 'text' : 'password'} autoComplete="new-password" {...register('confirmPassword')} />
            <button
              type="button"
              aria-label={showPassword ? 'Hide password' : 'Show password'}
              onClick={() => setShowPassword((value) => !value)}
            >
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </span>
          <FormMessage message={errors.confirmPassword?.message} />
        </label>
        <FormMessage message={error} />
        <button className="primary-button primary-button--full" type="submit" disabled={isSubmitting}>
          <KeyRound size={18} />
          {isSubmitting ? 'Resetting' : 'Reset password'}
        </button>
      </form>
    </AuthLayout>
  );
}

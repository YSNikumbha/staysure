import { zodResolver } from '@hookform/resolvers/zod';
import { Eye, EyeOff, LogIn } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { AuthLayout } from '../../components/AuthLayout';
import { FormMessage } from '../../components/FormMessage';
import { useAuthStore } from '../../store/authStore';
import { getApiErrorMessage } from '../../utils/apiError';

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string().min(1, 'Password is required')
});

type LoginForm = z.infer<typeof loginSchema>;

export function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const login = useAuthStore((state) => state.login);
  const [error, setError] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm<LoginForm>({ resolver: zodResolver(loginSchema) });

  const onSubmit = async (values: LoginForm) => {
    setError(null);
    try {
      await login(values);
      const from = (location.state as { from?: { pathname?: string } } | null)?.from?.pathname ?? '/profile';
      navigate(from, { replace: true });
    } catch (err) {
      setError(getApiErrorMessage(err, 'Login failed'));
    }
  };

  return (
    <AuthLayout
      eyebrow="Welcome back"
      title="Login to StaySure"
      subtitle="Continue to your profile, wishlist, bookings and owner tools."
      footer={(
        <>
          <Link to="/forgot-password">Forgot password?</Link>
          <Link to="/register">Create account</Link>
        </>
      )}
    >
      <form onSubmit={handleSubmit(onSubmit)} className="form-grid">
        <label>
          Email
          <input type="email" autoComplete="email" placeholder="you@example.com" {...register('email')} />
          <FormMessage message={errors.email?.message} />
        </label>
        <label>
          Password
          <span className="password-field">
            <input type={showPassword ? 'text' : 'password'} autoComplete="current-password" {...register('password')} />
            <button
              type="button"
              aria-label={showPassword ? 'Hide password' : 'Show password'}
              onClick={() => setShowPassword((value) => !value)}
            >
              {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
            </button>
          </span>
          <FormMessage message={errors.password?.message} />
        </label>
        <FormMessage message={error} />
        <button className="primary-button primary-button--full" type="submit" disabled={isSubmitting}>
          <LogIn size={18} />
          {isSubmitting ? 'Signing in' : 'Login'}
        </button>
      </form>
    </AuthLayout>
  );
}

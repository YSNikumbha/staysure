import { zodResolver } from '@hookform/resolvers/zod';
import { Eye, EyeOff, UserPlus } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { AuthLayout } from '../../components/AuthLayout';
import { FormMessage } from '../../components/FormMessage';
import { useAuthStore } from '../../store/authStore';
import { getApiErrorMessage } from '../../utils/apiError';

const registerSchema = z
  .object({
    firstName: z.string().min(1, 'First name is required').max(100),
    lastName: z.string().min(1, 'Last name is required').max(100),
    email: z.string().email(),
    phone: z.string().min(7, 'Phone is required').max(30),
    password: z.string().min(8, 'Password must be at least 8 characters').max(72),
    confirmPassword: z.string().min(1, 'Confirm your password')
  })
  .refine((value) => value.password === value.confirmPassword, {
    path: ['confirmPassword'],
    message: 'Passwords do not match'
  });

type RegisterForm = z.infer<typeof registerSchema>;

export function RegisterPage() {
  const navigate = useNavigate();
  const registerAccount = useAuthStore((state) => state.register);
  const [error, setError] = useState<string | null>(null);
  const [showPassword, setShowPassword] = useState(false);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting }
  } = useForm<RegisterForm>({ resolver: zodResolver(registerSchema) });

  const onSubmit = async (values: RegisterForm) => {
    setError(null);
    try {
      await registerAccount(values);
      navigate('/profile', { replace: true });
    } catch (err) {
      setError(getApiErrorMessage(err, 'Registration failed'));
    }
  };

  return (
    <AuthLayout
      eyebrow="New account"
      title="Create your StaySure account"
      subtitle="Use one account for PG discovery, booking requests and owner onboarding."
      wide
      footer={<Link to="/login">Already registered? Login</Link>}
    >
      <form onSubmit={handleSubmit(onSubmit)} className="form-grid two-column">
        <label>
          First name
          <input autoComplete="given-name" placeholder="First name" {...register('firstName')} />
          <FormMessage message={errors.firstName?.message} />
        </label>
        <label>
          Last name
          <input autoComplete="family-name" placeholder="Last name" {...register('lastName')} />
          <FormMessage message={errors.lastName?.message} />
        </label>
        <label>
          Email
          <input type="email" autoComplete="email" placeholder="you@example.com" {...register('email')} />
          <FormMessage message={errors.email?.message} />
        </label>
        <label>
          Phone
          <input autoComplete="tel" placeholder="Phone number" {...register('phone')} />
          <FormMessage message={errors.phone?.message} />
        </label>
        <label>
          Password
          <span className="password-field">
            <input type={showPassword ? 'text' : 'password'} autoComplete="new-password" {...register('password')} />
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
        <div className="form-span">
          <FormMessage message={error} />
          <button className="primary-button primary-button--full" type="submit" disabled={isSubmitting}>
            <UserPlus size={18} />
            {isSubmitting ? 'Creating account' : 'Create account'}
          </button>
        </div>
      </form>
    </AuthLayout>
  );
}

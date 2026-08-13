import { zodResolver } from '@hookform/resolvers/zod';
import { UserPlus } from 'lucide-react';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
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
    <section className="auth-panel">
      <div className="auth-box auth-box--wide">
        <p className="eyebrow">New account</p>
        <h1>Register</h1>
        <form onSubmit={handleSubmit(onSubmit)} className="form-grid two-column">
          <label>
            First name
            <input autoComplete="given-name" {...register('firstName')} />
            <FormMessage message={errors.firstName?.message} />
          </label>
          <label>
            Last name
            <input autoComplete="family-name" {...register('lastName')} />
            <FormMessage message={errors.lastName?.message} />
          </label>
          <label>
            Email
            <input type="email" autoComplete="email" {...register('email')} />
            <FormMessage message={errors.email?.message} />
          </label>
          <label>
            Phone
            <input autoComplete="tel" {...register('phone')} />
            <FormMessage message={errors.phone?.message} />
          </label>
          <label>
            Password
            <input type="password" autoComplete="new-password" {...register('password')} />
            <FormMessage message={errors.password?.message} />
          </label>
          <label>
            Confirm password
            <input type="password" autoComplete="new-password" {...register('confirmPassword')} />
            <FormMessage message={errors.confirmPassword?.message} />
          </label>
          <div className="form-span">
            <FormMessage message={error} />
            <button className="primary-button" type="submit" disabled={isSubmitting}>
              <UserPlus size={18} />
              {isSubmitting ? 'Creating account' : 'Register'}
            </button>
          </div>
        </form>
        <div className="auth-links">
          <Link to="/login">Already registered</Link>
        </div>
      </div>
    </section>
  );
}

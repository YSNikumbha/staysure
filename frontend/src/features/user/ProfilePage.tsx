import { zodResolver } from '@hookform/resolvers/zod';
import { KeyRound, Save } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { userApi } from '../../api/user.api';
import { FormMessage } from '../../components/FormMessage';
import { PageHeader } from '../../components/PageHeader';
import { StatusBadge } from '../../components/StatusBadge';
import { useAuthStore } from '../../store/authStore';
import { getApiErrorMessage } from '../../utils/apiError';

const profileSchema = z.object({
  firstName: z.string().min(1, 'First name is required').max(100),
  lastName: z.string().min(1, 'Last name is required').max(100),
  phone: z.string().min(7, 'Phone is required').max(30),
  profileImageUrl: z.string().max(500).optional()
});

const passwordSchema = z
  .object({
    currentPassword: z.string().min(1, 'Current password is required'),
    newPassword: z.string().min(8, 'Password must be at least 8 characters').max(72),
    confirmPassword: z.string().min(1, 'Confirm your password')
  })
  .refine((value) => value.newPassword === value.confirmPassword, {
    path: ['confirmPassword'],
    message: 'Passwords do not match'
  });

type ProfileForm = z.infer<typeof profileSchema>;
type PasswordForm = z.infer<typeof passwordSchema>;

export function ProfilePage() {
  const { user, setUser } = useAuthStore();
  const [profileMessage, setProfileMessage] = useState<string | null>(null);
  const [profileError, setProfileError] = useState<string | null>(null);
  const [passwordMessage, setPasswordMessage] = useState<string | null>(null);
  const [passwordError, setPasswordError] = useState<string | null>(null);

  const profileForm = useForm<ProfileForm>({ resolver: zodResolver(profileSchema) });
  const passwordForm = useForm<PasswordForm>({ resolver: zodResolver(passwordSchema) });

  useEffect(() => {
    if (user) {
      profileForm.reset({
        firstName: user.firstName,
        lastName: user.lastName,
        phone: user.phone,
        profileImageUrl: user.profileImageUrl ?? ''
      });
    }
  }, [profileForm, user]);

  const updateProfile = async (values: ProfileForm) => {
    setProfileError(null);
    setProfileMessage(null);
    try {
      const updated = await userApi.updateMe(values);
      setUser(updated);
      setProfileMessage('Profile updated.');
    } catch (err) {
      setProfileError(getApiErrorMessage(err, 'Unable to update profile'));
    }
  };

  const changePassword = async (values: PasswordForm) => {
    setPasswordError(null);
    setPasswordMessage(null);
    try {
      await userApi.changePassword(values);
      passwordForm.reset();
      setPasswordMessage('Password changed.');
    } catch (err) {
      setPasswordError(getApiErrorMessage(err, 'Unable to change password'));
    }
  };

  if (!user) {
    return <div className="route-state">Loading</div>;
  }

  return (
    <div className="stack">
      <PageHeader eyebrow="My account" title="Profile" />
      <section className="surface">
        <div className="profile-summary">
          <div>
            <h2>{user.firstName} {user.lastName}</h2>
            <p>{user.email}</p>
          </div>
          <div className="badge-row">
            <StatusBadge status={user.status} />
            {user.roles.map((role) => (
              <span className="role-pill" key={role}>{role.replaceAll('_', ' ')}</span>
            ))}
          </div>
        </div>
      </section>

      <section className="surface">
        <h2>Profile Details</h2>
        <form className="form-grid two-column" onSubmit={profileForm.handleSubmit(updateProfile)}>
          <label>
            First name
            <input {...profileForm.register('firstName')} />
            <FormMessage message={profileForm.formState.errors.firstName?.message} />
          </label>
          <label>
            Last name
            <input {...profileForm.register('lastName')} />
            <FormMessage message={profileForm.formState.errors.lastName?.message} />
          </label>
          <label>
            Phone
            <input {...profileForm.register('phone')} />
            <FormMessage message={profileForm.formState.errors.phone?.message} />
          </label>
          <label>
            Profile image URL
            <input {...profileForm.register('profileImageUrl')} />
            <FormMessage message={profileForm.formState.errors.profileImageUrl?.message} />
          </label>
          <div className="form-span">
            <FormMessage message={profileError} />
            <FormMessage message={profileMessage} tone="success" />
            <button className="primary-button" type="submit" disabled={profileForm.formState.isSubmitting}>
              <Save size={18} />
              {profileForm.formState.isSubmitting ? 'Saving' : 'Save profile'}
            </button>
          </div>
        </form>
      </section>

      <section className="surface">
        <h2>Change Password</h2>
        <form className="form-grid three-column" onSubmit={passwordForm.handleSubmit(changePassword)}>
          <label>
            Current password
            <input type="password" autoComplete="current-password" {...passwordForm.register('currentPassword')} />
            <FormMessage message={passwordForm.formState.errors.currentPassword?.message} />
          </label>
          <label>
            New password
            <input type="password" autoComplete="new-password" {...passwordForm.register('newPassword')} />
            <FormMessage message={passwordForm.formState.errors.newPassword?.message} />
          </label>
          <label>
            Confirm password
            <input type="password" autoComplete="new-password" {...passwordForm.register('confirmPassword')} />
            <FormMessage message={passwordForm.formState.errors.confirmPassword?.message} />
          </label>
          <div className="form-span">
            <FormMessage message={passwordError} />
            <FormMessage message={passwordMessage} tone="success" />
            <button className="secondary-button" type="submit" disabled={passwordForm.formState.isSubmitting}>
              <KeyRound size={18} />
              {passwordForm.formState.isSubmitting ? 'Changing' : 'Change password'}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
}

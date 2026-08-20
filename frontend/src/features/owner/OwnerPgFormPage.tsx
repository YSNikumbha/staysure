import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Save } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { z } from 'zod';
import { propertyApi } from '../../api/property.api';
import { FormMessage } from '../../components/FormMessage';
import type { GenderType, PgPropertyInput, PropertyStatus, PropertyType } from '../../types/property';
import { getApiErrorMessage } from '../../utils/apiError';
import { OwnerShell } from './OwnerShell';

const genderTypes: GenderType[] = ['MALE', 'FEMALE', 'COED'];
const propertyTypes: PropertyType[] = ['PG', 'HOSTEL', 'CO_LIVING', 'APARTMENT'];
const propertyStatuses: Array<Exclude<PropertyStatus, 'ARCHIVED'>> = ['DRAFT', 'ACTIVE', 'INACTIVE'];

const optionalNumber = (min: number, max?: number) => z.preprocess(
  (value) => {
    if (value === '' || value === null || value === undefined) {
      return undefined;
    }
    return Number(value);
  },
  max === undefined ? z.number().min(min).optional() : z.number().min(min).max(max).optional()
);

const optionalTime = z.preprocess(
  (value) => (typeof value === 'string' && value.trim() === '' ? undefined : value),
  z.string().optional()
);

const pgSchema = z.object({
  name: z.string().min(2, 'PG name is required').max(180),
  description: z.string().max(3000).optional(),
  genderType: z.enum(['MALE', 'FEMALE', 'COED']),
  propertyType: z.enum(['PG', 'HOSTEL', 'CO_LIVING', 'APARTMENT']),
  addressLine1: z.string().min(2, 'Address line 1 is required').max(255),
  addressLine2: z.string().max(255).optional(),
  area: z.string().min(2, 'Area is required').max(120),
  city: z.string().min(2, 'City is required').max(100),
  state: z.string().min(2, 'State is required').max(100),
  pincode: z.string().regex(/^[1-9][0-9]{5}$/, 'Enter a valid 6 digit pincode'),
  latitude: optionalNumber(-90, 90),
  longitude: optionalNumber(-180, 180),
  startingRent: z.coerce.number().min(0, 'Starting rent cannot be negative'),
  securityDeposit: z.coerce.number().min(0, 'Security deposit cannot be negative'),
  noticePeriodDays: z.coerce.number().min(0, 'Notice period cannot be negative'),
  lockInMonths: z.coerce.number().min(0, 'Lock-in period cannot be negative'),
  entryTime: optionalTime,
  foodAvailable: z.boolean(),
  status: z.enum(['DRAFT', 'ACTIVE', 'INACTIVE']),
  rules: z.object({
    visitorAllowed: z.boolean(),
    smokingAllowed: z.boolean(),
    alcoholAllowed: z.boolean(),
    cookingAllowed: z.boolean(),
    gateClosingTime: optionalTime,
    lateEntryAllowed: z.boolean(),
    noticePeriodDays: z.coerce.number().min(0, 'Notice period cannot be negative'),
    additionalRules: z.string().max(3000).optional()
  })
});

type PgFormValues = z.infer<typeof pgSchema>;

const defaultValues: PgFormValues = {
  name: '',
  description: '',
  genderType: 'COED',
  propertyType: 'PG',
  addressLine1: '',
  addressLine2: '',
  area: '',
  city: '',
  state: '',
  pincode: '',
  latitude: undefined,
  longitude: undefined,
  startingRent: 0,
  securityDeposit: 0,
  noticePeriodDays: 30,
  lockInMonths: 0,
  entryTime: '',
  foodAvailable: false,
  status: 'DRAFT',
  rules: {
    visitorAllowed: false,
    smokingAllowed: false,
    alcoholAllowed: false,
    cookingAllowed: false,
    gateClosingTime: '',
    lateEntryAllowed: false,
    noticePeriodDays: 30,
    additionalRules: ''
  }
};

export function OwnerPgFormPage() {
  const { id } = useParams();
  const pgId = id ? Number(id) : undefined;
  const isEdit = Number.isFinite(pgId);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [formError, setFormError] = useState<string | null>(null);

  const propertyQuery = useQuery({
    queryKey: ['owner-pg', pgId],
    queryFn: () => propertyApi.property(pgId!),
    enabled: isEdit
  });

  const form = useForm<PgFormValues>({
    resolver: zodResolver(pgSchema),
    defaultValues
  });

  useEffect(() => {
    const details = propertyQuery.data;
    if (!details) {
      return;
    }
    const property = details.property;
    const rules = details.rules;
    form.reset({
      name: property.name,
      description: property.description ?? '',
      genderType: property.genderType,
      propertyType: property.propertyType,
      addressLine1: property.addressLine1,
      addressLine2: property.addressLine2 ?? '',
      area: property.area,
      city: property.city,
      state: property.state,
      pincode: property.pincode,
      latitude: property.latitude ?? undefined,
      longitude: property.longitude ?? undefined,
      startingRent: Number(property.startingRent),
      securityDeposit: Number(property.securityDeposit),
      noticePeriodDays: property.noticePeriodDays,
      lockInMonths: property.lockInMonths,
      entryTime: toTimeInput(property.entryTime),
      foodAvailable: property.foodAvailable,
      status: property.status === 'ARCHIVED' ? 'INACTIVE' : property.status,
      rules: {
        visitorAllowed: rules?.visitorAllowed ?? false,
        smokingAllowed: rules?.smokingAllowed ?? false,
        alcoholAllowed: rules?.alcoholAllowed ?? false,
        cookingAllowed: rules?.cookingAllowed ?? false,
        gateClosingTime: toTimeInput(rules?.gateClosingTime),
        lateEntryAllowed: rules?.lateEntryAllowed ?? false,
        noticePeriodDays: rules?.noticePeriodDays ?? property.noticePeriodDays,
        additionalRules: rules?.additionalRules ?? ''
      }
    });
  }, [form, propertyQuery.data]);

  const saveMutation = useMutation({
    mutationFn: (values: PgFormValues) => {
      const input = toPropertyInput(values);
      return isEdit ? propertyApi.updateProperty(pgId!, input) : propertyApi.createProperty(input);
    },
    onSuccess: async (details) => {
      setFormError(null);
      await queryClient.invalidateQueries({ queryKey: ['owner-pgs'] });
      await queryClient.invalidateQueries({ queryKey: ['owner-dashboard'] });
      await queryClient.invalidateQueries({ queryKey: ['owner-pg', details.property.id] });
      navigate(`/owner/pgs/${details.property.id}`);
    },
    onError: (error) => setFormError(getApiErrorMessage(error, 'Unable to save PG'))
  });

  const title = useMemo(() => (isEdit ? 'Edit PG' : 'Create PG'), [isEdit]);

  if (propertyQuery.isLoading) {
    return (
      <OwnerShell title={title} eyebrow="Property inventory">
        <div className="owner-stack">
          <div className="owner-skeleton-card" />
          <div className="owner-skeleton-card" />
        </div>
      </OwnerShell>
    );
  }

  if (propertyQuery.isError) {
    return (
      <OwnerShell title={title} eyebrow="Property inventory">
        <div className="route-state">{getApiErrorMessage(propertyQuery.error, 'Unable to load PG')}</div>
      </OwnerShell>
    );
  }

  return (
    <OwnerShell
      title={title}
      eyebrow="Property inventory"
      actions={<Link className="secondary-link compact-button" to={isEdit ? `/owner/pgs/${pgId}` : '/owner/pgs'}>Back</Link>}
    >
      <form className="owner-form-layout" onSubmit={form.handleSubmit((values) => saveMutation.mutate(values))}>
        <aside className="owner-form-nav" aria-label="Property form sections">
          <p className="eyebrow">Sections</p>
          <a href="#basic">Basic Information</a>
          <a href="#location">Location</a>
          <a href="#pricing">Pricing</a>
          <a href="#rules">Rules</a>
          <button className="primary-button" type="submit" disabled={saveMutation.isPending}>
            <Save size={18} />
            {saveMutation.isPending ? 'Saving' : 'Save PG'}
          </button>
        </aside>

        <div className="owner-form-body">
          <section className="surface form-section" id="basic">
            <h2>Basic Information</h2>
            <p className="muted-copy">This core listing information is visible to users after admin verification.</p>
            <div className="form-grid two-column">
              <label className="form-span">
                PG Name
                <input {...form.register('name')} />
                <FormMessage message={form.formState.errors.name?.message} />
              </label>
              <label className="form-span">
                Description
                <textarea rows={4} {...form.register('description')} />
                <FormMessage message={form.formState.errors.description?.message} />
              </label>
              <label>
                Gender
                <select {...form.register('genderType')}>
                  {genderTypes.map((type) => <option key={type} value={type}>{type.replaceAll('_', ' ')}</option>)}
                </select>
              </label>
              <label>
                Property Type
                <select {...form.register('propertyType')}>
                  {propertyTypes.map((type) => <option key={type} value={type}>{type.replaceAll('_', ' ')}</option>)}
                </select>
              </label>
              <label>
                Status
                <select {...form.register('status')}>
                  {propertyStatuses.map((status) => <option key={status} value={status}>{status}</option>)}
                </select>
              </label>
              <label className="checkbox-field">
                <input type="checkbox" {...form.register('foodAvailable')} />
                Food Available
              </label>
            </div>
          </section>

          <section className="surface form-section" id="location">
            <h2>Location</h2>
            <p className="muted-copy">Use a complete address so the PG can pass verification and appear in location search.</p>
            <div className="form-grid two-column">
              <label className="form-span">
                Address Line 1
                <input {...form.register('addressLine1')} />
                <FormMessage message={form.formState.errors.addressLine1?.message} />
              </label>
              <label className="form-span">
                Address Line 2
                <input {...form.register('addressLine2')} />
                <FormMessage message={form.formState.errors.addressLine2?.message} />
              </label>
              <label>
                Area
                <input {...form.register('area')} />
                <FormMessage message={form.formState.errors.area?.message} />
              </label>
              <label>
                City
                <input {...form.register('city')} />
                <FormMessage message={form.formState.errors.city?.message} />
              </label>
              <label>
                State
                <input {...form.register('state')} />
                <FormMessage message={form.formState.errors.state?.message} />
              </label>
              <label>
                Pincode
                <input {...form.register('pincode')} />
                <FormMessage message={form.formState.errors.pincode?.message} />
              </label>
              <label>
                Latitude
                <input type="number" step="0.0000001" {...form.register('latitude')} />
                <FormMessage message={form.formState.errors.latitude?.message} />
              </label>
              <label>
                Longitude
                <input type="number" step="0.0000001" {...form.register('longitude')} />
                <FormMessage message={form.formState.errors.longitude?.message} />
              </label>
            </div>
          </section>

          <section className="surface form-section" id="pricing">
            <h2>Pricing</h2>
            <p className="muted-copy">Users see starting rent and deposit on cards and the PG detail page.</p>
            <div className="form-grid two-column">
              <label>
                Starting Rent
                <input type="number" min="0" {...form.register('startingRent')} />
                <FormMessage message={form.formState.errors.startingRent?.message} />
              </label>
              <label>
                Security Deposit
                <input type="number" min="0" {...form.register('securityDeposit')} />
                <FormMessage message={form.formState.errors.securityDeposit?.message} />
              </label>
              <label>
                Notice Period
                <input type="number" min="0" {...form.register('noticePeriodDays')} />
                <FormMessage message={form.formState.errors.noticePeriodDays?.message} />
              </label>
              <label>
                Lock-in Period
                <input type="number" min="0" {...form.register('lockInMonths')} />
                <FormMessage message={form.formState.errors.lockInMonths?.message} />
              </label>
              <label>
                Entry Time
                <input type="time" {...form.register('entryTime')} />
                <FormMessage message={form.formState.errors.entryTime?.message} />
              </label>
            </div>
          </section>

          <section className="surface form-section" id="rules">
            <h2>Rules</h2>
            <p className="muted-copy">Set house rules clearly before submitting the PG for verification.</p>
            <div className="form-grid two-column">
              <label className="checkbox-field">
                <input type="checkbox" {...form.register('rules.visitorAllowed')} />
                Visitor Allowed
              </label>
              <label className="checkbox-field">
                <input type="checkbox" {...form.register('rules.smokingAllowed')} />
                Smoking Allowed
              </label>
              <label className="checkbox-field">
                <input type="checkbox" {...form.register('rules.alcoholAllowed')} />
                Alcohol Allowed
              </label>
              <label className="checkbox-field">
                <input type="checkbox" {...form.register('rules.cookingAllowed')} />
                Cooking Allowed
              </label>
              <label>
                Gate Closing Time
                <input type="time" {...form.register('rules.gateClosingTime')} />
                <FormMessage message={form.formState.errors.rules?.gateClosingTime?.message} />
              </label>
              <label className="checkbox-field">
                <input type="checkbox" {...form.register('rules.lateEntryAllowed')} />
                Late Entry Allowed
              </label>
              <label>
                Rule Notice Period
                <input type="number" min="0" {...form.register('rules.noticePeriodDays')} />
                <FormMessage message={form.formState.errors.rules?.noticePeriodDays?.message} />
              </label>
              <label className="form-span">
                Additional Rules
                <textarea rows={4} {...form.register('rules.additionalRules')} />
                <FormMessage message={form.formState.errors.rules?.additionalRules?.message} />
              </label>
            </div>
          </section>

          <section className="surface owner-submit-bar">
            <FormMessage message={formError} />
            <button className="primary-button" type="submit" disabled={saveMutation.isPending}>
              <Save size={18} />
              {saveMutation.isPending ? 'Saving' : 'Save PG'}
            </button>
          </section>
        </div>
      </form>
    </OwnerShell>
  );
}

function toPropertyInput(values: PgFormValues): PgPropertyInput {
  return {
    name: values.name.trim(),
    description: blankToUndefined(values.description),
    genderType: values.genderType,
    propertyType: values.propertyType,
    addressLine1: values.addressLine1.trim(),
    addressLine2: blankToUndefined(values.addressLine2),
    area: values.area.trim(),
    city: values.city.trim(),
    state: values.state.trim(),
    pincode: values.pincode.trim(),
    latitude: values.latitude,
    longitude: values.longitude,
    startingRent: values.startingRent,
    securityDeposit: values.securityDeposit,
    noticePeriodDays: values.noticePeriodDays,
    lockInMonths: values.lockInMonths,
    entryTime: blankToUndefined(values.entryTime),
    foodAvailable: values.foodAvailable,
    status: values.status,
    rules: {
      visitorAllowed: values.rules.visitorAllowed,
      smokingAllowed: values.rules.smokingAllowed,
      alcoholAllowed: values.rules.alcoholAllowed,
      cookingAllowed: values.rules.cookingAllowed,
      gateClosingTime: blankToUndefined(values.rules.gateClosingTime),
      lateEntryAllowed: values.rules.lateEntryAllowed,
      noticePeriodDays: values.rules.noticePeriodDays,
      additionalRules: blankToUndefined(values.rules.additionalRules)
    }
  };
}

function blankToUndefined(value?: string) {
  return value && value.trim() ? value.trim() : undefined;
}

function toTimeInput(value?: string | null) {
  return value ? value.slice(0, 5) : '';
}

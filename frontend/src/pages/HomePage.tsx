import { useQuery } from '@tanstack/react-query';
import { ArrowRight, BedDouble, Building2, CheckCircle2, Heart, MapPin, Search, ShieldCheck, SlidersHorizontal } from 'lucide-react';
import { FormEvent, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { publicApi } from '../api/public.api';
import { EmptyState } from '../components/EmptyState';
import { SectionHeader } from '../components/SectionHeader';
import type { PublicPgCard } from '../types/property';
import { toAssetUrl } from '../utils/assets';

const discoveryLinks = [
  { label: 'Boys PG', to: '/find-pg?genderType=MALE' },
  { label: 'Girls PG', to: '/find-pg?genderType=FEMALE' },
  { label: 'Co-living', to: '/find-pg?propertyType=CO_LIVING' },
  { label: 'Single Sharing', to: '/find-pg?sharingType=SINGLE' },
  { label: 'Double Sharing', to: '/find-pg?sharingType=DOUBLE' }
];

const whyStaySure = [
  {
    icon: ShieldCheck,
    title: 'Verified Properties',
    text: 'Properties go through the StaySure verification workflow before appearing publicly.'
  },
  {
    icon: SlidersHorizontal,
    title: 'Transparent Details',
    text: 'Review pricing, amenities, room information and availability before requesting a booking.'
  },
  {
    icon: Heart,
    title: 'Easy Comparison',
    text: 'Save PGs to your wishlist and compare shortlisted options side by side.'
  },
  {
    icon: CheckCircle2,
    title: 'Simple Booking',
    text: 'Choose an available room and bed, then track onboarding from request to check-in.'
  }
];

const steps = [
  ['Search', 'Find PGs based on location and preferences.'],
  ['Compare', 'Review amenities, rooms and pricing.'],
  ['Request Booking', 'Choose an available room and bed.'],
  ['Move In', 'Complete onboarding and check-in.']
];

export function HomePage() {
  const [search, setSearch] = useState('');
  const navigate = useNavigate();

  const featuredQuery = useQuery({
    queryKey: ['home-featured-pgs'],
    queryFn: () => publicApi.searchPgs({ page: 0, size: 3, sort: 'latest' })
  });

  const featured = featuredQuery.data?.content ?? [];
  const previewProperty = useMemo(() => featured[0], [featured]);

  const submitSearch = (event: FormEvent) => {
    event.preventDefault();
    const params = new URLSearchParams();
    if (search.trim()) {
      params.set('search', search.trim());
    }
    navigate(`/find-pg${params.toString() ? `?${params.toString()}` : ''}`);
  };

  return (
    <div className="landing-page">
      <section className="landing-hero">
        <div className="landing-hero__copy">
          <p className="eyebrow">Verified PG discovery</p>
          <h1>Find a PG that feels easier to trust.</h1>
          <p>
            Discover verified PGs with transparent pricing, real room availability and a simpler
            booking-to-onboarding experience.
          </p>
          <form className="market-search" onSubmit={submitSearch}>
            <Search size={20} aria-hidden="true" />
            <label className="sr-only" htmlFor="home-search">Search by city, locality or PG name</label>
            <input
              id="home-search"
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search by city, locality or PG name"
            />
            <button className="primary-button" type="submit">Find PG</button>
          </form>
          <div className="trust-strip" aria-label="StaySure trust indicators">
            <span><ShieldCheck size={18} /> Verified PGs</span>
            <span><Building2 size={18} /> Real inventory</span>
            <span><BedDouble size={18} /> Bed-level booking</span>
          </div>
        </div>

        <aside className="hero-market-card" aria-label="StaySure listing preview">
          {previewProperty ? <HeroPropertyPreview property={previewProperty} /> : <HeroProcessPreview loading={featuredQuery.isLoading} />}
        </aside>
      </section>

      <section className="landing-section">
        <SectionHeader
          eyebrow="Recently verified"
          title="Featured PGs"
          description="A quick look at public listings loaded from StaySure discovery."
          actions={<Link className="secondary-link" to="/find-pg">View all PGs</Link>}
        />
        {featuredQuery.isLoading ? <div className="featured-grid"><FeaturedSkeleton /><FeaturedSkeleton /><FeaturedSkeleton /></div> : null}
        {featuredQuery.isError ? (
          <EmptyState
            title="Unable to load featured PGs"
            description="You can still open the full search page and try again."
            action={<Link className="secondary-link" to="/find-pg">Open Find PG</Link>}
          />
        ) : null}
        {!featuredQuery.isLoading && !featuredQuery.isError && featured.length === 0 ? (
          <EmptyState
            title="No verified PGs are public yet"
            description="Once owners publish verified active PGs, they will appear here automatically."
            action={<Link className="primary-link" to="/find-pg">Go to Find PG</Link>}
          />
        ) : null}
        {featured.length > 0 ? (
          <div className="featured-grid">
            {featured.map((property) => <FeaturedPgCard key={property.id} property={property} />)}
          </div>
        ) : null}
      </section>

      <section className="landing-section">
        <SectionHeader
          eyebrow="Discover by need"
          title="Start with what matters"
          description="Use shortcuts that map to existing StaySure filters."
        />
        <div className="shortcut-grid">
          {discoveryLinks.map((item) => (
            <Link className="shortcut-card" to={item.to} key={item.label}>
              <span>{item.label}</span>
              <ArrowRight size={18} />
            </Link>
          ))}
        </div>
      </section>

      <section className="landing-section landing-section--band">
        <SectionHeader
          eyebrow="Why StaySure"
          title="Built around verified listings and real onboarding"
          description="StaySure keeps discovery, booking and owner-managed inventory connected."
        />
        <div className="value-grid">
          {whyStaySure.map((item) => {
            const Icon = item.icon;
            return (
              <article className="value-card" key={item.title}>
                <span className="value-icon"><Icon size={22} /></span>
                <h3>{item.title}</h3>
                <p>{item.text}</p>
              </article>
            );
          })}
        </div>
      </section>

      <section className="landing-section">
        <SectionHeader eyebrow="How it works" title="From search to move-in" />
        <div className="step-grid">
          {steps.map(([title, text], index) => (
            <article className="step-card" key={title}>
              <span>{index + 1}</span>
              <h3>{title}</h3>
              <p>{text}</p>
            </article>
          ))}
        </div>
      </section>

      <section className="owner-cta-section">
        <div>
          <p className="eyebrow">For PG owners</p>
          <h2>Own or manage a PG?</h2>
          <p>
            List your property on StaySure and manage properties, rooms, beds, bookings and tenants
            from one place.
          </p>
        </div>
        <div className="hero-actions">
          <Link className="primary-link" to="/owner/apply">List Your PG</Link>
          <Link className="secondary-link" to="/for-owners">Learn More</Link>
        </div>
      </section>

      <section className="final-cta">
        <h2>Ready to find your next PG?</h2>
        <Link className="primary-link" to="/find-pg">Find PG</Link>
      </section>
    </div>
  );
}

function HeroPropertyPreview({ property }: { property: PublicPgCard }) {
  return (
    <article className="hero-property-preview">
      {property.coverImage ? (
        <img src={toAssetUrl(property.coverImage)} alt={property.name} />
      ) : (
        <div className="property-image-fallback">StaySure</div>
      )}
      <div>
        <span className="status-badge status-badge--success">Verified</span>
        <h2>{property.name}</h2>
        <p><MapPin size={16} /> {property.area}, {property.city}</p>
        <strong>Rs {Number(property.startingRent).toLocaleString()} / month onwards</strong>
        <Link className="secondary-link compact-button" to={`/pg/${property.slug}`}>View Details</Link>
      </div>
    </article>
  );
}

function HeroProcessPreview({ loading }: { loading: boolean }) {
  return (
    <div className="hero-process-preview">
      <span className="status-badge status-badge--success">{loading ? 'Loading listings' : 'Verified flow'}</span>
      <h2>Search with confidence</h2>
      <div className="mini-flow">
        <span>Verified PG</span>
        <span>Real rooms</span>
        <span>Bed request</span>
        <span>Onboarding</span>
      </div>
    </div>
  );
}

function FeaturedPgCard({ property }: { property: PublicPgCard }) {
  return (
    <article className="featured-card">
      {property.coverImage ? (
        <img src={toAssetUrl(property.coverImage)} alt={property.name} />
      ) : (
        <div className="property-image-fallback">StaySure</div>
      )}
      <div className="featured-card__body">
        <div>
          <span className="status-badge status-badge--success">Verified</span>
          <h3>{property.name}</h3>
          <p>{property.area}, {property.city}</p>
        </div>
        <div className="pg-card-facts">
          <span>{property.genderType.replaceAll('_', ' ')}</span>
          <span>{property.propertyType.replaceAll('_', ' ')}</span>
        </div>
        <div className="featured-card__footer">
          <strong>Rs {Number(property.startingRent).toLocaleString()}</strong>
          <span>{property.availableBeds} beds available</span>
        </div>
        <Link className="secondary-link compact-button" to={`/pg/${property.slug}`}>View Details</Link>
      </div>
    </article>
  );
}

function FeaturedSkeleton() {
  return (
    <div className="featured-card featured-card--loading" aria-hidden="true">
      <span />
      <div>
        <span />
        <span />
        <span />
      </div>
    </div>
  );
}

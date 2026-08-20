import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Filter, Search, SlidersHorizontal, X } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import type { FormEvent, ReactNode } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { publicApi, type PublicPgSearchParams } from '../api/public.api';
import { wishlistApi } from '../api/wishlist.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { PublicPgCard, PublicPgCardSkeleton } from '../components/PublicPgCard';
import type { Amenity, GenderType, PropertyType, PublicPgCard as PublicPgCardType, SharingType } from '../types/property';
import { useAuthStore } from '../store/authStore';
import { getApiErrorMessage } from '../utils/apiError';
import { addCompareItem, getCompareItems } from '../utils/compareStore';

const genderTypes: GenderType[] = ['MALE', 'FEMALE', 'COED'];
const propertyTypes: PropertyType[] = ['PG', 'HOSTEL', 'CO_LIVING', 'APARTMENT'];
const sharingTypes: SharingType[] = ['SINGLE', 'DOUBLE', 'TRIPLE', 'FOUR_SHARING', 'DORMITORY'];

const sortOptions: Array<{ value: NonNullable<PublicPgSearchParams['sort']>; label: string }> = [
  { value: 'latest', label: 'Newest' },
  { value: 'price_low_to_high', label: 'Rent: Low to High' },
  { value: 'price_high_to_low', label: 'Rent: High to Low' },
  { value: 'availability', label: 'Availability' }
];

export function FindPgPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const queryClient = useQueryClient();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const [compareMessage, setCompareMessage] = useState<string | null>(null);
  const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false);
  const [compareItems, setCompareItems] = useState(() => getCompareItems());
  const params = useMemo(() => readParams(searchParams), [searchParams]);
  const [searchInput, setSearchInput] = useState(params.search ?? '');

  useEffect(() => {
    setSearchInput(params.search ?? '');
  }, [params.search]);

  const pgsQuery = useQuery({
    queryKey: ['public-pgs', params],
    queryFn: () => publicApi.searchPgs(params)
  });

  const amenitiesQuery = useQuery({
    queryKey: ['public-amenities'],
    queryFn: publicApi.amenities
  });

  const wishlistQuery = useQuery({
    queryKey: ['wishlist'],
    queryFn: wishlistApi.list,
    enabled: isAuthenticated
  });

  const wishlistIds = new Set((wishlistQuery.data ?? []).map((item) => item.property.id));
  const comparedIds = new Set(compareItems.map((item) => item.id));
  const page = pgsQuery.data;
  const locationLabel = params.area || params.city || params.search;

  const addWishlist = useMutation({
    mutationFn: wishlistApi.add,
    onSuccess: async () => queryClient.invalidateQueries({ queryKey: ['wishlist'] })
  });

  const removeWishlist = useMutation({
    mutationFn: wishlistApi.remove,
    onSuccess: async () => queryClient.invalidateQueries({ queryKey: ['wishlist'] })
  });

  const setParam = (key: string, value?: string | number | boolean | null) => {
    const next = new URLSearchParams(searchParams);
    if (value === undefined || value === null || value === '') {
      next.delete(key);
    } else {
      next.set(key, String(value));
    }
    if (key !== 'page') {
      next.set('page', '0');
    }
    setSearchParams(next);
  };

  const removeParam = (key: string) => setParam(key, null);

  const submitSearch = (event: FormEvent) => {
    event.preventDefault();
    setParam('search', searchInput.trim() || null);
  };

  const clearSearch = () => {
    setSearchInput('');
    removeParam('search');
  };

  const clearFilters = () => {
    setSearchInput('');
    setSearchParams(new URLSearchParams({ page: '0' }));
  };

  const clearBudget = () => {
    const next = new URLSearchParams(searchParams);
    next.delete('minRent');
    next.delete('maxRent');
    next.set('page', '0');
    setSearchParams(next);
  };

  const toggleAmenity = (amenityId: number, checked: boolean) => {
    const ids = new Set(params.amenityIds ?? []);
    if (checked) {
      ids.add(amenityId);
    } else {
      ids.delete(amenityId);
    }
    setParam('amenityIds', [...ids].join(','));
  };

  const toggleWishlist = (property: PublicPgCardType) => {
    if (wishlistIds.has(property.id)) {
      removeWishlist.mutate(property.id);
    } else {
      addWishlist.mutate(property.id);
    }
  };

  const compare = (property: PublicPgCardType) => {
    try {
      const items = addCompareItem(property);
      setCompareItems(items);
      setCompareMessage(`${items.length} PG${items.length === 1 ? '' : 's'} selected for comparison.`);
    } catch (error) {
      setCompareMessage(error instanceof Error ? error.message : 'Unable to add PG to comparison.');
    }
  };

  const activeChips = buildActiveChips(params, amenitiesQuery.data ?? [], removeParam, clearBudget, toggleAmenity);

  return (
    <div className="discovery-page">
      <section className="discovery-hero-panel">
        <nav className="breadcrumb" aria-label="Breadcrumb">
          <Link to="/">Home</Link>
          <span>/</span>
          <span>Find PG</span>
        </nav>
        <div className="discovery-hero-copy">
          <div>
            <p className="eyebrow">PG discovery</p>
            <h1>{locationLabel ? `PGs in ${locationLabel}` : 'Find verified PGs'}</h1>
            <p>Search verified active PGs with real pricing, amenities and bed-level availability.</p>
          </div>
          <Link className="secondary-link" to="/compare">Compare shortlisted PGs</Link>
        </div>
        <form className="discovery-search" onSubmit={submitSearch}>
          <Search size={20} aria-hidden="true" />
          <label className="sr-only" htmlFor="pg-search">Search by PG, locality or city</label>
          <input
            id="pg-search"
            value={searchInput}
            onChange={(event) => setSearchInput(event.target.value)}
            placeholder="Search by PG, locality or city"
          />
          {searchInput ? (
            <button className="icon-button" type="button" onClick={clearSearch} aria-label="Clear search">
              <X size={18} />
            </button>
          ) : null}
          <button className="primary-button" type="submit">Search</button>
        </form>
        <div className="active-filter-row">
          {activeChips.length ? activeChips : <span className="filter-hint">Use filters to narrow verified PGs by location, rent and room type.</span>}
          {activeChips.length ? <button className="ghost-button" type="button" onClick={clearFilters}>Clear all</button> : null}
        </div>
      </section>

      <div className="mobile-filter-controls">
        <button className="secondary-button" type="button" onClick={() => setMobileFiltersOpen(true)}>
          <Filter size={17} />
          Filters
        </button>
        <label>
          Sort
          <select value={params.sort ?? 'latest'} onChange={(event) => setParam('sort', event.target.value)}>
            {sortOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
          </select>
        </label>
      </div>

      <section className="discovery-results-layout">
        <aside className="filter-panel filter-panel--market">
          <FilterPanel
            params={params}
            amenities={amenitiesQuery.data ?? []}
            amenitiesLoading={amenitiesQuery.isLoading}
            setParam={setParam}
            toggleAmenity={toggleAmenity}
            clearFilters={clearFilters}
          />
        </aside>

        <div className="results-panel results-panel--market">
          <div className="results-toolbar results-toolbar--market">
            <div>
              <span>{page ? `${page.totalElements} properties found` : 'Searching verified PGs'}</span>
              <p>{locationLabel ? `Showing results for ${locationLabel}` : 'Browse all verified active PG listings.'}</p>
            </div>
            <label>
              Sort By
              <select value={params.sort ?? 'latest'} onChange={(event) => setParam('sort', event.target.value)}>
                {sortOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
              </select>
            </label>
          </div>

          <FormMessage message={compareMessage} tone={compareMessage?.startsWith('Unable') ? 'error' : 'success'} />
          {addWishlist.isError ? <FormMessage message={getApiErrorMessage(addWishlist.error, 'Unable to update wishlist')} /> : null}
          {removeWishlist.isError ? <FormMessage message={getApiErrorMessage(removeWishlist.error, 'Unable to update wishlist')} /> : null}

          {pgsQuery.isLoading ? (
            <div className="pg-card-list">
              <PublicPgCardSkeleton />
              <PublicPgCardSkeleton />
              <PublicPgCardSkeleton />
            </div>
          ) : null}

          {pgsQuery.isError ? (
            <EmptyState
              title="We couldn't load PGs right now."
              description={getApiErrorMessage(pgsQuery.error, 'Try again in a moment.')}
              action={<button className="secondary-button" type="button" onClick={() => void pgsQuery.refetch()}>Try Again</button>}
            />
          ) : null}

          {!pgsQuery.isLoading && !pgsQuery.isError && page?.content.length === 0 ? (
            <EmptyState
              title="No PGs match your current filters."
              description="Try changing location, increasing budget or removing a few filters."
              action={<button className="primary-button" type="button" onClick={clearFilters}>Clear Filters</button>}
            />
          ) : null}

          {page && page.content.length > 0 ? (
            <>
              <div className="pg-card-list">
                {page.content.map((property) => (
                  <PublicPgCard
                    key={property.id}
                    property={property}
                    wishlisted={wishlistIds.has(property.id)}
                    compared={comparedIds.has(property.id)}
                    onToggleWishlist={toggleWishlist}
                    onCompare={compare}
                  />
                ))}
              </div>
              <Pagination
                page={page.page}
                totalPages={page.totalPages}
                first={page.first}
                last={page.last}
                setPage={(nextPage) => setParam('page', nextPage)}
              />
            </>
          ) : null}
        </div>
      </section>

      {mobileFiltersOpen ? (
        <div className="filter-drawer-backdrop" role="presentation">
          <aside className="filter-drawer" role="dialog" aria-modal="true" aria-label="PG filters">
            <div className="section-heading">
              <div>
                <p className="eyebrow">Filters</p>
                <h2>Refine PGs</h2>
              </div>
              <button className="icon-button" type="button" aria-label="Close filters" onClick={() => setMobileFiltersOpen(false)}>
                <X size={18} />
              </button>
            </div>
            <FilterPanel
              params={params}
              amenities={amenitiesQuery.data ?? []}
              amenitiesLoading={amenitiesQuery.isLoading}
              setParam={setParam}
              toggleAmenity={toggleAmenity}
              clearFilters={clearFilters}
            />
            <div className="filter-drawer-actions">
              <button className="secondary-button" type="button" onClick={clearFilters}>Clear All</button>
              <button className="primary-button" type="button" onClick={() => setMobileFiltersOpen(false)}>Apply Filters</button>
            </div>
          </aside>
        </div>
      ) : null}
    </div>
  );
}

type FilterPanelProps = {
  params: PublicPgSearchParams;
  amenities: Amenity[];
  amenitiesLoading: boolean;
  setParam: (key: string, value?: string | number | boolean | null) => void;
  toggleAmenity: (amenityId: number, checked: boolean) => void;
  clearFilters: () => void;
};

function FilterPanel({ params, amenities, amenitiesLoading, setParam, toggleAmenity, clearFilters }: FilterPanelProps) {
  return (
    <div className="filter-shell">
      <div className="filter-panel-header">
        <div>
          <p className="eyebrow">Filters</p>
          <h2>Refine search</h2>
        </div>
        <button className="ghost-button" type="button" onClick={clearFilters}>Clear</button>
      </div>

      <FilterSection title="Location">
        <label>
          City
          <input value={params.city ?? ''} onChange={(event) => setParam('city', event.target.value)} placeholder="Pune" />
        </label>
        <label>
          Area
          <input value={params.area ?? ''} onChange={(event) => setParam('area', event.target.value)} placeholder="Baner, Hinjawadi..." />
        </label>
      </FilterSection>

      <FilterSection title="Monthly Budget">
        <div className="filter-pair">
          <label>
            Minimum
            <input type="number" min="0" value={params.minRent ?? ''} onChange={(event) => setParam('minRent', event.target.value)} />
          </label>
          <label>
            Maximum
            <input type="number" min="0" value={params.maxRent ?? ''} onChange={(event) => setParam('maxRent', event.target.value)} />
          </label>
        </div>
      </FilterSection>

      <FilterSection title="Suitable For">
        <div className="filter-options">
          <Choice label="Any" selected={!params.genderType} onClick={() => setParam('genderType', null)} />
          {genderTypes.map((item) => (
            <Choice key={item} label={item.replaceAll('_', ' ')} selected={params.genderType === item} onClick={() => setParam('genderType', item)} />
          ))}
        </div>
      </FilterSection>

      <FilterSection title="Property Type">
        <div className="filter-options">
          <Choice label="Any" selected={!params.propertyType} onClick={() => setParam('propertyType', null)} />
          {propertyTypes.map((item) => (
            <Choice key={item} label={item.replaceAll('_', ' ')} selected={params.propertyType === item} onClick={() => setParam('propertyType', item)} />
          ))}
        </div>
      </FilterSection>

      <FilterSection title="Sharing Type">
        <div className="filter-options">
          <Choice label="Any" selected={!params.sharingType} onClick={() => setParam('sharingType', null)} />
          {sharingTypes.map((item) => (
            <Choice key={item} label={item.replaceAll('_', ' ')} selected={params.sharingType === item} onClick={() => setParam('sharingType', item)} />
          ))}
        </div>
      </FilterSection>

      <FilterSection title="Food & Availability">
        <label className="checkbox-field checkbox-field--market">
          <input type="checkbox" checked={params.foodAvailable === true} onChange={(event) => setParam('foodAvailable', event.target.checked ? true : null)} />
          Food available
        </label>
        <label className="checkbox-field checkbox-field--market">
          <input type="checkbox" checked={params.availableOnly === true} onChange={(event) => setParam('availableOnly', event.target.checked ? true : null)} />
          Available now
        </label>
      </FilterSection>

      <FilterSection title="Amenities">
        {amenitiesLoading ? <p className="muted-copy">Loading amenities</p> : null}
        <div className="filter-check-list">
          {amenities.map((amenity) => (
            <label className="checkbox-field checkbox-field--market" key={amenity.id}>
              <input
                type="checkbox"
                checked={(params.amenityIds ?? []).includes(amenity.id)}
                onChange={(event) => toggleAmenity(amenity.id, event.target.checked)}
              />
              {amenity.name}
            </label>
          ))}
        </div>
      </FilterSection>
    </div>
  );
}

function FilterSection({ title, children }: { title: string; children: ReactNode }) {
  return (
    <section className="filter-section">
      <h3>{title}</h3>
      {children}
    </section>
  );
}

function Choice({ label, selected, onClick }: { label: string; selected: boolean; onClick: () => void }) {
  return (
    <button className={`filter-choice ${selected ? 'filter-choice--active' : ''}`} type="button" onClick={onClick}>
      {label}
    </button>
  );
}

function Pagination({
  page,
  totalPages,
  first,
  last,
  setPage
}: {
  page: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  setPage: (page: number) => void;
}) {
  const pages = Array.from({ length: Math.min(totalPages, 5) }, (_, index) => {
    const start = Math.max(0, Math.min(page - 2, totalPages - 5));
    return start + index;
  }).filter((item) => item < totalPages);

  return (
    <nav className="pagination-row pagination-row--market" aria-label="Search result pages">
      <button className="secondary-button" type="button" disabled={first} onClick={() => setPage(page - 1)}>Previous</button>
      <div className="pagination-pages">
        {pages.map((item) => (
          <button
            key={item}
            className={item === page ? 'pagination-page pagination-page--active' : 'pagination-page'}
            type="button"
            onClick={() => setPage(item)}
            aria-current={item === page ? 'page' : undefined}
          >
            {item + 1}
          </button>
        ))}
      </div>
      <button className="secondary-button" type="button" disabled={last} onClick={() => setPage(page + 1)}>Next</button>
    </nav>
  );
}

function buildActiveChips(
  params: PublicPgSearchParams,
  amenities: Amenity[],
  removeParam: (key: string) => void,
  clearBudget: () => void,
  toggleAmenity: (amenityId: number, checked: boolean) => void
) {
  const chips: ReactNode[] = [];
  const push = (key: string, label: string) => {
    chips.push(
      <button className="filter-chip" type="button" key={key} onClick={() => removeParam(key)}>
        {label}
        <X size={14} />
      </button>
    );
  };

  if (params.search) push('search', params.search);
  if (params.city) push('city', params.city);
  if (params.area) push('area', params.area);
  if (params.minRent || params.maxRent) {
    chips.push(
      <button className="filter-chip" type="button" key="rent" onClick={clearBudget}>
        Rs {params.minRent ?? 0} - {params.maxRent ?? 'Any'}
        <X size={14} />
      </button>
    );
  }
  if (params.genderType) push('genderType', params.genderType.replaceAll('_', ' '));
  if (params.propertyType) push('propertyType', params.propertyType.replaceAll('_', ' '));
  if (params.sharingType) push('sharingType', params.sharingType.replaceAll('_', ' '));
  if (params.foodAvailable) push('foodAvailable', 'Food available');
  if (params.availableOnly) push('availableOnly', 'Available now');
  for (const amenityId of params.amenityIds ?? []) {
    const amenity = amenities.find((item) => item.id === amenityId);
    chips.push(
      <button className="filter-chip" type="button" key={`amenity-${amenityId}`} onClick={() => toggleAmenity(amenityId, false)}>
        {amenity?.name ?? `Amenity ${amenityId}`}
        <X size={14} />
      </button>
    );
  }
  return chips;
}

function readParams(params: URLSearchParams): PublicPgSearchParams {
  return {
    page: Number(params.get('page') ?? 0),
    size: Number(params.get('size') ?? 12),
    search: optional(params.get('search')),
    city: optional(params.get('city')),
    area: optional(params.get('area')),
    minRent: numberParam(params.get('minRent')),
    maxRent: numberParam(params.get('maxRent')),
    genderType: optional(params.get('genderType')) as GenderType | undefined,
    propertyType: optional(params.get('propertyType')) as PropertyType | undefined,
    sharingType: optional(params.get('sharingType')) as SharingType | undefined,
    foodAvailable: booleanParam(params.get('foodAvailable')),
    amenityIds: (params.get('amenityIds') ?? '').split(',').map((item) => Number(item)).filter(Boolean),
    availableOnly: booleanParam(params.get('availableOnly')) ?? false,
    sort: (optional(params.get('sort')) as PublicPgSearchParams['sort']) ?? 'latest'
  };
}

function optional(value: string | null) {
  return value && value.trim() ? value.trim() : undefined;
}

function numberParam(value: string | null) {
  if (!value) return undefined;
  const number = Number(value);
  return Number.isFinite(number) ? number : undefined;
}

function booleanParam(value: string | null) {
  if (value === 'true') return true;
  if (value === 'false') return false;
  return undefined;
}

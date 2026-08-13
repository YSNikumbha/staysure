import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Search } from 'lucide-react';
import { useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { publicApi, type PublicPgSearchParams } from '../api/public.api';
import { wishlistApi } from '../api/wishlist.api';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import { PublicPgCard } from '../components/PublicPgCard';
import type { GenderType, PropertyType, PublicPgCard as PublicPgCardType, SharingType } from '../types/property';
import { useAuthStore } from '../store/authStore';
import { getApiErrorMessage } from '../utils/apiError';
import { addCompareItem } from '../utils/compareStore';

const genderTypes: GenderType[] = ['MALE', 'FEMALE', 'COED'];
const propertyTypes: PropertyType[] = ['PG', 'HOSTEL', 'CO_LIVING', 'APARTMENT'];
const sharingTypes: SharingType[] = ['SINGLE', 'DOUBLE', 'TRIPLE', 'FOUR_SHARING', 'DORMITORY'];

export function FindPgPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const queryClient = useQueryClient();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const [compareMessage, setCompareMessage] = useState<string | null>(null);

  const params = useMemo(() => readParams(searchParams), [searchParams]);

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
      setCompareMessage(`${items.length} PG${items.length === 1 ? '' : 's'} selected for comparison.`);
    } catch (error) {
      setCompareMessage(error instanceof Error ? error.message : 'Unable to add PG to comparison.');
    }
  };

  const page = pgsQuery.data;

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Public discovery"
        title="Find PG"
        actions={<Link className="secondary-link" to="/compare">Compare</Link>}
      />

      <section className="surface discovery-layout">
        <aside className="filter-panel">
          <label>
            Search
            <div className="search-input">
              <Search size={16} />
              <input value={params.search ?? ''} onChange={(event) => setParam('search', event.target.value)} placeholder="City, area, or PG name" />
            </div>
          </label>
          <label>
            City
            <input value={params.city ?? ''} onChange={(event) => setParam('city', event.target.value)} />
          </label>
          <label>
            Area
            <input value={params.area ?? ''} onChange={(event) => setParam('area', event.target.value)} />
          </label>
          <div className="filter-pair">
            <label>
              Min Rent
              <input type="number" min="0" value={params.minRent ?? ''} onChange={(event) => setParam('minRent', event.target.value)} />
            </label>
            <label>
              Max Rent
              <input type="number" min="0" value={params.maxRent ?? ''} onChange={(event) => setParam('maxRent', event.target.value)} />
            </label>
          </div>
          <label>
            Gender
            <select value={params.genderType ?? ''} onChange={(event) => setParam('genderType', event.target.value)}>
              <option value="">Any</option>
              {genderTypes.map((item) => <option value={item} key={item}>{item.replaceAll('_', ' ')}</option>)}
            </select>
          </label>
          <label>
            Property Type
            <select value={params.propertyType ?? ''} onChange={(event) => setParam('propertyType', event.target.value)}>
              <option value="">Any</option>
              {propertyTypes.map((item) => <option value={item} key={item}>{item.replaceAll('_', ' ')}</option>)}
            </select>
          </label>
          <label>
            Sharing Type
            <select value={params.sharingType ?? ''} onChange={(event) => setParam('sharingType', event.target.value)}>
              <option value="">Any</option>
              {sharingTypes.map((item) => <option value={item} key={item}>{item.replaceAll('_', ' ')}</option>)}
            </select>
          </label>
          <label className="checkbox-field">
            <input type="checkbox" checked={params.foodAvailable === true} onChange={(event) => setParam('foodAvailable', event.target.checked ? true : null)} />
            Food Available
          </label>
          <label className="checkbox-field">
            <input type="checkbox" checked={params.availableOnly === true} onChange={(event) => setParam('availableOnly', event.target.checked ? true : null)} />
            Available Now
          </label>
          <div className="filter-group">
            <strong>Amenities</strong>
            {(amenitiesQuery.data ?? []).map((amenity) => (
              <label className="checkbox-field" key={amenity.id}>
                <input
                  type="checkbox"
                  checked={(params.amenityIds ?? []).includes(amenity.id)}
                  onChange={(event) => toggleAmenity(amenity.id, event.target.checked)}
                />
                {amenity.name}
              </label>
            ))}
          </div>
        </aside>

        <div className="results-panel">
          <div className="results-toolbar">
            <span>{page ? `${page.totalElements} PGs found` : 'Loading PGs'}</span>
            <label>
              Sort
              <select value={params.sort ?? 'latest'} onChange={(event) => setParam('sort', event.target.value)}>
                <option value="latest">Latest</option>
                <option value="price_low_to_high">Price low to high</option>
                <option value="price_high_to_low">Price high to low</option>
                <option value="availability">Availability</option>
              </select>
            </label>
          </div>
          <FormMessage message={compareMessage} tone="success" />
          {pgsQuery.isError ? <FormMessage message={getApiErrorMessage(pgsQuery.error, 'Unable to load PGs')} /> : null}
          {addWishlist.isError ? <FormMessage message={getApiErrorMessage(addWishlist.error, 'Unable to update wishlist')} /> : null}
          {removeWishlist.isError ? <FormMessage message={getApiErrorMessage(removeWishlist.error, 'Unable to update wishlist')} /> : null}

          {pgsQuery.isLoading ? <div className="route-state">Loading PGs</div> : null}
          <div className="pg-card-grid">
            {(page?.content ?? []).map((property) => (
              <PublicPgCard
                key={property.id}
                property={property}
                wishlisted={wishlistIds.has(property.id)}
                onToggleWishlist={toggleWishlist}
                onCompare={compare}
              />
            ))}
          </div>
          {!pgsQuery.isLoading && page?.content.length === 0 ? (
            <div className="empty-state">No PGs found for these filters. Try changing your location or budget.</div>
          ) : null}
          {page ? (
            <div className="pagination-row">
              <button className="secondary-button" type="button" disabled={page.first} onClick={() => setParam('page', page.page - 1)}>Previous</button>
              <span>Page {page.page + 1} of {Math.max(page.totalPages, 1)}</span>
              <button className="secondary-button" type="button" disabled={page.last} onClick={() => setParam('page', page.page + 1)}>Next</button>
            </div>
          ) : null}
        </div>
      </section>
    </div>
  );
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

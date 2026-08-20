import { useQueries } from '@tanstack/react-query';
import { BedDouble, MapPin, Scale, Trash2 } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { publicApi } from '../api/public.api';
import { EmptyState } from '../components/EmptyState';
import { StatusBadge } from '../components/StatusBadge';
import type { PublicPgDetails } from '../types/property';
import { getApiErrorMessage } from '../utils/apiError';
import { toAssetUrl } from '../utils/assets';
import { clearCompareItems, getCompareItems, removeCompareItem } from '../utils/compareStore';

export function ComparePage() {
  const [items, setItems] = useState(getCompareItems());
  const queries = useQueries({
    queries: items.map((item) => ({
      queryKey: ['public-pg', item.slug],
      queryFn: () => publicApi.pg(item.slug)
    }))
  });

  const properties = queries.map((query) => query.data).filter((property): property is PublicPgDetails => Boolean(property));
  const isLoading = queries.some((query) => query.isLoading);
  const error = queries.find((query) => query.isError)?.error;

  const remove = (id: number) => {
    setItems(removeCompareItem(id));
  };

  const clear = () => {
    clearCompareItems();
    setItems([]);
  };

  return (
    <div className="compare-page">
      <section className="compare-header">
        <div>
          <p className="eyebrow">PG comparison</p>
          <h1>Compare shortlisted PGs</h1>
          <p>Review rent, deposit, rooms, amenities and availability from verified public listings.</p>
        </div>
        <div className="hero-actions">
          <Link className="primary-link" to="/find-pg">Find PG</Link>
          {items.length ? <button className="danger-button" type="button" onClick={clear}>Clear Comparison</button> : null}
        </div>
      </section>

      {isLoading ? <div className="compare-loading">Loading comparison</div> : null}

      {error ? (
        <EmptyState
          title="Unable to load comparison."
          description={getApiErrorMessage(error, 'Please remove the unavailable PG or try again.')}
          action={<Link className="secondary-link" to="/find-pg">Back to Find PG</Link>}
        />
      ) : null}

      {items.length < 2 ? (
        <EmptyState
          title="Select at least two PGs to compare."
          description="You can compare up to three PGs at a time from search results, details or wishlist."
          action={<Link className="primary-link" to="/find-pg"><Scale size={17} /> Explore PGs</Link>}
        />
      ) : null}

      {properties.length > 0 ? (
        <>
          <section className="compare-card-grid">
            {properties.map((property) => (
              <article className="compare-summary-card" key={property.id}>
                {property.gallery[0]?.imageUrl ? (
                  <img src={toAssetUrl(property.gallery[0].imageUrl)} alt={property.name} />
                ) : (
                  <div className="property-image-fallback">StaySure</div>
                )}
                <div>
                  <StatusBadge status="VERIFIED" />
                  <h2>{property.name}</h2>
                  <p><MapPin size={15} /> {property.area}, {property.city}</p>
                  <strong>Rs {Number(property.startingRent).toLocaleString()} / month</strong>
                  <span><BedDouble size={15} /> {property.availableBedCount} beds available</span>
                </div>
                <div className="pg-card-actions">
                  <Link className="secondary-link compact-button" to={`/pg/${property.slug}`}>View Details</Link>
                  <button className="secondary-button compact-button" type="button" onClick={() => remove(property.id)}>
                    <Trash2 size={15} />
                    Remove
                  </button>
                </div>
              </article>
            ))}
          </section>

          <section className="compare-table-wrap compare-table-wrap--market">
            <table className="compare-table compare-table--market">
              <thead>
                <tr>
                  <th>Feature</th>
                  {properties.map((property) => (
                    <th key={property.id}>
                      <strong>{property.name}</strong>
                      <span>{property.area}, {property.city}</span>
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                <CompareRow label="Starting rent" values={properties.map((pg) => `Rs ${Number(pg.startingRent).toLocaleString()}`)} />
                <CompareRow label="Security deposit" values={properties.map((pg) => `Rs ${Number(pg.securityDeposit).toLocaleString()}`)} />
                <CompareRow label="Suitable for" values={properties.map((pg) => pg.genderType.replaceAll('_', ' '))} />
                <CompareRow label="Property type" values={properties.map((pg) => pg.propertyType.replaceAll('_', ' '))} />
                <CompareRow label="Food" values={properties.map((pg) => pg.foodAvailable ? 'Available' : 'Not available')} />
                <CompareRow label="Notice period" values={properties.map((pg) => `${pg.noticePeriodDays} days`)} />
                <CompareRow label="Lock-in" values={properties.map((pg) => `${pg.lockInMonths} months`)} />
                <CompareRow label="Available beds" values={properties.map((pg) => String(pg.availableBedCount))} />
                <CompareRow label="Room sharing" values={properties.map((pg) => [...new Set(pg.availableRooms.map((room) => room.sharingType.replaceAll('_', ' ')))].join(', ') || 'None')} />
                <CompareRow label="Amenities" values={properties.map((pg) => pg.amenities.map((amenity) => amenity.name).join(', ') || 'None')} />
                <CompareRow label="Entry time" values={properties.map((pg) => pg.entryTime ? pg.entryTime.slice(0, 5) : 'Not set')} />
                <tr>
                  <td>Verification</td>
                  {properties.map((pg) => <td key={pg.id}><StatusBadge status="VERIFIED" /></td>)}
                </tr>
              </tbody>
            </table>
          </section>
        </>
      ) : null}
    </div>
  );
}

function CompareRow({ label, values }: { label: string; values: string[] }) {
  return (
    <tr>
      <td>{label}</td>
      {values.map((value, index) => <td key={`${label}-${index}`}>{value}</td>)}
    </tr>
  );
}

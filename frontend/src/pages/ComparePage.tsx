import { useQueries } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { publicApi } from '../api/public.api';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { getApiErrorMessage } from '../utils/apiError';
import { clearCompareItems, getCompareItems, removeCompareItem } from '../utils/compareStore';
import { toAssetUrl } from '../utils/assets';
import { useState } from 'react';
import type { PublicPgDetails } from '../types/property';

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
    <div className="stack">
      <PageHeader
        eyebrow="PG comparison"
        title="Compare PGs"
        actions={
          <div className="action-row">
            <Link className="secondary-link" to="/find-pg">Find PG</Link>
            <button className="danger-button" type="button" onClick={clear}>Clear Comparison</button>
          </div>
        }
      />
      {isLoading ? <div className="route-state">Loading comparison</div> : null}
      {error ? <div className="route-state">{getApiErrorMessage(error, 'Unable to load comparison')}</div> : null}
      {items.length < 2 ? (
        <section className="surface empty-state">Select at least two PGs to compare.</section>
      ) : null}
      {properties.length > 0 ? (
        <section className="compare-table-wrap">
          <table className="compare-table">
            <thead>
              <tr>
                <th>Feature</th>
                {properties.map((property) => (
                  <th key={property.id}>
                    {property.gallery[0]?.imageUrl ? <img className="compare-image" src={toAssetUrl(property.gallery[0].imageUrl)} alt={property.name} /> : null}
                    <strong>{property.name}</strong>
                    <span>{property.area}, {property.city}</span>
                    <div className="action-row">
                      <Link className="secondary-link compact-button" to={`/pg/${property.slug}`}>View</Link>
                      <button className="secondary-button compact-button" type="button" onClick={() => remove(property.id)}>Remove</button>
                    </div>
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              <CompareRow label="Starting rent" values={properties.map((pg) => `Rs ${Number(pg.startingRent).toLocaleString()}`)} />
              <CompareRow label="Security deposit" values={properties.map((pg) => `Rs ${Number(pg.securityDeposit).toLocaleString()}`)} />
              <CompareRow label="Gender" values={properties.map((pg) => pg.genderType.replaceAll('_', ' '))} />
              <CompareRow label="Food" values={properties.map((pg) => pg.foodAvailable ? 'Available' : 'Not available')} />
              <CompareRow label="Notice period" values={properties.map((pg) => `${pg.noticePeriodDays} days`)} />
              <CompareRow label="Lock-in" values={properties.map((pg) => `${pg.lockInMonths} months`)} />
              <CompareRow label="Available beds" values={properties.map((pg) => String(pg.availableBedCount))} />
              <CompareRow label="Room sharing types" values={properties.map((pg) => [...new Set(pg.availableRooms.map((room) => room.sharingType.replaceAll('_', ' ')))].join(', ') || 'None')} />
              <CompareRow label="Amenities" values={properties.map((pg) => pg.amenities.map((amenity) => amenity.name).join(', ') || 'None')} />
              <CompareRow label="Entry time" values={properties.map((pg) => pg.entryTime ? pg.entryTime.slice(0, 5) : 'Not set')} />
              <tr>
                <td>Verification</td>
                {properties.map((pg) => <td key={pg.id}><StatusBadge status="VERIFIED" /></td>)}
              </tr>
            </tbody>
          </table>
        </section>
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

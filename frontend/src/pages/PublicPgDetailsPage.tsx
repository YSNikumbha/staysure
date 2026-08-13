import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Heart, Scale } from 'lucide-react';
import { useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { publicApi } from '../api/public.api';
import { wishlistApi } from '../api/wishlist.api';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { useAuthStore } from '../store/authStore';
import { getApiErrorMessage } from '../utils/apiError';
import { toAssetUrl } from '../utils/assets';
import { addCompareItem } from '../utils/compareStore';

export function PublicPgDetailsPage() {
  const { slug } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const [message, setMessage] = useState<string | null>(null);

  const pgQuery = useQuery({
    queryKey: ['public-pg', slug],
    queryFn: () => publicApi.pg(slug!),
    enabled: Boolean(slug)
  });

  const wishlistQuery = useQuery({
    queryKey: ['wishlist'],
    queryFn: wishlistApi.list,
    enabled: isAuthenticated
  });

  const addWishlist = useMutation({
    mutationFn: wishlistApi.add,
    onSuccess: async () => queryClient.invalidateQueries({ queryKey: ['wishlist'] }),
    onError: (error) => setMessage(getApiErrorMessage(error, 'Unable to save PG'))
  });

  const removeWishlist = useMutation({
    mutationFn: wishlistApi.remove,
    onSuccess: async () => queryClient.invalidateQueries({ queryKey: ['wishlist'] }),
    onError: (error) => setMessage(getApiErrorMessage(error, 'Unable to update wishlist'))
  });

  if (pgQuery.isLoading) {
    return <div className="route-state">Loading PG</div>;
  }

  if (pgQuery.isError || !pgQuery.data) {
    return <div className="route-state">{getApiErrorMessage(pgQuery.error, 'Unable to load PG')}</div>;
  }

  const pg = pgQuery.data;
  const wishlisted = (wishlistQuery.data ?? []).some((item) => item.property.id === pg.id);

  const toggleWishlist = () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    if (wishlisted) {
      removeWishlist.mutate(pg.id);
    } else {
      addWishlist.mutate(pg.id);
    }
  };

  const compare = () => {
    try {
      const items = addCompareItem({
        id: pg.id,
        slug: pg.slug,
        name: pg.name,
        coverImage: pg.gallery[0]?.imageUrl,
        area: pg.area,
        city: pg.city,
        genderType: pg.genderType,
        propertyType: pg.propertyType,
        startingRent: pg.startingRent,
        securityDeposit: pg.securityDeposit,
        foodAvailable: pg.foodAvailable,
        totalBeds: pg.totalBedCount,
        availableBeds: pg.availableBedCount,
        verificationStatus: 'VERIFIED',
        amenities: pg.amenities
      });
      setMessage(`${items.length} PG${items.length === 1 ? '' : 's'} selected for comparison.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'Unable to add PG to comparison.');
    }
  };

  return (
    <div className="stack">
      <PageHeader
        eyebrow="PG details"
        title={pg.name}
        actions={
          <div className="action-row">
            <Link className="secondary-link" to="/find-pg">Back</Link>
            <button className={`secondary-button ${wishlisted ? 'button-active' : ''}`} type="button" onClick={toggleWishlist}>
              <Heart size={17} fill={wishlisted ? 'currentColor' : 'none'} />
              Favourite
            </button>
            <button className="secondary-button" type="button" onClick={compare}>
              <Scale size={17} />
              Compare
            </button>
          </div>
        }
      />
      <FormMessage message={message} tone={message?.startsWith('Unable') ? 'error' : 'success'} />

      <section className="public-gallery">
        {(pg.gallery.length ? pg.gallery : [{ id: 0, imageUrl: '', category: 'OTHER', propertyId: pg.id, coverImage: false, sortOrder: 0, createdAt: '' }]).map((image) => (
          image.imageUrl ? (
            <img key={image.id} src={toAssetUrl(image.imageUrl)} alt={image.category.replaceAll('_', ' ')} />
          ) : (
            <div className="public-gallery-empty" key="empty">StaySure</div>
          )
        ))}
      </section>

      <section className="surface status-surface">
        <div>
          <h2>{pg.area}, {pg.city}</h2>
          <p>{pg.addressLine1}{pg.addressLine2 ? `, ${pg.addressLine2}` : ''}, {pg.state} {pg.pincode}</p>
        </div>
        <div className="badge-row">
          <StatusBadge status="VERIFIED" />
          <StatusBadge status={pg.genderType} />
          <StatusBadge status={pg.propertyType} />
        </div>
      </section>

      <section className="surface detail-grid">
        <div>
          <span>Starting rent</span>
          <strong>Rs {Number(pg.startingRent).toLocaleString()}</strong>
        </div>
        <div>
          <span>Security deposit</span>
          <strong>Rs {Number(pg.securityDeposit).toLocaleString()}</strong>
        </div>
        <div>
          <span>Available beds</span>
          <strong>{pg.availableBedCount} / {pg.totalBedCount}</strong>
        </div>
        <div>
          <span>Food</span>
          <strong>{pg.foodAvailable ? 'Available' : 'Not available'}</strong>
        </div>
      </section>

      <section className="surface">
        <h2>Overview</h2>
        <p className="muted-copy">{pg.description || 'No description provided.'}</p>
      </section>

      <section className="surface">
        <h2>Available Rooms</h2>
        <div className="room-availability-grid">
          {pg.availableRooms.map((room) => (
            <article className="availability-card" key={room.roomId}>
              <div>
                <strong>{room.sharingType.replaceAll('_', ' ')}</strong>
                <span>Room {room.roomNumber}</span>
              </div>
              <div>
                <span>Rent</span>
                <strong>Rs {Number(room.monthlyRent).toLocaleString()}/month</strong>
              </div>
              <div>
                <span>Available beds</span>
                <strong>{room.availableBeds}</strong>
              </div>
              <div className="badge-row">
                {room.acAvailable ? <StatusBadge status="AC" /> : null}
                {room.attachedBathroom ? <StatusBadge status="BATHROOM" /> : null}
                <StatusBadge status={room.furnishingType} />
              </div>
            </article>
          ))}
          {pg.availableRooms.length === 0 ? <p>No beds are currently available.</p> : null}
        </div>
      </section>

      <section className="surface">
        <h2>Amenities</h2>
        <div className="amenity-grid">
          {pg.amenities.map((amenity) => <span className="amenity-pill" key={amenity.id}>{amenity.name}</span>)}
          {pg.amenities.length === 0 ? <p>No amenities listed.</p> : null}
        </div>
      </section>

      <section className="surface rule-grid">
        <div>
          <span>Visitors</span>
          <strong>{pg.rules?.visitorAllowed ? 'Allowed' : 'Not allowed'}</strong>
        </div>
        <div>
          <span>Gate closing</span>
          <strong>{pg.rules?.gateClosingTime ? pg.rules.gateClosingTime.slice(0, 5) : 'Not set'}</strong>
        </div>
        <div>
          <span>Notice period</span>
          <strong>{pg.noticePeriodDays} days</strong>
        </div>
        <div>
          <span>Lock-in</span>
          <strong>{pg.lockInMonths} months</strong>
        </div>
        <div>
          <span>Entry time</span>
          <strong>{pg.entryTime ? pg.entryTime.slice(0, 5) : 'Not set'}</strong>
        </div>
        <div>
          <span>Owner</span>
          <strong>{pg.owner?.businessName ?? 'StaySure owner'}</strong>
        </div>
      </section>

      <section className="surface">
        <h2>Location</h2>
        <p className="muted-copy">{pg.area}, {pg.city}, {pg.state}</p>
      </section>
    </div>
  );
}

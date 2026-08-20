import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Bath,
  BedDouble,
  Building2,
  CheckCircle2,
  ChevronLeft,
  Heart,
  Home,
  MapPin,
  Scale,
  Utensils,
  X
} from 'lucide-react';
import { useState } from 'react';
import type { ReactNode } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { publicApi } from '../api/public.api';
import { wishlistApi } from '../api/wishlist.api';
import { BookingModal } from '../components/BookingModal';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { StatusBadge } from '../components/StatusBadge';
import { useAuthStore } from '../store/authStore';
import type { PublicPgDetails, PublicRoomAvailability } from '../types/property';
import { getApiErrorMessage } from '../utils/apiError';
import { toAssetUrl } from '../utils/assets';
import { addCompareItem, getCompareItems } from '../utils/compareStore';

export function PublicPgDetailsPage() {
  const { slug } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const [message, setMessage] = useState<string | null>(null);
  const [bookingOpen, setBookingOpen] = useState(false);
  const [initialRoomId, setInitialRoomId] = useState<number | undefined>();
  const [initialBedId, setInitialBedId] = useState<number | undefined>();
  const [photoIndex, setPhotoIndex] = useState<number | null>(null);
  const [compareItems, setCompareItems] = useState(() => getCompareItems());

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
    return <DetailsSkeleton />;
  }

  if (pgQuery.isError || !pgQuery.data) {
    return (
      <EmptyState
        title="We couldn't load this PG."
        description={getApiErrorMessage(pgQuery.error, 'The listing may be unavailable or no longer public.')}
        action={<Link className="secondary-link" to="/find-pg">Back to Find PG</Link>}
      />
    );
  }

  const pg = pgQuery.data;
  const wishlisted = (wishlistQuery.data ?? []).some((item) => item.property.id === pg.id);
  const compared = compareItems.some((item) => item.id === pg.id);
  const gallery = pg.gallery.filter((image) => image.imageUrl);
  const firstAvailableRoom = pg.availableRooms.find((room) => room.beds.length > 0);
  const hasAvailability = Boolean(firstAvailableRoom);

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
        coverImage: gallery[0]?.imageUrl,
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
      setCompareItems(items);
      setMessage(`${items.length} PG${items.length === 1 ? '' : 's'} selected for comparison.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'Unable to add PG to comparison.');
    }
  };

  const openBooking = (room?: PublicRoomAvailability, bedId?: number) => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    setInitialRoomId(room?.roomId ?? firstAvailableRoom?.roomId);
    setInitialBedId(bedId ?? room?.beds[0]?.id ?? firstAvailableRoom?.beds[0]?.id);
    setBookingOpen(true);
  };

  return (
    <div className="pg-detail-page">
      <nav className="breadcrumb" aria-label="Breadcrumb">
        <Link to="/">Home</Link>
        <span>/</span>
        <Link to={`/find-pg?city=${encodeURIComponent(pg.city)}`}>{pg.city}</Link>
        <span>/</span>
        <Link to={`/find-pg?area=${encodeURIComponent(pg.area)}`}>{pg.area}</Link>
        <span>/</span>
        <span>{pg.name}</span>
      </nav>

      <header className="pg-detail-header">
        <div>
          <div className="badge-row">
            <span className="verified-chip"><CheckCircle2 size={15} /> Verified</span>
            <StatusBadge status={pg.genderType} />
            <StatusBadge status={pg.propertyType} />
          </div>
          <h1>{pg.name}</h1>
          <p><MapPin size={17} /> {pg.area}, {pg.city}</p>
        </div>
        <div className="pg-detail-actions">
          <button className={`secondary-button ${wishlisted ? 'button-active' : ''}`} type="button" onClick={toggleWishlist}>
            <Heart size={17} fill={wishlisted ? 'currentColor' : 'none'} />
            {wishlisted ? 'Saved' : 'Wishlist'}
          </button>
          <button className={`secondary-button ${compared ? 'button-active' : ''}`} type="button" onClick={compare}>
            <Scale size={17} />
            {compared ? 'Added to Compare' : 'Compare'}
          </button>
        </div>
      </header>

      <FormMessage message={message} tone={message?.startsWith('Unable') ? 'error' : 'success'} />

      <section className="detail-gallery" aria-label="Property photos">
        {gallery.length ? (
          gallery.slice(0, 5).map((image, index) => (
            <button className="gallery-tile" type="button" key={image.id} onClick={() => setPhotoIndex(index)}>
              <img src={toAssetUrl(image.imageUrl)} alt={image.category.replaceAll('_', ' ')} />
              {index === 4 && gallery.length > 5 ? <span>View all photos</span> : null}
            </button>
          ))
        ) : (
          <div className="detail-gallery-empty">StaySure</div>
        )}
        {gallery.length > 0 ? (
          <button className="secondary-button gallery-view-button" type="button" onClick={() => setPhotoIndex(0)}>
            View All Photos
          </button>
        ) : null}
      </section>

      <section className="detail-facts">
        <Fact label="Starting Rent" value={`Rs ${Number(pg.startingRent).toLocaleString()}`} />
        <Fact label="Security Deposit" value={`Rs ${Number(pg.securityDeposit).toLocaleString()}`} />
        <Fact label="Available Beds" value={`${pg.availableBedCount} / ${pg.totalBedCount}`} />
        <Fact label="Food" value={pg.foodAvailable ? 'Available' : 'Not available'} icon={<Utensils size={18} />} />
      </section>

      <div className="detail-content-layout">
        <main className="detail-main">
          {pg.description ? (
            <section className="surface detail-section">
              <h2>About this PG</h2>
              <p>{pg.description}</p>
            </section>
          ) : null}

          <section className="surface detail-section">
            <h2>Amenities</h2>
            {pg.amenities.length ? (
              <div className="amenity-showcase">
                {pg.amenities.map((amenity) => <span className="amenity-pill" key={amenity.id}>{amenity.name}</span>)}
              </div>
            ) : (
              <p className="muted-copy">No amenities listed.</p>
            )}
          </section>

          <section className="surface detail-section">
            <div className="section-heading">
              <div>
                <h2>Available Rooms</h2>
                <p className="muted-copy">Only available beds returned by the public API are selectable.</p>
              </div>
            </div>
            {pg.availableRooms.length ? (
              <div className="room-list">
                {pg.availableRooms.map((room) => (
                  <RoomAvailabilityCard key={room.roomId} room={room} onBook={openBooking} />
                ))}
              </div>
            ) : (
              <div className="empty-state">No beds are currently available. Booking requests are disabled until a bed opens.</div>
            )}
          </section>

          <section className="surface detail-section">
            <h2>House Rules</h2>
            <div className="rules-showcase">
              <Rule label="Visitors" value={pg.rules?.visitorAllowed ? 'Allowed' : 'Not allowed'} />
              <Rule label="Smoking" value={pg.rules?.smokingAllowed ? 'Allowed' : 'Not allowed'} />
              <Rule label="Alcohol" value={pg.rules?.alcoholAllowed ? 'Allowed' : 'Not allowed'} />
              <Rule label="Cooking" value={pg.rules?.cookingAllowed ? 'Allowed' : 'Not allowed'} />
              <Rule label="Late entry" value={pg.rules?.lateEntryAllowed ? 'Allowed' : 'Not allowed'} />
              <Rule label="Gate closing" value={pg.rules?.gateClosingTime ? pg.rules.gateClosingTime.slice(0, 5) : 'Not set'} />
              <Rule label="Notice period" value={`${pg.noticePeriodDays} days`} />
              <Rule label="Lock-in" value={`${pg.lockInMonths} months`} />
              <Rule label="Entry time" value={pg.entryTime ? pg.entryTime.slice(0, 5) : 'Not set'} />
            </div>
            {pg.rules?.additionalRules ? <p className="muted-copy">{pg.rules.additionalRules}</p> : null}
          </section>

          <section className="surface detail-section">
            <h2>Location</h2>
            <div className="location-card">
              <MapPin size={20} />
              <p>
                {pg.addressLine1}{pg.addressLine2 ? `, ${pg.addressLine2}` : ''}, {pg.area}, {pg.city}, {pg.state} {pg.pincode}
              </p>
            </div>
          </section>
        </main>

        <aside className="booking-summary-card">
          <span className="verified-chip"><CheckCircle2 size={15} /> Verified listing</span>
          <div>
            <span>Starting from</span>
            <strong>Rs {Number(pg.startingRent).toLocaleString()}<small>/month</small></strong>
          </div>
          <div className="booking-summary-row">
            <span>Security Deposit</span>
            <strong>Rs {Number(pg.securityDeposit).toLocaleString()}</strong>
          </div>
          <div className="booking-summary-row">
            <span>Available Beds</span>
            <strong>{pg.availableBedCount}</strong>
          </div>
          <button className="primary-button" type="button" disabled={!hasAvailability} onClick={() => openBooking()}>
            Request Booking
          </button>
          <p>Booking request is reviewed by the PG owner.</p>
        </aside>
      </div>

      {photoIndex !== null ? (
        <GalleryModal
          pg={pg}
          index={photoIndex}
          setIndex={setPhotoIndex}
          onClose={() => setPhotoIndex(null)}
        />
      ) : null}

      {bookingOpen ? (
        <BookingModal
          pg={pg}
          initialRoomId={initialRoomId}
          initialBedId={initialBedId}
          onClose={() => setBookingOpen(false)}
          onSuccess={(bookingId) => {
            setBookingOpen(false);
            setMessage('Booking request submitted.');
            navigate(`/bookings/${bookingId}`);
          }}
        />
      ) : null}
    </div>
  );
}

function Fact({ label, value, icon }: { label: string; value: string; icon?: ReactNode }) {
  return (
    <article>
      <span>{icon}{label}</span>
      <strong>{value}</strong>
    </article>
  );
}

function RoomAvailabilityCard({ room, onBook }: { room: PublicRoomAvailability; onBook: (room: PublicRoomAvailability, bedId?: number) => void }) {
  return (
    <article className="room-card">
      <div className="room-card__header">
        <div>
          <h3>{room.sharingType.replaceAll('_', ' ')}</h3>
          <p>Room {room.roomNumber}</p>
        </div>
        <span className="availability-pill"><BedDouble size={15} /> {room.availableBeds} beds available</span>
      </div>
      <div className="room-card__facts">
        <Fact label="Monthly Rent" value={`Rs ${Number(room.monthlyRent).toLocaleString()}`} />
        <Fact label="Deposit" value={`Rs ${Number(room.securityDeposit).toLocaleString()}`} />
        <Fact label="Capacity" value={String(room.capacity)} />
      </div>
      <div className="room-card__features">
        {room.acAvailable ? <span><Home size={14} /> AC</span> : null}
        {room.attachedBathroom ? <span><Bath size={14} /> Attached bathroom</span> : null}
        <span><Building2 size={14} /> {room.furnishingType.replaceAll('_', ' ')}</span>
      </div>
      <div className="bed-selection-preview">
        {room.beds.map((bed) => (
          <button className="bed-select-chip" type="button" key={bed.id} onClick={() => onBook(room, bed.id)}>
            {bed.bedLabel || `Bed ${bed.bedNumber}`}
          </button>
        ))}
      </div>
      <button className="secondary-button" type="button" disabled={room.beds.length === 0} onClick={() => onBook(room)}>
        Select Room
      </button>
    </article>
  );
}

function Rule({ label, value }: { label: string; value: string }) {
  return (
    <article>
      <span>{label}</span>
      <strong>{value}</strong>
    </article>
  );
}

function GalleryModal({
  pg,
  index,
  setIndex,
  onClose
}: {
  pg: PublicPgDetails;
  index: number;
  setIndex: (index: number) => void;
  onClose: () => void;
}) {
  const gallery = pg.gallery.filter((image) => image.imageUrl);
  const image = gallery[index];
  if (!image) return null;

  return (
    <div className="modal-backdrop gallery-modal-backdrop" role="presentation">
      <div className="gallery-modal" role="dialog" aria-modal="true" aria-label={`${pg.name} photos`}>
        <div className="gallery-modal__header">
          <button className="icon-button" type="button" onClick={() => setIndex(Math.max(index - 1, 0))} disabled={index === 0} aria-label="Previous photo">
            <ChevronLeft size={18} />
          </button>
          <strong>{index + 1} of {gallery.length}</strong>
          <button className="icon-button" type="button" onClick={onClose} aria-label="Close gallery">
            <X size={18} />
          </button>
        </div>
        <img src={toAssetUrl(image.imageUrl)} alt={image.category.replaceAll('_', ' ')} />
        <div className="gallery-modal__thumbs">
          {gallery.map((item, itemIndex) => (
            <button
              className={itemIndex === index ? 'gallery-thumb gallery-thumb--active' : 'gallery-thumb'}
              type="button"
              key={item.id}
              onClick={() => setIndex(itemIndex)}
              aria-label={`Open photo ${itemIndex + 1}`}
            >
              <img src={toAssetUrl(item.imageUrl)} alt="" />
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

function DetailsSkeleton() {
  return (
    <div className="pg-detail-page">
      <div className="detail-skeleton detail-skeleton--title" />
      <div className="detail-skeleton detail-skeleton--gallery" />
      <div className="detail-skeleton detail-skeleton--body" />
    </div>
  );
}

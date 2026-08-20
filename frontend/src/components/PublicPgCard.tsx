import { BedDouble, CheckCircle2, Heart, MapPin, Scale, Utensils } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';
import type { PublicPgCard as PublicPgCardType } from '../types/property';
import { toAssetUrl } from '../utils/assets';

type PublicPgCardProps = {
  property: PublicPgCardType;
  wishlisted?: boolean;
  compared?: boolean;
  onToggleWishlist?: (property: PublicPgCardType) => void;
  onCompare?: (property: PublicPgCardType) => void;
};

export function PublicPgCard({
  property,
  wishlisted = false,
  compared = false,
  onToggleWishlist,
  onCompare
}: PublicPgCardProps) {
  const navigate = useNavigate();
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);

  const handleWishlist = () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    onToggleWishlist?.(property);
  };

  return (
    <article className="pg-card pg-card--market">
      <Link className="pg-card-media" to={`/pg/${property.slug}`} aria-label={`View ${property.name}`}>
        {property.coverImage ? (
          <img src={toAssetUrl(property.coverImage)} alt={property.name} loading="lazy" />
        ) : (
          <div className="pg-card-image--empty">StaySure</div>
        )}
        {property.verificationStatus === 'VERIFIED' ? (
          <span className="verified-chip"><CheckCircle2 size={15} /> Verified</span>
        ) : null}
      </Link>

      <div className="pg-card-body pg-card-body--market">
        <div className="pg-card-main">
          <div className="pg-card-heading">
            <div>
              <Link to={`/pg/${property.slug}`} className="pg-card-name">{property.name}</Link>
              <p><MapPin size={15} /> {property.area}, {property.city}</p>
            </div>
            <button
              className={`icon-button wishlist-button ${wishlisted ? 'icon-button--active' : ''}`}
              type="button"
              title={wishlisted ? 'Remove from wishlist' : 'Add to wishlist'}
              aria-label={wishlisted ? `Remove ${property.name} from wishlist` : `Add ${property.name} to wishlist`}
              onClick={handleWishlist}
            >
              <Heart size={18} fill={wishlisted ? 'currentColor' : 'none'} />
            </button>
          </div>

          <div className="pg-card-price pg-card-price--market">
            <span>Starting from</span>
            <strong>Rs {Number(property.startingRent).toLocaleString()}<small>/month</small></strong>
            <em>Security Rs {Number(property.securityDeposit).toLocaleString()}</em>
          </div>

          <div className="pg-card-facts pg-card-facts--market">
            <span>{property.genderType.replaceAll('_', ' ')}</span>
            <span>{property.propertyType.replaceAll('_', ' ')}</span>
            {property.foodAvailable ? <span><Utensils size={14} /> Food</span> : null}
          </div>

          <div className="pg-card-amenities">
            {property.amenities.slice(0, 4).map((amenity) => <span key={amenity.id}>{amenity.name}</span>)}
          </div>
        </div>

        <div className="pg-card-side">
          <span className={property.availableBeds > 0 ? 'availability-pill' : 'availability-pill availability-pill--empty'}>
            <BedDouble size={15} />
            {property.availableBeds > 0 ? `${property.availableBeds} beds available` : 'No beds available'}
          </span>
          <div className="pg-card-actions">
            <button
              className={`secondary-button compact-button ${compared ? 'button-active' : ''}`}
              type="button"
              onClick={() => onCompare?.(property)}
            >
              <Scale size={16} />
              {compared ? 'Added' : 'Compare'}
            </button>
            <Link className="primary-link compact-button" to={`/pg/${property.slug}`}>View Details</Link>
          </div>
        </div>
      </div>
    </article>
  );
}

export function PublicPgCardSkeleton() {
  return (
    <article className="pg-card pg-card--market pg-card--skeleton" aria-hidden="true">
      <span className="pg-card-media" />
      <div className="pg-card-body pg-card-body--market">
        <div className="pg-card-main">
          <span />
          <span />
          <span />
          <span />
        </div>
        <div className="pg-card-side">
          <span />
          <span />
        </div>
      </div>
    </article>
  );
}

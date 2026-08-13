import { Heart, Scale, Utensils } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import type { PublicPgCard as PublicPgCardType } from '../types/property';
import { useAuthStore } from '../store/authStore';
import { toAssetUrl } from '../utils/assets';

type PublicPgCardProps = {
  property: PublicPgCardType;
  wishlisted?: boolean;
  onToggleWishlist?: (property: PublicPgCardType) => void;
  onCompare?: (property: PublicPgCardType) => void;
};

export function PublicPgCard({ property, wishlisted = false, onToggleWishlist, onCompare }: PublicPgCardProps) {
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
    <article className="pg-card">
      {property.coverImage ? (
        <img className="pg-card-image" src={toAssetUrl(property.coverImage)} alt={property.name} />
      ) : (
        <div className="pg-card-image pg-card-image--empty">StaySure</div>
      )}
      <div className="pg-card-body">
        <div>
          <div className="pg-card-title-row">
            <h2>{property.name}</h2>
            <span className="status-badge status-badge--success">VERIFIED</span>
          </div>
          <p>{property.area}, {property.city}</p>
        </div>
        <div className="pg-card-facts">
          <span>{property.genderType.replaceAll('_', ' ')}</span>
          <span>{property.propertyType.replaceAll('_', ' ')}</span>
          {property.foodAvailable ? <span><Utensils size={14} /> Food</span> : null}
        </div>
        <div className="pg-card-price">
          <strong>Rs {Number(property.startingRent).toLocaleString()}</strong>
          <span>Security Rs {Number(property.securityDeposit).toLocaleString()}</span>
        </div>
        <div className="pg-card-amenities">
          {property.amenities.slice(0, 4).map((amenity) => <span key={amenity.id}>{amenity.name}</span>)}
        </div>
        <div className="pg-card-footer">
          <span>{property.availableBeds} available beds</span>
          <div className="action-row">
            <button className={`icon-button ${wishlisted ? 'icon-button--active' : ''}`} type="button" title="Favourite" aria-label="Favourite PG" onClick={handleWishlist}>
              <Heart size={16} fill={wishlisted ? 'currentColor' : 'none'} />
            </button>
            <button className="icon-button" type="button" title="Compare" aria-label="Compare PG" onClick={() => onCompare?.(property)}>
              <Scale size={16} />
            </button>
            <Link className="secondary-link compact-button" to={`/pg/${property.slug}`}>View Details</Link>
          </div>
        </div>
      </div>
    </article>
  );
}

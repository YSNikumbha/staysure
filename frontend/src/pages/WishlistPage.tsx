import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Heart } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { wishlistApi } from '../api/wishlist.api';
import { EmptyState } from '../components/EmptyState';
import { FormMessage } from '../components/FormMessage';
import { PublicPgCard, PublicPgCardSkeleton } from '../components/PublicPgCard';
import type { PublicPgCard as PublicPgCardType } from '../types/property';
import { getApiErrorMessage } from '../utils/apiError';
import { addCompareItem, getCompareItems } from '../utils/compareStore';

export function WishlistPage() {
  const queryClient = useQueryClient();
  const [message, setMessage] = useState<string | null>(null);
  const [compareItems, setCompareItems] = useState(() => getCompareItems());

  const wishlistQuery = useQuery({
    queryKey: ['wishlist'],
    queryFn: wishlistApi.list
  });

  const removeWishlist = useMutation({
    mutationFn: wishlistApi.remove,
    onSuccess: async () => {
      setMessage('PG removed from wishlist.');
      await queryClient.invalidateQueries({ queryKey: ['wishlist'] });
    },
    onError: (error) => setMessage(getApiErrorMessage(error, 'Unable to remove PG'))
  });

  const compare = (property: PublicPgCardType) => {
    try {
      const items = addCompareItem(property);
      setCompareItems(items);
      setMessage(`${items.length} PG${items.length === 1 ? '' : 's'} selected for comparison.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'Unable to add PG to comparison.');
    }
  };

  const items = wishlistQuery.data ?? [];
  const comparedIds = new Set(compareItems.map((item) => item.id));

  return (
    <div className="wishlist-page">
      <section className="wishlist-header">
        <div>
          <p className="eyebrow">Saved PGs</p>
          <h1>Your Wishlist</h1>
          <p>Keep track of verified PGs you want to revisit, compare or book later.</p>
        </div>
        <Link className="primary-link" to="/find-pg">Explore PGs</Link>
      </section>

      <FormMessage message={message} tone={message?.startsWith('Unable') ? 'error' : 'success'} />

      {wishlistQuery.isLoading ? (
        <div className="pg-card-list">
          <PublicPgCardSkeleton />
          <PublicPgCardSkeleton />
        </div>
      ) : null}

      {wishlistQuery.isError ? (
        <EmptyState
          title="Unable to load your wishlist."
          description={getApiErrorMessage(wishlistQuery.error, 'Please try again.')}
          action={<button className="secondary-button" type="button" onClick={() => void wishlistQuery.refetch()}>Try Again</button>}
        />
      ) : null}

      {!wishlistQuery.isLoading && !wishlistQuery.isError && items.length === 0 ? (
        <EmptyState
          title="No saved PGs yet."
          description="Use the heart icon on a property card to save PGs here."
          action={<Link className="primary-link" to="/find-pg"><Heart size={17} /> Find PGs</Link>}
        />
      ) : null}

      {items.length > 0 ? (
        <div className="pg-card-list">
          {items.map((item) => (
            <PublicPgCard
              key={item.id}
              property={item.property}
              wishlisted
              compared={comparedIds.has(item.property.id)}
              onToggleWishlist={(property) => removeWishlist.mutate(property.id)}
              onCompare={compare}
            />
          ))}
        </div>
      ) : null}
    </div>
  );
}

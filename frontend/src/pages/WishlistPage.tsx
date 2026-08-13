import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { wishlistApi } from '../api/wishlist.api';
import { FormMessage } from '../components/FormMessage';
import { PageHeader } from '../components/PageHeader';
import { PublicPgCard } from '../components/PublicPgCard';
import type { PublicPgCard as PublicPgCardType } from '../types/property';
import { getApiErrorMessage } from '../utils/apiError';
import { addCompareItem } from '../utils/compareStore';
import { useState } from 'react';

export function WishlistPage() {
  const queryClient = useQueryClient();
  const [message, setMessage] = useState<string | null>(null);

  const wishlistQuery = useQuery({
    queryKey: ['wishlist'],
    queryFn: wishlistApi.list
  });

  const removeWishlist = useMutation({
    mutationFn: wishlistApi.remove,
    onSuccess: async () => queryClient.invalidateQueries({ queryKey: ['wishlist'] }),
    onError: (error) => setMessage(getApiErrorMessage(error, 'Unable to remove PG'))
  });

  const compare = (property: PublicPgCardType) => {
    try {
      const items = addCompareItem(property);
      setMessage(`${items.length} PG${items.length === 1 ? '' : 's'} selected for comparison.`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'Unable to add PG to comparison.');
    }
  };

  return (
    <div className="stack">
      <PageHeader
        eyebrow="Saved PGs"
        title="Wishlist"
        actions={<Link className="secondary-link" to="/find-pg">Find PG</Link>}
      />
      <FormMessage message={message} tone={message?.startsWith('Unable') ? 'error' : 'success'} />
      {wishlistQuery.isLoading ? <div className="route-state">Loading wishlist</div> : null}
      {wishlistQuery.isError ? <div className="route-state">{getApiErrorMessage(wishlistQuery.error, 'Unable to load wishlist')}</div> : null}
      <div className="pg-card-grid">
        {(wishlistQuery.data ?? []).map((item) => (
          <PublicPgCard
            key={item.id}
            property={item.property}
            wishlisted
            onToggleWishlist={(property) => removeWishlist.mutate(property.id)}
            onCompare={compare}
          />
        ))}
      </div>
      {!wishlistQuery.isLoading && (wishlistQuery.data ?? []).length === 0 ? (
        <section className="surface empty-state">You haven't saved any PGs yet.</section>
      ) : null}
    </div>
  );
}

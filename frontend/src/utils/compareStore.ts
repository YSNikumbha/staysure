import type { PublicPgCard } from '../types/property';

const KEY = 'staysure_compare_pgs';
const MAX_COMPARE = 3;

export type CompareItem = Pick<PublicPgCard, 'id' | 'slug' | 'name' | 'coverImage' | 'area' | 'city'>;

export function getCompareItems(): CompareItem[] {
  try {
    const raw = window.localStorage.getItem(KEY);
    return raw ? JSON.parse(raw) as CompareItem[] : [];
  } catch {
    return [];
  }
}

export function addCompareItem(property: PublicPgCard) {
  const current = getCompareItems();
  if (current.some((item) => item.id === property.id)) {
    return current;
  }
  if (current.length >= MAX_COMPARE) {
    throw new Error('Compare up to 3 PGs at a time.');
  }
  const next = [...current, {
    id: property.id,
    slug: property.slug,
    name: property.name,
    coverImage: property.coverImage,
    area: property.area,
    city: property.city
  }];
  window.localStorage.setItem(KEY, JSON.stringify(next));
  return next;
}

export function removeCompareItem(propertyId: number) {
  const next = getCompareItems().filter((item) => item.id !== propertyId);
  window.localStorage.setItem(KEY, JSON.stringify(next));
  return next;
}

export function clearCompareItems() {
  window.localStorage.removeItem(KEY);
}

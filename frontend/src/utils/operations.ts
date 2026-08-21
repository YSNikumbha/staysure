import type { MealType } from '../types/operations';

export function label(value: string) {
  return value.replaceAll('_', ' ');
}

export function formatDate(value?: string | null) {
  return value ? new Date(value).toLocaleDateString('en-IN') : '-';
}

export function formatDateTime(value?: string | null) {
  return value ? new Date(value).toLocaleString('en-IN') : '-';
}

export function todayIso() {
  return new Date().toISOString().slice(0, 10);
}

export const mealOrder: MealType[] = ['BREAKFAST', 'LUNCH', 'SNACKS', 'DINNER'];

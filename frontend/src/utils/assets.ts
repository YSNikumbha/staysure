const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1';
const assetBaseUrl = apiBaseUrl.replace(/\/api\/v1\/?$/, '');

export function toAssetUrl(path: string) {
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  return `${assetBaseUrl}${path.startsWith('/') ? path : `/${path}`}`;
}

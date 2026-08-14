export function isValidUrl(value: string): boolean {
  if (!value || value.trim().length === 0) {
    return false;
  }
  try {
    const url = new URL(value);
    return url.protocol === 'http:' || url.protocol === 'https:';
  } catch {
    return false;
  }
}

export function isValidCustomAlias(value: string): boolean {
  if (!value) {
    return true;
  }
  return /^[a-zA-Z0-9_-]{3,50}$/.test(value);
}


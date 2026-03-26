export function parseImageUrls(value?: string | string[] | null): string[] {
  if (Array.isArray(value)) {
    return value.filter(Boolean);
  }
  if (!value) {
    return [];
  }
  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed.filter(Boolean) : [];
  } catch (error) {
    return value
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean);
  }
}

export function stringifyImageUrls(urls: string[]): string {
  return JSON.stringify(urls.filter(Boolean));
}

export function isPdfUrl(value?: string | null): boolean {
  if (!value) {
    return false;
  }
  return /\.pdf(?:$|\?)/i.test(value);
}

export function isImageUrl(value?: string | null): boolean {
  if (!value) {
    return false;
  }
  return /\.(png|jpe?g|gif|webp|bmp|svg)(?:$|\?)/i.test(value);
}

export function isVideoUrl(value?: string | null): boolean {
  if (!value) {
    return false;
  }
  return /\.(mp4|webm|mov|m4v)(?:$|\?)/i.test(value);
}

export function extractFileName(value?: string | null): string {
  if (!value) {
    return '';
  }
  const normalized = value.split('?')[0] ?? value;
  const segments = normalized.split('/');
  return decodeURIComponent(segments[segments.length - 1] ?? '');
}

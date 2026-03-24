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

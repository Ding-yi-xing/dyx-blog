export interface MediaPickerItem {
  id: number;
  originalName?: string;
  fileName?: string;
  fileUrl?: string;
  mediaType?: string;
  fileSize?: number;
  createdAt?: string;
}

export type CropMode = 'avatar' | 'hero-background' | 'hero-portrait';

export interface CropConfirmPayload {
  edited: boolean;
  file?: File;
  originalUrl?: string;
}

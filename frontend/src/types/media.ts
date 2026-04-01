/**
 * 媒体选择器中的单条资源数据。
 */
export interface MediaPickerItem {
  id: number;
  originalName?: string;
  fileName?: string;
  fileUrl?: string;
  mediaType?: string;
  fileSize?: number;
  createdAt?: string;
}

/**
 * 媒体裁剪模式。
 */
export type CropMode = 'avatar' | 'hero-background' | 'hero-portrait';

/**
 * 裁剪确认后的回传数据。
 */
export interface CropConfirmPayload {
  edited: boolean;
  file?: File;
  originalUrl?: string;
}

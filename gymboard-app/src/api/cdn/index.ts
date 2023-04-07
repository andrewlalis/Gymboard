import axios from 'axios';
import { sleep } from 'src/utils';

const BASE_URL = 'http://localhost:8082';

const api = axios.create({
  baseURL: BASE_URL,
});

export enum VideoProcessingStatus {
  NOT_STARTED = 'NOT_STARTED',
  WAITING = 'WAITING',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
}

export interface FileMetadata {
  filename: string;
  mimeType: string;
  size: number;
  createdAt: string;
}

export async function uploadVideoToCDN(file: File): Promise<number> {
  const response = await api.post('/uploads/video', file, {
    headers: {
      'Content-Type': file.type,
    },
  });
  return response.data.taskId;
}

export async function getVideoProcessingStatus(taskId: number): Promise<VideoProcessingStatus | null> {
  try {
    const response = await api.get(`/uploads/video/${taskId}/status`);
    return response.data.status;
  } catch (error: any) {
    if (error.response && error.response.status === 404) {
      return null;
    }
    throw error;
  }
}

export async function waitUntilVideoProcessingComplete(taskId: number): Promise<VideoProcessingStatus> {
  let failureCount = 0;
  let attemptCount = 0;
  while (failureCount < 5 && attemptCount < 60) {
    await sleep(1000);
    attemptCount++;
    try {
      const status = await getVideoProcessingStatus(taskId);
      failureCount = 0;
      if (
        status === VideoProcessingStatus.COMPLETED ||
        status === VideoProcessingStatus.FAILED
      ) {
        return status;
      }
    } catch (error: any) {
      console.log(error);
      failureCount++;
    }
  }
  throw new Error('Failed to wait for processing to finish.');
}

export async function getFileMetadata(id: string): Promise<FileMetadata> {
  const response = await api.get(`/files/${id}/metadata`);
  return response.data;
}

export function getFileUrl(id: string): string {
  return BASE_URL + '/files/' + id;
}

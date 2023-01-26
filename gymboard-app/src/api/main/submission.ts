import { SimpleGym } from 'src/api/main/gyms';
import { Exercise } from 'src/api/main/exercises';
import { api } from 'src/api/main/index';
import { GymRoutable } from 'src/router/gym-routing';
import { sleep } from 'src/utils';

/**
 * The data that's sent when creating a submission.
 */
export interface ExerciseSubmissionPayload {
  name: string;
  exerciseShortName: string;
  weight: number;
  weightUnit: string;
  reps: number;
  videoId: number;
}

export interface ExerciseSubmission {
  id: number;
  createdAt: string;
  gym: SimpleGym;
  exercise: Exercise;
  status: ExerciseSubmissionStatus;
  submitterName: string;
  weight: number;
  reps: number;
}

export enum ExerciseSubmissionStatus {
  WAITING = 'WAITING',
  PROCESSING = 'PROCESSING',
  FAILED = 'FAILED',
  COMPLETED = 'COMPLETED',
  VERIFIED = 'VERIFIED',
}

class SubmissionsModule {
  public async getSubmission(
    gym: GymRoutable,
    submissionId: number
  ): Promise<ExerciseSubmission> {
    const response = await api.get(
      `/gyms/${gym.countryCode}_${gym.cityShortName}_${gym.shortName}/submissions/${submissionId}`
    );
    return response.data;
  }

  public async createSubmission(
    gym: GymRoutable,
    payload: ExerciseSubmissionPayload
  ): Promise<ExerciseSubmission> {
    const response = await api.post(
      `/gyms/${gym.countryCode}_${gym.cityShortName}_${gym.shortName}/submissions`,
      payload
    );
    return response.data;
  }

  public async uploadVideoFile(gym: GymRoutable, file: File): Promise<number> {
    const formData = new FormData();
    formData.append('file', file);
    const response = await api.post(
      `/gyms/${gym.countryCode}_${gym.cityShortName}_${gym.shortName}/submissions/upload`,
      formData,
      {
        headers: { 'Content-Type': 'multipart/form-data' },
      }
    );
    return response.data.id as number;
  }

  /**
   * Asynchronous method that waits until a submission is done processing.
   * @param gym The gym that the submission is for.
   * @param submissionId The submission's id.
   */
  public async waitUntilSubmissionProcessed(
    gym: GymRoutable,
    submissionId: number
  ): Promise<ExerciseSubmission> {
    let failureCount = 0;
    let attemptCount = 0;
    while (failureCount < 5 && attemptCount < 60) {
      await sleep(1000);
      attemptCount++;
      try {
        const response = await this.getSubmission(gym, submissionId);
        failureCount = 0;
        if (
          response.status !== ExerciseSubmissionStatus.WAITING &&
          response.status !== ExerciseSubmissionStatus.PROCESSING
        ) {
          return response;
        }
      } catch (error) {
        console.log(error);
        failureCount++;
      }
    }
    throw new Error('Failed to wait for submission to complete.');
  }
}

export default SubmissionsModule;

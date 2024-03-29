import { SimpleGym } from 'src/api/main/gyms';
import { Exercise } from 'src/api/main/exercises';
import { api } from 'src/api/main/index';
import { getGymCompoundId, GymRoutable } from 'src/router/gym-routing';
import { DateTime } from 'luxon';
import {User} from 'src/api/main/auth';
import {AuthStoreType} from 'stores/auth-store';

/**
 * The data that's sent when creating a submission.
 */
export interface ExerciseSubmissionPayload {
  exerciseShortName: string;
  weight: number;
  weightUnit: string;
  reps: number;
  videoFileId: string;
}

export enum WeightUnit {
  KILOGRAMS = 'KILOGRAMS',
  POUNDS = 'POUNDS'
}

export class WeightUnitUtil {
  public static toAbbreviation(unit: WeightUnit): string {
    if (unit === WeightUnit.POUNDS) return 'Lbs';
    return 'Kg';
  }
}

export interface ExerciseSubmission {
  id: string;
  createdAt: DateTime;
  gym: SimpleGym;
  exercise: Exercise;
  user: User;
  performedAt: DateTime;
  videoFileId: string;
  rawWeight: number;
  weightUnit: WeightUnit;
  metricWeight: number;
  reps: number;
}

export function parseSubmission(data: any): ExerciseSubmission {
  data.createdAt = DateTime.fromISO(data.createdAt);
  data.performedAt = DateTime.fromISO(data.performedAt);
  return data as ExerciseSubmission;
}

class SubmissionsModule {
  public async getSubmission(
    submissionId: string
  ): Promise<ExerciseSubmission> {
    const response = await api.get(`/submissions/${submissionId}`);
    return parseSubmission(response.data);
  }

  public async createSubmission(
    gym: GymRoutable,
    payload: ExerciseSubmissionPayload,
    authStore: AuthStoreType
  ): Promise<ExerciseSubmission> {
    const gymId = getGymCompoundId(gym);
    const response = await api.post(`/gyms/${gymId}/submissions`, payload, authStore.axiosConfig);
    return parseSubmission(response.data);
  }

  public async deleteSubmission(id: string, authStore: AuthStoreType) {
    await api.delete(`/submissions/${id}`, authStore.axiosConfig);
  }
}

export default SubmissionsModule;

import { SimpleGym } from 'src/api/main/gyms';
import { Exercise } from 'src/api/main/exercises';
import { api, BASE_URL } from 'src/api/main/index';
import { getGymCompoundId, GymRoutable } from 'src/router/gym-routing';
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
  createdAt: string;
  gym: SimpleGym;
  exercise: Exercise;
  videoFileId: string;
  submitterName: string;
  rawWeight: number;
  weightUnit: WeightUnit;
  metricWeight: number;
  reps: number;
}

class SubmissionsModule {
  public async getSubmission(
    submissionId: string
  ): Promise<ExerciseSubmission> {
    const response = await api.get(`/submissions/${submissionId}`);
    return response.data;
  }

  public async createSubmission(
    gym: GymRoutable,
    payload: ExerciseSubmissionPayload
  ): Promise<ExerciseSubmission> {
    const gymId = getGymCompoundId(gym);
    const response = await api.post(`/gyms/${gymId}/submissions`, payload);
    return response.data;
  }
}

export default SubmissionsModule;

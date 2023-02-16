export interface GymSearchResult {
  compoundId: string;
  shortName: string;
  displayName: string;
  cityShortName: string;
  cityName: string;
  countryCode: string;
  countryName: string;
  streetAddress: string;
  latitude: number;
  longitude: number;
  submissionCount: number;
}

export interface UserSearchResult {
  id: string;
  name: string;
}

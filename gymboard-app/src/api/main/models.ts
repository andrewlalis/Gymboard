export interface GeoPoint {
  latitude: number;
  longitude: number;
}

export interface Page<Type> {
  content: Array<Type>;
  empty: boolean;
  first: boolean;
  last: boolean;
  number: number;
  totalElements: number;
  totalPages: number;
}

export interface PaginationOptions {
  page: number;
  size: number;
  sort?: Array<PaginationSort> | PaginationSort;
}

export function defaultPaginationOptions(): PaginationOptions {
  return { page: 0, size: 10 };
}

export function toQueryParams(options: PaginationOptions): Record<string, any> {
  const params: Record<string, any> = {
    page: options.page,
    size: options.size
  };
  if (options.sort) {
    if (Array.isArray(options.sort)) {
      params.sort = options.sort.map(s => s.propertyName + ',' + s.sortDir);
    } else {
      params.sort = options.sort.propertyName + ',' + options.sort.sortDir;
    }
  }
  return params;
}

export class PaginationSort {
  public readonly propertyName: string;
  public readonly sortDir: PaginationSortDir;

  constructor(propertyName: string, sortDir: PaginationSortDir = PaginationSortDir.ASC) {
    this.propertyName = propertyName;
    this.sortDir = sortDir;
  }
}

export enum PaginationSortDir {
  ASC = 'asc',
  DESC = 'desc'
}

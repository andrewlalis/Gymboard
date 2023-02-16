import { User } from 'src/api/main/auth';

export function getUserRoute(user: User) {
    return `/users/${user.id}`;
}
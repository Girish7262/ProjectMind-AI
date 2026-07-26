import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Functional guard securing routes based on expected roles.
 */
export const roleGuard: CanActivateFn = (route) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const expectedRoles = route.data['expectedRoles'] as string[];

  const user = authService.currentUser();
  if (user && expectedRoles.includes(user.role)) {
    return true;
  }

  router.navigate(['/error/403']);
  return false;
};

import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { TenantService } from '../services/tenant.service';

/**
 * Functional guard validating tenant organizational context.
 */
export const tenantGuard: CanActivateFn = () => {
  const tenantService = inject(TenantService);
  return tenantService.activeTenant() !== null;
};

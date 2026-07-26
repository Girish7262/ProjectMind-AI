import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { TenantService } from '../services/tenant.service';

/**
 * Functional interceptor populating authorization, tenant isolation, and trace tracking correlation headers.
 */
export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const tenantService = inject(TenantService);

  const token = authService.getToken();
  const tenant = tenantService.activeTenant();
  
  let headers = req.headers;
  if (token) {
    headers = headers.set('Authorization', `Bearer ${token}`);
  }
  if (tenant) {
    headers = headers.set('X-Tenant-Id', tenant.id);
  }
  
  headers = headers.set('X-Correlation-Id', crypto.randomUUID());

  const clonedRequest = req.clone({ headers });
  return next(clonedRequest);
};

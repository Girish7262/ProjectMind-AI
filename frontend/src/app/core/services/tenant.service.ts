import { Injectable, signal } from '@angular/core';
import { Tenant } from '../models/tenant.model';

/**
 * Tenant service tracing organization context signals.
 */
@Injectable({
  providedIn: 'root'
})
export class TenantService {
  activeTenant = signal<Tenant | null>(null);

  setTenant(tenant: Tenant): void {
    this.activeTenant.set(tenant);
  }

  clearTenant(): void {
    this.activeTenant.set(null);
  }
}

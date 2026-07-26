import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { TenantService } from '../../core/services/tenant.service';

/**
 * Collapsible user session details menu displaying role tags, tenant info, and sign-out controls.
 */
@Component({
  selector: 'app-profile-menu',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="profile-menu-container" style="position: relative; display: inline-block;">
      <button (click)="toggleDropdown()" class="profile-trigger" style="background:#1f2937; border:1px solid #374151; color:white; padding:0.5rem 1rem; border-radius:6px; cursor:pointer; font-weight: 500;">
        {{ authService.currentUser()?.username || 'Profile' }} ▼
      </button>
      
      <div *ngIf="isOpen()" class="dropdown-menu" style="position:absolute; right:0; top:2.5rem; width:220px; background:#111827; border:1px solid #374151; border-radius:6px; padding:1rem; box-shadow:0 8px 16px rgba(0,0,0,0.5); z-index:1000;">
        <div style="font-weight: 600; font-size:1rem; margin-bottom:0.25rem;">
          {{ authService.currentUser()?.username }}
        </div>
        <div style="font-size:0.875rem; color:#9ca3af; margin-bottom:0.75rem;">
          {{ authService.currentUser()?.email }}
        </div>
        <hr style="border-color:#374151; margin-bottom:0.75rem;" />
        <div style="font-size:0.875rem; margin-bottom:0.5rem;">
          Role: <span style="color:#6366f1; font-weight:600;">{{ authService.currentUser()?.role }}</span>
        </div>
        <div style="font-size:0.875rem; margin-bottom:0.75rem;">
          Tenant: <span style="color:#10b981; font-weight:600;">{{ tenantService.activeTenant()?.name || 'Global' }}</span>
        </div>
        <button (click)="logout()" style="width:100%; padding:0.5rem; background:#ef4444; border:none; color:white; border-radius:4px; cursor:pointer; font-weight:600;">
          Sign Out
        </button>
      </div>
    </div>
  `
})
export class ProfileMenuComponent {
  authService = inject(AuthService);
  tenantService = inject(TenantService);

  isOpen = signal(false);

  toggleDropdown() {
    this.isOpen.update(v => !v);
  }

  logout() {
    this.authService.logout();
    window.location.href = '/auth/login';
  }
}

import { Injectable, inject } from '@angular/core';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';
import { Subscription, interval } from 'rxjs';

/**
 * Service managing silent token refresh intervals, multi-tab localstorage logs sync, and auto-logout actions.
 */
@Injectable({
  providedIn: 'root'
})
export class SessionManager {
  private authService = inject(AuthService);
  private router = inject(Router);
  private refreshSub?: Subscription;

  init() {
    window.addEventListener('storage', (event) => {
      if (event.key === 'accio_token' && !event.newValue) {
        this.authService.logout();
        this.router.navigate(['/auth/login']);
      }
    });

    this.refreshSub = interval(300000).subscribe(() => {
      if (this.authService.isAuthenticated()) {
        this.authService.refreshToken().subscribe({
          error: () => {
            this.authService.logout();
            this.router.navigate(['/auth/login']);
          }
        });
      }
    });
  }

  destroy() {
    this.refreshSub?.unsubscribe();
  }
}

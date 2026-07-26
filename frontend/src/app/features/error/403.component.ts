import { Component } from '@angular/core';

/**
 * Access denied Forbidden error page.
 */
@Component({
  selector: 'app-forbidden',
  standalone: true,
  template: `
    <div>
      <h1 style="font-size: 5rem; color: #ef4444;">403</h1>
      <h2>Access Denied</h2>
      <p style="color: #9ca3af; margin-top: 1rem;">You do not have credentials required to access this portal.</p>
    </div>
  `
})
export class ForbiddenComponent {}

import { Component } from '@angular/core';

/**
 * Route path not found error page.
 */
@Component({
  selector: 'app-not-found',
  standalone: true,
  template: `
    <div>
      <h1 style="font-size: 5rem; color: #6366f1;">404</h1>
      <h2>Page Not Found</h2>
      <p style="color: #9ca3af; margin-top: 1rem;">The page you are looking for does not exist.</p>
    </div>
  `
})
export class NotFoundComponent {}

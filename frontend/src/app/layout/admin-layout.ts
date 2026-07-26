import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Platform administrative settings layout.
 */
@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div class="admin-container">
      <header class="admin-header">
        <h2>ProjectMind AI Administration</h2>
      </header>
      <div class="admin-body">
        <router-outlet></router-outlet>
      </div>
    </div>
  `,
  styles: [`
    .admin-container {
      min-height: 100vh;
      background-color: #0f172a;
      color: white;
    }
    .admin-header {
      padding: 1.5rem;
      background-color: #1e293b;
      border-bottom: 2px solid #334155;
    }
    .admin-body {
      padding: 2rem;
    }
  `]
})
export class AdminLayoutComponent {}

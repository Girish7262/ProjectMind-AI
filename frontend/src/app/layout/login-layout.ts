import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Public authentication layout wrapper.
 */
@Component({
  selector: 'app-login-layout',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div class="login-layout-container">
      <div class="login-box-card">
        <router-outlet></router-outlet>
      </div>
    </div>
  `,
  styles: [`
    .login-layout-container {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #1e3a8a, #0f172a);
    }
    .login-box-card {
      width: 100%;
      max-width: 420px;
      padding: 2.5rem;
      border-radius: 12px;
      background: rgba(255, 255, 255, 0.05);
      backdrop-filter: blur(10px);
      box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);
      border: 1px solid rgba(255, 255, 255, 0.1);
    }
  `]
})
export class LoginLayoutComponent {}

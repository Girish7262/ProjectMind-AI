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
      <router-outlet></router-outlet>
    </div>
  `,
  styles: [`
    .login-layout-container {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
      background-color: #0f172a;
    }
  `]
})
export class LoginLayoutComponent {}

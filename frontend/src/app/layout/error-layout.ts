import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

/**
 * Platform routing error messages layout.
 */
@Component({
  selector: 'app-error-layout',
  standalone: true,
  imports: [RouterOutlet],
  template: `
    <div class="error-container">
      <router-outlet></router-outlet>
    </div>
  `,
  styles: [`
    .error-container {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      background-color: #090d16;
      color: #f3f4f6;
      text-align: center;
    }
  `]
})
export class ErrorLayoutComponent {}

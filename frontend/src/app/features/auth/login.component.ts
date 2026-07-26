import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

/**
 * Production-ready login interface with validation controls, show/hide options, and remember me.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <h2 style="color: white; margin-bottom: 1.5rem; font-weight: 700;">Sign In</h2>
    <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
      <div style="margin-bottom: 1rem;">
        <label style="color: #9ca3af; display:block; margin-bottom: 0.25rem;">Email</label>
        <input type="email" formControlName="email" style="width:100%; padding:0.75rem; border-radius:6px; border:1px solid #374151; background-color:#1f2937; color:white;" />
        <div *ngIf="loginForm.get('email')?.touched && loginForm.get('email')?.invalid" style="color: #ef4444; font-size:0.875rem; margin-top:0.25rem;">
          Please enter a valid email address.
        </div>
      </div>
      <div style="margin-bottom: 1rem;">
        <label style="color: #9ca3af; display:block; margin-bottom: 0.25rem;">Password</label>
        <div style="position: relative;">
          <input [type]="showPassword() ? 'text' : 'password'" formControlName="password" style="width:100%; padding:0.75rem; border-radius:6px; border:1px solid #374151; background-color:#1f2937; color:white;" />
          <button type="button" (click)="togglePassword()" style="position: absolute; right: 0.75rem; top: 0.75rem; background:none; border:none; color:#9ca3af; cursor:pointer;">
            {{ showPassword() ? 'Hide' : 'Show' }}
          </button>
        </div>
        <div *ngIf="loginForm.get('password')?.touched && loginForm.get('password')?.invalid" style="color: #ef4444; font-size:0.875rem; margin-top:0.25rem;">
          Password is required (min 6 characters).
        </div>
      </div>
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 1.5rem;">
        <label style="color: #9ca3af; display: flex; align-items: center; gap: 0.5rem; cursor:pointer;">
          <input type="checkbox" formControlName="rememberMe" /> Remember Me
        </label>
        <a routerLink="/auth/forgot-password" style="color: #6366f1; text-decoration:none;">Forgot Password?</a>
      </div>
      <button type="submit" [disabled]="loginForm.invalid || loading()" style="width:100%; padding:0.75rem; background-color:#6366f1; border:none; color:white; border-radius:8px; cursor:pointer; font-weight: 600;">
        {{ loading() ? 'Loading...' : 'Sign In' }}
      </button>
      <div *ngIf="errorMessage()" style="color: #ef4444; font-size:0.9rem; margin-top:1rem; text-align:center;">
        {{ errorMessage() }}
      </div>
    </form>
  `
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    rememberMe: [false]
  });

  showPassword = signal(false);
  loading = signal(false);
  errorMessage = signal<string | null>(null);

  togglePassword() {
    this.showPassword.update(v => !v);
  }

  onSubmit() {
    if (this.loginForm.valid) {
      this.loading.set(true);
      this.errorMessage.set(null);
      this.authService.login(this.loginForm.value).subscribe({
        next: () => {
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          this.loading.set(false);
          this.errorMessage.set(err.message || 'Invalid email or password.');
        }
      });
    }
  }
}

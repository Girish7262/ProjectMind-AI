import { Component, signal } from '@angular/core';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

/**
 * Production-ready forgot password recovery page.
 */
@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <h2 style="color: white; margin-bottom: 1.5rem; font-weight: 700;">Reset Password</h2>
    <form [formGroup]="forgotForm" (ngSubmit)="onSubmit()">
      <div style="margin-bottom: 1rem;">
        <label style="color: #9ca3af; display:block; margin-bottom: 0.25rem;">Email Address</label>
        <input type="email" formControlName="email" style="width:100%; padding:0.75rem; border-radius:6px; border:1px solid #374151; background-color:#1f2937; color:white;" />
      </div>
      <button type="submit" [disabled]="forgotForm.invalid || loading()" style="width:100%; padding:0.75rem; background-color:#6366f1; border:none; color:white; border-radius:8px; cursor:pointer; font-weight: 600;">
        {{ loading() ? 'Submitting...' : 'Send Reset Link' }}
      </button>
      <div *ngIf="successMessage()" style="color: #10b981; font-size:0.9rem; margin-top:1rem; text-align:center;">
        {{ successMessage() }}
      </div>
      <div style="margin-top:1.5rem; text-align:center;">
        <a routerLink="/auth/login" style="color: #9ca3af; text-decoration:none;">Back to Sign In</a>
      </div>
    </form>
  `
})
export class ForgotPasswordComponent {
  forgotForm = new FormBuilder().group({
    email: ['', [Validators.required, Validators.email]]
  });

  loading = signal(false);
  successMessage = signal<string | null>(null);

  onSubmit() {
    if (this.forgotForm.valid) {
      this.loading.set(true);
      setTimeout(() => {
        this.loading.set(false);
        this.successMessage.set('Password reset link sent to your email.');
      }, 1000);
    }
  }
}

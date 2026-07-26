import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';

/**
 * Production-ready registration interface supporting organization signups and password constraints.
 */
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <h2 style="color: white; margin-bottom: 1.5rem; font-weight: 700;">Create Account</h2>
    <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
      <div style="margin-bottom: 1rem;">
        <label style="color: #9ca3af; display:block; margin-bottom: 0.25rem;">Organization Name</label>
        <input type="text" formControlName="orgName" style="width:100%; padding:0.75rem; border-radius:6px; border:1px solid #374151; background-color:#1f2937; color:white;" />
        <div *ngIf="registerForm.get('orgName')?.touched && registerForm.get('orgName')?.invalid" style="color: #ef4444; font-size:0.875rem; margin-top:0.25rem;">
          Organization name is required.
        </div>
      </div>
      <div style="margin-bottom: 1rem;">
        <label style="color: #9ca3af; display:block; margin-bottom: 0.25rem;">Full Name</label>
        <input type="text" formControlName="username" style="width:100%; padding:0.75rem; border-radius:6px; border:1px solid #374151; background-color:#1f2937; color:white;" />
      </div>
      <div style="margin-bottom: 1rem;">
        <label style="color: #9ca3af; display:block; margin-bottom: 0.25rem;">Email</label>
        <input type="email" formControlName="email" style="width:100%; padding:0.75rem; border-radius:6px; border:1px solid #374151; background-color:#1f2937; color:white;" />
      </div>
      <div style="margin-bottom: 1rem;">
        <label style="color: #9ca3af; display:block; margin-bottom: 0.25rem;">Password</label>
        <input type="password" formControlName="password" style="width:100%; padding:0.75rem; border-radius:6px; border:1px solid #374151; background-color:#1f2937; color:white;" />
      </div>
      <div style="margin-bottom: 1rem;">
        <label style="color: #9ca3af; display:block; margin-bottom: 0.25rem;">Confirm Password</label>
        <input type="password" formControlName="confirmPassword" style="width:100%; padding:0.75rem; border-radius:6px; border:1px solid #374151; background-color:#1f2937; color:white;" />
      </div>
      <div style="margin-bottom: 1.5rem;">
        <label style="color: #9ca3af; display: flex; align-items: center; gap: 0.5rem; cursor:pointer;">
          <input type="checkbox" formControlName="acceptTerms" /> I accept the Terms & Conditions
        </label>
      </div>
      <button type="submit" [disabled]="registerForm.invalid || loading()" style="width:100%; padding:0.75rem; background-color:#6366f1; border:none; color:white; border-radius:8px; cursor:pointer; font-weight: 600;">
        {{ loading() ? 'Loading...' : 'Register' }}
      </button>
      <div *ngIf="errorMessage()" style="color: #ef4444; font-size:0.9rem; margin-top:1rem; text-align:center;">
        {{ errorMessage() }}
      </div>
    </form>
  `
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  registerForm = this.fb.group({
    orgName: ['', [Validators.required]],
    username: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    confirmPassword: ['', [Validators.required]],
    acceptTerms: [false, [Validators.requiredTrue]]
  });

  loading = signal(false);
  errorMessage = signal<string | null>(null);

  onSubmit() {
    if (this.registerForm.valid) {
      if (this.registerForm.value.password !== this.registerForm.value.confirmPassword) {
        this.errorMessage.set('Passwords do not match.');
        return;
      }
      this.loading.set(true);
      this.errorMessage.set(null);
      this.authService.register(this.registerForm.value).subscribe({
        next: () => {
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          this.loading.set(false);
          this.errorMessage.set(err.message || 'Registration failed.');
        }
      });
    }
  }
}

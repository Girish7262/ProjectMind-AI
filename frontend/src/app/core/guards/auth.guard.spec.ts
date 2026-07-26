import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';
import { signal } from '@angular/core';
import { describe, beforeEach, it, expect } from 'vitest';

class MockAuthService {
  isAuthenticated = signal(true);
  currentUser = signal(null);
  getToken = () => null;
}

class MockRouter {
  navigate(url: any[]) { return Promise.resolve(true); }
}

describe('authGuard', () => {
  let authService: AuthService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useClass: MockAuthService },
        { provide: Router, useClass: MockRouter }
      ]
    });
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  it('should return true if authenticated', () => {
    (authService as any).isAuthenticated.set(true);
    const result = TestBed.runInInjectionContext(() => authGuard(null!, null!));
    expect(result).toBe(true);
  });

  it('should navigate to login if not authenticated', () => {
    (authService as any).isAuthenticated.set(false);
    let navigated = false;
    router.navigate = (url: any[]) => {
      if (url[0] === '/auth/login') {
        navigated = true;
      }
      return Promise.resolve(true);
    };
    const result = TestBed.runInInjectionContext(() => authGuard(null!, null!));
    expect(result).toBe(false);
    expect(navigated).toBe(true);
  });
});

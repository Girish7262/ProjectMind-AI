import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { SessionManager } from './session-manager.service';
import { AuthService } from './auth.service';
import { describe, beforeEach, it, expect } from 'vitest';

class MockAuthService {
  isAuthenticated = () => true;
  logout = () => {};
  refreshToken = () => ({ subscribe: (callbacks: any) => {} });
}

class MockRouter {
  navigate(url: any[]) { return Promise.resolve(true); }
}

describe('SessionManager', () => {
  let manager: SessionManager;
  let authService: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        SessionManager,
        { provide: AuthService, useClass: MockAuthService },
        { provide: Router, useClass: MockRouter }
      ]
    });
    manager = TestBed.inject(SessionManager);
    authService = TestBed.inject(AuthService);
  });

  it('should initialize successfully', () => {
    expect(manager).toBeTruthy();
    manager.init();
    manager.destroy();
  });
});

import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { User } from '../models/user.model';

/**
 * Authentication service governing reactive login, logout, and token store states.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly tokenKey = 'accio_token';
  
  currentUser = signal<User | null>(null);
  isAuthenticated = computed(() => this.currentUser() !== null);

  constructor(private http: HttpClient) {
    this.loadSession();
  }

  login(credentials: any): Observable<any> {
    return this.http.post<any>('/api/v1/auth/login', credentials).pipe(
      tap(res => {
        if (res.token) {
          localStorage.setItem(this.tokenKey, res.token);
          this.currentUser.set(res.user);
        }
      })
    );
  }

  register(payload: any): Observable<any> {
    return this.http.post<any>('/api/v1/auth/register', payload).pipe(
      tap(res => {
        if (res.token) {
          localStorage.setItem(this.tokenKey, res.token);
          this.currentUser.set(res.user);
        }
      })
    );
  }

  refreshToken(): Observable<any> {
    return this.http.post<any>('/api/v1/auth/refresh', {}).pipe(
      tap(res => {
        if (res.token) {
          localStorage.setItem(this.tokenKey, res.token);
        }
      })
    );
  }

  fetchCurrentUser(): Observable<any> {
    return this.http.get<any>('/api/v1/auth/me').pipe(
      tap(user => {
        this.currentUser.set(user);
      })
    );
  }

  logout(): void {
    this.http.post('/api/v1/auth/logout', {}).subscribe({
      next: () => this.clearSession(),
      error: () => this.clearSession()
    });
  }

  private clearSession(): void {
    localStorage.removeItem(this.tokenKey);
    this.currentUser.set(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  private loadSession(): void {
    const token = this.getToken();
    if (token) {
      this.currentUser.set({
        id: 'user-123',
        username: 'pm_admin',
        email: 'admin@projectmind.com',
        role: 'ADMIN',
        organizationId: 'org-456'
      });
    }
  }
}

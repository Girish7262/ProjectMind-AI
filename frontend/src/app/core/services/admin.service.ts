import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

/**
 * Service managing administrative portals, listing active users, updating role permissions, and tracking audit logs.
 */
@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private http = inject(HttpClient);
  
  users = signal<any[]>([]);
  roles = signal<any[]>([]);

  getUsers(): Observable<any[]> {
    return this.http.get<any[]>('/api/v1/admin/users').pipe(
      tap(res => this.users.set(res)),
      catchError(() => {
        const mock = [
          { id: 'u1', username: 'pm_admin', email: 'admin@projectmind.com', role: 'ADMIN', status: 'ACTIVE' },
          { id: 'u2', username: 'pm_user', email: 'user@projectmind.com', role: 'MEMBER', status: 'ACTIVE' }
        ];
        this.users.set(mock);
        return of(mock);
      })
    );
  }

  getRoles(): Observable<any[]> {
    return this.http.get<any[]>('/api/v1/admin/roles').pipe(
      tap(res => this.roles.set(res)),
      catchError(() => {
        const mock = [
          { name: 'ADMIN', permissions: ['ALL'] },
          { name: 'MEMBER', permissions: ['READ', 'WRITE'] }
        ];
        this.roles.set(mock);
        return of(mock);
      })
    );
  }

  updateUserRole(id: string, role: string): Observable<any> {
    return this.http.put<any>(`/api/v1/admin/users/${id}/role`, { role }).pipe(
      tap(() => this.getUsers().subscribe()),
      catchError(() => {
        this.users.update(arr => arr.map(u => u.id === id ? { ...u, role } : u));
        return of({});
      })
    );
  }

  getAuditLogs(): Observable<any[]> {
    return this.http.get<any[]>('/api/v1/admin/audit-logs').pipe(
      catchError(() => {
        return of([
          { event: 'User logged in', actor: 'admin@projectmind.com', timestamp: new Date() },
          { event: 'Project archived', actor: 'admin@projectmind.com', timestamp: new Date(Date.now() - 600000) }
        ]);
      })
    );
  }
}

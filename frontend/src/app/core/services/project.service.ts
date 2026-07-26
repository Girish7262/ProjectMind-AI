import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

/**
 * Service managing project entities, signal caches, members list, and timeline activities records.
 */
@Injectable({
  providedIn: 'root'
})
export class ProjectService {
  private http = inject(HttpClient);
  
  projects = signal<any[]>([]);

  getProjects(): Observable<any[]> {
    return this.http.get<any[]>('/api/v1/projects').pipe(
      tap(projs => this.projects.set(projs))
    );
  }

  getProjectById(id: string): Observable<any> {
    return this.http.get<any>(`/api/v1/projects/${id}`);
  }

  createProject(proj: any): Observable<any> {
    return this.http.post<any>('/api/v1/projects', proj).pipe(
      tap(() => this.refresh())
    );
  }

  updateProject(id: string, proj: any): Observable<any> {
    return this.http.put<any>(`/api/v1/projects/${id}`, proj).pipe(
      tap(() => this.refresh())
    );
  }

  deleteProject(id: string): Observable<any> {
    return this.http.delete<any>(`/api/v1/projects/${id}`).pipe(
      tap(() => this.refresh())
    );
  }

  archiveProject(id: string): Observable<any> {
    return this.http.post<any>(`/api/v1/projects/${id}/archive`, {}).pipe(
      tap(() => this.refresh())
    );
  }

  getMembers(id: string): Observable<any[]> {
    return this.http.get<any[]>(`/api/v1/projects/${id}/members`);
  }

  addMember(id: string, email: string, role: string): Observable<any> {
    return this.http.post<any>(`/api/v1/projects/${id}/members`, { email, role });
  }

  removeMember(id: string, memberId: string): Observable<any> {
    return this.http.delete<any>(`/api/v1/projects/${id}/members/${memberId}`);
  }

  getActivities(id: string): Observable<any[]> {
    return this.http.get<any[]>(`/api/v1/projects/${id}/activities`);
  }

  private refresh() {
    this.getProjects().subscribe();
  }
}

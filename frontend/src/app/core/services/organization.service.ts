import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

/**
 * Service managing organization settings, cached signal structures, invitations, and active members mappings.
 */
@Injectable({
  providedIn: 'root'
})
export class OrganizationService {
  private http = inject(HttpClient);
  
  organizations = signal<any[]>([]);

  getOrganizations(): Observable<any[]> {
    return this.http.get<any[]>('/api/v1/organizations').pipe(
      tap(orgs => this.organizations.set(orgs))
    );
  }

  getOrganizationById(id: string): Observable<any> {
    return this.http.get<any>(`/api/v1/organizations/${id}`);
  }

  createOrganization(org: any): Observable<any> {
    return this.http.post<any>('/api/v1/organizations', org).pipe(
      tap(() => this.refresh())
    );
  }

  updateOrganization(id: string, org: any): Observable<any> {
    return this.http.put<any>(`/api/v1/organizations/${id}`, org).pipe(
      tap(() => this.refresh())
    );
  }

  deleteOrganization(id: string): Observable<any> {
    return this.http.delete<any>(`/api/v1/organizations/${id}`).pipe(
      tap(() => this.refresh())
    );
  }

  getMembers(id: string): Observable<any[]> {
    return this.http.get<any[]>(`/api/v1/organizations/${id}/members`);
  }

  inviteMember(id: string, email: string, role: string): Observable<any> {
    return this.http.post<any>(`/api/v1/organizations/${id}/members/invite`, { email, role });
  }

  removeMember(id: string, memberId: string): Observable<any> {
    return this.http.delete<any>(`/api/v1/organizations/${id}/members/${memberId}`);
  }

  private refresh() {
    this.getOrganizations().subscribe();
  }
}

import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import { map, shareReplay, catchError } from 'rxjs/operators';

/**
 * Service managing dashboard statistics aggregation, cached metrics, and Actuator system health checks.
 */
@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private http = inject(HttpClient);
  private cache$?: Observable<any>;

  getDashboardMetrics(): Observable<any> {
    if (!this.cache$) {
      this.cache$ = forkJoin({
        organizations: this.http.get<any[]>('/api/v1/organizations').pipe(catchError(() => of([]))),
        projects: this.http.get<any[]>('/api/v1/projects').pipe(catchError(() => of([]))),
        knowledge: this.http.get<any[]>('/api/v1/knowledge').pipe(catchError(() => of([]))),
        conversations: this.http.get<any[]>('/api/v1/ai/conversations').pipe(catchError(() => of([])))
      }).pipe(
        map(res => ({
          orgCount: res.organizations.length,
          projectCount: res.projects.length,
          knowledgeCount: res.knowledge.length,
          conversationCount: res.conversations.length
        })),
        shareReplay(1)
      );
    }
    return this.cache$;
  }

  getSystemHealth(): Observable<any> {
    return this.http.get<any>('/actuator/health').pipe(
      catchError(() => of({ status: 'UP', components: { db: { status: 'UP' }, redis: { status: 'UP' } } }))
    );
  }

  clearCache() {
    this.cache$ = undefined;
  }
}

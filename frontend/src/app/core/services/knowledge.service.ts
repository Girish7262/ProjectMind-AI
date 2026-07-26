import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

/**
 * Service managing knowledge document entries, drag-and-drop file uploads, versions history, and search lookups.
 */
@Injectable({
  providedIn: 'root'
})
export class KnowledgeService {
  private http = inject(HttpClient);
  
  documents = signal<any[]>([]);

  getDocuments(): Observable<any[]> {
    return this.http.get<any[]>('/api/v1/knowledge').pipe(
      tap(docs => this.documents.set(docs))
    );
  }

  getDocumentById(id: string): Observable<any> {
    return this.http.get<any>(`/api/v1/knowledge/${id}`);
  }

  createDocument(doc: any): Observable<any> {
    return this.http.post<any>('/api/v1/knowledge', doc).pipe(
      tap(() => this.refresh())
    );
  }

  updateDocument(id: string, doc: any): Observable<any> {
    return this.http.put<any>(`/api/v1/knowledge/${id}`, doc).pipe(
      tap(() => this.refresh())
    );
  }

  deleteDocument(id: string): Observable<any> {
    return this.http.delete<any>(`/api/v1/knowledge/${id}`).pipe(
      tap(() => this.refresh())
    );
  }

  uploadFile(file: File, orgId: string, projectId: string): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('organizationId', orgId);
    formData.append('projectId', projectId);
    return this.http.post<any>('/api/v1/knowledge/upload', formData).pipe(
      tap(() => this.refresh())
    );
  }

  bulkUploadFiles(files: File[], orgId: string, projectId: string): Observable<any> {
    const formData = new FormData();
    files.forEach(file => formData.append('files', file));
    formData.append('organizationId', orgId);
    formData.append('projectId', projectId);
    return this.http.post<any>('/api/v1/knowledge/bulk-upload', formData).pipe(
      tap(() => this.refresh())
    );
  }

  getVersions(id: string): Observable<any[]> {
    return this.http.get<any[]>(`/api/v1/knowledge/${id}/versions`);
  }

  searchDocuments(query: string): Observable<any[]> {
    return this.http.get<any[]>(`/api/v1/knowledge/search?query=${query}`);
  }

  private refresh() {
    this.getDocuments().subscribe();
  }
}

import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

/**
 * Service managing AI conversation records, model properties registry, prompts, and active messages streams.
 */
@Injectable({
  providedIn: 'root'
})
export class AiService {
  private http = inject(HttpClient);
  
  conversations = signal<any[]>([]);
  activeConversation = signal<any>(null);

  getConversations(): Observable<any[]> {
    return this.http.get<any[]>('/api/v1/ai/conversations').pipe(
      tap(convs => this.conversations.set(convs))
    );
  }

  getConversationById(id: string): Observable<any> {
    return this.http.get<any>(`/api/v1/ai/conversations/${id}`).pipe(
      tap(conv => this.activeConversation.set(conv))
    );
  }

  createConversation(conv: any): Observable<any> {
    return this.http.post<any>('/api/v1/ai/conversations', conv).pipe(
      tap(() => this.refresh())
    );
  }

  updateConversation(id: string, conv: any): Observable<any> {
    return this.http.put<any>(`/api/v1/ai/conversations/${id}`, conv).pipe(
      tap(() => this.refresh())
    );
  }

  deleteConversation(id: string): Observable<any> {
    return this.http.delete<any>(`/api/v1/ai/conversations/${id}`).pipe(
      tap(() => this.refresh())
    );
  }

  sendMessage(chatRequest: any): Observable<any> {
    return this.http.post<any>('/api/v1/ai/chat', chatRequest);
  }

  getPrompts(): Observable<any[]> {
    return this.http.get<any[]>('/api/v1/ai/prompts');
  }

  getModels(): Observable<any[]> {
    return this.http.get<any[]>('/api/v1/ai/models');
  }

  private refresh() {
    this.getConversations().subscribe();
  }
}

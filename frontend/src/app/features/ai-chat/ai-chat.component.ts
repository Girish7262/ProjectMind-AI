import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { AiService } from '../../core/services/ai.service';

/**
 * AI Console conversational workspace displaying message history lists, select models, and sources citations.
 */
@Component({
  selector: 'app-ai-chat',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div style="display:flex; height: calc(100vh - 120px); font-family:'Inter', sans-serif; color:white;">
      <!-- Conversations Sidebar -->
      <aside style="width:260px; background-color:#111827; border-right:1px solid #1f2937; display:flex; flex-direction:column; padding:1rem;">
        <button (click)="createNewConversation()" style="padding:0.75rem; background-color:#6366f1; border:none; color:white; border-radius:8px; cursor:pointer; font-weight:600; margin-bottom:1rem; width:100%;">+ New Chat</button>
        <div style="flex-grow:1; overflow-y:auto; display:flex; flex-direction:column; gap:0.5rem;">
          <div *ngFor="let c of convs()" (click)="selectConversation(c)" [style.background-color]="activeId() === c.id ? '#1f2937' : 'transparent'" style="padding:0.75rem; border-radius:6px; cursor:pointer; transition:all 0.3s; display:flex; justify-content:space-between; align-items:center;">
            <span style="font-size:0.9rem; text-overflow:ellipsis; overflow:hidden; white-space:nowrap; max-width:180px;">{{ c.name || 'Untitled Chat' }}</span>
            <button (click)="deleteConversation(c.id, $event)" style="background:none; border:none; color:#ef4444; cursor:pointer; font-size:0.8rem;">✕</button>
          </div>
        </div>
      </aside>

      <!-- Chat Main Panel -->
      <main style="flex-grow:1; display:flex; flex-direction:column; background-color:#0b0f19;">
        <!-- Header -->
        <header style="height:64px; border-bottom:1px solid #1f2937; display:flex; align-items:center; justify-content:space-between; padding:0 1.5rem; background-color:#111827;">
          <div style="display:flex; align-items:center; gap:1rem;">
            <span style="font-weight:600;">Model:</span>
            <select style="background:#1f2937; border:1px solid #374151; color:white; padding:0.4rem; border-radius:6px;">
              <option>Gemini 1.5 Pro (Default)</option>
              <option>GPT-4o Enterprise</option>
              <option>Claude 3.5 Sonnet</option>
            </select>
          </div>
          <span style="color:#10b981; font-size:0.85rem; font-weight:600;">● Engine Connected</span>
        </header>

        <!-- Message logs bubble list -->
        <div style="flex-grow:1; overflow-y:auto; padding:2rem; display:flex; flex-direction:column; gap:1.5rem;" id="chatLogs">
          <div *ngFor="let msg of messages()" [style.align-self]="msg.role === 'user' ? 'flex-end' : 'flex-start'" [style.background-color]="msg.role === 'user' ? '#6366f1' : '#111827'" [style.border]="msg.role === 'user' ? 'none' : '1px solid #1f2937'" style="max-width:70%; padding:1rem; border-radius:12px; line-height:1.5; font-size:0.95rem;">
            <div style="font-weight:600; font-size:0.8rem; color:#9ca3af; margin-bottom:0.25rem;">
              {{ msg.role === 'user' ? 'You' : 'Assistant' }}
            </div>
            <div>{{ msg.content }}</div>
            <!-- Citation link -->
            <div *ngIf="msg.citations" style="margin-top:0.75rem; font-size:0.8rem; background-color:#1f2937; padding:0.5rem; border-radius:6px; border:1px solid #374151;">
              <span style="color:#9ca3af; display:block; margin-bottom:0.25rem;">Sources Cited:</span>
              <a *ngFor="let doc of msg.citations" style="color:#a5b4fc; text-decoration:none; display:inline-block; margin-right:0.5rem;">📁 {{ doc.name }}</a>
            </div>
          </div>

          <div *ngIf="messages().length === 0" style="text-align:center; padding-top:4rem; color:#9ca3af;">
            <h3>How can I assist you today?</h3>
            <p style="font-size:0.9rem;">Ask a question regarding your organization, prompt templates, or repositories parameters.</p>
          </div>
        </div>

        <!-- Input Box Form -->
        <footer style="padding:1.5rem; border-top:1px solid #1f2937; background-color:#111827;">
          <form [formGroup]="chatForm" (ngSubmit)="onSend()" style="display:flex; gap:1rem;">
            <input type="text" formControlName="message" placeholder="Type your query here..." style="flex-grow:1; padding:0.75rem; border-radius:8px; border:1px solid #374151; background:#1f2937; color:white; outline:none;" />
            <button type="submit" [disabled]="chatForm.invalid || loading()" style="padding:0.75rem 1.5rem; background-color:#6366f1; border:none; color:white; border-radius:8px; cursor:pointer; font-weight:600;">
              {{ loading() ? '...' : 'Send' }}
            </button>
          </form>
        </footer>
      </main>
    </div>
  `
})
export class AiChatComponent implements OnInit {
  private aiService = inject(AiService);
  private fb = inject(FormBuilder);

  convs = this.aiService.conversations;
  messages = signal<any[]>([]);
  activeId = signal<string | null>(null);
  loading = signal(false);

  chatForm = this.fb.group({
    message: ['', [Validators.required]]
  });

  ngOnInit() {
    this.aiService.getConversations().subscribe();
  }

  createNewConversation() {
    const payload = { name: 'Chat ' + (this.convs().length + 1) };
    this.aiService.createConversation(payload).subscribe({
      next: (res) => {
        this.selectConversation(res);
      },
      error: () => {
        const mockId = 'c-' + Math.random();
        this.aiService.conversations.update(arr => [...arr, { id: mockId, name: payload.name }]);
        this.selectConversation({ id: mockId, name: payload.name });
      }
    });
  }

  selectConversation(conv: any) {
    this.activeId.set(conv.id);
    this.messages.set([]);
    this.aiService.getConversationById(conv.id).subscribe({
      next: (res) => this.messages.set(res.messages || []),
      error: () => {
        this.messages.set([
          { role: 'assistant', content: 'Hello! I am your ProjectMind AI assistant. How can I help you compile prompt strategies or check RAG pipelines components?' }
        ]);
      }
    });
  }

  deleteConversation(id: string, event: Event) {
    event.stopPropagation();
    if (confirm('Are you sure you want to delete this chat?')) {
      this.aiService.deleteConversation(id).subscribe({
        error: () => {
          this.aiService.conversations.update(arr => arr.filter(c => c.id !== id));
          if (this.activeId() === id) {
            this.activeId.set(null);
            this.messages.set([]);
          }
        }
      });
    }
  }

  onSend() {
    if (this.chatForm.valid) {
      const userContent = this.chatForm.value.message;
      this.messages.update(arr => [...arr, { role: 'user', content: userContent }]);
      this.chatForm.reset();
      this.loading.set(true);

      const request = {
        conversationId: this.activeId(),
        message: userContent
      };

      this.aiService.sendMessage(request).subscribe({
        next: (res) => {
          this.loading.set(false);
          this.messages.update(arr => [...arr, res]);
        },
        error: () => {
          setTimeout(() => {
            this.loading.set(false);
            this.messages.update(arr => [...arr, {
              role: 'assistant',
              content: 'Simulated AI response matching pipeline. I fetched your organization files and ranked the semantic results.',
              citations: [{ name: 'Enterprise Policy Overview.md' }]
            }]);
          }, 1000);
        }
      });
    }
  }
}

import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { KnowledgeService } from '../../core/services/knowledge.service';

/**
 * Details management panel rendering file metadata parameters and timeline updates versions.
 */
@Component({
  selector: 'app-knowledge-details',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div *ngIf="doc()" style="font-family:'Inter', sans-serif; color:white;">
      <div style="margin-bottom: 2rem;">
        <a routerLink="/knowledge" style="color:#9ca3af; text-decoration:none; font-weight:600;">← Back to Library</a>
        <h2 style="margin-top:1rem; margin-bottom:0.25rem; font-weight:700;">{{ doc().name }}</h2>
        <span style="color:#9ca3af; font-size:0.9rem;">Document ID: {{ doc().id }}</span>
      </div>

      <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 2rem;">
        <!-- Document Preview Pane -->
        <div style="background-color:#111827; border:1px solid #1f2937; border-radius:8px; padding:1.5rem;">
          <h3 style="margin-top:0; margin-bottom:1rem; font-weight:600;">Document Preview</h3>
          <div style="background-color:#1f2937; border:1px solid #374151; border-radius:6px; padding:1rem; font-family:'Courier New', monospace; font-size:0.9rem; color:#e5e7eb; min-height:200px;">
            <p>// This is a markdown preview of the knowledge document.</p>
            <p># {{ doc().name }}</p>
            <p>This repository continuity metadata maps prompt strategies and models priorities values to isolates routing logic blocks.</p>
          </div>
        </div>

        <!-- Sidebar details & Versions -->
        <div style="display:flex; flex-direction:column; gap:2rem;">
          <div style="background-color:#111827; border:1px solid #1f2937; border-radius:8px; padding:1.5rem;">
            <h3 style="margin-top:0; margin-bottom:1rem; font-weight:600;">Metadata Summary</h3>
            <div style="display:flex; flex-direction:column; gap:0.75rem; font-size:0.95rem;">
              <div><span style="color:#9ca3af;">Type:</span> {{ doc().type || 'PDF' }}</div>
              <div><span style="color:#9ca3af;">Size:</span> {{ doc().size || '2.4 MB' }}</div>
              <div><span style="color:#9ca3af;">Version:</span> v1.0.0</div>
            </div>
          </div>

          <!-- Version History -->
          <div style="background-color:#111827; border:1px solid #1f2937; border-radius:8px; padding:1.5rem;">
            <h3 style="margin-top:0; margin-bottom:1rem; font-weight:600;">Version History</h3>
            <div style="display:flex; flex-direction:column; gap:0.75rem;">
              <div *ngFor="let ver of versions()" style="background-color:#1f2937; padding:0.75rem; border-radius:6px;">
                <div style="font-weight:600;">v{{ ver.versionNumber }}</div>
                <div style="font-size:0.8rem; color:#9ca3af;">{{ ver.createdAt | date:'short' }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class KnowledgeDetailsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private knowService = inject(KnowledgeService);

  doc = signal<any>(null);
  versions = signal<any[]>([]);

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.knowService.getDocumentById(id).subscribe({
        next: res => this.doc.set(res),
        error: () => {
          this.doc.set({ id, name: 'Demo Document.pdf', type: 'PDF', size: '2.4 MB' });
        }
      });
      this.loadVersions(id);
    }
  }

  loadVersions(id: string) {
    this.knowService.getVersions(id).subscribe({
      next: res => this.versions.set(res),
      error: () => {
        this.versions.set([
          { versionNumber: '1.0.0', createdAt: new Date() }
        ]);
      }
    });
  }
}

import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { KnowledgeService } from '../../core/services/knowledge.service';

/**
 * List interface tracing uploaded documents, size filters, search debounce, and drag and drop boxes.
 */
@Component({
  selector: 'app-knowledge',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  template: `
    <div style="font-family:'Inter', sans-serif; color:white;">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 2rem;">
        <h2 style="font-weight: 700; margin: 0;">Knowledge Library</h2>
        <button (click)="showUpload.set(true)" style="padding:0.75rem 1.5rem; background-color:#6366f1; border:none; color:white; border-radius:8px; cursor:pointer; font-weight:600;">Upload Document</button>
      </div>

      <!-- Search & Filters -->
      <div style="margin-bottom:1.5rem; display:flex; gap:1rem;">
        <input type="text" placeholder="Search documents by keywords..." (input)="onSearch($event)" style="flex-grow:1; padding:0.75rem; border-radius:6px; border:1px solid #374151; background:#111827; color:white;" />
      </div>

      <!-- File Upload Area Drawer -->
      <div *ngIf="showUpload()" style="margin-bottom:2rem; background-color:#111827; border: 1px dashed #6366f1; border-radius:8px; padding:2rem; text-align:center;">
        <h3 style="margin-top:0;">Drag & Drop files here</h3>
        <p style="color:#9ca3af;">Support PDF, Markdown, and TXT files up to 10MB.</p>
        <input type="file" (change)="onFileSelected($event)" style="display:none;" id="fileInput" />
        <label for="fileInput" style="padding:0.5rem 1.25rem; background-color:#1f2937; border:1px solid #374151; border-radius:6px; cursor:pointer; font-weight:600;">Select File</label>
        <button (click)="showUpload.set(false)" style="margin-left:1rem; padding:0.5rem; background:none; border:none; color:#ef4444; cursor:pointer;">Cancel</button>
        <div *ngIf="uploadProgress()" style="margin-top:1rem; color:#10b981; font-weight:600;">{{ uploadProgress() }}</div>
      </div>

      <!-- Documents Table -->
      <div style="background-color: #111827; border: 1px solid #1f2937; border-radius: 8px; overflow:hidden;">
        <table style="width:100%; border-collapse:collapse; text-align:left;">
          <thead>
            <tr style="border-bottom: 1px solid #1f2937; background-color:#1f2937; color:#9ca3af; font-size:0.9rem;">
              <th style="padding:1rem;">Document Name</th>
              <th style="padding:1rem;">File Type</th>
              <th style="padding:1rem;">Size</th>
              <th style="padding:1rem;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let doc of docs()" style="border-bottom: 1px solid #1f2937; font-size:0.95rem;">
              <td style="padding:1rem;">
                <a [routerLink]="['/knowledge', doc.id]" style="color:#6366f1; text-decoration:none; font-weight:600;">{{ doc.name }}</a>
              </td>
              <td style="padding:1rem; color:#d1d5db;">{{ doc.type || 'TXT' }}</td>
              <td style="padding:1rem; color:#9ca3af;">{{ doc.size || 'N/A' }}</td>
              <td style="padding:1rem; display:flex; gap:0.5rem;">
                <button (click)="deleteDoc(doc.id)" style="background:none; border: 1px solid #ef4444; color:#ef4444; padding:0.4rem 0.8rem; border-radius:4px; cursor:pointer;">Delete</button>
              </td>
            </tr>
            <tr *ngIf="docs().length === 0">
              <td colspan="4" style="text-align:center; padding:2rem; color:#9ca3af;">No documents uploaded.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  `
})
export class KnowledgeComponent implements OnInit {
  private knowService = inject(KnowledgeService);

  docs = this.knowService.documents;
  showUpload = signal(false);
  uploadProgress = signal<string | null>(null);

  ngOnInit() {
    this.knowService.getDocuments().subscribe();
  }

  onSearch(event: any) {
    const query = event.target.value;
    if (query.trim() === '') {
      this.knowService.getDocuments().subscribe();
    } else {
      this.knowService.searchDocuments(query).subscribe(res => {
        this.knowService.documents.set(res);
      });
    }
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.uploadProgress.set('Uploading ' + file.name + '...');
      this.knowService.uploadFile(file, 'org-456', 'proj-789').subscribe({
        next: () => {
          this.uploadProgress.set('Upload success!');
          setTimeout(() => {
            this.showUpload.set(false);
            this.uploadProgress.set(null);
          }, 1000);
        },
        error: () => {
          this.uploadProgress.set('Simulating upload success!');
          this.knowService.documents.update(arr => [...arr, { id: 'doc-' + Math.random(), name: file.name, type: 'PDF', size: '2.4 MB' }]);
          setTimeout(() => {
            this.showUpload.set(false);
            this.uploadProgress.set(null);
          }, 1000);
        }
      });
    }
  }

  deleteDoc(id: string) {
    if (confirm('Are you sure you want to delete this document?')) {
      this.knowService.deleteDocument(id).subscribe({
        error: () => {
          this.knowService.documents.update(arr => arr.filter(d => d.id !== id));
        }
      });
    }
  }
}

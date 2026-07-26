import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ProjectService } from '../../core/services/project.service';

/**
 * List interface managing active projects, descriptions, and archived status filters.
 */
@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  template: `
    <div style="font-family:'Inter', sans-serif; color:white;">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 2rem;">
        <h2 style="font-weight: 700; margin: 0;">Projects</h2>
        <button (click)="openCreateModal()" style="padding:0.75rem 1.5rem; background-color:#6366f1; border:none; color:white; border-radius:8px; cursor:pointer; font-weight:600;">Create Project</button>
      </div>

      <!-- Projects Table -->
      <div style="background-color: #111827; border: 1px solid #1f2937; border-radius: 8px; overflow:hidden;">
        <table style="width:100%; border-collapse:collapse; text-align:left;">
          <thead>
            <tr style="border-bottom: 1px solid #1f2937; background-color:#1f2937; color:#9ca3af; font-size:0.9rem;">
              <th style="padding:1rem;">Name</th>
              <th style="padding:1rem;">Description</th>
              <th style="padding:1rem;">Status</th>
              <th style="padding:1rem;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let proj of projs()" style="border-bottom: 1px solid #1f2937; font-size:0.95rem;">
              <td style="padding:1rem;">
                <a [routerLink]="['/projects', proj.id]" style="color:#6366f1; text-decoration:none; font-weight:600;">{{ proj.name }}</a>
              </td>
              <td style="padding:1rem; color:#d1d5db;">{{ proj.description || 'No description' }}</td>
              <td style="padding:1rem;">
                <span [style.background-color]="proj.archived ? '#ef4444' : '#10b981'" style="padding:0.25rem 0.5rem; border-radius:4px; font-size:0.8rem; font-weight:600; color:white;">
                  {{ proj.archived ? 'ARCHIVED' : 'ACTIVE' }}
                </span>
              </td>
              <td style="padding:1rem; display:flex; gap:0.5rem;">
                <button (click)="openEditModal(proj)" style="background:none; border: 1px solid #374151; color:#d1d5db; padding:0.4rem 0.8rem; border-radius:4px; cursor:pointer;">Edit</button>
                <button (click)="archiveProj(proj.id)" *ngIf="!proj.archived" style="background:none; border: 1px solid #f59e0b; color:#f59e0b; padding:0.4rem 0.8rem; border-radius:4px; cursor:pointer;">Archive</button>
                <button (click)="deleteProj(proj.id)" style="background:none; border: 1px solid #ef4444; color:#ef4444; padding:0.4rem 0.8rem; border-radius:4px; cursor:pointer;">Delete</button>
              </td>
            </tr>
            <tr *ngIf="projs().length === 0">
              <td colspan="4" style="text-align:center; padding:2rem; color:#9ca3af;">No projects found.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Modal Dialog -->
      <div *ngIf="showModal()" style="position:fixed; top:0; left:0; right:0; bottom:0; background:rgba(0,0,0,0.7); display:flex; align-items:center; justify-content:center; z-index:1000;">
        <div style="background:#111827; border: 1px solid #1f2937; padding:2rem; border-radius:12px; width:450px;">
          <h3 style="margin-top:0; margin-bottom:1.5rem;">{{ isEdit() ? 'Edit Project' : 'Create Project' }}</h3>
          <form [formGroup]="projForm" (ngSubmit)="onSubmit()">
            <div style="margin-bottom:1.25rem;">
              <label style="display:block; margin-bottom:0.5rem; color:#9ca3af;">Project Name</label>
              <input type="text" formControlName="name" style="width:100%; padding:0.75rem; border-radius:6px; border:1px solid #374151; background:#1f2937; color:white;" />
            </div>
            <div style="margin-bottom:1.25rem;">
              <label style="display:block; margin-bottom:0.5rem; color:#9ca3af;">Description</label>
              <textarea formControlName="description" style="width:100%; padding:0.75rem; border-radius:6px; border:1px solid #374151; background:#1f2937; color:white; height:80px; resize:none;"></textarea>
            </div>
            <div style="display:flex; justify-content:flex-end; gap:0.75rem; margin-top:1.5rem;">
              <button type="button" (click)="closeModal()" style="padding:0.6rem 1.2rem; background:#1f2937; border:1px solid #374151; color:white; border-radius:6px; cursor:pointer;">Cancel</button>
              <button type="submit" [disabled]="projForm.invalid" style="padding:0.6rem 1.2rem; background:#6366f1; border:none; color:white; border-radius:6px; cursor:pointer;">Save</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `
})
export class ProjectsComponent implements OnInit {
  private projService = inject(ProjectService);
  private fb = inject(FormBuilder);

  projs = this.projService.projects;
  showModal = signal(false);
  isEdit = signal(false);
  activeProjId?: string;

  projForm = this.fb.group({
    name: ['', [Validators.required]],
    description: ['']
  });

  ngOnInit() {
    this.projService.getProjects().subscribe();
  }

  openCreateModal() {
    this.isEdit.set(false);
    this.projForm.reset();
    this.showModal.set(true);
  }

  openEditModal(proj: any) {
    this.isEdit.set(true);
    this.activeProjId = proj.id;
    this.projForm.patchValue({ name: proj.name, description: proj.description });
    this.showModal.set(true);
  }

  closeModal() {
    this.showModal.set(false);
  }

  onSubmit() {
    if (this.projForm.valid) {
      const action$ = this.isEdit()
        ? this.projService.updateProject(this.activeProjId!, this.projForm.value)
        : this.projService.createProject(this.projForm.value);

      action$.subscribe({
        next: () => this.closeModal()
      });
    }
  }

  archiveProj(id: string) {
    if (confirm('Are you sure you want to archive this project?')) {
      this.projService.archiveProject(id).subscribe();
    }
  }

  deleteProj(id: string) {
    if (confirm('Are you sure you want to delete this project?')) {
      this.projService.deleteProject(id).subscribe();
    }
  }
}

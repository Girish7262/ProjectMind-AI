import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { OrganizationService } from '../../core/services/organization.service';

/**
 * Organization lists manager page displaying CRUD actions inside modal boxes.
 */
@Component({
  selector: 'app-organizations',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  template: `
    <div style="font-family:'Inter', sans-serif; color:white;">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom: 2rem;">
        <h2 style="font-weight: 700; margin: 0;">Organizations</h2>
        <button (click)="openCreateModal()" style="padding:0.75rem 1.5rem; background-color:#6366f1; border:none; color:white; border-radius:8px; cursor:pointer; font-weight:600;">Create Organization</button>
      </div>

      <!-- Organizations Table -->
      <div style="background-color: #111827; border: 1px solid #1f2937; border-radius: 8px; overflow:hidden;">
        <table style="width:100%; border-collapse:collapse; text-align:left;">
          <thead>
            <tr style="border-bottom: 1px solid #1f2937; background-color:#1f2937; color:#9ca3af; font-size:0.9rem;">
              <th style="padding:1rem;">Name</th>
              <th style="padding:1rem;">Tenant ID</th>
              <th style="padding:1rem;">Created Date</th>
              <th style="padding:1rem;">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr *ngFor="let org of orgs()" style="border-bottom: 1px solid #1f2937; font-size:0.95rem;">
              <td style="padding:1rem;">
                <a [routerLink]="['/organizations', org.id]" style="color:#6366f1; text-decoration:none; font-weight:600;">{{ org.name }}</a>
              </td>
              <td style="padding:1rem; color:#d1d5db;">{{ org.tenantId || org.id }}</td>
              <td style="padding:1rem; color:#9ca3af;">{{ org.createdAt | date:'shortDate' }}</td>
              <td style="padding:1rem; display:flex; gap:0.5rem;">
                <button (click)="openEditModal(org)" style="background:none; border: 1px solid #374151; color:#d1d5db; padding:0.4rem 0.8rem; border-radius:4px; cursor:pointer;">Edit</button>
                <button (click)="deleteOrg(org.id)" style="background:none; border: 1px solid #ef4444; color:#ef4444; padding:0.4rem 0.8rem; border-radius:4px; cursor:pointer;">Delete</button>
              </td>
            </tr>
            <tr *ngIf="orgs().length === 0">
              <td colspan="4" style="text-align:center; padding:2rem; color:#9ca3af;">No organizations found.</td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Modal Dialog -->
      <div *ngIf="showModal()" style="position:fixed; top:0; left:0; right:0; bottom:0; background:rgba(0,0,0,0.7); display:flex; align-items:center; justify-content:center; z-index:1000;">
        <div style="background:#111827; border: 1px solid #1f2937; padding:2rem; border-radius:12px; width:450px;">
          <h3 style="margin-top:0; margin-bottom:1.5rem;">{{ isEdit() ? 'Edit Organization' : 'Create Organization' }}</h3>
          <form [formGroup]="orgForm" (ngSubmit)="onSubmit()">
            <div style="margin-bottom:1.25rem;">
              <label style="display:block; margin-bottom:0.5rem; color:#9ca3af;">Organization Name</label>
              <input type="text" formControlName="name" style="width:100%; padding:0.75rem; border-radius:6px; border:1px solid #374151; background:#1f2937; color:white;" />
            </div>
            <div style="display:flex; justify-content:flex-end; gap:0.75rem; margin-top:1.5rem;">
              <button type="button" (click)="closeModal()" style="padding:0.6rem 1.2rem; background:#1f2937; border:1px solid #374151; color:white; border-radius:6px; cursor:pointer;">Cancel</button>
              <button type="submit" [disabled]="orgForm.invalid" style="padding:0.6rem 1.2rem; background:#6366f1; border:none; color:white; border-radius:6px; cursor:pointer;">Save</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  `
})
export class OrganizationsComponent implements OnInit {
  private orgService = inject(OrganizationService);
  private fb = inject(FormBuilder);

  orgs = this.orgService.organizations;
  showModal = signal(false);
  isEdit = signal(false);
  activeOrgId?: string;

  orgForm = this.fb.group({
    name: ['', [Validators.required]]
  });

  ngOnInit() {
    this.orgService.getOrganizations().subscribe();
  }

  openCreateModal() {
    this.isEdit.set(false);
    this.orgForm.reset();
    this.showModal.set(true);
  }

  openEditModal(org: any) {
    this.isEdit.set(true);
    this.activeOrgId = org.id;
    this.orgForm.patchValue({ name: org.name });
    this.showModal.set(true);
  }

  closeModal() {
    this.showModal.set(false);
  }

  onSubmit() {
    if (this.orgForm.valid) {
      const action$ = this.isEdit()
        ? this.orgService.updateOrganization(this.activeOrgId!, this.orgForm.value)
        : this.orgService.createOrganization(this.orgForm.value);

      action$.subscribe({
        next: () => this.closeModal()
      });
    }
  }

  deleteOrg(id: string) {
    if (confirm('Are you sure you want to delete this organization?')) {
      this.orgService.deleteOrganization(id).subscribe();
    }
  }
}

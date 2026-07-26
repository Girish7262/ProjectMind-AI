import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { OrganizationService } from '../../core/services/organization.service';

/**
 * Organization details management panel rendering system tags and members list columns.
 */
@Component({
  selector: 'app-organization-details',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  template: `
    <div *ngIf="org()" style="font-family:'Inter', sans-serif; color:white;">
      <div style="margin-bottom: 2rem;">
        <a routerLink="/organizations" style="color:#9ca3af; text-decoration:none; font-weight:600;">← Back to Organizations</a>
        <h2 style="margin-top:1rem; margin-bottom:0.25rem; font-weight:700;">{{ org().name }}</h2>
        <span style="color:#9ca3af; font-size:0.9rem;">Tenant ID: {{ org().id }}</span>
      </div>

      <!-- Grid Layout -->
      <div style="display: grid; grid-template-columns: 1fr 2fr; gap: 2rem;">
        <!-- Metadata Details Card -->
        <div style="background-color:#111827; border:1px solid #1f2937; border-radius:8px; padding:1.5rem; height:fit-content;">
          <h3 style="margin-top:0; margin-bottom:1rem; font-weight:600;">Details</h3>
          <div style="display:flex; flex-direction:column; gap:0.75rem; font-size:0.95rem;">
            <div><span style="color:#9ca3af;">Created:</span> {{ org().createdAt | date:'mediumDate' }}</div>
            <div><span style="color:#9ca3af;">Status:</span> <span style="color:#10b981; font-weight:600;">ACTIVE</span></div>
          </div>
        </div>

        <!-- Members Management -->
        <div style="background-color:#111827; border:1px solid #1f2937; border-radius:8px; padding:1.5rem;">
          <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1.5rem;">
            <h3 style="margin-top:0; margin-bottom:0; font-weight:600;">Members</h3>
            <button (click)="showInvite.set(true)" style="padding:0.5rem 1rem; background-color:#6366f1; border:none; color:white; border-radius:6px; cursor:pointer; font-weight:600;">Invite Member</button>
          </div>

          <!-- Invite Drawer Dialog -->
          <div *ngIf="showInvite()" style="margin-bottom:1.5rem; background-color:#1f2937; padding:1rem; border-radius:6px; border:1px solid #374151;">
            <h4 style="margin-top:0; margin-bottom:1rem;">Invite Member</h4>
            <form [formGroup]="inviteForm" (ngSubmit)="onInvite()" style="display:flex; gap:1rem; align-items:flex-end;">
              <div style="flex-grow:1;">
                <label style="display:block; margin-bottom:0.25rem; font-size:0.85rem; color:#9ca3af;">Email</label>
                <input type="email" formControlName="email" style="width:100%; padding:0.5rem; border-radius:4px; border:1px solid #374151; background:#111827; color:white;" />
              </div>
              <div>
                <label style="display:block; margin-bottom:0.25rem; font-size:0.85rem; color:#9ca3af;">Role</label>
                <select formControlName="role" style="padding:0.5rem; border-radius:4px; border:1px solid #374151; background:#111827; color:white; width:120px;">
                  <option value="MEMBER">Member</option>
                  <option value="ADMIN">Admin</option>
                </select>
              </div>
              <button type="submit" [disabled]="inviteForm.invalid" style="padding:0.5rem 1rem; background-color:#10b981; border:none; color:white; border-radius:4px; cursor:pointer; font-weight:600;">Send</button>
              <button type="button" (click)="showInvite.set(false)" style="padding:0.5rem 1rem; background:#374151; border:none; color:white; border-radius:4px; cursor:pointer;">Cancel</button>
            </form>
          </div>

          <!-- Members list -->
          <div style="display:flex; flex-direction:column; gap:0.75rem;">
            <div *ngFor="let member of members()" style="display:flex; justify-content:space-between; align-items:center; background-color:#1f2937; padding:0.75rem; border-radius:6px;">
              <div>
                <div style="font-weight:600;">{{ member.email }}</div>
                <div style="font-size:0.8rem; color:#9ca3af;">Role: {{ member.role }}</div>
              </div>
              <button (click)="removeMember(member.id)" style="background:none; border:1px solid #ef4444; color:#ef4444; padding:0.3rem 0.6rem; border-radius:4px; cursor:pointer; font-size:0.85rem;">Remove</button>
            </div>
            <div *ngIf="members().length === 0" style="color:#9ca3af; text-align:center; padding:1.5rem;">
              No members found. Invite some members to get started.
            </div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class OrganizationDetailsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private orgService = inject(OrganizationService);
  private fb = inject(FormBuilder);

  org = signal<any>(null);
  members = signal<any[]>([]);
  showInvite = signal(false);

  inviteForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    role: ['MEMBER', [Validators.required]]
  });

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.orgService.getOrganizationById(id).subscribe({
        next: res => this.org.set(res),
        error: () => {
          // Offline fallback
          this.org.set({ id, name: 'Demo Organization', createdAt: new Date() });
        }
      });
      this.loadMembers(id);
    }
  }

  loadMembers(id: string) {
    this.orgService.getMembers(id).subscribe({
      next: res => this.members.set(res),
      error: () => {
        // Offline fallback
        this.members.set([
          { id: 'm1', email: 'admin@projectmind.com', role: 'ADMIN' },
          { id: 'm2', email: 'user@projectmind.com', role: 'MEMBER' }
        ]);
      }
    });
  }

  onInvite() {
    if (this.inviteForm.valid) {
      const orgId = this.org().id;
      const { email, role } = this.inviteForm.value;
      this.orgService.inviteMember(orgId, email!, role!).subscribe({
        next: () => {
          this.loadMembers(orgId);
          this.showInvite.set(false);
          this.inviteForm.reset({ role: 'MEMBER' });
        },
        error: () => {
          // Offline local list append simulation
          this.members.update(arr => [...arr, { id: 'mock-' + Math.random(), email: email!, role: role! }]);
          this.showInvite.set(false);
          this.inviteForm.reset({ role: 'MEMBER' });
        }
      });
    }
  }

  removeMember(memberId: string) {
    if (confirm('Are you sure you want to remove this member?')) {
      const orgId = this.org().id;
      this.orgService.removeMember(orgId, memberId).subscribe({
        next: () => this.loadMembers(orgId),
        error: () => {
          // Offline local list remove simulation
          this.members.update(arr => arr.filter(m => m.id !== memberId));
        }
      });
    }
  }
}

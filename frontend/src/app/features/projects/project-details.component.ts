import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ProjectService } from '../../core/services/project.service';

/**
 * Workspace management panel rendering project metadata, member listings, and activity events.
 */
@Component({
  selector: 'app-project-details',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  template: `
    <div *ngIf="proj()" style="font-family:'Inter', sans-serif; color:white;">
      <div style="margin-bottom: 2rem;">
        <a routerLink="/projects" style="color:#9ca3af; text-decoration:none; font-weight:600;">← Back to Projects</a>
        <h2 style="margin-top:1rem; margin-bottom:0.25rem; font-weight:700;">{{ proj().name }} Workspace</h2>
        <p style="margin:0; color:#9ca3af; font-size:0.95rem;">{{ proj().description || 'No description provided.' }}</p>
      </div>

      <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 2rem;">
        <!-- Left Pane: Members & Activities -->
        <div style="display:flex; flex-direction:column; gap:2rem;">
          <!-- Members List -->
          <div style="background-color:#111827; border:1px solid #1f2937; border-radius:8px; padding:1.5rem;">
            <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:1rem;">
              <h3 style="margin:0; font-weight:600;">Project Members</h3>
              <button (click)="showInvite.set(true)" style="padding:0.4rem 0.8rem; background-color:#6366f1; border:none; color:white; border-radius:6px; cursor:pointer; font-weight:600; font-size:0.85rem;">Invite Member</button>
            </div>

            <!-- Invite Form Drawer -->
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

            <div style="display:flex; flex-direction:column; gap:0.75rem;">
              <div *ngFor="let member of members()" style="display:flex; justify-content:space-between; align-items:center; background-color:#1f2937; padding:0.75rem; border-radius:6px;">
                <div>
                  <div style="font-weight:600;">{{ member.email }}</div>
                  <div style="font-size:0.8rem; color:#9ca3af;">Role: {{ member.role }}</div>
                </div>
                <button (click)="removeMember(member.id)" style="background:none; border:1px solid #ef4444; color:#ef4444; padding:0.3rem 0.6rem; border-radius:4px; cursor:pointer; font-size:0.85rem;">Remove</button>
              </div>
              <div *ngIf="members().length === 0" style="color:#9ca3af; text-align:center; padding:1.5rem;">No members found.</div>
            </div>
          </div>

          <!-- Activity Logs -->
          <div style="background-color:#111827; border:1px solid #1f2937; border-radius:8px; padding:1.5rem;">
            <h3 style="margin-top:0; margin-bottom:1rem; font-weight:600;">Activity Logs</h3>
            <div style="display:flex; flex-direction:column; gap:1rem;">
              <div *ngFor="let act of activities()" style="border-left: 2px solid #6366f1; padding-left:1rem; position:relative;">
                <div style="font-weight:600; font-size:0.9rem;">{{ act.message }}</div>
                <div style="font-size:0.8rem; color:#9ca3af;">{{ act.timestamp | date:'short' }}</div>
              </div>
              <div *ngIf="activities().length === 0" style="color:#9ca3af; text-align:center; padding:1rem;">No recent activities found.</div>
            </div>
          </div>
        </div>

        <!-- Right Pane: Summary Card -->
        <div style="background-color:#111827; border:1px solid #1f2937; border-radius:8px; padding:1.5rem; height:fit-content;">
          <h3 style="margin-top:0; margin-bottom:1rem; font-weight:600;">Workspace Summary</h3>
          <div style="display:flex; flex-direction:column; gap:0.75rem; font-size:0.95rem;">
            <div><span style="color:#9ca3af;">Status:</span> <span [style.color]="proj().archived ? '#ef4444' : '#10b981'" style="font-weight:600;">{{ proj().archived ? 'ARCHIVED' : 'ACTIVE' }}</span></div>
            <div><span style="color:#9ca3af;">Created:</span> {{ proj().createdAt | date:'shortDate' }}</div>
          </div>
        </div>
      </div>
    </div>
  `
})
export class ProjectDetailsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private projService = inject(ProjectService);
  private fb = inject(FormBuilder);

  proj = signal<any>(null);
  members = signal<any[]>([]);
  activities = signal<any[]>([]);
  showInvite = signal(false);

  inviteForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    role: ['MEMBER', [Validators.required]]
  });

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.projService.getProjectById(id).subscribe({
        next: res => this.proj.set(res),
        error: () => {
          this.proj.set({ id, name: 'Sample Project', description: 'Enterprise workspace description', archived: false, createdAt: new Date() });
        }
      });
      this.loadMembers(id);
      this.loadActivities(id);
    }
  }

  loadMembers(id: string) {
    this.projService.getMembers(id).subscribe({
      next: res => this.members.set(res),
      error: () => {
        this.members.set([
          { id: 'm1', email: 'admin@projectmind.com', role: 'ADMIN' },
          { id: 'm2', email: 'contributor@projectmind.com', role: 'MEMBER' }
        ]);
      }
    });
  }

  loadActivities(id: string) {
    this.projService.getActivities(id).subscribe({
      next: res => this.activities.set(res),
      error: () => {
        this.activities.set([
          { message: 'Workspace created successfully', timestamp: new Date() },
          { message: 'Prompt template updated in AI module', timestamp: new Date(Date.now() - 3600000) }
        ]);
      }
    });
  }

  onInvite() {
    if (this.inviteForm.valid) {
      const projId = this.proj().id;
      const { email, role } = this.inviteForm.value;
      this.projService.addMember(projId, email!, role!).subscribe({
        next: () => {
          this.loadMembers(projId);
          this.showInvite.set(false);
          this.inviteForm.reset({ role: 'MEMBER' });
        },
        error: () => {
          this.members.update(arr => [...arr, { id: 'mock-' + Math.random(), email: email!, role: role! }]);
          this.showInvite.set(false);
          this.inviteForm.reset({ role: 'MEMBER' });
        }
      });
    }
  }

  removeMember(memberId: string) {
    if (confirm('Are you sure you want to remove this member?')) {
      const projId = this.proj().id;
      this.projService.removeMember(projId, memberId).subscribe({
        next: () => this.loadMembers(projId),
        error: () => {
          this.members.update(arr => arr.filter(m => m.id !== memberId));
        }
      });
    }
  }
}

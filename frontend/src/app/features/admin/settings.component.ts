import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { AdminService } from '../../core/services/admin.service';

/**
 * Administration console panel routing child dashboard. Supports user roles assignments, models parameters, and audit history.
 */
@Component({
  selector: 'app-admin-settings',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div style="font-family:'Inter', sans-serif; color:white;">
      <h2 style="font-weight:700; margin-bottom:1.5rem;">Administration Console</h2>

      <!-- Navigation Tabs -->
      <div style="display:flex; gap:1.5rem; border-bottom:1px solid #1f2937; margin-bottom:2rem; padding-bottom:0.5rem;">
        <button *ngFor="let tab of tabs" (click)="activeTab.set(tab.id)" [style.color]="activeTab() === tab.id ? '#6366f1' : '#9ca3af'" [style.border-bottom]="activeTab() === tab.id ? '2px solid #6366f1' : 'none'" style="background:none; border:none; padding:0.5rem 1rem; cursor:pointer; font-weight:600; font-size:1rem; transition:all 0.3s;">
          {{ tab.name }}
        </button>
      </div>

      <!-- Tab Content Area -->
      <div [ngSwitch]="activeTab()">
        <!-- User Management -->
        <div *ngSwitchCase="'users'" style="background-color:#111827; border:1px solid #1f2937; border-radius:8px; overflow:hidden;">
          <table style="width:100%; border-collapse:collapse; text-align:left;">
            <thead>
              <tr style="border-bottom: 1px solid #1f2937; background-color:#1f2937; color:#9ca3af; font-size:0.9rem;">
                <th style="padding:1rem;">Username</th>
                <th style="padding:1rem;">Email</th>
                <th style="padding:1rem;">Role</th>
                <th style="padding:1rem;">Status</th>
                <th style="padding:1rem;">Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let u of users()" style="border-bottom: 1px solid #1f2937; font-size:0.95rem;">
                <td style="padding:1rem; font-weight:600;">{{ u.username }}</td>
                <td style="padding:1rem; color:#d1d5db;">{{ u.email }}</td>
                <td style="padding:1rem;">
                  <select [value]="u.role" (change)="changeUserRole(u.id, $event)" style="background:#1f2937; border:1px solid #374151; color:white; padding:0.25rem; border-radius:4px;">
                    <option value="ADMIN">Admin</option>
                    <option value="MEMBER">Member</option>
                  </select>
                </td>
                <td style="padding:1rem;">
                  <span style="color:#10b981; font-weight:600;">{{ u.status }}</span>
                </td>
                <td style="padding:1rem;">
                  <button style="background:none; border:1px solid #ef4444; color:#ef4444; padding:0.3rem 0.6rem; border-radius:4px; cursor:pointer;">Disable</button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Role Permissions Matrix -->
        <div *ngSwitchCase="'roles'" style="background-color:#111827; border:1px solid #1f2937; border-radius:8px; padding:1.5rem;">
          <h3 style="margin-top:0; margin-bottom:1rem; font-weight:600;">Permissions Matrix</h3>
          <div style="display:flex; flex-direction:column; gap:1rem;">
            <div *ngFor="let r of roles()" style="display:flex; justify-content:space-between; align-items:center; background-color:#1f2937; padding:1rem; border-radius:6px; border:1px solid #374151;">
              <span style="font-weight:600; font-size:1rem;">{{ r.name }}</span>
              <div style="display:flex; gap:0.5rem;">
                <span *ngFor="let p of r.permissions" style="background-color:#6366f1; font-size:0.8rem; padding:0.25rem 0.5rem; border-radius:4px;">{{ p }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- AI Configurations Settings -->
        <div *ngSwitchCase="'ai'" style="background-color:#111827; border:1px solid #1f2937; border-radius:8px; padding:1.5rem; width:550px;">
          <h3 style="margin-top:0; margin-bottom:1.5rem; font-weight:600;">LLM Provider Defaults</h3>
          <form [formGroup]="aiForm" (ngSubmit)="saveAiSettings()">
            <div style="margin-bottom:1.25rem;">
              <label style="display:block; margin-bottom:0.5rem; color:#9ca3af;">Default Model</label>
              <select formControlName="defaultModel" style="width:100%; padding:0.75rem; border-radius:6px; border:1px solid #374151; background:#1f2937; color:white;">
                <option value="gemini-1.5-pro">Gemini 1.5 Pro</option>
                <option value="gpt-4o">GPT-4o Enterprise</option>
                <option value="claude-3-5-sonnet">Claude 3.5 Sonnet</option>
              </select>
            </div>
            <div style="margin-bottom:1.25rem;">
              <label style="display:block; margin-bottom:0.5rem; color:#9ca3af;">Temperature (0.0 to 1.0)</label>
              <input type="number" step="0.1" min="0" max="1" formControlName="temperature" style="width:100%; padding:0.75rem; border-radius:6px; border:1px solid #374151; background:#1f2937; color:white;" />
            </div>
            <button type="submit" [disabled]="aiForm.invalid" style="padding:0.6rem 1.2rem; background:#6366f1; border:none; color:white; border-radius:6px; cursor:pointer; font-weight:600;">Save Parameters</button>
            <div *ngIf="successMessage()" style="color:#10b981; font-size:0.9rem; margin-top:1rem;">
              {{ successMessage() }}
            </div>
          </form>
        </div>

        <!-- Audit History Logs -->
        <div *ngSwitchCase="'audit'" style="background-color:#111827; border:1px solid #1f2937; border-radius:8px; overflow:hidden;">
          <table style="width:100%; border-collapse:collapse; text-align:left;">
            <thead>
              <tr style="border-bottom: 1px solid #1f2937; background-color:#1f2937; color:#9ca3af; font-size:0.9rem;">
                <th style="padding:1rem;">Event Log</th>
                <th style="padding:1rem;">Actor</th>
                <th style="padding:1rem;">Timestamp</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let log of auditLogs()" style="border-bottom: 1px solid #1f2937; font-size:0.95rem;">
                <td style="padding:1rem; font-weight:600;">{{ log.event }}</td>
                <td style="padding:1rem; color:#d1d5db;">{{ log.actor }}</td>
                <td style="padding:1rem; color:#9ca3af;">{{ log.timestamp | date:'short' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `
})
export class AdminSettingsComponent implements OnInit {
  private adminService = inject(AdminService);
  private fb = inject(FormBuilder);

  tabs = [
    { id: 'users', name: 'User Management' },
    { id: 'roles', name: 'Permissions Matrix' },
    { id: 'ai', name: 'AI Configs' },
    { id: 'audit', name: 'Audit Logs' }
  ];

  activeTab = signal('users');
  users = this.adminService.users;
  roles = this.adminService.roles;
  auditLogs = signal<any[]>([]);
  successMessage = signal<string | null>(null);

  aiForm = this.fb.group({
    defaultModel: ['gemini-1.5-pro', [Validators.required]],
    temperature: [0.7, [Validators.required, Validators.min(0), Validators.max(1)]]
  });

  ngOnInit() {
    this.adminService.getUsers().subscribe();
    this.adminService.getRoles().subscribe();
    this.adminService.getAuditLogs().subscribe(res => this.auditLogs.set(res));
  }

  changeUserRole(id: string, event: Event) {
    const select = event.target as HTMLSelectElement;
    this.adminService.updateUserRole(id, select.value).subscribe();
  }

  saveAiSettings() {
    if (this.aiForm.valid) {
      this.successMessage.set('AI default preferences saved.');
      setTimeout(() => this.successMessage.set(null), 2000);
    }
  }
}

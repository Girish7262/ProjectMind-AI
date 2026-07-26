import { Component, inject } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { AuthService } from '../core/services/auth.service';
import { ProfileMenuComponent } from '../features/profile/profile-menu.component';

/**
 * Enterprise dashboard workspace layout.
 */
@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, ProfileMenuComponent],
  template: `
    <div class="app-container">
      <aside class="sidebar">
        <div class="logo">ProjectMind AI</div>
        <nav class="nav-menu">
          <a routerLink="/dashboard" class="nav-item">Dashboard</a>
          <a routerLink="/organizations" class="nav-item">Organizations</a>
          <a routerLink="/projects" class="nav-item">Projects</a>
          <a routerLink="/knowledge" class="nav-item">Knowledge Base</a>
          <a routerLink="/ai-chat" class="nav-item">AI Console</a>
        </nav>
        <button class="logout-btn" (click)="logout()">Sign Out</button>
      </aside>
      <main class="main-content">
        <header class="top-nav">
          <div class="breadcrumb">Workspace / Active Dashboard</div>
          <app-profile-menu></app-profile-menu>
        </header>
        <section class="page-body">
          <router-outlet></router-outlet>
        </section>
      </main>
    </div>
  `,
  styles: [`
    .app-container {
      display: flex;
      min-height: 100vh;
      background-color: #0b0f19;
      color: #f3f4f6;
      font-family: 'Inter', sans-serif;
    }
    .sidebar {
      width: 260px;
      background-color: #111827;
      display: flex;
      flex-direction: column;
      padding: 1.5rem;
      border-right: 1px solid #1f2937;
    }
    .logo {
      font-size: 1.5rem;
      font-weight: 700;
      margin-bottom: 2rem;
      color: #6366f1;
    }
    .nav-menu {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
      flex-grow: 1;
    }
    .nav-item {
      padding: 0.75rem 1rem;
      border-radius: 8px;
      color: #9ca3af;
      text-decoration: none;
      transition: all 0.3s;
    }
    .nav-item:hover, .nav-item:focus {
      background-color: #1f2937;
      color: #ffffff;
    }
    .logout-btn {
      background: none;
      border: 1px solid #dc2626;
      color: #dc2626;
      padding: 0.75rem;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;
    }
    .logout-btn:hover {
      background-color: #dc2626;
      color: white;
    }
    .main-content {
      flex-grow: 1;
      display: flex;
      flex-direction: column;
    }
    .top-nav {
      height: 64px;
      background-color: #111827;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 2rem;
      border-bottom: 1px solid #1f2937;
    }
    .page-body {
      padding: 2rem;
      flex-grow: 1;
      overflow-y: auto;
    }
  `]
})
export class AppLayoutComponent {
  authService = inject(AuthService);

  logout(): void {
    this.authService.logout();
  }
}

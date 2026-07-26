import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

/**
 * Platform standalone route registry mapping authenticated workspaces, administrators panel, and error states.
 */
export const routes: Routes = [
  // Public Route Layout
  {
    path: 'auth',
    loadComponent: () => import('./layout/login-layout').then(m => m.LoginLayoutComponent),
    children: [
      {
        path: 'login',
        loadComponent: () => import('./features/auth/login.component').then(m => m.LoginComponent)
      },
      {
        path: 'register',
        loadComponent: () => import('./features/auth/register.component').then(m => m.RegisterComponent)
      },
      {
        path: 'forgot-password',
        loadComponent: () => import('./features/auth/forgot-password.component').then(m => m.ForgotPasswordComponent)
      },
      { path: '', redirectTo: 'login', pathMatch: 'full' }
    ]
  },

  // Authenticated App Workspace Layout
  {
    path: '',
    loadComponent: () => import('./layout/app-layout').then(m => m.AppLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'organizations',
        loadComponent: () => import('./features/organizations/organizations.component').then(m => m.OrganizationsComponent)
      },
      {
        path: 'organizations/:id',
        loadComponent: () => import('./features/organizations/organization-details.component').then(m => m.OrganizationDetailsComponent)
      },
      {
        path: 'projects',
        loadComponent: () => import('./features/projects/projects.component').then(m => m.ProjectsComponent)
      },
      {
        path: 'projects/:id',
        loadComponent: () => import('./features/projects/project-details.component').then(m => m.ProjectDetailsComponent)
      },
      {
        path: 'knowledge',
        loadComponent: () => import('./features/knowledge/knowledge.component').then(m => m.KnowledgeComponent)
      },
      {
        path: 'knowledge/:id',
        loadComponent: () => import('./features/knowledge/knowledge-details.component').then(m => m.KnowledgeDetailsComponent)
      },
      {
        path: 'ai-chat',
        loadComponent: () => import('./features/ai-chat/ai-chat.component').then(m => m.AiChatComponent)
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  // Admin Panel Layout
  {
    path: 'admin',
    loadComponent: () => import('./layout/admin-layout').then(m => m.AdminLayoutComponent),
    canActivate: [authGuard, roleGuard],
    data: { expectedRoles: ['ADMIN'] },
    children: [
      {
        path: 'settings',
        loadComponent: () => import('./features/admin/settings.component').then(m => m.AdminSettingsComponent)
      }
    ]
  },

  // Error page Layout
  {
    path: 'error',
    loadComponent: () => import('./layout/error-layout').then(m => m.ErrorLayoutComponent),
    children: [
      {
        path: '403',
        loadComponent: () => import('./features/error/403.component').then(m => m.ForbiddenComponent)
      },
      {
        path: '404',
        loadComponent: () => import('./features/error/404.component').then(m => m.NotFoundComponent)
      }
    ]
  },

  { path: '**', redirectTo: 'error/404' }
];

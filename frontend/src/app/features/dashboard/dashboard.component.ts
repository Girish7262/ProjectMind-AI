import { Component, inject, OnInit, AfterViewInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DashboardService } from '../../core/services/dashboard.service';
import { SystemHealthComponent } from './widgets/system-health.component';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

/**
 * Enterprise analytics dashboard layout incorporating statistics widgets, Chart.js trends, and quick operations.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, SystemHealthComponent],
  template: `
    <div class="dashboard-wrapper" style="font-family:'Inter', sans-serif; color:white;">
      <!-- Welcome Banner -->
      <div style="background: linear-gradient(135deg, #1e1b4b, #311042); border: 1px solid #312e81; padding: 2rem; border-radius: 12px; margin-bottom: 2rem;">
        <h1 style="margin: 0 0 0.5rem 0; font-size: 1.8rem; font-weight:700;">Welcome to ProjectMind AI Admin Console</h1>
        <p style="margin: 0; color: #a5b4fc; font-size: 1rem;">AI Knowledge Continuity dashboard and system telemetry overview.</p>
      </div>

      <!-- Quick Search Bar (Mock Ctrl+K) -->
      <div style="margin-bottom: 2rem; display:flex; gap:1rem; align-items:center; background-color:#111827; border: 1px solid #1f2937; padding: 0.75rem 1.25rem; border-radius: 8px;">
        <span style="color:#9ca3af;">🔍 Search anything...</span>
        <input type="text" placeholder="Type projects, metrics or models name..." style="background:none; border:none; color:white; flex-grow:1; outline:none;" />
        <span style="background-color:#1f2937; border:1px solid #374151; color:#9ca3af; padding:0.25rem 0.5rem; border-radius:4px; font-size:0.8rem;">Ctrl + K</span>
      </div>

      <!-- Stats Cards Grid -->
      <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 1.5rem; margin-bottom: 2rem;">
        <div class="stat-card" style="background-color: #111827; border: 1px solid #1f2937; border-radius: 8px; padding: 1.5rem;">
          <div style="color: #9ca3af; font-size: 0.9rem; font-weight:600; margin-bottom: 0.5rem;">Organizations</div>
          <div style="font-size: 1.8rem; font-weight: 700; color: #6366f1;">{{ metrics().orgCount }}</div>
        </div>
        <div class="stat-card" style="background-color: #111827; border: 1px solid #1f2937; border-radius: 8px; padding: 1.5rem;">
          <div style="color: #9ca3af; font-size: 0.9rem; font-weight:600; margin-bottom: 0.5rem;">Projects</div>
          <div style="font-size: 1.8rem; font-weight: 700; color: #10b981;">{{ metrics().projectCount }}</div>
        </div>
        <div class="stat-card" style="background-color: #111827; border: 1px solid #1f2937; border-radius: 8px; padding: 1.5rem;">
          <div style="color: #9ca3af; font-size: 0.9rem; font-weight:600; margin-bottom: 0.5rem;">Knowledge Files</div>
          <div style="font-size: 1.8rem; font-weight: 700; color: #f59e0b;">{{ metrics().knowledgeCount }}</div>
        </div>
        <div class="stat-card" style="background-color: #111827; border: 1px solid #1f2937; border-radius: 8px; padding: 1.5rem;">
          <div style="color: #9ca3af; font-size: 0.9rem; font-weight:600; margin-bottom: 0.5rem;">AI Conversations</div>
          <div style="font-size: 1.8rem; font-weight: 700; color: #ec4899;">{{ metrics().conversationCount }}</div>
        </div>
      </div>

      <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 2rem; margin-bottom: 2rem;">
        <!-- Chart Widget -->
        <div style="background-color: #111827; border: 1px solid #1f2937; border-radius: 8px; padding: 1.5rem;">
          <h3 style="margin-top: 0; margin-bottom: 1.5rem; font-weight:600;">AI Usage Trends</h3>
          <div style="height: 250px; position:relative;">
            <canvas id="aiUsageChart"></canvas>
          </div>
        </div>

        <!-- System Status -->
        <app-system-health></app-system-health>
      </div>

      <!-- Quick Actions Panel -->
      <div style="background-color: #111827; border: 1px solid #1f2937; border-radius: 8px; padding: 1.5rem;">
        <h3 style="margin-top:0; margin-bottom:1rem; font-weight:600;">Quick Actions</h3>
        <div style="display:flex; gap:1rem;">
          <a routerLink="/projects" style="flex-grow:1; text-align:center; padding: 1rem; background-color:#1f2937; border:1px solid #374151; border-radius:8px; text-decoration:none; color:white; font-weight:600; transition:all 0.3s;">Manage Projects</a>
          <a routerLink="/knowledge" style="flex-grow:1; text-align:center; padding: 1rem; background-color:#1f2937; border:1px solid #374151; border-radius:8px; text-decoration:none; color:white; font-weight:600; transition:all 0.3s;">Upload Knowledge</a>
          <a routerLink="/ai-chat" style="flex-grow:1; text-align:center; padding: 1rem; background-color:#6366f1; border-radius:8px; text-decoration:none; color:white; font-weight:600; transition:all 0.3s;">Launch AI Console</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .stat-card {
      transition: transform 0.3s, box-shadow 0.3s;
    }
    .stat-card:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 20px rgba(99, 102, 241, 0.15);
    }
  `]
})
export class DashboardComponent implements OnInit, AfterViewInit {
  private dashboardService = inject(DashboardService);
  
  metrics = signal<any>({ orgCount: 0, projectCount: 0, knowledgeCount: 0, conversationCount: 0 });

  ngOnInit() {
    this.dashboardService.getDashboardMetrics().subscribe({
      next: (res) => this.metrics.set(res),
      error: () => {
        this.metrics.set({ orgCount: 4, projectCount: 12, knowledgeCount: 156, conversationCount: 1042 });
      }
    });
  }

  ngAfterViewInit() {
    this.initChart();
  }

  initChart() {
    const ctx = document.getElementById('aiUsageChart') as HTMLCanvasElement;
    if (ctx) {
      new Chart(ctx, {
        type: 'line',
        data: {
          labels: ['Week 1', 'Week 2', 'Week 3', 'Week 4', 'Week 5', 'Week 6'],
          datasets: [{
            label: 'Conversations Activity',
            data: [120, 245, 180, 480, 520, 710],
            borderColor: '#6366f1',
            backgroundColor: 'rgba(99, 102, 241, 0.1)',
            fill: true,
            tension: 0.4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: { grid: { color: '#1f2937' }, ticks: { color: '#9ca3af' } },
            x: { grid: { color: '#1f2937' }, ticks: { color: '#9ca3af' } }
          },
          plugins: {
            legend: { display: false }
          }
        }
      });
    }
  }
}

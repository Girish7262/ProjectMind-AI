import { Component, signal, inject, OnInit, OnDestroy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SessionManager } from './core/services/session-manager.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit, OnDestroy {
  protected readonly title = signal('frontend');
  private sessionManager = inject(SessionManager);

  ngOnInit() {
    this.sessionManager.init();
  }

  ngOnDestroy() {
    this.sessionManager.destroy();
  }
}

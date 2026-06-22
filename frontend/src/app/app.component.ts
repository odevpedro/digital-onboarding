import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <header class="header">
      <div class="header-content">
        <a routerLink="/" class="logo">
          <span class="logo-icon">B</span>
          <span class="logo-text">Digital Onboarding</span>
        </a>
        <nav class="nav">
          <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact:true}" class="nav-link">
            Propostas
          </a>
        </nav>
      </div>
    </header>
    <main class="main-content">
      <router-outlet></router-outlet>
    </main>
    <footer class="footer">
      <p>&copy; 2026 Banco Digital S.A. — Simulador de Onboarding</p>
    </footer>
  `,
  styles: [`
    .header {
      background: var(--color-dark);
      color: white;
      padding: 0 24px;
      position: sticky;
      top: 0;
      z-index: 100;
    }
    .header-content {
      max-width: 1200px;
      margin: 0 auto;
      display: flex;
      align-items: center;
      justify-content: space-between;
      height: 60px;
    }
    .logo {
      display: flex;
      align-items: center;
      gap: 12px;
      text-decoration: none;
      color: white;
    }
    .logo-icon {
      background: var(--color-primary);
      width: 36px;
      height: 36px;
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 20px;
    }
    .logo-text {
      font-weight: 600;
      font-size: 18px;
    }
    .nav { display: flex; gap: 8px; }
    .nav-link {
      color: rgba(255,255,255,0.7);
      text-decoration: none;
      padding: 8px 16px;
      border-radius: 6px;
      font-size: 14px;
      transition: all 0.2s;
    }
    .nav-link:hover, .nav-link.active {
      color: white;
      background: rgba(255,255,255,0.1);
    }
    .main-content {
      max-width: 1200px;
      margin: 24px auto;
      padding: 0 24px;
      min-height: calc(100vh - 120px);
    }
    .footer {
      text-align: center;
      padding: 16px;
      color: var(--color-text-light);
      font-size: 12px;
    }
  `]
})
export class AppComponent {}

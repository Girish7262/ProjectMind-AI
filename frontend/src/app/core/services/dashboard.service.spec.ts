import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { DashboardService } from './dashboard.service';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';

describe('DashboardService', () => {
  let service: DashboardService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DashboardService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(DashboardService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    service.clearCache();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve metrics and aggregate counts', () => {
    service.getDashboardMetrics().subscribe(metrics => {
      expect(metrics.orgCount).toBe(2);
      expect(metrics.projectCount).toBe(1);
    });

    const orgsReq = httpMock.expectOne('/api/v1/organizations');
    expect(orgsReq.request.method).toBe('GET');
    orgsReq.flush([{}, {}]);

    const projectsReq = httpMock.expectOne('/api/v1/projects');
    expect(projectsReq.request.method).toBe('GET');
    projectsReq.flush([{}]);

    const knowledgeReq = httpMock.expectOne('/api/v1/knowledge');
    expect(knowledgeReq.request.method).toBe('GET');
    knowledgeReq.flush([]);

    const conversationsReq = httpMock.expectOne('/api/v1/ai/conversations');
    expect(conversationsReq.request.method).toBe('GET');
    conversationsReq.flush([]);
  });
});

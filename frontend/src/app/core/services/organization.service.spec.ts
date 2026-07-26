import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { OrganizationService } from './organization.service';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';

describe('OrganizationService', () => {
  let service: OrganizationService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        OrganizationService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(OrganizationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve organizations list and cache in signal', () => {
    const mockOrgs = [{ id: 'org-1', name: 'Org One' }, { id: 'org-2', name: 'Org Two' }];
    service.getOrganizations().subscribe(res => {
      expect(res.length).toBe(2);
      expect(service.organizations().length).toBe(2);
    });

    const req = httpMock.expectOne('/api/v1/organizations');
    expect(req.request.method).toBe('GET');
    req.flush(mockOrgs);
  });
});

import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AdminService } from './admin.service';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';

describe('AdminService', () => {
  let service: AdminService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AdminService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve users list and cache in signal', () => {
    service.getUsers().subscribe(res => {
      expect(res.length).toBe(2);
      expect(service.users().length).toBe(2);
    });

    const req = httpMock.expectOne('/api/v1/admin/users');
    expect(req.request.method).toBe('GET');
    req.flush([
      { id: 'u1', username: 'user1' },
      { id: 'u2', username: 'user2' }
    ]);
  });
});

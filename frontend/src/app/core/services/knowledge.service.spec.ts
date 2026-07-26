import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { KnowledgeService } from './knowledge.service';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';

describe('KnowledgeService', () => {
  let service: KnowledgeService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        KnowledgeService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(KnowledgeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve documents list and cache in signal', () => {
    const mockDocs = [{ id: 'd-1', name: 'Document One' }, { id: 'd-2', name: 'Document Two' }];
    service.getDocuments().subscribe(res => {
      expect(res.length).toBe(2);
      expect(service.documents().length).toBe(2);
    });

    const req = httpMock.expectOne('/api/v1/knowledge');
    expect(req.request.method).toBe('GET');
    req.flush(mockDocs);
  });
});

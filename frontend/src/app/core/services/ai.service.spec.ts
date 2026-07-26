import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AiService } from './ai.service';
import { describe, beforeEach, afterEach, it, expect } from 'vitest';

describe('AiService', () => {
  let service: AiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AiService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });
    service = TestBed.inject(AiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve conversations list and cache in signal', () => {
    const mockConvs = [{ id: 'c-1', name: 'Chat One' }, { id: 'c-2', name: 'Chat Two' }];
    service.getConversations().subscribe(res => {
      expect(res.length).toBe(2);
      expect(service.conversations().length).toBe(2);
    });

    const req = httpMock.expectOne('/api/v1/ai/conversations');
    expect(req.request.method).toBe('GET');
    req.flush(mockConvs);
  });
});

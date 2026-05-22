import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { take } from 'rxjs';
import { SupplierService } from './supplier.service';
import { EnvironmentConfig } from '../../../core/types/providers/environment-config';
import { MOCK_SUPPLIERS } from '../../../core/mocks/data/products.mock';
import { MOCK_ENVIRONMENT_CONFIG } from '../../../core/mocks/data/environment.mock';

describe('SupplierService', () => {
    let service: SupplierService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                SupplierService,
                provideHttpClient(),
                provideHttpClientTesting(),
                {
                    provide: EnvironmentConfig,
                    useValue: MOCK_ENVIRONMENT_CONFIG
                }
            ]
        });

        service = TestBed.inject(SupplierService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    describe('Initialization', () => {
        it('should be created', () => {
            expect(service).toBeTruthy();
        });

        it('should initialize with empty signals', () => {
            expect(service.suppliers()).toEqual([]);
            expect(service.loading()).toBe(false);
            expect(service.error()).toBeNull();
        });
    });

    describe('loadAll()', () => {
        it('should load all suppliers successfully', () => {
            service.loadAll().pipe(take(1)).subscribe();

            const req = httpMock.expectOne(`${MOCK_ENVIRONMENT_CONFIG.apiUrl}/suppliers`);
            expect(req.request.method).toBe('GET');
            req.flush(MOCK_SUPPLIERS);

            expect(service.suppliers()).toEqual(MOCK_SUPPLIERS);
            expect(service.loading()).toBe(false);
            expect(service.error()).toBeNull();
        });

        it('should set loading to true while request is in flight', () => {
            service.loadAll().pipe(take(1)).subscribe();

            expect(service.loading()).toBe(true);

            const req = httpMock.expectOne(`${MOCK_ENVIRONMENT_CONFIG.apiUrl}/suppliers`);
            req.flush(MOCK_SUPPLIERS);

            expect(service.loading()).toBe(false);
        });

        it('should set error and return empty array on failure', () => {
            service.loadAll().pipe(take(1)).subscribe();

            const req = httpMock.expectOne(`${MOCK_ENVIRONMENT_CONFIG.apiUrl}/suppliers`);
            req.error(new ProgressEvent('Network error'));

            expect(service.suppliers()).toEqual([]);
            expect(service.error()).toBe('Failed to load suppliers');
            expect(service.loading()).toBe(false);
        });

        it('should handle empty supplier list', () => {
            service.loadAll().pipe(take(1)).subscribe();

            const req = httpMock.expectOne(`${MOCK_ENVIRONMENT_CONFIG.apiUrl}/suppliers`);
            req.flush([]);

            expect(service.suppliers()).toEqual([]);
            expect(service.error()).toBeNull();
        });
    });
});

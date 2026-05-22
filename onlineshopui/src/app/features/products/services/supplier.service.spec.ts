import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { take } from 'rxjs';
import { SupplierService } from './supplier.service';
import { EnvironmentConfig } from '../../../core/types/providers/environment-config';
import { MOCK_SUPPLIERS } from '../../../core/mocks/data/suppliers.mock';
import { MOCK_ENVIRONMENT_CONFIG } from '../../../core/mocks/data/environment.mock';

describe('SupplierService', () => {
    let service: SupplierService;
    let httpMock: HttpTestingController;
    const apiUrl = MOCK_ENVIRONMENT_CONFIG.apiUrl;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                SupplierService,
                provideHttpClient(),
                provideHttpClientTesting(),
                { provide: EnvironmentConfig, useValue: MOCK_ENVIRONMENT_CONFIG }
            ]
        });
        service = TestBed.inject(SupplierService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => httpMock.verify());

    describe('Initialization', () => {
        it('should be created', () => expect(service).toBeTruthy());

        it('should initialize with empty signals', () => {
            expect(service.suppliers()).toEqual([]);
            expect(service.selectedSupplier()).toBeNull();
            expect(service.loading()).toBe(false);
            expect(service.error()).toBeNull();
        });
    });

    describe('loadAll()', () => {
        it('should load all suppliers successfully', () => {
            service.loadAll().pipe(take(1)).subscribe();
            const req = httpMock.expectOne(`${apiUrl}/suppliers`);
            expect(req.request.method).toBe('GET');
            req.flush(MOCK_SUPPLIERS);

            expect(service.suppliers()).toEqual(MOCK_SUPPLIERS);
            expect(service.loading()).toBe(false);
            expect(service.error()).toBeNull();
        });

        it('should set loading true while request is in flight', () => {
            service.loadAll().pipe(take(1)).subscribe();
            expect(service.loading()).toBe(true);
            httpMock.expectOne(`${apiUrl}/suppliers`).flush(MOCK_SUPPLIERS);
            expect(service.loading()).toBe(false);
        });

        it('should set error on failure', () => {
            service.loadAll().pipe(take(1)).subscribe();
            httpMock.expectOne(`${apiUrl}/suppliers`).error(new ProgressEvent('error'));
            expect(service.error()).toBe('Failed to load suppliers');
            expect(service.loading()).toBe(false);
        });

        it('should handle empty list', () => {
            service.loadAll().pipe(take(1)).subscribe();
            httpMock.expectOne(`${apiUrl}/suppliers`).flush([]);
            expect(service.suppliers()).toEqual([]);
            expect(service.error()).toBeNull();
        });
    });

    describe('loadById()', () => {
        it('should load a single supplier successfully', () => {
            const supplier = MOCK_SUPPLIERS[0];
            service.loadById(supplier.id).pipe(take(1)).subscribe();
            const req = httpMock.expectOne(`${apiUrl}/suppliers/${supplier.id}`);
            expect(req.request.method).toBe('GET');
            req.flush(supplier);

            expect(service.selectedSupplier()).toEqual(supplier);
            expect(service.loading()).toBe(false);
            expect(service.error()).toBeNull();
        });

        it('should set loading true while request is in flight', () => {
            service.loadById('sup-1').pipe(take(1)).subscribe();
            expect(service.loading()).toBe(true);
            httpMock.expectOne(`${apiUrl}/suppliers/sup-1`).flush(MOCK_SUPPLIERS[0]);
            expect(service.loading()).toBe(false);
        });

        it('should set error and null selectedSupplier on failure', () => {
            service.loadById('sup-404').pipe(take(1)).subscribe();
            httpMock.expectOne(`${apiUrl}/suppliers/sup-404`).error(new ProgressEvent('error'));
            expect(service.error()).toBe('Failed to load supplier');
            expect(service.selectedSupplier()).toBeNull();
        });
    });
});

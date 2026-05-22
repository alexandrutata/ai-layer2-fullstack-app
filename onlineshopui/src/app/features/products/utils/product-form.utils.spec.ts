import { FormControl } from '@angular/forms';
import { uuidValidator, createProductForm } from './product-form.utils';
import { MOCK_PRODUCTS } from '../../../core/mocks/data/products.mock';

describe('uuidValidator', () => {
    it('should return null for a valid UUID', () => {
        const control = new FormControl('123e4567-e89b-12d3-a456-426614174000');
        expect(uuidValidator(control)).toBeNull();
    });

    it('should return invalidUuid error for a non-UUID string', () => {
        const control = new FormControl('not-a-uuid');
        expect(uuidValidator(control)).toEqual({ invalidUuid: true });
    });

    it('should return null for an empty string (defer to required)', () => {
        const control = new FormControl('');
        expect(uuidValidator(control)).toBeNull();
    });
});

describe('createProductForm', () => {
    it('should create an empty form with all controls', () => {
        const form = createProductForm();
        expect(form.contains('name')).toBe(true);
        expect(form.contains('description')).toBe(true);
        expect(form.contains('price')).toBe(true);
        expect(form.contains('weight')).toBe(true);
        expect(form.contains('categoryId')).toBe(true);
        expect(form.contains('supplierId')).toBe(true);
        expect(form.contains('imageUrl')).toBe(true);
    });

    it('should pre-populate from existing product', () => {
        const product = MOCK_PRODUCTS[0];
        const form = createProductForm(product);
        expect(form.value.name).toBe(product.name);
        expect(form.value.categoryId).toBe(product.category.id);
        expect(form.value.supplierId).toBe(product.supplier?.id ?? '');
    });

    it('should mark supplierId invalid for non-UUID value', () => {
        const form = createProductForm();
        form.get('supplierId')!.setValue('bad-id');
        expect(form.get('supplierId')!.valid).toBe(false);
        expect(form.get('supplierId')!.errors?.['invalidUuid']).toBe(true);
    });

    it('should mark categoryId invalid for non-UUID value', () => {
        const form = createProductForm();
        form.get('categoryId')!.setValue('bad-id');
        expect(form.get('categoryId')!.valid).toBe(false);
        expect(form.get('categoryId')!.errors?.['invalidUuid']).toBe(true);
    });
});

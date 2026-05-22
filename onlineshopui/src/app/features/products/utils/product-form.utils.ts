import { FormControl, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ProductDto } from '../../../core/types/dtos/product.dto';
import { ProductFormGroup } from '../types/product-form.types';
import { FormGroup } from '@angular/forms';

const UUID_PATTERN = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function uuidValidator(control: AbstractControl): ValidationErrors | null {
    const value = control.value as string;
    if (!value) return null;
    return UUID_PATTERN.test(value) ? null : { invalidUuid: true };
}

export function createProductForm(product?: ProductDto): ProductFormGroup {
    return new FormGroup({
        name: new FormControl<string>(product?.name ?? '', {
            nonNullable: true,
            validators: [Validators.required, Validators.minLength(3)]
        }),
        description: new FormControl<string>(product?.description ?? '', {
            nonNullable: true,
            validators: [Validators.required]
        }),
        price: new FormControl<number>(product?.price ?? 0, {
            nonNullable: true,
            validators: [Validators.required, Validators.min(0)]
        }),
        weight: new FormControl<number>(product?.weight ?? 0, {
            nonNullable: true,
            validators: [Validators.required, Validators.min(0)]
        }),
        categoryId: new FormControl<string>(product?.category?.id ?? '', {
            nonNullable: true,
            validators: [Validators.required, uuidValidator]
        }),
        supplierId: new FormControl<string>(product?.supplier?.id ?? '', {
            nonNullable: true,
            validators: [Validators.required, uuidValidator]
        }),
        imageUrl: new FormControl<string>(product?.imageUrl ?? '', {
            nonNullable: true,
            validators: [Validators.required]
        })
    });
}

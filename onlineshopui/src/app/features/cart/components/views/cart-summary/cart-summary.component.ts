import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { CardComponent } from '../../../../../clib/components/card/card.component';
import { ErrorMessageComponent } from '../../../../../clib/components/error-message/error-message.component';
import { AddressDto } from '../../../../../core/types/dtos/location.dto';
import { createAddressForm } from '../../../utils/address-form.utils';

@Component({
    selector: 'app-cart-summary',
    imports: [CardComponent, ReactiveFormsModule, ErrorMessageComponent],
    templateUrl: './cart-summary.component.html',
    changeDetection: ChangeDetectionStrategy.Default
})
export class CartSummaryComponent {
    subtotal = input.required<string>();
    itemCount = input.required<number>();
    isSubmitting = input<boolean>(false);

    checkout = output<AddressDto>();
    clear = output<void>();

    readonly addressForm = createAddressForm();

    onCheckout(): void {
        this.addressForm.markAllAsTouched();
        if (this.addressForm.invalid) return;
        this.checkout.emit(this.addressForm.getRawValue() as AddressDto);
    }

    onClear(): void {
        this.clear.emit();
    }
}

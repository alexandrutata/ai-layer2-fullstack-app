import { Component, input, computed, inject, ChangeDetectionStrategy, OnInit, signal, OnDestroy } from '@angular/core';
import { AbstractControl } from '@angular/forms';
import { Subscription } from 'rxjs';
import {
    ValidationMessages,
    ValidationMessagesMap
} from '../../../core/types/providers/validation-messages';

@Component({
    selector: 'app-error-message',
    standalone: true,
    template: `
        @if (errorMessage()) {
            <p class="mt-1 text-sm text-red-600 dark:text-red-400">
                {{ errorMessage() }}
            </p>
        }
    `,
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ErrorMessageComponent implements OnInit, OnDestroy {
    control = input.required<AbstractControl | null>();

    private readonly validationMessages = inject<ValidationMessagesMap>(ValidationMessages);

    // Signal updated whenever the control's status or touched state changes
    private readonly _controlVersion = signal(0);
    private _sub: Subscription | null = null;

    ngOnInit(): void {
        const ctrl = this.control();
        if (!ctrl) return;
        // statusChanges fires on validity changes; we also need touched changes
        // markAllAsTouched() doesn't emit on statusChanges, but calling
        // updateValueAndValidity after markAllAsTouched does — handled in parent.
        // We subscribe to both events here for completeness.
        this._sub = ctrl.events.subscribe(() => {
            this._controlVersion.update(v => v + 1);
        });
    }

    ngOnDestroy(): void {
        this._sub?.unsubscribe();
    }

    errorMessage = computed(() => {
        this._controlVersion(); // reactive dependency
        const ctrl = this.control();
        if (!ctrl?.errors || !ctrl.touched) {
            return null;
        }

        const errorKeys = Object.keys(ctrl.errors);
        if (errorKeys.length === 0) {
            return null;
        }

        const firstErrorKey = errorKeys[0];
        const messageFunction = this.validationMessages[firstErrorKey];

        if (!messageFunction) {
            return `Validation error: ${firstErrorKey}`;
        }

        return messageFunction(ctrl.errors[firstErrorKey]);
    });
}

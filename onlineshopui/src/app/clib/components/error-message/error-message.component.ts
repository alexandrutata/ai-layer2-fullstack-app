import { Component, input, computed, inject, ChangeDetectionStrategy, OnInit, signal, DestroyRef } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AbstractControl } from '@angular/forms';
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
export class ErrorMessageComponent implements OnInit {
    control = input.required<AbstractControl | null>();

    private readonly validationMessages = inject<ValidationMessagesMap>(ValidationMessages);
    private readonly destroyRef = inject(DestroyRef);

    private readonly _controlVersion = signal(0);

    ngOnInit(): void {
        const ctrl = this.control();
        if (!ctrl) return;
        ctrl.events.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
            this._controlVersion.update(v => v + 1);
        });
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

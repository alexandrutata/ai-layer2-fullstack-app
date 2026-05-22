import { HttpResponse } from '@angular/common/http';
import { MOCK_SUPPLIERS } from '../../data/suppliers.mock';
import { SupplierDto } from '../../../types/dtos/supplier.dto';

type SuppliersHandlerContext = {
    method: string;
    path: string;
    body: unknown;
};

export function handleSuppliersFeature(
    context: SuppliersHandlerContext
): HttpResponse<unknown> | null {
    const { method, path, body } = context;

    if (method === 'GET' && path === '/suppliers') {
        return handleGetSuppliers();
    }

    if (method === 'GET' && path.match(/^\/suppliers\/[\w-]+$/)) {
        const id = path.split('/').pop()!;
        return handleGetSupplierById(id);
    }

    if (method === 'POST' && path === '/suppliers') {
        return handleCreateSupplier(body as Partial<SupplierDto>);
    }

    if (method === 'PUT' && path.match(/^\/suppliers\/[\w-]+$/)) {
        const id = path.split('/').pop()!;
        return handleUpdateSupplier(id, body as Partial<SupplierDto>);
    }

    if (method === 'DELETE' && path.match(/^\/suppliers\/[\w-]+$/)) {
        const id = path.split('/').pop()!;
        return handleDeleteSupplier(id);
    }

    return null;
}

function handleGetSuppliers(): HttpResponse<unknown> {
    return new HttpResponse({ status: 200, body: MOCK_SUPPLIERS });
}

function handleGetSupplierById(id: string): HttpResponse<unknown> {
    const supplier = MOCK_SUPPLIERS.find(s => s.id === id);
    if (!supplier) {
        return new HttpResponse({ status: 404, statusText: 'Not Found', body: { message: 'Supplier not found' } });
    }
    return new HttpResponse({ status: 200, body: supplier });
}

function handleCreateSupplier(body: Partial<SupplierDto>): HttpResponse<unknown> {
    const newSupplier: SupplierDto = {
        id: `sup-${MOCK_SUPPLIERS.length + 1}`,
        name: body.name ?? '',
        email: body.email ?? '',
        phone: body.phone ?? '',
        address: body.address ?? ''
    };
    MOCK_SUPPLIERS.push(newSupplier);
    return new HttpResponse({ status: 201, body: newSupplier });
}

function handleUpdateSupplier(id: string, body: Partial<SupplierDto>): HttpResponse<unknown> {
    const index = MOCK_SUPPLIERS.findIndex(s => s.id === id);
    if (index === -1) {
        return new HttpResponse({ status: 404, statusText: 'Not Found', body: { message: 'Supplier not found' } });
    }
    MOCK_SUPPLIERS[index] = { ...MOCK_SUPPLIERS[index], ...body, id };
    return new HttpResponse({ status: 200, body: MOCK_SUPPLIERS[index] });
}

function handleDeleteSupplier(id: string): HttpResponse<unknown> {
    const index = MOCK_SUPPLIERS.findIndex(s => s.id === id);
    if (index === -1) {
        return new HttpResponse({ status: 404, statusText: 'Not Found', body: { message: 'Supplier not found' } });
    }
    MOCK_SUPPLIERS.splice(index, 1);
    return new HttpResponse({ status: 204, body: null });
}

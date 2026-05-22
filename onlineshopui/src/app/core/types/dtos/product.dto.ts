export type ProductCategoryDto = {
    id: string;
    name: string;
    description: string;
};

export type SupplierDto = {
    id: string;
    name: string;
    email: string;
    phone: string;
    address: string;
};

export type ProductDto = {
    id: string;
    name: string;
    description: string;
    price: number;
    weight: number;
    category: ProductCategoryDto;
    supplier: SupplierDto;
    imageUrl: string;
};

export type CreateProductRequest = Omit<ProductDto, 'id' | 'category' | 'supplier'> & {
    categoryId: string;
    supplierId: string;
};

export type UpdateProductRequest = Partial<ProductDto> & { categoryId?: string; supplierId?: string };

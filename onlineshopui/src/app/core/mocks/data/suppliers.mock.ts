import { SupplierDto } from '../../types/dtos/supplier.dto';

export const MOCK_SUPPLIERS: SupplierDto[] = [
    {
        id: 'add00001-0000-0000-0000-000000000001',
        name: 'TechSupply Co.',
        email: 'contact@techsupply.com',
        phone: '+1-800-123-4567',
        address: '123 Silicon Valley Blvd, San Jose, CA 95101'
    },
    {
        id: 'add00002-0000-0000-0000-000000000002',
        name: 'FashionWholesale Ltd.',
        email: 'orders@fashionwholesale.com',
        phone: '+1-800-234-5678',
        address: '456 Garment District, New York, NY 10018'
    },
    {
        id: 'add00003-0000-0000-0000-000000000003',
        name: 'HomeGoods Distributors',
        email: 'sales@homegoods-dist.com',
        phone: '+1-800-345-6789',
        address: '789 Industrial Park, Chicago, IL 60601'
    }
];

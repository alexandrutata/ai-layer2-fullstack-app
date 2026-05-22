# Architecture

This document describes the structure and design of the fullstack e-commerce application.

---

## Overview

A fullstack e-commerce platform with role-based access control (Customer and Administrator). Customers can browse products, manage a shopping cart, and place orders. Administrators can create, update, and delete products.

| Layer    | Technology          | Version  | Port |
|----------|---------------------|----------|------|
| Frontend | Angular             | 21.2.0   | 4200 |
| Backend  | Spring Boot         | 4.0.6    | 3000 |
| Database | PostgreSQL          | 18       | 5433 |

---

## Repository Layout

```
layer2-fullstack-app/
├── docker/
│   └── development/
│       └── docker-compose.yml       # PostgreSQL container
├── onlineshopapi/                   # Spring Boot backend
├── onlineshopui/                    # Angular frontend
├── CLAUDE.md                        # Claude Code project instructions
└── README.md
```

---

## Backend (`onlineshopapi`)

### Entry Point

`OnlineShopApiApplication.java` — Spring Boot main class. Runs on port `3000` with context path `/api`.

### Package Structure

```
src/main/java/msg/onlineshopapi/
├── config/        - OpenAPI / Swagger configuration
├── controller/    - REST controllers
├── dto/           - Request/response data transfer objects
│   └── mapper/    - Entity ↔ DTO mappers
├── exception/     - Custom exceptions and global handler
├── model/         - JPA entities
├── repository/    - Spring Data JPA repositories
├── security/      - JWT filter, service, config
└── service/       - Business logic
    └── strategy/  - Order fulfillment strategy pattern
```

### Controllers

| Controller                   | Base Path          | Responsibility                     |
|------------------------------|--------------------|------------------------------------|
| `AuthController`             | `/api/auth`        | Register, login                    |
| `ProductController`          | `/api/products`    | Product CRUD                       |
| `ProductCategoryController`  | `/api/categories`  | Category listing                   |
| `OrderController`            | `/api/orders`      | Order creation and retrieval       |

### Data Model (JPA Entities)

```
User (id, firstName, lastName, email, password, role)
  └── Order (id, userId, createdAt, address)
        └── OrderDetail (orderId + productId + shippedFromId, quantity)

Product (id, name, description, price, weight, imageUrl, categoryId)
  └── ProductCategory (id, name, description)

Stock (productId + locationId, quantity)
  └── Location (id, name, country, city, county, streetAddress)

Address (embedded in Order)
```

Composite primary keys: `StockId` (productId, locationId), `OrderDetailId` (orderId, productId, shippedFromId).

### Security

Authentication uses JWT tokens issued by the backend and validated on every request.

```
POST /api/auth/login      → returns JWT
POST /api/auth/register   → creates user, returns JWT

JwtAuthFilter             → extracts Bearer token from Authorization header
JwtService                → signs and validates tokens (HS256, 24h expiry)
UserDetailsServiceImpl    → loads user by email for Spring Security
SecurityConfig            → configures public vs. protected routes
```

Roles: `ADMIN` and `CUSTOMER`. Endpoints are protected with `@PreAuthorize` annotations.

### Order Fulfillment Strategy

The order processing logic is interchangeable via `application.yml`:

```yaml
order:
  strategy: SINGLE_LOCATION   # or MOST_ABUNDANT
```

| Strategy             | Behavior                                               |
|----------------------|--------------------------------------------------------|
| `SingleLocationStrategy`  | Ships all items from the warehouse with sufficient stock for the full order |
| `MostAbundantStrategy`    | Ships each item from the warehouse that has the most stock of that product  |

Both implement the `OrderStrategy` interface. `OrderStrategyConfig` selects the active bean.

### Database Migrations (Flyway)

```
src/main/resources/db/migration/
├── V1__create_tables.sql           # Schema: all 8 tables
└── local/
    └── V1.1__populate_mock_data.sql  # Seed data (loaded in local profile only)
```

Mock seed data includes 4 categories, 10 products, 2 warehouse locations, 3 users (1 admin + 2 customers), and 2 orders.

### Key Configuration Files

| File                                     | Purpose                                         |
|------------------------------------------|-------------------------------------------------|
| `src/main/resources/application.yml`     | Base config (port, DB env vars, JWT, CORS)      |
| `src/main/resources/application-local.yml` | Local dev overrides (DB URL, CORS, JWT secret, Flyway local data) |
| `pom.xml`                                | Maven dependencies and build config             |

---

## Frontend (`onlineshopui`)

### Entry Point

`src/main.ts` bootstraps the app. `app.config.ts` wires providers (HTTP client, router, icons). `app.routes.ts` defines top-level lazy routes.

### Directory Structure

```
src/app/
├── clib/          - Shared component library
├── core/          - App-wide config, services, and types
└── features/      - Lazy-loaded feature modules
    ├── auth/
    ├── cart/
    ├── orders/
    └── products/
```

### clib — Shared Component Library

| Component            | Description                               |
|----------------------|-------------------------------------------|
| `card`               | Generic card wrapper                      |
| `error-message`      | Inline form/API error display             |
| `icon`               | Lucide icon wrapper                       |
| `modal`              | Dialog/modal overlay                      |
| `navbar`             | Top navigation bar                        |
| `notification-popup` | Toast notification                        |
| `spinner`            | Loading spinner                           |
| `root-layout`        | Layout shell with navbar                  |
| `theme.service`      | Dark / light theme toggle                 |

### core — Application Infrastructure

```
core/
├── config/constants/    - Route constants, icon mappings, validation messages
├── mocks/               - Mock Service Worker data and HTTP interceptor (mock mode)
├── providers/           - DI providers (environment, mock API, validation messages)
├── services/            - notifications.service (global toast state)
└── types/               - Shared TypeScript interfaces, DTOs, and enums
```

**Mock mode** (`npm run start:mock`) uses an Angular HTTP interceptor (`mock-api.interceptor.ts`) backed by handler files that return pre-defined payloads, enabling frontend development without a running backend.

### features — Lazy-Loaded Modules

#### auth

| File                       | Purpose                                      |
|----------------------------|----------------------------------------------|
| `login-page`               | Login form                                   |
| `register-page`            | Registration form                            |
| `auth.guard.ts`            | Redirects unauthenticated users to `/auth/login` |
| `guest.guard.ts`           | Redirects authenticated users away from auth pages |
| `roles.guard.ts`           | Restricts routes by user role                |
| `auth-token.interceptor.ts`| Adds `Authorization: Bearer <token>` to all requests |
| `auth.service.ts`          | Login/register API calls, token storage, user state |

#### products

| File                       | Purpose                                       |
|----------------------------|-----------------------------------------------|
| `product-catalog-page`     | Grid of all products (CUSTOMER + ADMIN)       |
| `product-detail-page`      | Single product with quantity selector and add-to-cart |
| `product-create-page`      | Admin: create new product                     |
| `product-update-page`      | Admin: edit existing product                  |
| `product-card`             | Reusable product display card                 |
| `product-form`             | Shared form component (create/update)         |
| `product.service.ts`       | Product CRUD API calls                        |

#### cart

| File                       | Purpose                                       |
|----------------------------|-----------------------------------------------|
| `cart-overview-page`       | Cart contents with quantity controls          |
| `cart-item-row`            | Single line item with increase/decrease/remove |
| `cart-summary`             | Order subtotal and action buttons             |
| `cart.service.ts`          | Cart state management (localStorage-backed)   |

#### orders

| File                       | Purpose                                       |
|----------------------------|-----------------------------------------------|
| `orders-overview-page`     | List of all user orders                       |
| `order-detail-page`        | Full details for a single order               |
| `order-card`               | Order summary card (ID, date, total, item count) |
| `orders.service.ts`        | Order create/list/get API calls               |

### Authentication Flow

```
1. User submits login form
2. auth.service  →  POST /api/auth/login  →  JWT token
3. Token stored in localStorage
4. AuthTokenInterceptor adds header on every subsequent HTTP request
5. authGuard reads token presence to allow/deny navigation
6. Backend JwtAuthFilter validates token on protected endpoints
```

### Routing Structure

```
/                         → redirects to /products/overview
/auth/login               → LoginPage        (guestGuard)
/auth/register            → RegisterPage     (guestGuard)
/products/overview        → ProductCatalogPage  (authGuard)
/products/:id             → ProductDetailPage   (authGuard)
/products/create          → ProductCreatePage   (authGuard + rolesGuard[ADMIN])
/products/:id/edit        → ProductUpdatePage   (authGuard + rolesGuard[ADMIN])
/cart                     → CartOverviewPage    (authGuard)
/orders/overview          → OrdersOverviewPage  (authGuard)
/orders/:id               → OrderDetailPage     (authGuard)
```

### Key Configuration Files

| File                          | Purpose                                          |
|-------------------------------|--------------------------------------------------|
| `angular.json`                | Build configurations (development, mock, production) |
| `src/environments/`           | Per-environment `apiUrl` and `envType` settings  |
| `package.json`                | NPM scripts and dependency versions              |
| `tsconfig.json`               | TypeScript config (strict, ES2022, decorators)   |
| `src/styles.css`              | Tailwind CSS 4 entry point                       |

---

## Database

### Schema Summary

```sql
product_categories (id, name, description)
products           (id, name, description, price, weight, image_url, category_id)
locations          (id, name, country, city, county, street_address)
stocks             (product_id, location_id, quantity)          -- composite PK
users              (id, first_name, last_name, email, password, role)
orders             (id, user_id, created_at, country, city, county, street_address)
order_details      (order_id, product_id, shipped_from_id, quantity)  -- composite PK
```

### Local Credentials

```
Host:     localhost:5433
Database: shopdb
User:     shopuser
Password: shoppassword
Schema:   onlineshop
```

---

## Development Environments

| Mode           | Command                  | API Source              |
|----------------|--------------------------|-------------------------|
| Real backend   | `npm start`              | `http://localhost:3000/api` |
| Mock backend   | `npm run start:mock`     | Angular HTTP interceptor |
| Production     | `npm run build`          | Configured via env var  |

---

## API Documentation

Swagger UI is available when the backend is running:

```
http://localhost:3000/api/swagger-ui.html
```

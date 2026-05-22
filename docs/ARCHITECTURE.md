# Architecture

This document describes the structure, data flow, and key decisions in the layer2-fullstack-app — a fullstack e-commerce platform with product catalog, shopping cart, order management, and role-based access.

## Stack

| Layer    | Technology             | Version |
|----------|------------------------|---------|
| Backend  | Spring Boot            | 4.0.6   |
| Frontend | Angular                | 21.2    |
| Database | PostgreSQL             | 18      |
| Runtime  | Java                   | 21      |
| Runtime  | Node.js                | 24+     |

## System overview

```
Browser (Angular SPA)
    │  JWT in Authorization header
    ▼
Spring Boot API  (localhost:3000/api)
    │  JPA / Flyway
    ▼
PostgreSQL  (localhost:5433)
```

The frontend and backend are independently deployable. Communication is stateless JWT — no server-side sessions.

---

## Backend

### Package layout

```
onlineshopapi/src/main/java/msg/onlineshopapi/
├── config/           OpenAPI / Swagger configuration
├── controller/       REST endpoints (Auth, Product, ProductCategory, Order)
├── dto/              Request and response shapes
│   └── mapper/       Entity ↔ DTO conversions
├── model/            JPA entities
├── repository/       Spring Data JPA interfaces
├── service/          Business logic
│   └── strategy/     Pluggable order-fulfillment algorithms
├── security/         Spring Security, JWT filter and service
└── exception/        Global handler and typed exceptions
```

### Data model

```
product_categories ──< products ──< order_details >── orders >── users
                                          │
                        stocks >── locations ──< order_details (shipped_from)
```

**Tables:**

| Table               | Purpose                                        |
|---------------------|------------------------------------------------|
| `users`             | Accounts; role is `CUSTOMER` or `ADMIN`        |
| `products`          | Catalog items with price, weight, image        |
| `product_categories`| Groups products                                |
| `locations`         | Warehouse/fulfillment sites                    |
| `stocks`            | Inventory per product per location (composite PK) |
| `orders`            | Customer orders with delivery address          |
| `order_details`     | Line items; records which location fulfilled each item |

Schema is managed by Flyway. Migrations live in `src/main/resources/db/migration/`.

### API endpoints

All routes are prefixed `/api`.

| Method | Path                      | Auth     | Role  |
|--------|---------------------------|----------|-------|
| POST   | /auth/register            | Public   |       |
| POST   | /auth/login               | Public   |       |
| GET    | /auth/profile             | JWT      | Any   |
| GET    | /products                 | JWT      | Any   |
| GET    | /products/:id             | JWT      | Any   |
| POST   | /products                 | JWT      | Admin |
| PUT    | /products/:id             | JWT      | Admin |
| DELETE | /products/:id             | JWT      | Admin |
| GET    | /product-categories       | JWT      | Any   |
| GET    | /orders                   | JWT      | Any   |
| GET    | /orders/:id               | JWT      | Any   |
| POST   | /orders                   | JWT      | Any   |

OpenAPI schema: `GET /v3/api-docs`  
Swagger UI: `GET /swagger-ui.html`

### Order fulfillment strategies

The `OrderService` delegates stock allocation to a pluggable `OrderStrategy`. Strategy is selected via `app.order.strategy` in `application.yml`.

| Strategy              | Behavior                                          |
|-----------------------|---------------------------------------------------|
| `SINGLE_LOCATION`     | Fulfills the entire order from one location       |
| `MOST_ABUNDANT`       | Allocates each item from the location with most stock |

### Security

- Spring Security with stateless session management
- `JwtAuthFilter` validates the `Authorization: Bearer` header on every request
- Passwords hashed with BCrypt
- CORS origins configurable via `CORS_ALLOWED_ORIGINS`
- Token TTL: 24 hours

---

## Frontend

### Module layout

```
onlineshopui/src/app/
├── app.ts            Root component
├── app.routes.ts     Top-level route definitions
├── app.config.ts     DI providers
├── clib/             Shared component library
│   ├── components/   card, error-message, icon, modal, navbar, notification-popup, spinner
│   ├── layouts/      root-layout (navbar + router outlet)
│   └── services/     theme.service (dark/light mode)
├── core/
│   ├── config/       Route constants, validation constants, icon constants
│   ├── mocks/        Mock data and MSW request handlers
│   ├── providers/    DI setup for environment, mock API, validation messages
│   ├── services/     notifications.service
│   └── types/        Shared DTOs, enums, provider types
└── features/         Lazy-loaded feature modules
    ├── auth/
    ├── products/
    ├── cart/
    └── orders/
```

### Route tree

```
/auth                   (guest guard)
  /login
  /register

/                       (auth guard)
  /products
    /overview           Product catalog
    /create             Create product  (admin)
    /update/:id         Edit product    (admin)
    /:id                Product detail
  /cart
    /overview
  /orders
    /overview
    /details/:id

** → /products/overview
```

**Guards:**

| Guard        | Protects                             |
|--------------|--------------------------------------|
| `authGuard`  | All routes under `/`                 |
| `guestGuard` | `/auth/login`, `/auth/register`      |
| `rolesGuard` | Admin-only routes (`/products/create`, `/products/update/:id`) |

### Authentication flow

1. `AuthService` posts credentials to `/api/auth/login` and stores the returned JWT in `localStorage` under `access_token`.
2. `AuthTokenInterceptor` reads the token and injects `Authorization: Bearer <token>` on every outgoing HTTP request.
3. On 401, the interceptor clears storage and redirects to `/auth/login`.

### State management

Angular Signals are used for reactive state (cart contents, current user, notifications). There is no external state library.

### Mock mode

Running `npm run start:mock` activates Mock Service Worker (MSW). MSW intercepts HTTP requests in the browser and returns fixtures from `core/mocks/`. This lets the frontend run without a backend.

### Environments

| Config        | `apiUrl`                  | Backend     |
|---------------|---------------------------|-------------|
| `development` | `http://localhost:3000/api` | Real Spring Boot |
| `mock`        | *(MSW intercepts)*        | None        |
| `production`  | `${API_URL}`              | Real        |

File replacements are configured in `angular.json`.

---

## Infrastructure

### Docker (development)

`docker/development/docker-compose.yml` runs a single PostgreSQL 18 container.

| Setting        | Value         |
|----------------|---------------|
| Container      | `postgres_db` |
| Host port      | 5433          |
| Database       | `shopdb`      |
| User           | `shopuser`    |
| Password       | `shoppassword`|
| Volume         | `shop-data-volume` |

### Environment variables

**Backend:**

| Variable               | Purpose                         |
|------------------------|---------------------------------|
| `DB_HOST`              | Postgres host                   |
| `DB_PORT`              | Postgres port                   |
| `DB_NAME`              | Database name                   |
| `DB_USERNAME`          | Database user                   |
| `DB_PASSWORD`          | Database password               |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed origins |
| `JWT_SECRET`           | JWT signing key                 |

For local development, the `local` Spring profile pre-populates these from `application-local.yml`.

---

## Key design decisions

**Stateless JWT auth** — no session store required; scales horizontally without sticky sessions.

**Composite-key stock table** — `(product_id, location_id)` models multi-warehouse inventory without a separate join entity.

**Pluggable order strategy** — `OrderStrategy` interface decouples fulfillment logic from the service layer; adding a new strategy requires no changes to `OrderService`.

**Standalone Angular components** — no `NgModules`; each component declares its own imports, enabling fine-grained lazy loading.

**MSW mock layer** — frontend development is fully independent from backend availability; mock handlers mirror the real API contract.

**Flyway migrations** — schema changes are versioned alongside code; `V1__create_tables.sql` is the single source of truth for the database structure.

# Qavzuro

A full-stack e-commerce platform built with Java 21, Spring Boot, MongoDB, React, and Vite.

Qavzuro provides customer shopping flows, authentication and authorization, product management, carts, checkout, inventory, orders, returns, reviews, coupons, notifications, audit logging, and role-based administration.

---

## Project Status

The project has been locally verified for the backend build and unit test suite.

### Backend verification

- Java 21
- Maven 3.9+
- `mvn clean package -DskipTests` — **passed**
- `mvn test` — **56 tests passed**
- MongoDB connectivity has been tested locally
- Spring Boot application runs on port `8080`

### Frontend verification

The frontend is implemented with React + Vite and is configured to run on port `5173`.

### Payment status

The current payment implementation uses a **sandbox/fake payment provider** for development and testing. No real payment gateway or production payment processing is currently integrated.

---

## 1. Prerequisites

- **Java 21** JDK
- **Maven 3.9+**
- **Node.js 20+** and npm
- **MongoDB 6+**
- Docker Desktop (optional, for running MongoDB)
- Microsoft Edge (optional, for Playwright E2E tests)

---

## 2. Run MongoDB

### Option A — Docker

```bash
docker run -d --name qavzuro-mongo -p 27017:27017 mongo:8.0
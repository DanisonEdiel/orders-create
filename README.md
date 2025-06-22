# Order Creator Microservice

This microservice is part of the Orders Domain and is responsible for creating new orders in the system. It authenticates users through an external authentication microservice and links orders to authenticated users.

## Architecture

The service follows Clean Architecture (Hexagonal Architecture) with the following layers:
- Domain Layer: Contains core business logic and entities
- Application Layer: Service layer that implements use cases
- Infrastructure Layer: Database access and AWS SNS integration
- Interface Layer: REST API controllers

## Prerequisites

- Java 21
- Maven
- PostgreSQL 14+
- AWS CLI configured with proper credentials
- Docker (for local development)

## Setup

1. Clone the repository
2. Configure environment variables or application.properties:
   ```
   # Database
   spring.datasource.url=jdbc:postgresql://localhost:5432/orders_db
   spring.datasource.username=postgres
   spring.datasource.password=postgres

   # AWS
   cloud.aws.credentials.access-key=your_access_key
   cloud.aws.credentials.secret-key=your_secret_key
   cloud.aws.region.static=us-east-1
   cloud.aws.sns.topic.order-created=arn:aws:sns:us-east-1:your-account:order-created

   # Authentication Service
   auth.service.url=http://34.234.56.149:8000
   auth.service.token-validation-endpoint=/auth/validate-token
   ```

3. Build and run:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## Authentication

This microservice uses JWT-based authentication, delegating the authentication to a separate User Authentication microservice located at `http://34.234.56.149:8000`.

To obtain a JWT token for testing:
1. Call the authentication service's login endpoint
2. Include the JWT token in the `Authorization` header with the `Bearer ` prefix

## API Documentation

Swagger UI is available at: `http://localhost:8080/swagger-ui.html`

## Testing

### Using curl

1. First, obtain a JWT token:
```bash
curl -X POST http://34.234.56.149:8000/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password123"
  }'
```

2. Create an order using the returned token:
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {
        "productName": "Dell XPS 13 Laptop",
        "quantity": 1,
        "unitPrice": 1299.99
      },
      {
        "productName": "External SSD",
        "quantity": 2,
        "unitPrice": 89.99
      }
    ]
  }'
```

### Using Postman

1. Import the Postman collection from `docs/postman/OrderCreator.postman_collection.json`
2. Execute the "Login" request to get a JWT token
3. The collection has variables configured to store the token for subsequent requests
4. Execute the "Create Order" request which will automatically use the stored token

## Testing Strategy

### Unit Tests
- Test domain entities and their behavior
- Test service layer with mocked security context
- Test event publishing with mocked AWS services

### Integration Tests
- Test database operations
- Test AWS SNS integration using Testcontainers
- Test JWT authentication flow with mocked auth service

### Functional Tests
- End-to-end testing of the entire order creation flow
- Test error scenarios and validation

## CI/CD Pipeline

This project uses GitHub Actions for CI/CD:
- Builds and tests the application on every push and PR
- Publishes Docker images to Docker Hub
- Deploys to AWS using Terraform (when merging to main branch)

## Infrastructure as Code

The `/terraform` directory contains Terraform configurations for deploying:
- ECS Fargate service
- RDS PostgreSQL database
- Load balancer and security groups
- SNS topic for events
- Auto-scaling policies

## Local Development

For local development, you can use Docker Compose to run:
- PostgreSQL database
- LocalStack for AWS services simulation
- Wiremock for mocking the authentication service

```bash
# Start development environment
docker-compose up -d
```

## Monitoring

The service exposes:
- Prometheus metrics at `/actuator/prometheus`
- Health endpoint at `/actuator/health`
- Logging with Logback and JSON formatting for ELK stack

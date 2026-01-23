# API Testing Guide

This guide provides sample requests for testing all the API endpoints of the Personal Finance Application.

## Quick Start

1. Start the application: `mvn spring-boot:run`
2. The API will be available at: `http://localhost:8080`
3. Use the demo credentials to test: username `demo`, password `password123`

## Authentication Flow

### 1. Register a New User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### 2. Login and Get JWT Token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  
  
  -d '{
    "usernameOrEmail": "amit230604@gmail.com",
    "password": "password123"
  }'
```

**Copy the JWT token from the response for subsequent requests.**

### 3. Get Current User Info
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Income Management

### Add Income
```bash
curl -X POST http://localhost:8080/api/income \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Consulting Work",
    "amount": 2000.00,
    "category": "FREELANCE",
    "date": "2024-01-15",
    "isRecurring": false,
    "notes": "One-time consulting project"
  }'
```

### Get All Incomes
```bash
curl -X GET http://localhost:8080/api/income \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Income Categories
```bash
curl -X GET http://localhost:8080/api/income/categories \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Income by Category
```bash
curl -X GET "http://localhost:8080/api/income/by-category?startDate=2024-01-01&endDate=2024-01-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Expense Management

### Add Expense
```bash
curl -X POST http://localhost:8080/api/expenses \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Restaurant Dinner",
    "amount": 85.50,
    "category": "FOOD",
    "date": "2024-01-15",
    "notes": "Dinner with friends"
  }'
```

### Get All Expenses
```bash
curl -X GET http://localhost:8080/api/expenses \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Expenses by Category
```bash
curl -X GET "http://localhost:8080/api/expenses/by-category?startDate=2024-01-01&endDate=2024-01-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Recent Expenses
```bash
curl -X GET "http://localhost:8080/api/expenses/recent?limit=5" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Goal Management

### Create Goal
```bash
curl -X POST http://localhost:8080/api/goals \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Home Down Payment",
    "description": "Save for a house down payment",
    "targetAmount": 50000.00,
    "currentAmount": 5000.00,
    "targetDate": "2025-06-01",
    "priority": "HIGH"
  }'
```

### Get All Goals
```bash
curl -X GET http://localhost:8080/api/goals \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Active Goals
```bash
curl -X GET http://localhost:8080/api/goals/active \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Update Goal Progress
```bash
curl -X PATCH http://localhost:8080/api/goals/1/add-progress \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 500.00
  }'
```

### Get Goal Stats
```bash
curl -X GET http://localhost:8080/api/goals/stats \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Dashboard and Analytics

### Get Dashboard Overview
```bash
curl -X GET http://localhost:8080/api/dashboard/overview \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Spending Analysis
```bash
curl -X GET "http://localhost:8080/api/dashboard/spending-analysis?startDate=2024-01-01&endDate=2024-01-31" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Monthly Trends
```bash
curl -X GET "http://localhost:8080/api/dashboard/monthly-trends?monthsBack=6" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Financial Insights
```bash
curl -X GET http://localhost:8080/api/dashboard/insights \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Investment Recommendations

### Get Investment Capacity
```bash
curl -X GET http://localhost:8080/api/investments/capacity \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Investment Recommendations
```bash
curl -X GET "http://localhost:8080/api/investments/recommendations?riskProfile=MODERATE" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Risk Profiles
```bash
curl -X GET http://localhost:8080/api/investments/risk-profiles \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Testing with Postman

### Environment Variables
Create a Postman environment with:
- `baseUrl`: `http://localhost:8080`
- `jwtToken`: (set after login)

### Collection Structure
1. **Authentication**
   - Register User
   - Login
   - Get Current User

2. **Income Management**
   - Add Income
   - Get Incomes
   - Update Income
   - Delete Income

3. **Expense Management**
   - Add Expense
   - Get Expenses
   - Update Expense
   - Delete Expense

4. **Goal Management**
   - Create Goal
   - Get Goals
   - Update Goal
   - Update Progress

5. **Dashboard**
   - Overview
   - Analytics
   - Insights

6. **Investments**
   - Get Recommendations
   - Risk Analysis

## Sample Data

The application loads sample data on startup with:
- Demo user (username: `demo`, password: `password123`)
- Sample income entries (salary, freelance)
- Sample expenses (rent, groceries, transportation)
- Sample goals (emergency fund, vacation, new car)

## Error Handling

All endpoints return consistent error responses:

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed",
  "validationErrors": {
    "amount": "Amount must be positive"
  }
}
```

## Status Codes

- `200 OK`: Successful request
- `201 Created`: Resource created successfully
- `400 Bad Request`: Invalid input or validation error
- `401 Unauthorized`: Invalid or missing JWT token
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Duplicate resource
- `500 Internal Server Error`: Server error

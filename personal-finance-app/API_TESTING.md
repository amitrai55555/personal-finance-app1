# API Testing Guide

This guide provides sample requests for testing all the API endpoints of the **Personal Finance Application** (port 8080) and the **Recommendation Service** (port 8081).

## Quick Start

1. Start **Recommendation Service**: `cd recommendation-service && mvn spring-boot:run` (port 8081)
2. Start **Personal Finance App**: `cd personal-finance-app && mvn spring-boot:run` (port 8080)
3. Both require: MySQL on `localhost:3306`, database `personal_finance`, Java 21

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
    "usernameOrEmail": "testuser",
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

---

## Financial Coach (via Recommendation Service)

The coach endpoint reads your **real financial data** (income, expenses, goals) from the database and sends it to the Recommendation Service for personalized advice.

### Get Coach Advice (auto-detect risk profile)
```bash
curl -X GET http://localhost:8080/api/coach/advice \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Get Coach Advice (explicit risk profile)
```bash
curl -X GET "http://localhost:8080/api/coach/advice?riskProfile=AGGRESSIVE" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response includes:**
- `portfolio` — investment allocation with stock/bond/REIT/alternative recommendations
- `savingsAdvice` — personalized savings tips based on your savings rate
- `expenseAdvice` — top spend categories with actionable steps to cut
- `goalAdvice` — monthly contribution plan for each active goal
- `finbot` — AI-generated greeting, summary, actions & risks (when AI is enabled)

---

## AI Chat (Finbot)

Chat with the AI financial coach. Your real financial profile is automatically attached to provide context-aware answers.

### Chat about Savings
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "How can I improve my savings this month?",
    "topic": "savings"
  }'
```

### Chat about Investing
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "What investments should I consider given my financial situation?",
    "topic": "investing"
  }'
```

### Chat about Debt
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "How should I manage my debt payments?",
    "topic": "debt"
  }'
```

### General Financial Chat
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Give me a quick overview of my financial health"
  }'
```

### Continue a Conversation
```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "message": "Tell me more about that last point",
    "conversationId": "my-conversation-1"
  }'
```

---

## Recommendation Service — Direct API (port 8081)

These endpoints can be called directly against the Recommendation Service without JWT authentication.

### Health Check
```bash
curl http://localhost:8081/actuator/health
```

### Generate Investment Portfolio
```bash
curl -X POST http://localhost:8081/api/recommendations/investments \
  -H "Content-Type: application/json" \
  -d '{
    "riskProfile": "MODERATE",
    "monthlyIncome": 50000,
    "monthlyExpenses": 30000,
    "monthlySavings": 20000,
    "savingsRate": 40.0,
    "investmentCapacity": 4000,
    "expensesByCategory": {
      "FOOD": 8000,
      "HOUSING": 12000,
      "TRANSPORTATION": 5000,
      "ENTERTAINMENT": 3000,
      "UTILITIES": 2000
    },
    "goals": [{
      "title": "Emergency Fund",
      "targetAmount": 100000,
      "currentAmount": 25000,
      "targetDate": "2026-12-01",
      "priority": "HIGH"
    }]
  }'
```

### Get Coach Advice (with profile data)
```bash
curl -X POST http://localhost:8081/api/recommendations/coach \
  -H "Content-Type: application/json" \
  -d '{
    "riskProfile": "MODERATE",
    "monthlyIncome": 50000,
    "monthlyExpenses": 30000,
    "monthlySavings": 20000,
    "savingsRate": 40.0,
    "investmentCapacity": 4000,
    "expensesByCategory": {
      "FOOD": 8000,
      "HOUSING": 12000,
      "TRANSPORTATION": 5000
    },
    "goals": [{
      "title": "Emergency Fund",
      "targetAmount": 100000,
      "currentAmount": 25000,
      "targetDate": "2026-12-01",
      "priority": "HIGH"
    }]
  }'
```

### Get Coach Advice with AI Enrichment
```bash
curl -X POST "http://localhost:8081/api/recommendations/coach?useAI=true" \
  -H "Content-Type: application/json" \
  -d '{
    "riskProfile": "AGGRESSIVE",
    "userName": "Amit",
    "monthlyIncome": 80000,
    "monthlyExpenses": 40000,
    "monthlySavings": 40000,
    "savingsRate": 50.0,
    "investmentCapacity": 8000,
    "expensesByCategory": {
      "FOOD": 10000,
      "HOUSING": 15000,
      "SHOPPING": 8000
    },
    "goals": [{
      "title": "Buy a House",
      "targetAmount": 2000000,
      "currentAmount": 200000,
      "targetDate": "2028-01-01",
      "priority": "HIGH"
    }]
  }'
```

### AI Chat (Bridge Endpoint)
```bash
curl -X POST http://localhost:8081/api/recommendations/chat \
  -H "Content-Type: application/json" \
  -d '{
    "message": "How should I allocate my investments?",
    "topic": "investing",
    "riskProfile": "MODERATE",
    "conversationId": "test-conv-1",
    "profile": {
      "monthlyIncome": 50000,
      "monthlyExpenses": 30000,
      "monthlySavings": 20000,
      "savingsRate": 40.0,
      "investmentCapacity": 4000,
      "riskProfile": "MODERATE"
    }
  }'
```

### Finbot Direct Chat
```bash
curl -X POST http://localhost:8081/api/finbot/chat \
  -H "Content-Type: application/json" \
  -d '{
    "messages": [{
      "role": "user",
      "content": "I earn 50,000/month and save 40%. What should I invest in?"
    }]
  }'
```

---

## Testing with Postman

### Environment Variables
Create a Postman environment with:
- `baseUrl`: `http://localhost:8080`
- `recUrl`: `http://localhost:8081`
- `jwtToken`: (set after login)

### Collection Structure
1. **Authentication** — Register, Login, Get User
2. **Income Management** — Add, Get, Update, Delete
3. **Expense Management** — Add, Get, Update, Delete
4. **Goal Management** — Create, Get, Update Progress
5. **Dashboard** — Overview, Trends, Insights, Spending Analysis
6. **Investments** — Capacity, Recommendations, Risk Profiles
7. **Financial Coach** — Get Advice (auto/explicit risk profile)
8. **AI Chat** — Savings, Investing, Debt, General
9. **Recommendation Service Direct** — Portfolio, Coach, AI Chat, Finbot

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

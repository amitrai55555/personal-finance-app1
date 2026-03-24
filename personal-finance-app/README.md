# Personal Finance Management Application

A comprehensive personal finance management web application built with **Java Spring Boot**, featuring **JWT authentication**, **Spring Security**, and **AI-powered investment recommendations**.

## 🚀 Key Features

### 1. Dashboard Overview
- Quick stats showing total income, expenses, net income, and goals progress
- Interactive spending chart by category
- Recent transactions display
- Goals progress visualization

### 2. Income Tracking
- Add income sources with categories (Salary, Freelance, Investments, etc.)
- Mark income as recurring or one-time
- Visual breakdown by category with charts
- Income history with management options

### 3. Expense Management
- Categorized expense tracking (Housing, Food, Transportation, etc.)
- Visual spending analysis with bar charts
- Real-time expense totals and category breakdowns
- Easy expense deletion and management

### 4. Financial Goals
- Set goals with target amounts, deadlines, and priorities
- Visual progress bars with completion percentages
- Priority-based color coding (High/Medium/Low)
- Goal completion celebrations

### 5. AI Investment Recommendations
- Smart analysis of investment capacity (20% of net income)
- AI-powered stock, ETF, and crypto recommendations
- Confidence ratings and detailed reasoning
- Recommended portfolio allocation (60% stocks, 20% bonds, 15% real estate, 5% alternatives)

## 🛠️ Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.0
- **Security**: Spring Security with JWT authentication
- **Database**: H2 (development), MySQL (production)
- **ORM**: Spring Data JPA with Hibernate
- **Build Tool**: Maven
- **API**: RESTful APIs with JSON

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0+ (for production)
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

## 🔧 Setup Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd personal-finance-app
```

### 2. Build the Application
```bash
mvn clean install
```

### 3. Run in Development Mode
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### 4. Access H2 Console (Development)
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

### 5. Production Setup with MySQL

1. Create MySQL database:
```sql
CREATE DATABASE personal_finance;
```

2. Update `application-prod.properties` with your MySQL credentials

3. Run with production profile:
```bash
mvn spring-boot:run -Dspring.profiles.active=prod
```

## 📖 API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "johndoe",
  "password": "password123"
}
```

#### Get Current User
```http
GET /api/auth/me
Authorization: Bearer <jwt-token>
```

### Income Endpoints

#### Add Income
```http
POST /api/income
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "description": "Monthly Salary",
  "amount": 5000.00,
  "category": "SALARY",
  "date": "2024-01-15",
  "isRecurring": true,
  "recurrenceType": "MONTHLY",
  "notes": "Regular monthly salary"
}
```

#### Get All Incomes
```http
GET /api/income
Authorization: Bearer <jwt-token>
```

#### Get Income by Category
```http
GET /api/income/by-category?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer <jwt-token>
```

#### Update Income
```http
PUT /api/income/{id}
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "description": "Updated Salary",
  "amount": 5500.00,
  "category": "SALARY",
  "date": "2024-01-15"
}
```

#### Delete Income
```http
DELETE /api/income/{id}
Authorization: Bearer <jwt-token>
```

### Expense Endpoints

#### Add Expense
```http
POST /api/expenses
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "description": "Grocery Shopping",
  "amount": 150.00,
  "category": "FOOD",
  "date": "2024-01-15",
  "notes": "Weekly groceries"
}
```

#### Get All Expenses
```http
GET /api/expenses
Authorization: Bearer <jwt-token>
```

#### Get Expenses by Category
```http
GET /api/expenses/by-category?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer <jwt-token>
```

### Goal Endpoints

#### Create Goal
```http
POST /api/goals
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "title": "Emergency Fund",
  "description": "Build emergency fund for 6 months expenses",
  "targetAmount": 10000.00,
  "currentAmount": 1000.00,
  "targetDate": "2024-12-31",
  "priority": "HIGH"
}
```

#### Get All Goals
```http
GET /api/goals
Authorization: Bearer <jwt-token>
```

#### Update Goal Progress
```http
PATCH /api/goals/{id}/progress
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "amount": 1500.00
}
```

#### Add to Goal Progress
```http
PATCH /api/goals/{id}/add-progress
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "amount": 500.00
}
```

### Dashboard Endpoints

#### Get Dashboard Overview
```http
GET /api/dashboard/overview
Authorization: Bearer <jwt-token>
```

#### Get Spending Analysis
```http
GET /api/dashboard/spending-analysis?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer <jwt-token>
```

#### Get Monthly Trends
```http
GET /api/dashboard/monthly-trends?monthsBack=6
Authorization: Bearer <jwt-token>
```

#### Get Financial Insights
```http
GET /api/dashboard/insights
Authorization: Bearer <jwt-token>
```

### Investment Endpoints

#### Get Investment Recommendations
```http
GET /api/investments/recommendations?riskProfile=MODERATE
Authorization: Bearer <jwt-token>
```

#### Get Investment Capacity
```http
GET /api/investments/capacity
Authorization: Bearer <jwt-token>
```

#### Get Risk Profiles
```http
GET /api/investments/risk-profiles
Authorization: Bearer <jwt-token>
```

### AI Chat Endpoint

#### Chat With Financial Coach
```http
POST /api/chat
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "message": "How should I allocate my bonus?",
  "topic": "investing",
  "riskProfile": "MODERATE",
  "conversationId": "optional-id"
}
```

Returns the AI reply plus optional actionable suggestions. Falls back to built-in guidance if the AI microservice is unreachable.

## 🔒 Security Features

- **JWT Authentication**: Secure token-based authentication
- **Password Encryption**: BCrypt password hashing
- **CORS Support**: Cross-origin resource sharing enabled
- **Role-based Access**: User and Admin roles
- **Input Validation**: Comprehensive request validation
- **Error Handling**: Global exception handling

## 📊 Data Models

### User
- Personal information and authentication
- Relationships to income, expenses, and goals

### Income
- Income tracking with categories and recurrence
- Categories: Salary, Freelance, Investments, Business, Rental, Bonus, Other

### Expense
- Expense tracking with categories and recurrence
- Categories: Housing, Food, Transportation, Utilities, Healthcare, Entertainment, Shopping, Education, Insurance, Other

### Goal
- Financial goal setting and progress tracking
- Priority levels: High, Medium, Low
- Status: Active, Completed, Paused, Cancelled

## 🤖 AI Features

### Investment Recommendations
- **Risk Profiling**: Automatic risk assessment based on financial patterns
- **Portfolio Allocation**: Intelligent asset allocation based on risk profile
- **Investment Suggestions**: Specific stock, ETF, bond, and alternative investment recommendations
- **Confidence Scoring**: AI confidence ratings for each recommendation
- **Detailed Analysis**: Pros, cons, and reasoning for each suggestion

### AI Chat Coach
- **Context-aware chat** that uses your financial profile and risk settings
- **Microservice-backed** with graceful fallbacks when the AI service is unavailable
- **Actionable suggestions** returned alongside the AI reply
### Risk Profiles
- **Conservative**: 40% stocks, 40% bonds, 15% real estate, 5% alternatives
- **Moderate**: 60% stocks, 20% bonds, 15% real estate, 5% alternatives
- **Aggressive**: 80% stocks, 10% bonds, 5% real estate, 5% alternatives

## 🚀 Running the Application

### Development Mode
```bash
mvn spring-boot:run
```

### Production Mode
```bash
mvn spring-boot:run -Dspring.profiles.active=prod
```

### Building JAR
```bash
mvn clean package
java -jar target/personal-finance-app-1.0.0.jar
```

## 🧪 Testing

### Run Tests
```bash
mvn test
```

### Run with Coverage
```bash
mvn clean test jacoco:report
```

## 🔧 Configuration

### Environment Variables
- `DB_USERNAME`: Database username (production)
- `DB_PASSWORD`: Database password (production)
- `JWT_SECRET`: JWT signing secret (production)
- `JWT_EXPIRATION`: JWT expiration time in milliseconds

### Profiles
- `default`: Development with H2 database
- `prod`: Production with MySQL database

## 📝 API Response Format

### Success Response
```json
{
  "id": 1,
  "description": "Monthly Salary",
  "amount": 5000.00,
  "category": "SALARY",
  "date": "2024-01-15",
  "createdAt": "2024-01-15T10:30:00"
}
```

### Error Response
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Input validation failed",
  "validationErrors": {
    "amount": "Amount must be positive",
    "description": "Description is required"
  }
}
```

## 🎨 Frontend Integration

This backend is designed to work with modern frontend frameworks like React, Angular, or Vue.js. The API provides:

- **RESTful endpoints** for all financial operations
- **JWT tokens** for authentication
- **JSON responses** for easy consumption
- **CORS enabled** for cross-origin requests
- **Comprehensive error handling** with meaningful messages

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 📞 Support

For support and questions, please open an issue in the repository or contact the development team.

---
ollama api key :844807b580ab4f62ab99b9b34a9bae48.Wc7gVfIOh9MrcIkSqSyuqni1
**Built with ❤️ using Spring Boot and modern Java development practices**

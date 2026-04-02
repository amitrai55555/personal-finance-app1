# Quick Start Guide

## 🚀 Get Started in 5 Minutes

### Prerequisites
1. **Java 17+** - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)
2. **Maven 3.6+** - Download from [Apache Maven](https://maven.apache.org/download.cgi)

### Quick Setup

#### Windows Users
1. Double-click `run.bat` 
2. The script will check dependencies and start the application

#### Mac/Linux Users
1. Run `chmod +x run.sh` (first time only)
2. Run `./run.sh`

#### Manual Setup
```bash
# Navigate to project directory
cd personal-finance-app

# Clean and compile
mvn clean compile

# Run the application
mvn spring-boot:run
```

### Access the Application

- **API Base URL**: `http://localhost:8080`
- **H2 Database Console**: `http://localhost:8080/h2-console`
- **Demo Credentials**: username: `demo`, password: `password123`

### Test the API

#### 1. Login to get JWT token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "demo",
    "password": "password123"
  }'
```

#### 2. Get dashboard overview (replace YOUR_JWT_TOKEN)
```bash
curl -X GET http://localhost:8080/api/dashboard/overview \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 3. Get investment recommendations
```bash
curl -X GET http://localhost:8080/api/investments/recommendations \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Key API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/login` | POST | User authentication |
| `/api/auth/register` | POST | User registration |
| `/api/dashboard/overview` | GET | Dashboard statistics |
| `/api/income` | GET/POST | Income management |
| `/api/expenses` | GET/POST | Expense tracking |
| `/api/goals` | GET/POST | Financial goals |
| `/api/investments/recommendations` | GET | AI investment advice |

### Features to Test

1. **Authentication**: Register new user, login, get profile
2. **Income Tracking**: Add salary, freelance income with categories
3. **Expense Management**: Track expenses by category with totals
4. **Financial Goals**: Set savings goals with progress tracking
5. **Dashboard Analytics**: View spending patterns and insights
6. **AI Recommendations**: Get personalized investment advice

### Sample Data Included

The application comes with pre-loaded sample data:
- Demo user account
- Sample income and expense transactions
- Financial goals with progress
- Ready-to-test AI recommendations

### Need Help?

1. Check the detailed `README.md` for complete documentation
2. Review `API_TESTING.md` for comprehensive API examples
3. Access H2 console to view database tables and data
4. Check application logs for debugging information

### Production Deployment

For production use:
1. Update `application-prod.properties` with MySQL settings
2. Set environment variables for security
3. Run with production profile: `mvn spring-boot:run -Dspring.profiles.active=prod`

---

**🎉 Your personal finance application is ready to use!**

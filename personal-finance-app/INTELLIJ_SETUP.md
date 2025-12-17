# IntelliJ IDEA Setup Guide

## 🚀 Setting up Personal Finance Application in IntelliJ IDEA

### Prerequisites

1. **IntelliJ IDEA** (Community or Ultimate Edition)
2. **Java 17+** (OpenJDK or Oracle JDK)
3. **Maven** (usually bundled with IntelliJ)

### Step-by-Step Setup

#### 1. Open the Project in IntelliJ IDEA

**Option A: Open existing project**
1. Launch IntelliJ IDEA
2. Click "Open" on the welcome screen
3. Navigate to `C:\Users\hp\personal-finance-app`
4. Select the folder and click "OK"

**Option B: Import Maven project**
1. Launch IntelliJ IDEA
2. Click "Open or Import" on the welcome screen
3. Navigate to `C:\Users\hp\personal-finance-app\pom.xml`
4. Select the `pom.xml` file and click "Open"
5. Choose "Open as Project" when prompted

#### 2. Configure Project SDK

1. Go to **File → Project Structure** (Ctrl+Alt+Shift+S)
2. Under **Project Settings → Project**
3. Set **Project SDK** to Java 17 or higher
4. Set **Project language level** to 17
5. Click "OK"

#### 3. Configure Maven

1. Go to **File → Settings** (Ctrl+Alt+S)
2. Navigate to **Build, Execution, Deployment → Build Tools → Maven**
3. Ensure **Maven home path** is correctly set
4. Check **Use plugin registry** and **Work offline** if needed
5. Click "OK"

#### 4. Import Maven Dependencies

1. IntelliJ should automatically detect the `pom.xml` and show a notification
2. Click "Import Maven Projects" in the notification
3. Or manually: **View → Tool Windows → Maven**
4. Click the refresh icon in the Maven tool window
5. Wait for dependencies to download

#### 5. Enable Spring Boot Support

1. Go to **File → Settings** (Ctrl+Alt+S)
2. Navigate to **Languages & Frameworks → Spring**
3. Enable **Spring Support**
4. Navigate to **Spring → Spring Boot**
5. Enable **Spring Boot Support**
6. Click "OK"

### 🏃‍♂️ Running the Application

#### Method 1: Using Run Configuration (Recommended)

1. The project comes with a pre-configured run configuration
2. Look for "PersonalFinanceApplication" in the run configurations dropdown (top-right)
3. Click the green **Run** button (▶️) or press **Shift+F10**

#### Method 2: Run Main Class Directly

1. Navigate to `src/main/java/com/finance/PersonalFinanceApplication.java`
2. Right-click on the file
3. Select **Run 'PersonalFinanceApplication.main()'**

#### Method 3: Using Maven Tool Window

1. Open **View → Tool Windows → Maven**
2. Navigate to **Plugins → spring-boot**
3. Double-click **spring-boot:run**

#### Method 4: Using Terminal in IntelliJ

1. Open **View → Tool Windows → Terminal**
2. Run command: `mvn spring-boot:run`

### 🔧 IntelliJ IDEA Configuration Tips

#### Enable Automatic Import
1. **File → Settings → Editor → General → Auto Import**
2. Check **Add unambiguous imports on the fly**
3. Check **Optimize imports on the fly**

#### Configure Code Style
1. **File → Settings → Editor → Code Style → Java**
2. Set **Tab size: 4**, **Indent: 4**
3. Enable **Use tab character** if preferred

#### Enable Spring Boot Features
1. **File → Settings → Languages & Frameworks → Spring Boot**
2. Enable **Spring Boot** support
3. This enables features like:
   - Auto-completion for `application.properties`
   - Spring Boot run configurations
   - Endpoint mapping view

### 📱 Testing the Application

#### 1. Verify Application Startup

After running, you should see output like:
```
Started PersonalFinanceApplication in X.XXX seconds
Demo login credentials:
Username: demo
Password: password123
```

#### 2. Access Application URLs

- **API Base**: `http://localhost:8080`
- **H2 Console**: `http://localhost:8080/h2-console`

#### 3. Test with IntelliJ HTTP Client

1. Create a new file: `test-requests.http`
2. Add test requests:

```http
### Login to get JWT token
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "demo",
  "password": "password123"
}

### Get dashboard overview (replace with actual token)
GET http://localhost:8080/api/dashboard/overview
Authorization: Bearer YOUR_JWT_TOKEN_HERE

### Get investment recommendations
GET http://localhost:8080/api/investments/recommendations?riskProfile=MODERATE
Authorization: Bearer YOUR_JWT_TOKEN_HERE
```

### 🛠️ Debugging Configuration

#### Enable Debug Mode
1. Click the **Debug** button (🐛) instead of Run
2. Or use **Shift+F9**
3. Set breakpoints by clicking in the left gutter of the code editor

#### Common Debug Points
- `AuthController.authenticateUser()` - for authentication issues
- `DashboardService.getDashboardOverview()` - for dashboard data
- `InvestmentService.generatePortfolioRecommendation()` - for AI recommendations

### 🔍 Useful IntelliJ Tools

#### Database Tool Window
1. **View → Tool Windows → Database**
2. Add H2 database connection:
   - **Host**: localhost
   - **Database**: testdb
   - **User**: sa
   - **Password**: password
   - **URL**: jdbc:h2:mem:testdb

#### Spring Tool Window
1. **View → Tool Windows → Spring**
2. View Spring beans, endpoints, and configurations

#### Maven Tool Window
1. **View → Tool Windows → Maven**
2. Run Maven goals, view dependencies

### ⚡ Hot Reload Setup

#### Enable Spring Boot DevTools
1. DevTools is already included in `pom.xml`
2. **File → Settings → Build, Execution, Deployment → Compiler**
3. Check **Build project automatically**
4. **Help → Find Action** → search "Registry"
5. Find and enable: `compiler.automake.allow.when.app.running`

### 🎯 Keyboard Shortcuts

- **Ctrl+Shift+F10**: Run current file
- **Shift+F10**: Run last configuration
- **Shift+F9**: Debug last configuration
- **Ctrl+F2**: Stop running application
- **Ctrl+Shift+A**: Find Action
- **Alt+F12**: Open Terminal
- **Ctrl+E**: Recent files

### 📋 Project Structure in IntelliJ

```
personal-finance-app/
├── 📁 .idea/                    # IntelliJ configuration
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/com/finance/
│   │   │   ├── 📁 config/       # Configuration classes
│   │   │   ├── 📁 controller/   # REST controllers
│   │   │   ├── 📁 dto/          # Data transfer objects
│   │   │   ├── 📁 entity/       # JPA entities
│   │   │   ├── 📁 exception/    # Exception handling
│   │   │   ├── 📁 repository/   # Data repositories
│   │   │   ├── 📁 security/     # Security configuration
│   │   │   ├── 📁 service/      # Business logic
│   │   │   └── PersonalFinanceApplication.java
│   │   └── 📁 resources/
│   │       ├── application.properties
│   │       └── application-prod.properties
│   └── 📁 test/                 # Test files
├── 📄 pom.xml                   # Maven configuration
├── 📄 README.md                 # Project documentation
├── 📄 API_TESTING.md           # API testing guide
└── 📄 QUICK_START.md           # Quick start guide
```

### 🐛 Troubleshooting

#### Common Issues and Solutions

1. **Maven dependencies not loading**
   - Solution: Refresh Maven projects in Maven tool window

2. **Java version issues**
   - Solution: Check Project SDK settings (File → Project Structure)

3. **Spring Boot not recognized**
   - Solution: Enable Spring and Spring Boot plugins in Settings

4. **Port 8080 already in use**
   - Solution: Change port in `application.properties`: `server.port=8081`

5. **H2 console not accessible**
   - Solution: Check `spring.h2.console.enabled=true` in properties file

### 🎉 You're Ready!

Once the application starts successfully, you can:

1. **Access the API** at `http://localhost:8080`
2. **View H2 Console** at `http://localhost:8080/h2-console`
3. **Test with demo credentials**: username: `demo`, password: `password123`
4. **Use the HTTP client** in IntelliJ for API testing
5. **Debug and develop** with full IDE support

The application includes sample data and all features are fully functional!

# 🎯 Running Personal Finance App in IntelliJ IDEA

## Quick Setup Instructions

### Step 1: Open Project in IntelliJ IDEA

1. **Launch IntelliJ IDEA**
2. **Click "Open"** on the welcome screen
3. **Navigate to**: `C:\Users\hp\personal-finance-app`
4. **Select the folder** and click "OK"
5. **Wait for IntelliJ** to index the project and download dependencies

### Step 2: Verify Project Configuration

1. **Check Project SDK**:
   - Go to **File → Project Structure** (Ctrl+Alt+Shift+S)
   - Ensure **Project SDK** is set to **Java 17** or higher
   - Click "OK"

2. **Verify Maven Import**:
   - Look for the **Maven tool window** on the right side
   - If not visible: **View → Tool Windows → Maven**
   - Click the refresh icon if needed

### Step 3: Run the Application

1. **Locate the main class**:
   - Navigate to `src/main/java/com/finance/PersonalFinanceApplication.java`

2. **Run the application** (choose one method):
   
   **Method A**: Right-click on `PersonalFinanceApplication.java` → **Run 'PersonalFinanceApplication.main()'**
   
   **Method B**: Click the green **Run** button (▶️) in the top toolbar
   
   **Method C**: Use the pre-configured run configuration "PersonalFinanceApplication"

### Step 4: Verify Application Started

Look for this output in the console:
```
Started PersonalFinanceApplication in X.XXX seconds
Sample data loaded successfully!
Demo login credentials:
Username: demo
Password: password123
```

### Step 5: Test the Application

1. **Open your web browser**
2. **Visit**: `http://localhost:8080/h2-console` (to see the database)
3. **H2 Console login**:
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`
   - Password: `password`

### Step 6: Test APIs with IntelliJ HTTP Client

1. **Open the file**: `test-requests.http`
2. **Click the green arrow** next to "User Login (Demo User)" request
3. **Copy the JWT token** from the response
4. **Test other endpoints** by clicking their green arrows

## 🔧 IntelliJ IDEA Features You Can Use

### 1. Spring Boot Dashboard
- **View → Tool Windows → Spring Boot**
- See application status and endpoints

### 2. Database Tool Window
- **View → Tool Windows → Database**
- Connect to H2 database to view tables and data

### 3. HTTP Client
- Use `test-requests.http` file for API testing
- Built-in response viewer and token management

### 4. Live Reload (Hot Swap)
- Make code changes while app is running
- Changes automatically reload (thanks to Spring Boot DevTools)

## 🎯 What You'll See When Running

### Console Output
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

...

Started PersonalFinanceApplication in 3.456 seconds
Sample data loaded successfully!
Demo login credentials:
Username: demo
Password: password123
```

### Available URLs
- **Main API**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console
- **Health Check**: http://localhost:8080/actuator/health (if actuator enabled)

### Sample Data Available
- **Demo user account** (username: demo, password: password123)
- **Sample income entries** (salary, freelance work)
- **Sample expenses** (rent, groceries, gas)
- **Sample financial goals** (emergency fund, vacation, new car)

## 🚨 Troubleshooting

### Issue: Maven dependencies not loading
**Solution**: 
- Go to **View → Tool Windows → Maven**
- Click the refresh icon (🔄)
- Or: **File → Invalidate Caches and Restart**

### Issue: Java version error
**Solution**: 
- **File → Project Structure → Project**
- Set **Project SDK** to Java 17+

### Issue: Port 8080 already in use
**Solution**: 
- Change port in `application.properties`: `server.port=8081`
- Or stop other applications using port 8080

### Issue: Spring Boot features not working
**Solution**: 
- **File → Settings → Plugins**
- Enable **Spring** and **Spring Boot** plugins

## 🎉 Success Indicators

✅ **Application starts without errors**  
✅ **Console shows "Started PersonalFinanceApplication"**  
✅ **H2 console accessible**  
✅ **Demo login works in test-requests.http**  
✅ **APIs return data when tested**  

## 📋 Next Steps

1. **Test all API endpoints** using `test-requests.http`
2. **Explore the H2 database** to see data structure
3. **Make code modifications** and see live reload in action
4. **Add breakpoints** and debug the application
5. **Create additional test users** and data

---

**🎊 Your Personal Finance Application is now running in IntelliJ IDEA!**

All features are working:
- ✅ JWT Authentication
- ✅ Income & Expense Tracking  
- ✅ Financial Goals Management
- ✅ Dashboard Analytics
- ✅ AI Investment Recommendations

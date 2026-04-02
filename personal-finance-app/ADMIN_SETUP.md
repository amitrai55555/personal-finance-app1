# Admin Role Setup Guide

## Overview
This document explains the admin role implementation and how to test it.

## What Was Added

### Backend Changes

1. **AdminController.java** - New controller with admin-only endpoints
   - Location: `src/main/java/com/finance/controller/AdminController.java`
   - Endpoint: `GET /api/admin/ping` (requires ROLE_ADMIN)
   - Purpose: Verify admin access and test role-based security

2. **DataLoader.java** - Updated to seed admin user
   - Location: `src/main/java/com/finance/config/DataLoader.java`
   - Creates admin user on startup if not exists
   - Admin credentials are logged to console

3. **User Entity** - Already has Role enum (USER, ADMIN)
   - Location: `src/main/java/com/finance/entity/User.java`
   - Role field is mapped as `@Enumerated(EnumType.STRING)`

4. **Security Configuration** - Already configured
   - Method-level security enabled with `@PreAuthorize`
   - JWT authentication working with role-based access

### Frontend Changes

1. **profile-handler.js** - New script for admin link visibility
   - Location: `js/profile-handler.js`
   - Checks user role from sessionStorage
   - Shows/hides "Switch to Admin" link in profile dropdown

2. **dashboard.html** - Updated to include profile handler
   - Admin link is hidden by default
   - Script tag added to load profile-handler.js

3. **api-service.js** - Already extracts role from login response
   - Stores user role in sessionStorage
   - Used by profile handler to determine visibility

## Admin User Credentials

When you start the backend application, an admin user is automatically created:

```
Username: admin
Email: admin@example.com
Password: admin123
Role: ADMIN
```

## Regular User Credentials (for comparison)

The demo user (non-admin) credentials:

```
Username: demo
Email: demo@example.com
Password: password123
Role: USER
```

## Testing Instructions

### 1. Start the Backend

```bash
cd D:\Minor_project\personal-finance-app
mvnw clean install
mvnw spring-boot:run
```

Or use the provided scripts:
- Windows: `run.bat` or `start-both.bat`
- Unix/Mac: `./run.sh`

**Check console output** for the message:
```
Admin user created: username=admin, password=admin123
```

### 2. Start the Frontend

Open `D:\Minor_project\finboard-main-main\index.html` in your browser.

### 3. Test Admin Access

#### Login as Admin
1. Go to login page
2. Enter credentials:
   - Username or Email: `admin`
   - Password: `admin123`
3. Click "Sign In to Dashboard"

#### Verify Admin Access
1. After login, you should be redirected to the dashboard
2. Click on your profile image in the top-right corner
3. **You should see** "Switch to Admin" option in the dropdown
4. Click "Switch to Admin" to navigate to Admin.html

#### Test Admin API Endpoint
Open browser console and run:
```javascript
// This should return {status: "ok", message: "Admin access verified"}
fetch('http://localhost:8080/api/admin/ping', {
    headers: {
        'Authorization': 'Bearer ' + sessionStorage.getItem('fintrackr_token')
    }
}).then(r => r.json()).then(console.log)
```

### 4. Test Regular User (No Admin Access)

#### Login as Regular User
1. Logout from admin account
2. Login with demo credentials:
   - Username or Email: `demo`
   - Password: `password123`

#### Verify No Admin Access
1. After login, click on your profile image
2. **You should NOT see** "Switch to Admin" option
3. The admin link is hidden for regular users

#### Test API Rejection
Open browser console and run:
```javascript
// This should return 403 Forbidden
fetch('http://localhost:8080/api/admin/ping', {
    headers: {
        'Authorization': 'Bearer ' + sessionStorage.getItem('fintrackr_token')
    }
}).then(r => console.log('Status:', r.status))
```

## Troubleshooting

### Admin Link Not Showing

1. **Check if logged in as admin**:
   ```javascript
   // Open browser console
   const user = JSON.parse(sessionStorage.getItem('fintrackr_user'));
   console.log('User role:', user.role);
   // Should print: "ROLE_ADMIN" or "ADMIN"
   ```

2. **Check backend response**:
   - Open Network tab in browser DevTools
   - Look for POST request to `/api/auth/login`
   - Response should include `"role": "ROLE_ADMIN"`

3. **Verify script is loaded**:
   - Check browser console for errors
   - Look for message: "Admin access granted for user: admin"

### Admin API Endpoint Returns 403

1. **Check JWT token**:
   ```javascript
   console.log(sessionStorage.getItem('fintrackr_token'));
   ```

2. **Verify user has ROLE_ADMIN** in database:
   - Check H2 console: http://localhost:8080/h2-console
   - JDBC URL: `jdbc:h2:mem:financedb`
   - Query: `SELECT * FROM users WHERE username = 'admin'`
   - Role column should show: `ADMIN`

3. **Check UserPrincipal authorities**:
   - Backend logs should show: `ROLE_ADMIN` in authorities

## How It Works

### Backend Flow

1. User logs in with admin credentials
2. `AuthController.login()` authenticates user
3. `UserPrincipal.create()` converts User role to GrantedAuthority
4. Authority is added as `"ROLE_" + user.getRole().name()` = `ROLE_ADMIN`
5. JWT token is generated with user info
6. `JwtResponse` includes role field from `user.getRole()`
7. When accessing admin endpoints, Spring Security checks `@PreAuthorize("hasRole('ADMIN')")`

### Frontend Flow

1. User logs in via login form
2. `apiService.login()` calls backend `/api/auth/login`
3. Backend returns JWT token + user info (including role)
4. `apiService` stores:
   - `fintrackr_token`: JWT token
   - `fintrackr_user`: JSON with {id, username, email, firstName, lastName, role}
5. `profile-handler.js` runs on dashboard load
6. Checks if `user.role === 'ROLE_ADMIN'` or `'ADMIN'` or `'admin'`
7. Shows/hides admin link accordingly

## Adding More Admin Features

To add more admin-only features:

### Backend

Add methods in `AdminController.java`:
```java
@GetMapping("/users")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getAllUsers() {
    // Admin-only endpoint
}
```

### Frontend

Check role before showing UI elements:
```javascript
const userData = sessionStorage.getItem('fintrackr_user');
const user = JSON.parse(userData);
const isAdmin = user.role === 'ROLE_ADMIN' || user.role === 'ADMIN';

if (isAdmin) {
    // Show admin features
}
```

## Security Notes

1. **Password Storage**: Passwords are hashed using BCrypt
2. **JWT Token**: Tokens expire after 24 hours (configurable in application.properties)
3. **Role Check**: Both backend (@PreAuthorize) and frontend (UI visibility) enforce role checks
4. **Admin Creation**: Admin user is only created if it doesn't exist (idempotent)

## Production Recommendations

Before deploying to production:

1. **Change default admin password**:
   ```java
   // In DataLoader.java
   admin.setPassword(passwordEncoder.encode("STRONG_PASSWORD_HERE"));
   ```

2. **Use environment variables**:
   ```properties
   # application.properties
   admin.username=${ADMIN_USERNAME:admin}
   admin.password=${ADMIN_PASSWORD:admin123}
   ```

3. **Add admin user management**:
   - Create endpoint to change admin password
   - Add email verification for admin accounts
   - Implement admin role assignment UI

4. **Audit logging**:
   - Log all admin actions
   - Track admin endpoint access
   - Monitor failed admin login attempts

## Support

For issues or questions:
1. Check console logs (both frontend and backend)
2. Verify database state via H2 console
3. Test API endpoints directly using curl or Postman
4. Check browser DevTools Network tab for API responses

# OfficeGrid - Complete Fix Instructions

## STEP 1: Fix Supabase Dashboard Settings

Go to your Supabase Dashboard: https://supabase.com/dashboard/project/hvotdfpvylxcmfzdcexv

### 1.1 Disable Email Confirmation (CRITICAL!)
1. Go to **Authentication** (left sidebar)
2. Click **Providers**
3. Click **Email**
4. **TURN OFF** "Confirm email" toggle
5. Click **Save**

### 1.2 Check API Keys
1. Go to **Settings** → **API**
2. Under **Project API Keys**, you should see:
   - `anon public` key (starts with `eyJ...` or `sb_publishable_...`)
   - `service_role` key (DON'T use this in app!)

If your key starts with `sb_publishable_`, that's fine - it's the new format.

## STEP 2: Run Database Fix

1. Go to **SQL Editor** in Supabase Dashboard
2. Click **New Query**
3. Open the file: `C:\Users\shakt\Desktop\OfficeGrid\COMPLETE_FIX.sql`
4. Copy ALL the contents
5. Paste into SQL Editor
6. Click **Run**

You should see a list of tables at the end:
- analytics
- audit_logs
- employees
- notification_settings
- notifications
- organisations
- task_remarks
- tasks

## STEP 3: Verify Your local.properties

Your `local.properties` file should have:
```
SUPABASE_URL=https://hvotdfpvylxcmfzdcexv.supabase.co
SUPABASE_ANON_KEY=sb_publishable_O3L1EllbLlKe-6uUFbsE2g_0nArq98x
```

The `sb_publishable_` format is the NEW format and should work!

## STEP 4: Clean and Rebuild the App

In Android Studio:
1. **File** → **Invalidate Caches** → **Invalidate and Restart**
2. After restart: **Build** → **Clean Project**
3. Then: **Build** → **Rebuild Project**

## STEP 5: Test Signup

1. Run the app
2. Try signing up as ADMIN:
   - Email: test@example.com
   - Password: Test1234 (min 8 chars)
   - Workspace ID: MYORG1
   - Organization Name: My Organization
   - Organization Type: Technology

## TROUBLESHOOTING

### If still getting auth errors:

Check Logcat for `SupabaseAuth` tag - it will show the exact error.

Common issues:
1. **"User already registered"** - Use a different email or login instead
2. **"Invalid password"** - Password must be at least 6-8 characters
3. **Network error** - Check internet connection
4. **403/401 error** - Check API key in local.properties

### If database errors:

Run this in SQL Editor to check your tables:
```sql
SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;
```

### If key format issues:

The new `sb_publishable_` key format should work with Supabase Kotlin SDK 3.0.2.
If it doesn't, try getting the JWT format key from Dashboard → Settings → API.

## APP FLOW REMINDER

### Admin Flow:
1. Admin signs up → Creates organization with unique Workspace ID
2. Admin is auto-approved
3. Admin can assign tasks to employees
4. Admin can approve/reject employee join requests

### Employee Flow:
1. Employee signs up (no workspace needed at signup)
2. Employee joins workspace by entering Workspace ID
3. Admin approves the join request
4. Employee can now see tasks assigned to them

---

After following these steps, your app should work!

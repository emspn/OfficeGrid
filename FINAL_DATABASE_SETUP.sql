-- ============================================
-- OFFICEGRID - COMPLETE WORKING DATABASE
-- ============================================
-- STEP 1: Delete EVERYTHING and start fresh
-- STEP 2: Create tables WITHOUT RLS
-- STEP 3: Your app will work
-- ============================================

-- DELETE ALL EXISTING TABLES
DROP TABLE IF EXISTS public.task_remarks CASCADE;
DROP TABLE IF EXISTS public.tasks CASCADE;
DROP TABLE IF EXISTS public.notifications CASCADE;
DROP TABLE IF EXISTS public.notification_settings CASCADE;
DROP TABLE IF EXISTS public.audit_logs CASCADE;
DROP TABLE IF EXISTS public.analytics CASCADE;
DROP TABLE IF EXISTS public.employees CASCADE;
DROP TABLE IF EXISTS public.organisations CASCADE;

-- CREATE ORGANISATIONS (NO RLS)
CREATE TABLE public.organisations (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    type TEXT,
    admin_id UUID NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- CREATE EMPLOYEES (NO RLS)
CREATE TABLE public.employees (
    id UUID NOT NULL,
    name TEXT NOT NULL,
    email TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'EMPLOYEE',
    company_id TEXT NOT NULL,
    status TEXT DEFAULT 'PENDING',
    is_approved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (id, company_id)
);

-- CREATE TASKS (NO RLS)
CREATE TABLE public.tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    status TEXT NOT NULL DEFAULT 'TODO',
    priority TEXT NOT NULL DEFAULT 'MEDIUM',
    assigned_to UUID NOT NULL,
    created_by UUID NOT NULL,
    company_id TEXT NOT NULL,
    due_date TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- CREATE TASK_REMARKS (NO RLS)
CREATE TABLE public.task_remarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL,
    user_id UUID NOT NULL,
    user_name TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- CREATE NOTIFICATIONS (NO RLS)
CREATE TABLE public.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    type TEXT NOT NULL DEFAULT 'SYSTEM',
    is_read BOOLEAN DEFAULT FALSE,
    related_id TEXT,
    company_id TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- CREATE NOTIFICATION_SETTINGS (NO RLS)
CREATE TABLE public.notification_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    task_assigned BOOLEAN DEFAULT TRUE,
    task_updated BOOLEAN DEFAULT TRUE,
    task_overdue BOOLEAN DEFAULT TRUE,
    remarks BOOLEAN DEFAULT TRUE,
    join_requests BOOLEAN DEFAULT TRUE,
    system_notifications BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- CREATE AUDIT_LOGS (NO RLS)
CREATE TABLE public.audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    action TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id TEXT,
    user_id UUID NOT NULL,
    user_name TEXT NOT NULL,
    details TEXT,
    company_id TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- CREATE ANALYTICS (NO RLS)
CREATE TABLE public.analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id TEXT NOT NULL UNIQUE,
    total_tasks INTEGER DEFAULT 0,
    completed_tasks INTEGER DEFAULT 0,
    pending_tasks INTEGER DEFAULT 0,
    in_progress_tasks INTEGER DEFAULT 0,
    total_employees INTEGER DEFAULT 0,
    pending_approvals INTEGER DEFAULT 0,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- DONE - CHECK TABLES CREATED
SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;

-- ============================================
-- OFFICEGRID - COMPLETE DATABASE RESET & SETUP
-- ============================================
-- This script will completely rebuild your database
-- Run this in Supabase SQL Editor
-- ============================================

-- ============================================
-- STEP 1: DROP ALL EXISTING TABLES (Clean Slate)
-- ============================================
DROP TABLE IF EXISTS public.task_remarks CASCADE;
DROP TABLE IF EXISTS public.tasks CASCADE;
DROP TABLE IF EXISTS public.notifications CASCADE;
DROP TABLE IF EXISTS public.notification_settings CASCADE;
DROP TABLE IF EXISTS public.audit_logs CASCADE;
DROP TABLE IF EXISTS public.analytics CASCADE;
DROP TABLE IF EXISTS public.task_templates CASCADE;
DROP TABLE IF EXISTS public.employee_workspaces CASCADE;
DROP TABLE IF EXISTS public.employees CASCADE;
DROP TABLE IF EXISTS public.organisations CASCADE;

-- ============================================
-- STEP 2: CREATE ORGANISATIONS TABLE
-- ============================================
CREATE TABLE public.organisations (
    id TEXT PRIMARY KEY,                    -- Workspace code like "ACME123"
    name TEXT NOT NULL,                     -- Organization name
    type TEXT,                              -- Organization type
    admin_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.organisations ENABLE ROW LEVEL SECURITY;

-- Anyone can view organisations (to validate workspace codes)
CREATE POLICY "Anyone can view organisations" ON public.organisations
    FOR SELECT USING (true);

-- Users can create their own organisation
CREATE POLICY "Users can create organisations" ON public.organisations
    FOR INSERT WITH CHECK (admin_id = auth.uid());

-- Admins can manage their organisation
CREATE POLICY "Admins can manage their organisation" ON public.organisations
    FOR ALL USING (admin_id = auth.uid());

-- ============================================
-- STEP 3: CREATE EMPLOYEES TABLE
-- ============================================
-- Composite primary key allows same user to join multiple workspaces
CREATE TABLE public.employees (
    id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    email TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'EMPLOYEE',
    company_id TEXT NOT NULL REFERENCES public.organisations(id) ON DELETE CASCADE,
    status TEXT DEFAULT 'PENDING',
    is_approved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    user_email TEXT,
    PRIMARY KEY (id, company_id)            -- Composite key for multi-workspace
);

ALTER TABLE public.employees ENABLE ROW LEVEL SECURITY;

-- Employees can view their own records
CREATE POLICY "Users can view own employee records" ON public.employees
    FOR SELECT USING (id = auth.uid());

-- Admins can view employees in their workspace
CREATE POLICY "Admins can view workspace employees" ON public.employees
    FOR SELECT USING (
        company_id IN (SELECT id FROM public.organisations WHERE admin_id = auth.uid())
    );

-- Users can insert themselves (join workspace)
CREATE POLICY "Users can join workspaces" ON public.employees
    FOR INSERT WITH CHECK (id = auth.uid());

-- Admins can update employees (approve/reject)
CREATE POLICY "Admins can update employees" ON public.employees
    FOR UPDATE USING (
        company_id IN (SELECT id FROM public.organisations WHERE admin_id = auth.uid())
    );

-- Admins can delete employees
CREATE POLICY "Admins can delete employees" ON public.employees
    FOR DELETE USING (
        company_id IN (SELECT id FROM public.organisations WHERE admin_id = auth.uid())
    );

-- ============================================
-- STEP 4: CREATE TASKS TABLE
-- ============================================
CREATE TABLE public.tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    status TEXT NOT NULL DEFAULT 'TODO' CHECK (status IN ('TODO', 'IN_PROGRESS', 'PENDING_COMPLETION', 'DONE')),
    priority TEXT NOT NULL DEFAULT 'MEDIUM' CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
    assigned_to UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_by UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    company_id TEXT NOT NULL REFERENCES public.organisations(id) ON DELETE CASCADE,
    due_date TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;

-- Users can view tasks in their workspace
CREATE POLICY "Users can view tasks" ON public.tasks
    FOR SELECT USING (
        -- Admin can see all tasks in their org
        company_id IN (SELECT id FROM public.organisations WHERE admin_id = auth.uid())
        OR
        -- Employee can see tasks assigned to them
        assigned_to = auth.uid()
    );

-- Admins can insert tasks
CREATE POLICY "Admins can create tasks" ON public.tasks
    FOR INSERT WITH CHECK (
        company_id IN (SELECT id FROM public.organisations WHERE admin_id = auth.uid())
    );

-- Admins can update any task, employees can update their own
CREATE POLICY "Users can update tasks" ON public.tasks
    FOR UPDATE USING (
        company_id IN (SELECT id FROM public.organisations WHERE admin_id = auth.uid())
        OR assigned_to = auth.uid()
    );

-- Admins can delete tasks
CREATE POLICY "Admins can delete tasks" ON public.tasks
    FOR DELETE USING (
        company_id IN (SELECT id FROM public.organisations WHERE admin_id = auth.uid())
    );

-- ============================================
-- STEP 5: CREATE TASK REMARKS TABLE
-- ============================================
CREATE TABLE public.task_remarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES public.tasks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    user_name TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.task_remarks ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can view remarks" ON public.task_remarks
    FOR SELECT USING (true);

CREATE POLICY "Users can add remarks" ON public.task_remarks
    FOR INSERT WITH CHECK (user_id = auth.uid());

CREATE POLICY "Users can delete own remarks" ON public.task_remarks
    FOR DELETE USING (user_id = auth.uid());

-- ============================================
-- STEP 6: CREATE NOTIFICATIONS TABLE
-- ============================================
CREATE TABLE public.notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    type TEXT NOT NULL DEFAULT 'SYSTEM',
    is_read BOOLEAN DEFAULT FALSE,
    related_id TEXT,                        -- For deep linking to tasks
    company_id TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;

-- Users can see their own notifications
CREATE POLICY "Users can view own notifications" ON public.notifications
    FOR SELECT USING (user_id = auth.uid());

-- Anyone can insert notifications (system inserts for other users)
CREATE POLICY "Anyone can insert notifications" ON public.notifications
    FOR INSERT WITH CHECK (true);

-- Users can update own notifications (mark as read)
CREATE POLICY "Users can update own notifications" ON public.notifications
    FOR UPDATE USING (user_id = auth.uid());

-- Users can delete own notifications
CREATE POLICY "Users can delete own notifications" ON public.notifications
    FOR DELETE USING (user_id = auth.uid());

-- ============================================
-- STEP 7: CREATE NOTIFICATION SETTINGS TABLE
-- ============================================
CREATE TABLE public.notification_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES auth.users(id) ON DELETE CASCADE,
    task_assigned BOOLEAN DEFAULT TRUE,
    task_updated BOOLEAN DEFAULT TRUE,
    task_overdue BOOLEAN DEFAULT TRUE,
    remarks BOOLEAN DEFAULT TRUE,
    join_requests BOOLEAN DEFAULT TRUE,
    system_notifications BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.notification_settings ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Users can manage own settings" ON public.notification_settings
    FOR ALL USING (user_id = auth.uid());

-- ============================================
-- STEP 8: CREATE AUDIT LOGS TABLE
-- ============================================
CREATE TABLE public.audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    action TEXT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id TEXT,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    user_name TEXT NOT NULL,
    details TEXT,
    company_id TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Admins can view audit logs" ON public.audit_logs
    FOR SELECT USING (
        company_id IN (SELECT id FROM public.organisations WHERE admin_id = auth.uid())
    );

CREATE POLICY "Anyone can insert audit logs" ON public.audit_logs
    FOR INSERT WITH CHECK (true);

-- ============================================
-- STEP 9: CREATE ANALYTICS TABLE
-- ============================================
CREATE TABLE public.analytics (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    company_id TEXT NOT NULL UNIQUE REFERENCES public.organisations(id) ON DELETE CASCADE,
    total_tasks INTEGER DEFAULT 0,
    completed_tasks INTEGER DEFAULT 0,
    pending_tasks INTEGER DEFAULT 0,
    in_progress_tasks INTEGER DEFAULT 0,
    total_employees INTEGER DEFAULT 0,
    pending_approvals INTEGER DEFAULT 0,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.analytics ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Admins can view analytics" ON public.analytics
    FOR SELECT USING (
        company_id IN (SELECT id FROM public.organisations WHERE admin_id = auth.uid())
    );

CREATE POLICY "Anyone can manage analytics" ON public.analytics
    FOR ALL USING (true);

-- ============================================
-- STEP 10: CREATE TASK TEMPLATES TABLE
-- ============================================
CREATE TABLE public.task_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    priority TEXT DEFAULT 'MEDIUM',
    company_id TEXT NOT NULL REFERENCES public.organisations(id) ON DELETE CASCADE,
    created_by UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.task_templates ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Admins can manage templates" ON public.task_templates
    FOR ALL USING (
        company_id IN (SELECT id FROM public.organisations WHERE admin_id = auth.uid())
    );

-- ============================================
-- STEP 11: CREATE INDEXES
-- ============================================
CREATE INDEX idx_employees_id ON public.employees(id);
CREATE INDEX idx_employees_company_id ON public.employees(company_id);
CREATE INDEX idx_employees_is_approved ON public.employees(is_approved);

CREATE INDEX idx_tasks_company_id ON public.tasks(company_id);
CREATE INDEX idx_tasks_assigned_to ON public.tasks(assigned_to);
CREATE INDEX idx_tasks_status ON public.tasks(status);
CREATE INDEX idx_tasks_created_at ON public.tasks(created_at DESC);

CREATE INDEX idx_notifications_user_id ON public.notifications(user_id);
CREATE INDEX idx_notifications_is_read ON public.notifications(is_read);
CREATE INDEX idx_notifications_created_at ON public.notifications(created_at DESC);

CREATE INDEX idx_audit_logs_company_id ON public.audit_logs(company_id);
CREATE INDEX idx_audit_logs_created_at ON public.audit_logs(created_at DESC);

-- ============================================
-- STEP 12: ENABLE REALTIME
-- ============================================
-- Enable realtime for key tables (ignore errors if already added)
DO $$
BEGIN
    BEGIN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.tasks;
    EXCEPTION WHEN duplicate_object THEN
        NULL;
    END;
    BEGIN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.employees;
    EXCEPTION WHEN duplicate_object THEN
        NULL;
    END;
    BEGIN
        ALTER PUBLICATION supabase_realtime ADD TABLE public.notifications;
    EXCEPTION WHEN duplicate_object THEN
        NULL;
    END;
END $$;

-- ============================================
-- DONE! 🎉
-- ============================================
-- Your database is now ready!
--
-- Tables created:
-- 1. organisations - Workspaces/companies
-- 2. employees - User memberships (multi-workspace)
-- 3. tasks - Task management
-- 4. task_remarks - Comments on tasks
-- 5. notifications - User notifications
-- 6. notification_settings - User preferences
-- 7. audit_logs - Activity logs
-- 8. analytics - Dashboard stats
-- 9. task_templates - Task templates
--
-- All RLS policies are configured!
-- Realtime is enabled for tasks, employees, notifications!
-- ============================================

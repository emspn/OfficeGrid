-- ============================================
-- 🛡️ OFFICEGRID - ELITE UNIFIED PRODUCTION SETUP
-- WARNING: This script drops ALL existing data and recreates the OS environment.
-- Run this ONCE in your Supabase SQL Editor.
-- Last Updated: Feb 2, 2026
-- ============================================

-- STEP 1: ABSOLUTE WIPE (Force drop everything)
DROP TABLE IF EXISTS public.task_remarks CASCADE;
DROP TABLE IF EXISTS public.tasks CASCADE;
DROP TABLE IF EXISTS public.notifications CASCADE;
DROP TABLE IF EXISTS public.notification_settings CASCADE;
DROP TABLE IF EXISTS public.audit_logs CASCADE;
DROP TABLE IF EXISTS public.analytics CASCADE;
DROP TABLE IF EXISTS public.employees CASCADE;
DROP TABLE IF EXISTS public.organisations CASCADE;

DROP FUNCTION IF EXISTS public.join_workspace CASCADE;
DROP FUNCTION IF EXISTS public.approve_employee CASCADE;
DROP FUNCTION IF EXISTS public.update_operative_role CASCADE;

-- STEP 2: CORE IDENTITY ARCHITECTURE

-- 1. ORGANISATIONS (Central Workspace Registry)
CREATE TABLE public.organisations (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    type TEXT,
    admin_id UUID NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. EMPLOYEES (Multi-Node Membership)
CREATE TABLE public.employees (
    id UUID NOT NULL,
    name TEXT NOT NULL,
    email TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'EMPLOYEE',
    company_id TEXT NOT NULL REFERENCES public.organisations(id) ON DELETE CASCADE,
    status TEXT DEFAULT 'PENDING',
    is_approved BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    PRIMARY KEY (id, company_id)
);

-- 3. TASKS (Operational Unit Registry)
CREATE TABLE public.tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    status TEXT NOT NULL DEFAULT 'TODO',
    priority TEXT NOT NULL DEFAULT 'MEDIUM',
    assigned_to UUID NOT NULL,
    created_by UUID NOT NULL,
    company_id TEXT NOT NULL REFERENCES public.organisations(id) ON DELETE CASCADE,
    due_date TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. TASK REMARKS (Communication Logs)
CREATE TABLE public.task_remarks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id UUID NOT NULL REFERENCES public.tasks(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    user_name TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. NOTIFICATIONS (Neural Handshake Stream)
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

-- 6. AUDIT LOGS (System Integrity Registry)
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

-- STEP 3: SYSTEM LOGIC (Handshake Functions)

-- 🧬 Join Workspace Logic
CREATE OR REPLACE FUNCTION public.join_workspace(
    workspace_code TEXT,
    employee_user_id UUID,
    employee_name TEXT,
    employee_email TEXT
) RETURNS VOID AS $$
BEGIN
    INSERT INTO public.employees (id, name, email, company_id, status, is_approved)
    VALUES (employee_user_id, employee_name, employee_email, workspace_code, 'PENDING', FALSE)
    ON CONFLICT (id, company_id) DO NOTHING;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 🛡️ Node Approval Logic
CREATE OR REPLACE FUNCTION public.approve_employee(employee_id UUID, workspace_id TEXT)
RETURNS VOID AS $$
BEGIN
    UPDATE public.employees
    SET is_approved = TRUE, status = 'APPROVED'
    WHERE id = employee_id AND company_id = workspace_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- 🛠️ Role Optimization Logic
CREATE OR REPLACE FUNCTION public.update_operative_role(employee_id UUID, workspace_id TEXT, new_role TEXT)
RETURNS VOID AS $$
BEGIN
    UPDATE public.employees 
    SET role = new_role 
    WHERE id = employee_id AND company_id = workspace_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- STEP 4: SECURITY & PERFORMANCE

-- Disable RLS for rapid deployment (Enable in production)
ALTER TABLE public.organisations DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.employees DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.tasks DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.task_remarks DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications DISABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs DISABLE ROW LEVEL SECURITY;

-- High-Performance Indexing
CREATE INDEX IF NOT EXISTS idx_employees_lookup ON public.employees(id, company_id);
CREATE INDEX IF NOT EXISTS idx_tasks_node ON public.tasks(assigned_to, company_id);
CREATE INDEX IF NOT EXISTS idx_audit_stream ON public.audit_logs(company_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_stream ON public.notifications(user_id, created_at DESC);

-- STEP 5: ENABLE NEURAL REALTIME
-- Clean publication
DROP PUBLICATION IF EXISTS supabase_realtime;
CREATE PUBLICATION supabase_realtime;

-- Add tables to stream
ALTER PUBLICATION supabase_realtime ADD TABLE public.organisations;
ALTER PUBLICATION supabase_realtime ADD TABLE public.employees;
ALTER PUBLICATION supabase_realtime ADD TABLE public.tasks;
ALTER PUBLICATION supabase_realtime ADD TABLE public.notifications;
ALTER PUBLICATION supabase_realtime ADD TABLE public.audit_logs;

-- STEP 6: VERIFY FINAL STATE
SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;

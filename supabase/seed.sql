-- OfficeGrid Production Seed Data
-- Note: Replace UUID placeholders with actual IDs from your Supabase Auth > Users table

-- 1. Clear existing tasks (optional, for clean seed)
-- TRUNCATE public.tasks;

-- 2. Insert Tasks
-- Assumptions:
-- ADMIN_ID: UUID of admin@officegrid.com
-- EMP1_ID: UUID of emp1@officegrid.com
-- EMP2_ID: UUID of emp2@officegrid.com

INSERT INTO public.tasks (title, description, status, priority, assigned_to, created_by)
VALUES 
  -- Admin tasks (Admin sees all regardless of assignment)
  ('Infrastructure Hardening', 'Verify all RLS policies and environment configs', 'DONE', 'HIGH', 'ADMIN_ID', 'ADMIN_ID'),
  ('Team Sync', 'Weekly coordination meeting', 'TODO', 'MEDIUM', 'ADMIN_ID', 'ADMIN_ID'),
  ('Budget Approval', 'Review Q4 department budgets', 'IN_PROGRESS', 'HIGH', 'ADMIN_ID', 'ADMIN_ID'),

  -- Employee 1 tasks (Only visible to Admin and Emp 1)
  ('Feature A Implementation', 'Develop the core module for Feature A', 'IN_PROGRESS', 'MEDIUM', 'EMP1_ID', 'ADMIN_ID'),
  ('Documentation', 'Update technical specs for the API', 'DONE', 'LOW', 'EMP1_ID', 'ADMIN_ID'),

  -- Employee 2 tasks (Only visible to Admin and Emp 2)
  ('Bug Fix #102', 'Resolve the intermittent crash in navigation', 'TODO', 'HIGH', 'EMP2_ID', 'ADMIN_ID'),
  ('Unit Testing', 'Achieve 80% coverage on data layer', 'TODO', 'MEDIUM', 'EMP2_ID', 'ADMIN_ID');

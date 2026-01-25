-- 1. Enable RLS (Ensure it's enabled)
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;

-- 2. Drop existing policies to avoid conflicts during update
DROP POLICY IF EXISTS "Admins can view all tasks" ON public.tasks;
DROP POLICY IF EXISTS "Employees can view assigned tasks" ON public.tasks;
DROP POLICY IF EXISTS "Admins can select all tasks" ON public.tasks;
DROP POLICY IF EXISTS "Employees can select assigned tasks" ON public.tasks;

-- 3. HARDENED ADMIN POLICY: Full read access to all tasks
-- Uses nullif/coalesce to prevent access if role metadata is missing or malformed.
CREATE POLICY "Admins can view all tasks" ON public.tasks
FOR SELECT
TO authenticated
USING (
  coalesce(nullif(auth.jwt() -> 'user_metadata' ->> 'role', ''), 'NONE') = 'ADMIN'
);

-- 4. HARDENED EMPLOYEE POLICY: Read access only to assigned tasks
-- Enforces that assigned_to matches the user's UID and the role is explicitly 'EMPLOYEE'.
CREATE POLICY "Employees can view assigned tasks" ON public.tasks
FOR SELECT
TO authenticated
USING (
  coalesce(nullif(auth.jwt() -> 'user_metadata' ->> 'role', ''), 'NONE') = 'EMPLOYEE'
  AND assigned_to IS NOT NULL 
  AND assigned_to = auth.uid()
);

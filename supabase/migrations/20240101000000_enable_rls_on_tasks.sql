-- Enable Row Level Security
ALTER TABLE public.tasks ENABLE ROW LEVEL SECURITY;

-- ADMIN Policy: Full read access to all tasks
CREATE POLICY "Admins can view all tasks" ON public.tasks
FOR SELECT
TO authenticated
USING (
  (auth.jwt() -> 'user_metadata' ->> 'role') = 'ADMIN'
);

-- EMPLOYEE Policy: Read access only to assigned tasks
CREATE POLICY "Employees can view assigned tasks" ON public.tasks
FOR SELECT
TO authenticated
USING (
  (auth.jwt() -> 'user_metadata' ->> 'role') = 'EMPLOYEE'
  AND assigned_to = auth.uid()
);

-- Ensure default deny for other operations (since only SELECT was requested)
-- Note: RLS is already "deny by default", so no explicit deny policies are strictly needed 
-- unless there are existing non-RLS permissions.

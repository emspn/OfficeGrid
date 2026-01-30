-- Multi-Workspace Support for Employees
-- This allows one employee to join multiple workspaces

-- ============================================
-- STEP 1: UPDATE EMPLOYEES TABLE
-- ============================================

-- Add workspace_id column if not exists (for clarity)
-- Note: company_id already exists and serves as workspace_id

-- Add status column for each workspace membership
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'employees' AND column_name = 'status') THEN
        ALTER TABLE public.employees
        ADD COLUMN status TEXT DEFAULT 'pending' CHECK (status IN ('pending', 'active', 'rejected'));
    END IF;
END $$;

-- Add user_email column to track which user owns this membership
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'employees' AND column_name = 'user_email') THEN
        ALTER TABLE public.employees
        ADD COLUMN user_email TEXT;
    END IF;
END $$;

-- ============================================
-- STEP 2: UPDATE PRIMARY KEY & CONSTRAINTS
-- ============================================

-- Drop old unique constraint if exists
ALTER TABLE public.employees DROP CONSTRAINT IF EXISTS employees_email_key;

-- Create composite unique constraint: one user can join each company only once
-- But same user can join multiple companies
CREATE UNIQUE INDEX IF NOT EXISTS employees_user_company_unique
ON public.employees(id, company_id);

-- ============================================
-- STEP 3: UPDATE RLS POLICIES
-- ============================================

-- Drop old policies
DROP POLICY IF EXISTS "Employees can view own data" ON public.employees;
DROP POLICY IF EXISTS "Admins can view company employees" ON public.employees;
DROP POLICY IF EXISTS "Allow employee insert on signup" ON public.employees;

-- New Policy 1: Employees can view their own workspace memberships
CREATE POLICY "Employees can view own memberships"
ON public.employees
FOR SELECT
TO authenticated
USING (
    id = auth.uid()
    OR user_email = auth.jwt() ->> 'email'
);

-- New Policy 2: Admins can view employees in their company
CREATE POLICY "Admins can view company employees"
ON public.employees
FOR SELECT
TO authenticated
USING (
    company_id IN (
        SELECT company_id
        FROM public.employees
        WHERE id = auth.uid()
        AND is_approved = true
    )
    AND coalesce(nullif(auth.jwt() -> 'user_metadata' ->> 'role', ''), 'EMPLOYEE') = 'ADMIN'
);

-- New Policy 3: Allow employee to join workspaces (insert with pending status)
CREATE POLICY "Allow employee workspace join"
ON public.employees
FOR INSERT
TO authenticated
WITH CHECK (
    id = auth.uid()
    AND status = 'pending'
);

-- New Policy 4: Admins can update employee status in their company
CREATE POLICY "Admins can update employee status"
ON public.employees
FOR UPDATE
TO authenticated
USING (
    company_id IN (
        SELECT company_id
        FROM public.employees
        WHERE id = auth.uid()
        AND is_approved = true
    )
    AND coalesce(nullif(auth.jwt() -> 'user_metadata' ->> 'role', ''), 'EMPLOYEE') = 'ADMIN'
)
WITH CHECK (
    company_id IN (
        SELECT company_id
        FROM public.employees
        WHERE id = auth.uid()
        AND is_approved = true
    )
);

-- ============================================
-- STEP 4: CREATE HELPER FUNCTIONS
-- ============================================

-- Function to get employee's active workspaces
CREATE OR REPLACE FUNCTION get_employee_workspaces(employee_id UUID)
RETURNS TABLE (
    company_id TEXT,
    company_name TEXT,
    status TEXT,
    task_count BIGINT
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        e.company_id,
        o.name as company_name,
        e.status,
        COUNT(t.id) as task_count
    FROM public.employees e
    LEFT JOIN public.organisations o ON o.id = e.company_id
    LEFT JOIN public.tasks t ON t.company_id = e.company_id AND t.assigned_to = employee_id
    WHERE e.id = employee_id
    GROUP BY e.company_id, o.name, e.status;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Function to join a workspace
CREATE OR REPLACE FUNCTION join_workspace(
    workspace_code TEXT,
    employee_user_id UUID,
    employee_name TEXT,
    employee_email TEXT
)
RETURNS JSON AS $$
DECLARE
    workspace_exists BOOLEAN;
    already_member BOOLEAN;
    result JSON;
BEGIN
    -- Check if workspace exists
    SELECT EXISTS(
        SELECT 1 FROM public.organisations WHERE id = workspace_code
    ) INTO workspace_exists;

    IF NOT workspace_exists THEN
        RETURN json_build_object(
            'success', false,
            'error', 'Workspace code not found'
        );
    END IF;

    -- Check if already a member
    SELECT EXISTS(
        SELECT 1 FROM public.employees
        WHERE id = employee_user_id AND company_id = workspace_code
    ) INTO already_member;

    IF already_member THEN
        RETURN json_build_object(
            'success', false,
            'error', 'Already a member of this workspace'
        );
    END IF;

    -- Insert employee record with pending status
    INSERT INTO public.employees (
        id,
        company_id,
        name,
        email,
        user_email,
        role,
        status,
        is_approved
    ) VALUES (
        employee_user_id,
        workspace_code,
        employee_name,
        employee_email,
        employee_email,
        'EMPLOYEE',
        'pending',
        false
    );

    RETURN json_build_object(
        'success', true,
        'message', 'Join request sent. Waiting for admin approval.'
    );

EXCEPTION WHEN OTHERS THEN
    RETURN json_build_object(
        'success', false,
        'error', SQLERRM
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================
-- STEP 5: UPDATE TASKS TABLE QUERIES
-- ============================================

-- Tasks are already company-specific via company_id
-- No changes needed to tasks table structure

-- ============================================
-- STEP 6: CREATE INDEXES FOR PERFORMANCE
-- ============================================

-- Index for faster workspace lookups
CREATE INDEX IF NOT EXISTS idx_employees_user_id_status
ON public.employees(id, status);

-- Index for faster company employee lookups
CREATE INDEX IF NOT EXISTS idx_employees_company_status
ON public.employees(company_id, status);

-- Index for faster task filtering by workspace
CREATE INDEX IF NOT EXISTS idx_tasks_company_assigned
ON public.tasks(company_id, assigned_to);

-- ============================================
-- STEP 7: MIGRATE EXISTING DATA
-- ============================================

-- Update existing employees to have 'active' status
UPDATE public.employees
SET status = CASE
    WHEN is_approved = true THEN 'active'
    ELSE 'pending'
END
WHERE status IS NULL;

-- Update user_email for existing employees
UPDATE public.employees
SET user_email = email
WHERE user_email IS NULL;

-- ============================================
-- STEP 8: VERIFY MIGRATION
-- ============================================

-- Check employees table structure
SELECT
    column_name,
    data_type,
    column_default
FROM information_schema.columns
WHERE table_name = 'employees'
ORDER BY ordinal_position;

-- Check indexes
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'employees';

-- Check policies
SELECT policyname, permissive, roles, cmd, qual
FROM pg_policies
WHERE tablename = 'employees';

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- 1. Check if an employee can join multiple workspaces
-- SELECT * FROM public.employees WHERE id = 'USER_ID';

-- 2. Get all workspaces for an employee
-- SELECT * FROM get_employee_workspaces('USER_ID');

-- 3. Test joining a workspace
-- SELECT join_workspace('COMPANY_ID', 'USER_ID', 'Employee Name', 'email@example.com');

-- ============================================
-- ROLLBACK (if needed)
-- ============================================

-- To rollback this migration, run:
/*
DROP FUNCTION IF EXISTS get_employee_workspaces(UUID);
DROP FUNCTION IF EXISTS join_workspace(TEXT, UUID, TEXT, TEXT);
DROP INDEX IF EXISTS idx_employees_user_id_status;
DROP INDEX IF EXISTS idx_employees_company_status;
DROP INDEX IF EXISTS idx_tasks_company_assigned;
ALTER TABLE public.employees DROP COLUMN IF EXISTS status;
ALTER TABLE public.employees DROP COLUMN IF EXISTS user_email;
*/

-- ============================================
-- NOTES
-- ============================================

/*
KEY CHANGES:
1. Added 'status' column: pending, active, rejected
2. Added 'user_email' column: tracks user across workspaces
3. Updated RLS policies: support multi-workspace
4. Created helper functions: get_employee_workspaces, join_workspace
5. Added indexes: for better performance
6. Migrated existing data: set default status

USAGE:
- Employee signs up (no company_id needed)
- Employee calls join_workspace('ABC123', user_id, name, email)
- Record created with status='pending'
- Admin approves: UPDATE employees SET status='active', is_approved=true
- Employee can now access that workspace
- Employee can join multiple workspaces by calling join_workspace again

BACKWARD COMPATIBILITY:
- Existing code will continue to work
- is_approved column still works
- company_id is still the workspace identifier
- Just adds multi-workspace capability on top
*/

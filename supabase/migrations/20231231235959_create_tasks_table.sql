-- Create Tasks Table
CREATE TABLE IF NOT EXISTS public.tasks (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    title text NOT NULL,
    description text NOT NULL DEFAULT '',
    status text NOT NULL DEFAULT 'TODO',
    priority text NOT NULL DEFAULT 'MEDIUM',
    assigned_to uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_by uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    company_id text NOT NULL,
    due_date bigint NOT NULL,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_to ON public.tasks(assigned_to);
CREATE INDEX IF NOT EXISTS idx_tasks_created_by ON public.tasks(created_by);
CREATE INDEX IF NOT EXISTS idx_tasks_company_id ON public.tasks(company_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON public.tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date ON public.tasks(due_date);

-- Add constraint for status enum
ALTER TABLE public.tasks
ADD CONSTRAINT tasks_status_check
CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE'));

-- Add constraint for priority enum
ALTER TABLE public.tasks
ADD CONSTRAINT tasks_priority_check
CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH'));

-- Add trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION public.update_tasks_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_tasks_timestamp
    BEFORE UPDATE ON public.tasks
    FOR EACH ROW
    EXECUTE FUNCTION public.update_tasks_updated_at();

-- Note: RLS policies are defined in separate migration files:
-- - 20240101000000_enable_rls_on_tasks.sql
-- - 20240101000001_hardened_rls_tasks.sql

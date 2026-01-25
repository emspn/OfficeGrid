-- Create Task Remarks Table
CREATE TABLE IF NOT EXISTS public.task_remarks (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    task_id uuid NOT NULL REFERENCES public.tasks(id) ON DELETE CASCADE,
    message text NOT NULL,
    created_by uuid NOT NULL REFERENCES auth.users(id),
    created_at timestamp with time zone DEFAULT now()
);

-- Enable RLS
ALTER TABLE public.task_remarks ENABLE ROW LEVEL SECURITY;

-- ADMIN Policy: Can view and add all remarks
CREATE POLICY "Admins can view all remarks" ON public.task_remarks
FOR SELECT TO authenticated
USING (
  coalesce(nullif(auth.jwt() -> 'user_metadata' ->> 'role', ''), 'NONE') = 'ADMIN'
);

CREATE POLICY "Admins can insert any remark" ON public.task_remarks
FOR INSERT TO authenticated
WITH CHECK (
  coalesce(nullif(auth.jwt() -> 'user_metadata' ->> 'role', ''), 'NONE') = 'ADMIN'
);

-- EMPLOYEE Policy: Can view and add remarks only for their assigned tasks
CREATE POLICY "Employees can view remarks for their assigned tasks" ON public.task_remarks
FOR SELECT TO authenticated
USING (
  EXISTS (
    SELECT 1 FROM public.tasks 
    WHERE public.tasks.id = public.task_remarks.task_id 
    AND public.tasks.assigned_to = auth.uid()
  )
);

CREATE POLICY "Employees can add remarks to their assigned tasks" ON public.task_remarks
FOR INSERT TO authenticated
WITH CHECK (
  EXISTS (
    SELECT 1 FROM public.tasks 
    WHERE public.tasks.id = task_id 
    AND public.tasks.assigned_to = auth.uid()
  )
  AND created_by = auth.uid()
);

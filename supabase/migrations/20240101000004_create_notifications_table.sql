-- Create Notification Types Enum
DO $$ BEGIN
    CREATE TYPE public.notification_type AS ENUM ('TASK_ASSIGNED', 'TASK_UPDATED', 'TASK_COMPLETED', 'NEW_REMARK', 'TASK_OVERDUE');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Create Notifications Table
CREATE TABLE IF NOT EXISTS public.notifications (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    title text NOT NULL,
    message text NOT NULL,
    type public.notification_type NOT NULL,
    company_id text NOT NULL,
    is_read boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now()
);

-- Enable RLS
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;

-- ADMIN Policy: Can view all notifications in the same company
CREATE POLICY "Admins can view company notifications" ON public.notifications
FOR SELECT TO authenticated
USING (
  coalesce(nullif(auth.jwt() -> 'user_metadata' ->> 'role', ''), 'NONE') = 'ADMIN'
  AND company_id = (auth.jwt() -> 'user_metadata' ->> 'company_id')
);

-- EMPLOYEE Policy: Can view only their own notifications
CREATE POLICY "Users can view own notifications" ON public.notifications
FOR SELECT TO authenticated
USING (
  user_id = auth.uid()
);

-- Policy to allow users to mark their own notifications as read
CREATE POLICY "Users can update own notifications" ON public.notifications
FOR UPDATE TO authenticated
USING (user_id = auth.uid())
WITH CHECK (user_id = auth.uid());

-- TRIGGER logic for automated notifications (Conceptual placeholders)
-- In a real production environment, these would be expanded to handle 
-- automated inserts when tasks or remarks change.

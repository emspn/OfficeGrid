-- Enable Realtime for Tasks Table
-- This allows real-time synchronization between Company and Employee apps

-- Enable Realtime publication for tasks
ALTER PUBLICATION supabase_realtime ADD TABLE public.tasks;

-- Enable Realtime publication for employees
ALTER PUBLICATION supabase_realtime ADD TABLE public.employees;

-- Enable Realtime publication for task_remarks (for future use)
ALTER PUBLICATION supabase_realtime ADD TABLE public.task_remarks;

-- Enable Realtime publication for notifications (if not already enabled)
ALTER PUBLICATION supabase_realtime ADD TABLE public.notifications;

-- Verify publications
SELECT * FROM pg_publication_tables WHERE pubname = 'supabase_realtime';

-- Note: After running this migration, restart your Supabase Realtime service
-- or wait a few seconds for changes to propagate

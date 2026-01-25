-- Create Notification Settings Table
CREATE TABLE IF NOT EXISTS public.notification_settings (
    user_id uuid PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    task_assigned boolean DEFAULT true,
    task_updated boolean DEFAULT true,
    task_overdue boolean DEFAULT true,
    remarks boolean DEFAULT true,
    updated_at timestamp with time zone DEFAULT now()
);

-- Enable RLS
ALTER TABLE public.notification_settings ENABLE ROW LEVEL SECURITY;

-- User Policy: Users can only manage their own settings
CREATE POLICY "Users can manage own settings" ON public.notification_settings
FOR ALL TO authenticated
USING (user_id = auth.uid())
WITH CHECK (user_id = auth.uid());

-- Admin Policy: Admins can view all settings
CREATE POLICY "Admins can view all settings" ON public.notification_settings
FOR SELECT TO authenticated
USING (
  coalesce(nullif(auth.jwt() -> 'user_metadata' ->> 'role', ''), 'NONE') = 'ADMIN'
);

-- Trigger to create default settings on user creation
CREATE OR REPLACE FUNCTION public.handle_new_user_notification_settings()
RETURNS trigger AS $$
BEGIN
  INSERT INTO public.notification_settings (user_id)
  VALUES (new.id);
  RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'on_auth_user_created_settings') THEN
        CREATE TRIGGER on_auth_user_created_settings
          AFTER INSERT ON auth.users
          FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user_notification_settings();
    END IF;
END $$;

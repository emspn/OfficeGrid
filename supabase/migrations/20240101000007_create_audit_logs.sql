-- Create Audit Logs Table
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id uuid DEFAULT gen_random_uuid() PRIMARY KEY,
    event_type text NOT NULL,
    title text NOT NULL,
    description text NOT NULL,
    user_id uuid NOT NULL REFERENCES auth.users(id),
    user_email text NOT NULL,
    company_id text NOT NULL,
    created_at timestamp with time zone DEFAULT now()
);

-- Enable RLS
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;

-- ADMIN Policy: Can view all audit logs in the company
CREATE POLICY "Admins can view company audit logs" ON public.audit_logs
FOR SELECT TO authenticated
USING (
  coalesce(nullif(auth.jwt() -> 'user_metadata' ->> 'role', ''), 'NONE') = 'ADMIN'
  AND company_id = (auth.jwt() -> 'user_metadata' ->> 'company_id')
);

-- Deny all for non-admins (implicitly denied by RLS being enabled and no other policies)

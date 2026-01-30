-- Create Organisations Table
CREATE TABLE IF NOT EXISTS public.organisations (
    id text PRIMARY KEY,
    name text NOT NULL,
    type text NOT NULL,
    admin_id uuid NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    created_at timestamp with time zone DEFAULT now(),
    updated_at timestamp with time zone DEFAULT now()
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_organisations_admin_id ON public.organisations(admin_id);

-- Enable RLS
ALTER TABLE public.organisations ENABLE ROW LEVEL SECURITY;

-- ADMIN Policy: Admins can view their own organisation
CREATE POLICY "Admins can view their organisation" ON public.organisations
FOR SELECT TO authenticated
USING (
  admin_id = auth.uid()
  OR id = (auth.jwt() -> 'user_metadata' ->> 'company_id')
);

-- ADMIN Policy: Only admins can insert their organisation (during signup)
CREATE POLICY "Admins can insert their organisation" ON public.organisations
FOR INSERT TO authenticated
WITH CHECK (
  coalesce(nullif(auth.jwt() -> 'user_metadata' ->> 'role', ''), 'NONE') = 'ADMIN'
  AND admin_id = auth.uid()
);

-- ADMIN Policy: Admins can update their own organisation
CREATE POLICY "Admins can update their organisation" ON public.organisations
FOR UPDATE TO authenticated
USING (admin_id = auth.uid())
WITH CHECK (admin_id = auth.uid());

-- EMPLOYEE Policy: Employees can view their company organisation
CREATE POLICY "Employees can view their company organisation" ON public.organisations
FOR SELECT TO authenticated
USING (
  id = (auth.jwt() -> 'user_metadata' ->> 'company_id')
);

-- Add trigger to update updated_at timestamp
CREATE OR REPLACE FUNCTION public.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_organisations_updated_at
    BEFORE UPDATE ON public.organisations
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at_column();

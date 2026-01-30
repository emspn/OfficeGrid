-- Create Employees Table
CREATE TABLE IF NOT EXISTS public.employees (
    id uuid PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    name text NOT NULL,
    email text NOT NULL,
    role text NOT NULL,
    company_id text NOT NULL,
    is_approved boolean DEFAULT false,
    created_at timestamp with time zone DEFAULT now()
);

-- Enable RLS
ALTER TABLE public.employees ENABLE ROW LEVEL SECURITY;

-- ADMIN Policy: Can view all employees in the same company
CREATE POLICY "Admins can view company employees" ON public.employees
FOR SELECT TO authenticated
USING (
  coalesce(nullif(auth.jwt() -> 'user_metadata' ->> 'role', ''), 'NONE') = 'ADMIN'
  AND company_id = (auth.jwt() -> 'user_metadata' ->> 'company_id')
);

-- EMPLOYEE Policy: Can view only their own profile
CREATE POLICY "Employees can view own profile" ON public.employees
FOR SELECT TO authenticated
USING (
  id = auth.uid()
);

-- ADMIN Policy: Can update employee approval status
CREATE POLICY "Admins can update employee approval" ON public.employees
FOR UPDATE TO authenticated
USING (
  coalesce(nullif(auth.jwt() -> 'user_metadata' ->> 'role', ''), 'NONE') = 'ADMIN'
  AND company_id = (auth.jwt() -> 'user_metadata' ->> 'company_id')
)
WITH CHECK (
  coalesce(nullif(auth.jwt() -> 'user_metadata' ->> 'role', ''), 'NONE') = 'ADMIN'
  AND company_id = (auth.jwt() -> 'user_metadata' ->> 'company_id')
);

-- Trigger to keep employees table in sync with auth.users (Optional but recommended)
CREATE OR REPLACE FUNCTION public.handle_new_user_profile()
RETURNS trigger AS $$
BEGIN
  INSERT INTO public.employees (id, name, email, role, company_id, is_approved)
  VALUES (
    new.id, 
    coalesce(new.raw_user_meta_data->>'full_name', new.email), 
    new.email, 
    coalesce(new.raw_user_meta_data->>'role', 'EMPLOYEE'), 
    coalesce(new.raw_user_meta_data->>'company_id', 'default'),
    coalesce((new.raw_user_meta_data->>'is_approved')::boolean,
      CASE WHEN coalesce(new.raw_user_meta_data->>'role', 'EMPLOYEE') = 'ADMIN' THEN true ELSE false END
    )
  );
  RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Check if trigger exists before creating
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'on_auth_user_created_profile') THEN
        CREATE TRIGGER on_auth_user_created_profile
          AFTER INSERT ON auth.users
          FOR EACH ROW EXECUTE PROCEDURE public.handle_new_user_profile();
    END IF;
END $$;

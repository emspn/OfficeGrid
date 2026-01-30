-- Add is_approved column to employees table
ALTER TABLE public.employees
ADD COLUMN IF NOT EXISTS is_approved boolean DEFAULT false;

-- Create index for faster queries
CREATE INDEX IF NOT EXISTS idx_employees_is_approved ON public.employees(is_approved);
CREATE INDEX IF NOT EXISTS idx_employees_company_approved ON public.employees(company_id, is_approved);

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

-- Update trigger to set is_approved based on role
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
    coalesce((new.raw_user_meta_data->>'is_approved')::boolean, false)
  );
  RETURN new;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- For existing employees without is_approved, set based on role
-- Admins are auto-approved, employees need manual approval
UPDATE public.employees
SET is_approved = CASE
    WHEN role = 'ADMIN' THEN true
    ELSE false
END
WHERE is_approved IS NULL;

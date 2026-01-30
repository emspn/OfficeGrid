-- Migration to add is_approved column to existing employees table
-- Run this if you already have the employees table created

-- Add is_approved column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public'
        AND table_name = 'employees'
        AND column_name = 'is_approved'
    ) THEN
        ALTER TABLE public.employees
        ADD COLUMN is_approved boolean DEFAULT false;

        -- Set admins to approved, employees to pending
        UPDATE public.employees
        SET is_approved = CASE
            WHEN role = 'ADMIN' THEN true
            ELSE false
        END;
    END IF;
END $$;

-- Create or replace the policy for updating approval status
DROP POLICY IF EXISTS "Admins can update employee approval" ON public.employees;

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

-- Update the trigger function to include is_approved
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

-- Add index for better query performance
CREATE INDEX IF NOT EXISTS idx_employees_is_approved ON public.employees(is_approved);
CREATE INDEX IF NOT EXISTS idx_employees_company_approved ON public.employees(company_id, is_approved);

-- Fix employees table policies for signup
-- Add missing INSERT policy so new users can create their profile

-- Add INSERT policy for new signups (allows users to insert their own profile)
CREATE POLICY "Users can insert own profile" ON public.employees
FOR INSERT TO authenticated
WITH CHECK (
  id = auth.uid()
);

-- Also add a policy for service role to insert during signup
CREATE POLICY "Service role can insert profiles" ON public.employees
FOR INSERT TO service_role
WITH CHECK (true);

-- Grant necessary permissions
GRANT INSERT ON public.employees TO authenticated;
GRANT INSERT ON public.employees TO service_role;

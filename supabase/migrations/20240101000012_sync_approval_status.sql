-- Function to update user metadata when employee is approved
-- This ensures auth.users metadata stays in sync with employees table

CREATE OR REPLACE FUNCTION public.update_user_approval_status()
RETURNS trigger AS $$
BEGIN
  -- When is_approved changes in employees table, update auth.users metadata
  IF NEW.is_approved IS DISTINCT FROM OLD.is_approved THEN
    UPDATE auth.users
    SET raw_user_meta_data = jsonb_set(
      COALESCE(raw_user_meta_data, '{}'::jsonb),
      '{is_approved}',
      to_jsonb(NEW.is_approved)
    )
    WHERE id = NEW.id;
  END IF;

  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Drop trigger if exists and recreate
DROP TRIGGER IF EXISTS sync_user_approval_status ON public.employees;

CREATE TRIGGER sync_user_approval_status
  AFTER UPDATE OF is_approved ON public.employees
  FOR EACH ROW
  EXECUTE FUNCTION public.update_user_approval_status();

-- Also create a function that can be called directly by admins
CREATE OR REPLACE FUNCTION public.approve_employee(employee_id uuid)
RETURNS void AS $$
BEGIN
  -- Update employees table
  UPDATE public.employees
  SET is_approved = true
  WHERE id = employee_id;

  -- Update auth metadata
  UPDATE auth.users
  SET raw_user_meta_data = jsonb_set(
    COALESCE(raw_user_meta_data, '{}'::jsonb),
    '{is_approved}',
    'true'::jsonb
  )
  WHERE id = employee_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION public.approve_employee(uuid) TO authenticated;

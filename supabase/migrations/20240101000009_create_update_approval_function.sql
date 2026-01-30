-- Create RPC function to update user approval status in auth.users metadata
-- This is needed because we can't directly UPDATE auth.users from client-side code

CREATE OR REPLACE FUNCTION public.update_user_approval_status(user_id uuid, is_approved boolean)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
BEGIN
  -- Only allow admins to approve users
  IF coalesce(nullif(auth.jwt() -> 'user_metadata' ->> 'role', ''), 'NONE') != 'ADMIN' THEN
    RAISE EXCEPTION 'Only admins can approve users';
  END IF;

  -- Update the user's metadata
  UPDATE auth.users
  SET raw_user_meta_data =
    jsonb_set(
      COALESCE(raw_user_meta_data, '{}'::jsonb),
      '{is_approved}',
      to_jsonb(is_approved)
    )
  WHERE id = user_id;

  -- Verify the update was successful
  IF NOT FOUND THEN
    RAISE EXCEPTION 'User not found';
  END IF;
END;
$$;

-- Grant execute permission to authenticated users
GRANT EXECUTE ON FUNCTION public.update_user_approval_status TO authenticated;

-- Add helpful comment
COMMENT ON FUNCTION public.update_user_approval_status IS 'Updates the is_approved flag in auth.users metadata. Admin only.';

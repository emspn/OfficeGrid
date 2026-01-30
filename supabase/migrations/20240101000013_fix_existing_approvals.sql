-- Quick fix: Update existing users' metadata if employees table shows approved
-- Run this once to fix any users who were approved but metadata not synced

DO $$
DECLARE
    emp_record RECORD;
BEGIN
    FOR emp_record IN
        SELECT id, is_approved
        FROM public.employees
        WHERE is_approved = true
    LOOP
        UPDATE auth.users
        SET raw_user_meta_data = jsonb_set(
            COALESCE(raw_user_meta_data, '{}'::jsonb),
            '{is_approved}',
            'true'::jsonb
        )
        WHERE id = emp_record.id
        AND (raw_user_meta_data->>'is_approved')::boolean IS DISTINCT FROM true;

        RAISE NOTICE 'Updated user %', emp_record.id;
    END LOOP;
END $$;

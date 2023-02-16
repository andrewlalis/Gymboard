SELECT
    u.id as id,
    u.email as email,
    u.name as name,
    (
        SELECT COUNT(id)
        FROM submission
        WHERE submission.user_id = u.id
    ) as submission_count,
    p.account_private as account_private,
    p.locale as locale
FROM auth_user u
LEFT JOIN auth_user_preferences p ON u.id = p.user_id
WHERE u.activated = TRUE
ORDER BY u.created_at;
SELECT
    u.id as id,
    u.email as email,
    u.name as name
FROM auth_user u
LEFT JOIN auth_user_preferences p ON u.id = p.user_id
WHERE u.activated = TRUE
ORDER BY u.created_at;
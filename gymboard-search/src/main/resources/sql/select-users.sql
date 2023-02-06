SELECT
    u.id as id,
    u.email as email,
    u.name as name
FROM auth_user u
WHERE u.activated = TRUE
ORDER BY u.created_at;
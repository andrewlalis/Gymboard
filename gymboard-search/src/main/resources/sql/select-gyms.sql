SELECT
    gym.short_name as short_name,
    gym.display_name as display_name,
    city.short_name as city_short_name,
    city.name as city_name,
    country.code as country_code,
    country.name as country_name,
    gym.street_address as street_address,
    gym.latitude as latitude,
    gym.longitude as longitude
FROM gym
LEFT JOIN city on gym.city_short_name = city.short_name
LEFT JOIN country on gym.city_country_code = country.code
ORDER BY gym.created_at;

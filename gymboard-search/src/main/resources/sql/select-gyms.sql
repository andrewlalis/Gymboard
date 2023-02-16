SELECT
    gym.short_name AS short_name,
    gym.display_name AS display_name,
    city.short_name AS city_short_name,
    city.name AS city_name,
    country.code AS country_code,
    country.name AS country_name,
    gym.street_address AS street_address,
    gym.latitude AS latitude,
    gym.longitude AS longitude,
    (
        SELECT COUNT(id)
        FROM submission
        WHERE submission.gym_short_name = gym.short_name AND
              submission.gym_city_short_name = gym.city_short_name AND
              submission.gym_city_country_code = gym.city_country_code
    ) AS submission_count
FROM gym
LEFT JOIN city ON gym.city_short_name = city.short_name
LEFT JOIN country ON gym.city_country_code = country.code
ORDER BY gym.created_at;

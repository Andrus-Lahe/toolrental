-- Apply once to existing databases to expose the tool's city in the AI search view.
CREATE OR REPLACE VIEW tool_rental.ai_tool_search AS
SELECT
    t.id AS tool_id,
    t.name AS tool_name,
    c.category_name AS category_name,
    d.district_name AS location_name,
    (
        t.status = 'A'
        AND NOT EXISTS (
            SELECT 1
            FROM tool_rental.booking b
            WHERE b.tool_id = t.id
              AND b.status = 'C'
              AND (CURRENT_TIMESTAMP AT TIME ZONE 'Europe/Tallinn')::date
                  BETWEEN b.start_date AND b.end_date
        )
    ) AS is_available_now,
    t.description AS tool_description,
    (t.status = 'A') AS is_visible,
    ci.city_name AS city_name
FROM tool_rental.tool t
JOIN tool_rental.category c ON c.id = t.category_id
LEFT JOIN tool_rental.profile p ON p.user_id = t.owner_id
LEFT JOIN tool_rental.location l ON l.id = p.location_id
LEFT JOIN tool_rental.district d ON d.id = l.district_id
LEFT JOIN tool_rental.city ci ON ci.id = d.city_id;

-- Local demo data. The google_sub value is fictitious and cannot be used for Google login.
-- Run against database with psql -X -v ON_ERROR_STOP=1 -f docs/database/seed-marko.sql.
BEGIN;
SET LOCAL search_path TO public;
SET LOCAL client_encoding TO 'UTF8';

INSERT INTO role (id, role_name) VALUES (1, 'admin'), (2, 'customer')
    ON CONFLICT (id) DO UPDATE SET role_name = EXCLUDED.role_name;
SELECT setval('role_id_seq', GREATEST((SELECT max(id) FROM role),
    (SELECT last_value FROM role_id_seq)), true);

DO $$
DECLARE
    demo_role_id integer;
    demo_city_id integer;
    demo_district_id integer;
    demo_location_id integer;
    demo_user_id integer;
    drill_category_id integer;
    ladder_category_id integer;
BEGIN
    IF EXISTS (SELECT 1 FROM app_user WHERE google_sub = 'demo-marko-tamm') THEN
        RAISE NOTICE 'Marko demo data already exists; no changes made.';
        RETURN;
    END IF;

    SELECT id INTO STRICT demo_role_id FROM role WHERE role_name = 'admin';

    INSERT INTO city (city_name) VALUES ('Tallinn') ON CONFLICT (city_name) DO NOTHING;
    SELECT id INTO STRICT demo_city_id FROM city WHERE city_name = 'Tallinn';

    INSERT INTO district (city_id, district_name) VALUES (demo_city_id, 'Kristiine')
        ON CONFLICT (city_id, district_name) DO NOTHING;
    SELECT id INTO STRICT demo_district_id FROM district
        WHERE city_id = demo_city_id AND district_name = 'Kristiine';

    INSERT INTO location (district_id, street_name, house_number)
        VALUES (demo_district_id, 'Teddre', '28') RETURNING id INTO demo_location_id;

    INSERT INTO app_user (first_name, last_name, google_sub, role_id, status)
        VALUES ('Marko', 'Tamm', 'demo-marko-tamm', demo_role_id, 'A')
        RETURNING id INTO demo_user_id;

    INSERT INTO profile (location_id, user_id, email, phone, created_at, updated_at)
        VALUES (demo_location_id, demo_user_id, 'email@Gmail.com', '56565656',
            TIMESTAMP '2026-09-18 10:00:00', TIMESTAMP '2026-09-18 10:00:00');

    INSERT INTO category (category_name) VALUES ('Elektritööriistad'), ('Redelid')
        ON CONFLICT (category_name) DO NOTHING;
    SELECT id INTO STRICT drill_category_id FROM category WHERE category_name = 'Elektritööriistad';
    SELECT id INTO STRICT ladder_category_id FROM category WHERE category_name = 'Redelid';

    INSERT INTO tool (owner_id, category_id, name, description, status, created_at, updated_at)
        VALUES
        (demo_user_id, drill_category_id, 'Akutrell',
            '18 V akutrell, kaks akut ja laadija. Heas korras.', 'A',
            TIMESTAMP '2026-09-18 10:15:00', TIMESTAMP '2026-09-18 10:15:00'),
        (demo_user_id, ladder_category_id, 'Redel',
            'Alumiiniumist kokkupandav redel, töökõrgus 3 meetrit.', 'U',
            TIMESTAMP '2026-09-18 10:20:00', TIMESTAMP '2026-09-18 10:20:00');

    -- Liis, her tools, the rental and demo images are added below.
END;
$$;

DO $$
DECLARE
    marko_id integer;
    liis_id integer;
    customer_role_id integer;
    tallinn_id integer;
    liis_district_id integer;
    liis_location_id integer;
    electric_category_id integer;
    garden_category_id integer;
    ladder_id integer;
BEGIN
    SELECT id INTO STRICT marko_id FROM app_user WHERE google_sub = 'demo-marko-tamm';
    SELECT id INTO STRICT customer_role_id FROM role WHERE role_name = 'customer';
    SELECT id INTO STRICT tallinn_id FROM city WHERE city_name = 'Tallinn';

    INSERT INTO district (city_id, district_name) VALUES (tallinn_id, 'Mustamäe')
        ON CONFLICT (city_id, district_name) DO NOTHING;
    SELECT id INTO STRICT liis_district_id FROM district
        WHERE city_id = tallinn_id AND district_name = 'Mustamäe';

    SELECT id INTO liis_id FROM app_user WHERE google_sub = 'demo-liis-kask';
    IF liis_id IS NULL THEN
        INSERT INTO app_user (first_name, last_name, google_sub, role_id, status)
            VALUES ('Liis', 'Kask', 'demo-liis-kask', customer_role_id, 'A')
            RETURNING id INTO liis_id;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM profile WHERE user_id = liis_id) THEN
        INSERT INTO location (district_id, street_name, house_number, apartment_number)
            VALUES (liis_district_id, 'Sõpruse pst', '120', '8')
            RETURNING id INTO liis_location_id;
        INSERT INTO profile (location_id, user_id, email, phone, created_at, updated_at)
            VALUES (liis_location_id, liis_id, 'liis.kask@example.com', '55501002',
                TIMESTAMP '2026-09-18 10:30:00', TIMESTAMP '2026-09-18 10:30:00');
    END IF;

    INSERT INTO category (category_name) VALUES ('Aiatööriistad')
        ON CONFLICT (category_name) DO NOTHING;
    SELECT id INTO STRICT electric_category_id FROM category WHERE category_name = 'Elektritööriistad';
    SELECT id INTO STRICT garden_category_id FROM category WHERE category_name = 'Aiatööriistad';

    INSERT INTO tool (owner_id, category_id, name, description, status, created_at, updated_at)
        SELECT liis_id, electric_category_id, 'Tikksaag',
            '500 W elektriline tikksaag koos kolme saelehega. Sobib puidu lõikamiseks.', 'A',
            TIMESTAMP '2026-09-18 10:40:00', TIMESTAMP '2026-09-18 10:40:00'
        WHERE NOT EXISTS (SELECT 1 FROM tool WHERE owner_id = liis_id AND name = 'Tikksaag');

    INSERT INTO tool (owner_id, category_id, name, description, status, created_at, updated_at)
        SELECT liis_id, garden_category_id, 'Hekikäärid',
            'Käsitsi kasutatavad hekikäärid, tera pikkus 25 cm. Heas korras.', 'A',
            TIMESTAMP '2026-09-18 10:45:00', TIMESTAMP '2026-09-18 10:45:00'
        WHERE NOT EXISTS (SELECT 1 FROM tool WHERE owner_id = liis_id AND name = 'Hekikäärid');

    SELECT id INTO STRICT ladder_id FROM tool WHERE owner_id = marko_id AND name = 'Redel';
    IF NOT EXISTS (
        SELECT 1 FROM booking WHERE tool_id = ladder_id AND renter_id = liis_id
            AND start_date = DATE '2026-09-18' AND end_date = DATE '2026-09-21'
    ) THEN
        IF EXISTS (
            SELECT 1 FROM booking WHERE tool_id = ladder_id AND status = 'C'
                AND start_date <= DATE '2026-09-21' AND end_date >= DATE '2026-09-18'
        ) THEN
            RAISE EXCEPTION 'The demo rental overlaps an existing confirmed booking.';
        END IF;
        INSERT INTO booking (tool_id, renter_id, start_date, end_date, status,
                             owner_message, created_at, updated_at)
            VALUES (ladder_id, liis_id, DATE '2026-09-18', DATE '2026-09-21', 'C',
                'Palun tagasta redel 21. septembril enne kella 18.',
                TIMESTAMP '2026-09-18 11:00:00', TIMESTAMP '2026-09-18 11:00:00');
        UPDATE tool SET status = 'U', updated_at = TIMESTAMP '2026-09-18 11:00:00'
            WHERE id = ladder_id;
    END IF;
    -- google_event_id stays NULL: no real Google Calendar event is created.
END;
$$;

-- Simple SVG illustrations, stored as UTF-8 bytes; these are demo icons, not photos.
WITH illustrations (google_sub, tool_name, drawing) AS (
    VALUES
    ('demo-marko-tamm', 'Akutrell',
     '<path fill="#2563eb" d="M50 60h105v45H50z M85 105h35v60H85z"/><path fill="#334155" d="M155 70h25v25h-25z M180 78h30v9h-30z M70 155h65v15H70z"/>'),
    ('demo-marko-tamm', 'Redel',
     '<g stroke="#64748b" stroke-width="10" fill="none"><path d="M70 175L90 35 M170 175L150 35 M86 60h68 M82 90h76 M78 120h84 M74 150h92"/></g>'),
    ('demo-liis-kask', 'Tikksaag',
     '<path fill="#0d9488" d="M65 90h100v60H65z"/><path fill="none" stroke="#0d9488" stroke-width="15" d="M85 90V55h55v35"/><path stroke="#334155" stroke-width="8" d="M55 155h125 M150 130v45"/>'),
    ('demo-liis-kask', 'Hekikäärid',
     '<g fill="none" stroke-width="10"><path stroke="#64748b" d="M65 35l105 135 M175 35L70 170"/><path stroke="#ea580c" d="M145 140l25 30 M95 140l-25 30"/></g><circle cx="120" cy="105" r="9" fill="#334155"/>')
)
INSERT INTO tool_image (tool_id, image_data, is_main)
    SELECT t.id,
        convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240">'
            || '<rect width="240" height="240" rx="16" fill="#f1f5f9"/>'
            || i.drawing || '<text x="120" y="215" text-anchor="middle" font-family="sans-serif" font-size="20" fill="#0f172a">'
            || i.tool_name || '</text></svg>', 'UTF8'), true
    FROM illustrations i
    JOIN app_user u ON u.google_sub = i.google_sub
    JOIN tool t ON t.owner_id = u.id AND t.name = i.tool_name
    WHERE NOT EXISTS (SELECT 1 FROM tool_image ti WHERE ti.tool_id = t.id);

UPDATE app_user SET role_id = 1 WHERE google_sub = 'demo-marko-tamm' AND role_id <> 1;
UPDATE app_user SET role_id = 2 WHERE google_sub = 'demo-liis-kask' AND role_id <> 2;

COMMIT;

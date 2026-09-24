-- Role
INSERT INTO role (id, role_name) VALUES
    (1, 'admin'),
    (2, 'customer');

-- App User
INSERT INTO app_user (id, role_id, first_name, last_name, google_sub, status) VALUES
    (1, 1, 'Marko', 'Tamm', 'demo-marko-tamm', 'A'),
    (3, 2, 'Liis', 'Kask', 'demo-liis-kask', 'A');

-- City
INSERT INTO city (id, city_name) VALUES
    (1, 'Tallinn'),
    (2, 'Tartu'),
    (3, 'Pärnu');

-- District
INSERT INTO district (id, city_id, district_name) VALUES
    -- Tallinn
    (1, 1, 'Kristiine'),
    (2, 1, 'Mustamäe'),
    (3, 1, 'Haabersti'),
    (4, 1, 'Kesklinn'),
    (5, 1, 'Lasnamäe'),
    (6, 1, 'Nõmme'),
    (7, 1, 'Pirita'),
    (8, 1, 'Põhja-Tallinn'),
    -- Tartu
    (9, 2, 'Annelinn'),
    (10, 2, 'Ihaste'),
    (11, 2, 'Jaamamõisa'),
    (12, 2, 'Karlova'),
    (13, 2, 'Kesklinn'),
    (14, 2, 'Kvissentali'),
    (15, 2, 'Maarjamõisa'),
    (16, 2, 'Raadi-Kruusamäe'),
    (17, 2, 'Ropka'),
    (18, 2, 'Ropka tööstusrajoon'),
    (19, 2, 'Ränilinn'),
    (20, 2, 'Supilinn'),
    (21, 2, 'Tammelinn'),
    (22, 2, 'Tähtvere'),
    (23, 2, 'Vaksali'),
    (24, 2, 'Variku'),
    (25, 2, 'Veeriku'),
    (26, 2, 'Ülejõe'),
    -- Pärnu
    (27, 3, 'Vana-Pärnu'),
    (28, 3, 'Ülejõe'),
    (29, 3, 'Rääma'),
    (30, 3, 'Tammiste'),
    (31, 3, 'Kesklinn'),
    (32, 3, 'Raeküla'),
    (33, 3, 'Lodja');

-- Location
INSERT INTO location (id, district_id, street_name, house_number, apartment_number, lng, lat) VALUES
    (1, 1, 'Teddre', '28', NULL, NULL, NULL),
    (3, 2, 'Sõpruse pst', '120', '8', NULL, NULL);

-- Profile
INSERT INTO profile (id, location_id, user_id, email, phone, created_at, updated_at) VALUES
    (1, 1, 1, 'email@Gmail.com', '56565656', '2026-09-18 10:00:00', '2026-09-18 10:00:00'),
    (3, 3, 3, 'liis.kask@example.com', '55501002', '2026-09-18 10:30:00', '2026-09-18 10:30:00');

-- Category
INSERT INTO category (id, category_name, description, sequence) VALUES
    (1, 'Aiatööd', 'Muruniidukid, labidad, rehad', 100),
    (2, 'Ehitustööd', 'Trellid, ketassaed, redelid', 200),
    (3, 'Koristamine', 'Tekstiilipesurid, aknapesurid, aurupesurid', 300),
    (4, 'Muud', 'Lumelabidad, naabrimehed, naabrinaised', 10000);

-- Category Image
INSERT INTO category_image (id, category_id, image_data) VALUES
    (1, 1, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240"><rect width="240" height="240" rx="16" fill="#f1f5f9"/><circle cx="120" cy="120" r="40" fill="#16a34a"/><text x="120" y="200" text-anchor="middle" font-family="sans-serif" font-size="18" fill="#0f172a">Aiatööd</text></svg>', 'UTF8')),
    (2, 2, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240"><rect width="240" height="240" rx="16" fill="#f1f5f9"/><circle cx="120" cy="120" r="40" fill="#2563eb"/><text x="120" y="200" text-anchor="middle" font-family="sans-serif" font-size="18" fill="#0f172a">Ehitustööd</text></svg>', 'UTF8')),
    (3, 3, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240"><rect width="240" height="240" rx="16" fill="#f1f5f9"/><circle cx="120" cy="120" r="40" fill="#0d9488"/><text x="120" y="200" text-anchor="middle" font-family="sans-serif" font-size="18" fill="#0f172a">Koristamine</text></svg>', 'UTF8')),
    (4, 4, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240"><rect width="240" height="240" rx="16" fill="#f1f5f9"/><circle cx="120" cy="120" r="40" fill="#ea580c"/><text x="120" y="200" text-anchor="middle" font-family="sans-serif" font-size="18" fill="#0f172a">Muud</text></svg>', 'UTF8'));

-- Tool
INSERT INTO tool (id, owner_id, category_id, name, description, status, created_at, updated_at) VALUES
    (1, 1, 2, 'Akutrell', '18 V akutrell, kaks akut ja laadija. Sobib puurimiseks ja kruvide keeramiseks.', 'A', '2026-09-18 10:15:00', '2026-09-18 10:15:00'),
    (2, 1, 2, 'Redel', 'Alumiiniumist kokkupandav redel, töökõrgus 3 meetrit.', 'U', '2026-09-18 10:20:00', '2026-09-18 11:00:00'),
    (3, 3, 3, 'Tolmuimeja', '1200 W tolmuimeja koos põrandaotsiku ja praootsikuga. Sobib kodu koristamiseks.', 'A', '2026-09-18 10:40:00', '2026-09-18 10:40:00'),
    (4, 3, 1, 'Hekikäärid', 'Käsitsi kasutatavad hekikäärid, tera pikkus 25 cm. Sobivad heki pügamiseks.', 'A', '2026-09-18 10:45:00', '2026-09-18 10:45:00'),
    (5, 1, 1, 'Muruniiduk', 'Elektriline muruniiduk, lõikelaius 32 cm ja 30-liitrine kogumiskast.', 'A', '2026-09-18 11:00:00', '2026-09-18 11:00:00'),
    (6, 1, 3, 'Survepesur', '130-baarine survepesur koos 6-meetrise voolikuga. Sobib terrassi ja aia puhastamiseks.', 'A', '2026-09-18 11:15:00', '2026-09-18 11:15:00'),
    (7, 1, 4, 'Matkatelk', 'Kahekohaline veekindel matkatelk koos vaiade ja kandekotiga.', 'A', '2026-09-18 11:30:00', '2026-09-18 11:30:00'),
    (8, 3, 4, 'Projektor', 'Full HD projektor HDMI-sisendiga. Kaasas toitejuhe ja kaugjuhtimispult.', 'A', '2026-09-18 11:45:00', '2026-09-18 11:45:00');

-- Tool Image
INSERT INTO tool_image (id, tool_id, image_data, is_main) VALUES
    (1, 1, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240"><rect width="240" height="240" rx="16" fill="#f1f5f9"/><path fill="#2563eb" d="M50 60h105v45H50z M85 105h35v60H85z\"/><path fill="#334155" d="M155 70h25v25h-25z M180 78h30v9h-30z M70 155h65v15H70z\"/><text x="120" y="215" text-anchor="middle" font-family="sans-serif" font-size="20" fill="#0f172a">Akutrell</text></svg>', 'UTF8'), true),
    (2, 2, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240"><rect width="240" height="240" rx="16" fill="#f1f5f9"/><g stroke="#64748b" stroke-width="10" fill="none"><path d="M70 175L90 35 M170 175L150 35 M86 60h68 M82 90h76 M78 120h84 M74 150h92\"/></g><text x="120" y="215" text-anchor="middle" font-family="sans-serif" font-size="20" fill="#0f172a">Redel</text></svg>', 'UTF8'), true),
    (3, 3, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240"><rect width="240" height="240" rx="16" fill="#f1f5f9"/><path fill="#0d9488" d="M65 90h100v60H65z\"/><path fill="none" stroke="#0d9488" stroke-width="15" d="M85 90V55h55v35\"/><path stroke="#334155" stroke-width="8" d="M55 155h125 M150 130v45\"/><text x="120" y="215" text-anchor="middle" font-family="sans-serif" font-size="20" fill="#0f172a">Tolmuimeja</text></svg>', 'UTF8'), true),
    (4, 4, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240"><rect width="240" height="240" rx="16" fill="#f1f5f9"/><g fill="none" stroke-width="10"><path stroke="#64748b" d="M65 35l105 135 M175 35L70 170\"/><path stroke="#ea580c" d="M145 140l25 30 M95 140l-25 30\"/></g><circle cx="120" cy="105" r="9" fill="#334155\"/><text x="120" y="215" text-anchor="middle" font-family="sans-serif" font-size="20" fill="#0f172a">Hekikäärid</text></svg>', 'UTF8'), true),
    (5, 5, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240"><rect width="240" height="240" rx="16" fill="#f1f5f9"/><text x="120" y="125" text-anchor="middle" font-family="sans-serif" font-size="20" fill="#0f172a">Muruniiduk</text></svg>', 'UTF8'), true),
    (6, 6, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240"><rect width="240" height="240" rx="16" fill="#f1f5f9"/><text x="120" y="125" text-anchor="middle" font-family="sans-serif" font-size="20" fill="#0f172a">Survepesur</text></svg>', 'UTF8'), true),
    (7, 7, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240"><rect width="240" height="240" rx="16" fill="#f1f5f9"/><text x="120" y="125" text-anchor="middle" font-family="sans-serif" font-size="20" fill="#0f172a">Matkatelk</text></svg>', 'UTF8'), true),
    (8, 8, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="240" height="240" viewBox="0 0 240 240"><rect width="240" height="240" rx="16" fill="#f1f5f9"/><text x="120" y="125" text-anchor="middle" font-family="sans-serif" font-size="20" fill="#0f172a">Projektor</text></svg>', 'UTF8'), true);

-- Booking
INSERT INTO booking (id, tool_id, renter_id, start_date, end_date, status, owner_message, google_event_id, created_at, updated_at) VALUES
    (1, 1, 3, '2026-10-02', '2026-10-04', 'P', NULL, NULL, '2026-09-18 11:00:00', '2026-09-18 11:00:00'),
    (2, 2, 3, '2026-09-18', '2026-09-21', 'C', 'Palun tagasta redel 21. septembril enne kella 18.', NULL, '2026-09-18 11:00:00', '2026-09-18 11:00:00'),
    (3, 4, 1, '2026-10-05', '2026-10-07', 'R', 'Soovitud kuupäevadel ei saa tööriista välja laenata.', NULL, '2026-09-18 11:00:00', '2026-09-18 11:00:00');

-- Reset sequence values to max ID
SELECT setval('role_id_seq', coalesce((SELECT max(id) FROM role), 1));
SELECT setval('app_user_id_seq', coalesce((SELECT max(id) FROM app_user), 1));
SELECT setval('city_id_seq', coalesce((SELECT max(id) FROM city), 1));
SELECT setval('district_id_seq', coalesce((SELECT max(id) FROM district), 1));
SELECT setval('location_id_seq', coalesce((SELECT max(id) FROM location), 1));
SELECT setval('profile_id_seq', coalesce((SELECT max(id) FROM profile), 1));
SELECT setval('category_id_seq', coalesce((SELECT max(id) FROM category), 1));
SELECT setval('category_image_id_seq', coalesce((SELECT max(id) FROM category_image), 1));
SELECT setval('tool_id_seq', coalesce((SELECT max(id) FROM tool), 1));
SELECT setval('tool_image_id_seq', coalesce((SELECT max(id) FROM tool_image), 1));
SELECT setval('booking_id_seq', coalesce((SELECT max(id) FROM booking), 1));

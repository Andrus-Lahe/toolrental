-- Role
INSERT INTO role (id, role_name) VALUES
    (1, 'admin'),
    (2, 'customer');

-- App User
INSERT INTO app_user (id, role_id, first_name, last_name, google_sub, status) VALUES
    (1, 1, 'Marko', 'Tamm', 'demo-marko-tamm', 'A'),
    (3, 2, 'Liis', 'Kask', 'demo-liis-kask', 'A'),
    (4, 2, 'Karin', 'Saar', 'demo-karin-saar', 'A'),
    (5, 2, 'Rasmus', 'Lepp', 'demo-rasmus-lepp', 'A'),
    (6, 2, 'Triin', 'Mets', 'demo-triin-mets', 'A'),
    (7, 2, 'Martin', 'Oja', 'demo-martin-oja', 'A'),
    (8, 2, 'Anneli', 'Põld', 'demo-anneli-põld', 'A'),
    (9, 2, 'Sander', 'Kivi', 'demo-sander-kivi', 'A'),
    (10, 2, 'Mari', 'Kuusk', 'demo-mari-kuusk', 'A'),
    (11, 2, 'Joonas', 'Rebane', 'demo-joonas-rebane', 'A'),
    (12, 2, 'Eliise', 'Teder', 'demo-eliise-teder', 'A');

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
    (3, 2, 'Sõpruse pst', '120', '8', NULL, NULL),
    (4, 5, 'Pae', '18', '12', NULL, NULL),
    (5, 6, 'Vabaduse pst', '91', NULL, NULL, NULL),
    (6, 8, 'Kopli', '42', '5', NULL, NULL),
    (7, 12, 'Kalevi', '24', '3', NULL, NULL),
    (8, 9, 'Kalda tee', '11', '8', NULL, NULL),
    (9, 22, 'Kreutzwaldi', '33', NULL, NULL, NULL),
    (10, 29, 'Rääma', '27', '6', NULL, NULL),
    (11, 31, 'Aia', '8', '2', NULL, NULL),
    (12, 32, 'Riia mnt', '74', NULL, NULL, NULL);

-- Profile
INSERT INTO profile (id, location_id, user_id, email, phone, created_at, updated_at) VALUES
    (1, 1, 1, 'marko.tamm@example.com', '+372 5550 1101', '2026-09-18 10:00:00', '2026-09-18 10:00:00'),
    (3, 3, 3, 'liis.kask@example.com', '+372 5550 1103', '2026-09-18 10:30:00', '2026-09-18 10:30:00'),
    (4, 4, 4, 'karin.saar@example.com', '+372 5550 1104', '2026-09-18 12:00:00', '2026-09-18 12:00:00'),
    (5, 5, 5, 'rasmus.lepp@example.com', '+372 5550 1105', '2026-09-18 12:00:00', '2026-09-18 12:00:00'),
    (6, 6, 6, 'triin.mets@example.com', '+372 5550 1106', '2026-09-18 12:00:00', '2026-09-18 12:00:00'),
    (7, 7, 7, 'martin.oja@example.com', '+372 5550 1107', '2026-09-18 12:00:00', '2026-09-18 12:00:00'),
    (8, 8, 8, 'anneli.pold@example.com', '+372 5550 1108', '2026-09-18 12:00:00', '2026-09-18 12:00:00'),
    (9, 9, 9, 'sander.kivi@example.com', '+372 5550 1109', '2026-09-18 12:00:00', '2026-09-18 12:00:00'),
    (10, 10, 10, 'mari.kuusk@example.com', '+372 5550 1110', '2026-09-18 12:00:00', '2026-09-18 12:00:00'),
    (11, 11, 11, 'joonas.rebane@example.com', '+372 5550 1111', '2026-09-18 12:00:00', '2026-09-18 12:00:00'),
    (12, 12, 12, 'eliise.teder@example.com', '+372 5550 1112', '2026-09-18 12:00:00', '2026-09-18 12:00:00');

-- Category
INSERT INTO category (id, category_name, description, sequence) VALUES
    (1, 'Aiatööd', 'Muruniidukid, labidad, rehad', 100),
    (2, 'Ehitustööd', 'Trellid, ketassaed, redelid', 200),
    (3, 'Koristamine', 'Tekstiilipesurid, aknapesurid, aurupesurid', 300),
    (4, 'Muud', 'Lumelabidad, naabrimehed, naabrinaised', 10000);

-- Category Image
INSERT INTO category_image (id, category_id, image_data) VALUES
    (1, 1, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#eaf5e8"/><stop offset="1" stop-color="#cce7d6"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#85aa85" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path d="M56 99h104l-15 34H66z" fill="#4b9b6b"/><path d="M85 98V59h53l18 40" fill="#62bd81"/><path d="M55 104L30 47h28" fill="none" stroke="#40596a" stroke-width="9" stroke-linecap="round"/><circle cx="76" cy="137" r="14" fill="#344a59"/><circle cx="142" cy="137" r="14" fill="#344a59"/><path d="M90 82h35" stroke="#d7eedb" stroke-width="7"/></g></svg>', 'UTF8')),
    (2, 2, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#f9f0e3"/><stop offset="1" stop-color="#e4d5c0"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#c49a76" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path fill="#344c63" d="M25 55h119l22 14v23H25z"/><path fill="#e7b561" d="M25 55h116v36H25z"/><path fill="#526c7c" d="M159 67h32v11h-32zM77 91h33v50H77z"/><path fill="#263d53" d="M70 137h47v12H70z"/><circle cx="54" cy="74" r="8" fill="#fff1cc"/></g></svg>', 'UTF8')),
    (3, 3, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#e6f4f3"/><stop offset="1" stop-color="#c7e4e5"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#75aeb0" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path fill="#3a9f9c" d="M50 79q0-29 30-29h35q32 0 32 31v43H50z"/><path fill="#236f78" d="M61 120h85v15H61z"/><circle cx="72" cy="138" r="12" fill="#344c63"/><circle cx="132" cy="138" r="12" fill="#344c63"/><path d="M113 54V31h48v35" fill="none" stroke="#40586d" stroke-width="9"/><path d="M160 65q27 19 2 42l-7 6" fill="none" stroke="#40586d" stroke-width="7"/></g></svg>', 'UTF8')),
    (4, 4, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#f5ecf1"/><stop offset="1" stop-color="#e5d8e8"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#aa93b1" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path fill="#dc855b" d="M14 132L99 26l88 106z"/><path fill="#f1ba78" d="M99 26v106H14z"/><path fill="#a74d48" d="M79 132l20-54 22 54z"/><path d="M10 134h180" stroke="#657b64" stroke-width="7" stroke-linecap="round"/><path d="M99 26v106" stroke="#fff1cf" stroke-width="4"/></g></svg>', 'UTF8'));

-- Tool
INSERT INTO tool (id, owner_id, category_id, name, description, status, created_at, updated_at) VALUES
    (1, 1, 2, 'Akutrell', '18 V akutrell, kaks akut ja laadija. Sobib puurimiseks ja kruvide keeramiseks.', 'A', '2026-09-18 10:15:00', '2026-09-18 10:15:00'),
    (2, 1, 2, 'Redel', 'Alumiiniumist kokkupandav redel, töökõrgus 3 meetrit.', 'U', '2026-09-18 10:20:00', '2026-09-18 11:00:00'),
    (3, 3, 3, 'Tolmuimeja', '1200 W tolmuimeja koos põrandaotsiku ja praootsikuga. Sobib kodu koristamiseks.', 'A', '2026-09-18 10:40:00', '2026-09-18 10:40:00'),
    (4, 3, 1, 'Hekikäärid', 'Käsitsi kasutatavad hekikäärid, tera pikkus 25 cm. Sobivad heki pügamiseks.', 'A', '2026-09-18 10:45:00', '2026-09-18 10:45:00'),
    (5, 1, 1, 'Muruniiduk', 'Elektriline muruniiduk, lõikelaius 32 cm ja 30-liitrine kogumiskast.', 'A', '2026-09-18 11:00:00', '2026-09-18 11:00:00'),
    (6, 1, 3, 'Survepesur', '130-baarine survepesur koos 6-meetrise voolikuga. Sobib terrassi ja aia puhastamiseks.', 'A', '2026-09-18 11:15:00', '2026-09-18 11:15:00'),
    (7, 1, 4, 'Matkatelk', 'Kahekohaline veekindel matkatelk koos vaiade ja kandekotiga.', 'A', '2026-09-18 11:30:00', '2026-09-18 11:30:00'),
    (8, 3, 4, 'Projektor', 'Full HD projektor HDMI-sisendiga. Kaasas toitejuhe ja kaugjuhtimispult.', 'A', '2026-09-18 11:45:00', '2026-09-18 11:45:00'),
    (9, 4, 1, 'Lehepuhur', 'Akutoitel lehepuhur koos kahe akuga. Sobib lehtede ja kerge prahi koristamiseks.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (10, 4, 2, 'Ketassaag', '1600 W ketassaag koos juhiku ja saekettaga. Sobib puidu lõikamiseks.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (11, 5, 1, 'Labidas', 'Tugev terasest aialabidas pika puidust varrega.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (12, 5, 3, 'Aknapesur', 'Akuga aknapesur koos pihusti ja mikrokiudlapiga.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (13, 6, 2, 'Nurklihvija', '125 mm nurklihvija koos kaitsekatte ja lõikeketastega.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (14, 6, 4, 'Kokkupandav laud', 'Kerge kokkupandav laud koduõue või ürituse tarbeks.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (15, 7, 1, 'Reha', 'Lai metallrehaga aiatööriist lehtede ja mulla tasandamiseks.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (16, 7, 3, 'Aurupesur', 'Mitme otsikuga aurupesur köögi ja vannitoa puhastamiseks.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (17, 8, 2, 'Värvirull', 'Pikendatava varrega värvirull ja värvialus seinte värvimiseks.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (18, 8, 4, 'Pikendusjuhe', '20 meetri pikkune välistingimustesse sobiv kaablirull.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (19, 9, 1, 'Aiakäru', 'Üherattaline mahukas aiakäru mulla ja okste vedamiseks.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (20, 9, 3, 'Tekstiilipesur', 'Pihustav tekstiilipesur diivanite ja vaipade puhastamiseks.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (21, 10, 2, 'Vasarpuur', 'SDS-plus vasarpuur koos puuride ja kandekohvriga.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (22, 10, 4, 'Matkapliit', 'Kompaktne kahe põletiga matkapliit koos kandekotiga.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (23, 11, 1, 'Aiavoolik', '25 meetri pikkune aiavoolik koos ühenduste ja pihustusotsikuga.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (24, 11, 2, 'Töökoja lamp', 'Laetav LED-töölamp reguleeritava jalaga.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (25, 12, 3, 'Põrandapesur', 'Elektriline põrandapesur kõvade põrandapindade puhastamiseks.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00'),
    (26, 12, 4, 'Generaator', 'Vaikne invertergeneraator väikeste elektriseadmete toitmiseks.', 'A', '2026-09-18 12:15:00', '2026-09-18 12:15:00');

-- Tool Image
INSERT INTO tool_image (id, tool_id, image_data, is_main) VALUES
    (1, 1, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#f9f0e3"/><stop offset="1" stop-color="#e4d5c0"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#c49a76" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path fill="#344c63" d="M25 55h119l22 14v23H25z"/><path fill="#e7b561" d="M25 55h116v36H25z"/><path fill="#526c7c" d="M159 67h32v11h-32zM77 91h33v50H77z"/><path fill="#263d53" d="M70 137h47v12H70z"/><circle cx="54" cy="74" r="8" fill="#fff1cc"/></g></svg>', 'UTF8'), true),
    (2, 2, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#f9f0e3"/><stop offset="1" stop-color="#e4d5c0"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#c49a76" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path d="M58 12L36 149M142 12l22 137" fill="none" stroke="#516b7b" stroke-width="12" stroke-linecap="round"/><path d="M56 36h89M51 63h98M47 90h106M42 117h116M38 143h125" fill="none" stroke="#d49a53" stroke-width="10" stroke-linecap="round"/></g></svg>', 'UTF8'), true),
    (3, 3, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#e6f4f3"/><stop offset="1" stop-color="#c7e4e5"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#75aeb0" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path fill="#3a9f9c" d="M50 79q0-29 30-29h35q32 0 32 31v43H50z"/><path fill="#236f78" d="M61 120h85v15H61z"/><circle cx="72" cy="138" r="12" fill="#344c63"/><circle cx="132" cy="138" r="12" fill="#344c63"/><path d="M113 54V31h48v35" fill="none" stroke="#40586d" stroke-width="9"/><path d="M160 65q27 19 2 42l-7 6" fill="none" stroke="#40586d" stroke-width="7"/></g></svg>', 'UTF8'), true),
    (4, 4, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#eaf5e8"/><stop offset="1" stop-color="#cce7d6"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#85aa85" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path d="M43 27l108 90M155 25L49 117" fill="none" stroke="#879da0" stroke-width="12" stroke-linecap="round"/><path d="M122 108l38 39M79 108l-38 39" fill="none" stroke="#c7774a" stroke-width="14" stroke-linecap="round"/><circle cx="100" cy="78" r="11" fill="#394c58"/></g></svg>', 'UTF8'), true),
    (5, 5, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#eaf5e8"/><stop offset="1" stop-color="#cce7d6"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#85aa85" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path d="M56 99h104l-15 34H66z" fill="#4b9b6b"/><path d="M85 98V59h53l18 40" fill="#62bd81"/><path d="M55 104L30 47h28" fill="none" stroke="#40596a" stroke-width="9" stroke-linecap="round"/><circle cx="76" cy="137" r="14" fill="#344a59"/><circle cx="142" cy="137" r="14" fill="#344a59"/><path d="M90 82h35" stroke="#d7eedb" stroke-width="7"/></g></svg>', 'UTF8'), true),
    (6, 6, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#e6f4f3"/><stop offset="1" stop-color="#c7e4e5"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#75aeb0" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><rect x="58" y="59" width="90" height="77" rx="15" fill="#399fa9"/><circle cx="96" cy="91" r="21" fill="#dcefed"/><circle cx="96" cy="91" r="10" fill="#577d8b"/><circle cx="75" cy="139" r="11" fill="#354c60"/><circle cx="134" cy="139" r="11" fill="#354c60"/><path d="M148 80q32-15 37 11l-17 20" fill="none" stroke="#3b5968" stroke-width="8"/><path d="M160 108l30-29" stroke="#344c63" stroke-width="7"/></g></svg>', 'UTF8'), true),
    (7, 7, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#f5ecf1"/><stop offset="1" stop-color="#e5d8e8"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#aa93b1" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path fill="#dc855b" d="M14 132L99 26l88 106z"/><path fill="#f1ba78" d="M99 26v106H14z"/><path fill="#a74d48" d="M79 132l20-54 22 54z"/><path d="M10 134h180" stroke="#657b64" stroke-width="7" stroke-linecap="round"/><path d="M99 26v106" stroke="#fff1cf" stroke-width="4"/></g></svg>', 'UTF8'), true),
    (8, 8, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#f5ecf1"/><stop offset="1" stop-color="#e5d8e8"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#aa93b1" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><rect x="26" y="63" width="150" height="75" rx="17" fill="#e6e9dc"/><rect x="30" y="72" width="139" height="55" rx="10" fill="#93a6aa"/><circle cx="78" cy="100" r="24" fill="#435d70"/><circle cx="78" cy="100" r="13" fill="#b6dce3"/><path d="M132 89h22M132 103h22" stroke="#f8f5e7" stroke-width="7" stroke-linecap="round"/><path d="M46 138v10m111-10v10" stroke="#485c65" stroke-width="9"/></g></svg>', 'UTF8'), true),
    (9, 9, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#eaf5e8"/><stop offset="1" stop-color="#cce7d6"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#85aa85" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path fill="#db9b58" d="M59 67h75l24 26h-98z"/><path fill="#4a6472" d="M148 86h41v15h-41z"/><path d="M82 67V49h30v18M75 94v35h39V94" fill="none" stroke="#4a6472" stroke-width="12"/><path d="M19 75l22-13M13 93l27-3M21 110l22 8" stroke="#8ab2ab" stroke-width="6" stroke-linecap="round"/></g></svg>', 'UTF8'), true),
    (10, 10, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#f9f0e3"/><stop offset="1" stop-color="#e4d5c0"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#c49a76" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path fill="#d49c5b" d="M27 75h94v44H27z"/><path fill="#526b78" d="M121 86h53v16h-53z"/><circle cx="105" cy="109" r="33" fill="#dce5df" stroke="#849ca3" stroke-width="8"/><circle cx="105" cy="109" r="7" fill="#6d8793"/><path d="M47 75V55h45v20" fill="none" stroke="#4c6272" stroke-width="10"/></g></svg>', 'UTF8'), true),
    (11, 11, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#eaf5e8"/><stop offset="1" stop-color="#cce7d6"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#85aa85" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path d="M100 20v91" stroke="#aa7851" stroke-width="13" stroke-linecap="round"/><path d="M76 21q0-18 24-18t24 18v9H76z" fill="none" stroke="#526d78" stroke-width="9"/><path d="M70 106h60v21q-4 27-30 32-27-6-30-32z" fill="#829ba0"/></g></svg>', 'UTF8'), true),
    (12, 12, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#e6f4f3"/><stop offset="1" stop-color="#c7e4e5"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#75aeb0" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path d="M65 35h75v49H65z" fill="#9dcbd1" stroke="#eaf7ee" stroke-width="7"/><path d="M38 78h115" stroke="#4e7383" stroke-width="13" stroke-linecap="round"/><path d="M96 85v56" stroke="#5c8991" stroke-width="13" stroke-linecap="round"/><path d="M88 141h18" stroke="#344e61" stroke-width="10" stroke-linecap="round"/><circle cx="53" cy="43" r="8" fill="#fff6d9"/></g></svg>', 'UTF8'), true),
    (13, 13, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#f9f0e3"/><stop offset="1" stop-color="#e4d5c0"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#c49a76" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path fill="#d48154" d="M31 76h102v31H31z"/><path d="M84 76V56h36" fill="none" stroke="#496273" stroke-width="10"/><circle cx="150" cy="102" r="30" fill="#d6e0dd" stroke="#728e99" stroke-width="8"/><circle cx="150" cy="102" r="7" fill="#435967"/><path d="M39 109l-16 22" stroke="#455e70" stroke-width="13" stroke-linecap="round"/></g></svg>', 'UTF8'), true),
    (14, 14, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#f5ecf1"/><stop offset="1" stop-color="#e5d8e8"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#aa93b1" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path fill="#c38e5d" d="M24 59h152v21H24z"/><path d="M49 80l-12 65M150 80l14 65M85 80v65m30-65v65" stroke="#657b7c" stroke-width="10" stroke-linecap="round"/><path d="M35 95h130" stroke="#93a1a0" stroke-width="5"/></g></svg>', 'UTF8'), true),
    (15, 15, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#eaf5e8"/><stop offset="1" stop-color="#cce7d6"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#85aa85" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path d="M102 14v127" stroke="#ae7d57" stroke-width="12" stroke-linecap="round"/><path d="M42 101h120" stroke="#506f78" stroke-width="12" stroke-linecap="round"/><path d="M47 107v35m21-35v35m21-35v35m21-35v35m21-35v35m21-35v35" stroke="#506f78" stroke-width="7" stroke-linecap="round"/></g></svg>', 'UTF8'), true),
    (16, 16, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#e6f4f3"/><stop offset="1" stop-color="#c7e4e5"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#75aeb0" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><rect x="46" y="63" width="104" height="69" rx="17" fill="#5ba9a8"/><path d="M70 64V46h55v18" fill="none" stroke="#4c6e7a" stroke-width="10"/><circle cx="71" cy="137" r="11" fill="#435c6a"/><circle cx="132" cy="137" r="11" fill="#435c6a"/><path d="M88 41q-11-11 0-24m26 24q-11-11 0-24m26 25q-10-11 1-24" fill="none" stroke="#a9dad7" stroke-width="6" stroke-linecap="round"/></g></svg>', 'UTF8'), true),
    (17, 17, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#f9f0e3"/><stop offset="1" stop-color="#e4d5c0"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#c49a76" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><rect x="35" y="31" width="106" height="40" rx="13" fill="#e6b76e"/><path d="M142 50h25v42h-67v51" fill="none" stroke="#637d88" stroke-width="10" stroke-linejoin="round"/><path d="M89 142h23" stroke="#40586b" stroke-width="17" stroke-linecap="round"/><path d="M37 74h101" stroke="#bd865b" stroke-width="5"/></g></svg>', 'UTF8'), true),
    (18, 18, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#f5ecf1"/><stop offset="1" stop-color="#e5d8e8"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#aa93b1" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><circle cx="103" cy="92" r="54" fill="#e0ad62"/><circle cx="103" cy="92" r="36" fill="none" stroke="#344e61" stroke-width="9"/><circle cx="103" cy="92" r="20" fill="#607782"/><path d="M55 48l-16-20m16 111l-16 19m99-5l17-25M50 92h54" stroke="#465e6f" stroke-width="10" stroke-linecap="round"/><path d="M151 86h30v14h-30" fill="#374d61"/></g></svg>', 'UTF8'), true),
    (19, 19, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#eaf5e8"/><stop offset="1" stop-color="#cce7d6"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#85aa85" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path fill="#d58d60" d="M34 57h133l-24 54H57z"/><path d="M35 57L18 42m139 70l20 29M62 111l-16 30" stroke="#536c70" stroke-width="9" stroke-linecap="round"/><circle cx="98" cy="133" r="17" fill="#354b60"/><circle cx="98" cy="133" r="7" fill="#9eada8"/></g></svg>', 'UTF8'), true),
    (20, 20, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#e6f4f3"/><stop offset="1" stop-color="#c7e4e5"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#75aeb0" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path fill="#427e91" d="M57 57h92v71H57z"/><path d="M68 53V37h70v16" fill="none" stroke="#3a5e70" stroke-width="10"/><path d="M82 83h44M82 99h44" stroke="#b2d8d2" stroke-width="8" stroke-linecap="round"/><circle cx="73" cy="133" r="12" fill="#354b60"/><circle cx="135" cy="133" r="12" fill="#354b60"/><path d="M149 85h22l13 27" fill="none" stroke="#506c78" stroke-width="8"/></g></svg>', 'UTF8'), true),
    (21, 21, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#f9f0e3"/><stop offset="1" stop-color="#e4d5c0"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#c49a76" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path fill="#d78b58" d="M25 55h123v42H25z"/><path fill="#4c6675" d="M145 65h43v13h-43zM75 97h36v45H75z"/><path d="M56 53V35h61v18" fill="none" stroke="#466071" stroke-width="10"/><path d="M69 143h49" stroke="#33495d" stroke-width="13" stroke-linecap="round"/><path d="M17 98V55" stroke="#697f85" stroke-width="9"/></g></svg>', 'UTF8'), true),
    (22, 22, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#f5ecf1"/><stop offset="1" stop-color="#e5d8e8"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#aa93b1" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><rect x="28" y="83" width="146" height="52" rx="9" fill="#5f7a79"/><rect x="41" y="89" width="122" height="28" rx="6" fill="#a7b5a3"/><circle cx="71" cy="100" r="13" fill="#455c65"/><circle cx="130" cy="100" r="13" fill="#455c65"/><path d="M65 73q-10-11 4-23 12 12 5 23m50 0q-9-11 5-23 11 12 4 23" fill="#e9a85e"/><path d="M38 135v12m126-12v12" stroke="#445b65" stroke-width="8"/></g></svg>', 'UTF8'), true),
    (23, 23, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#eaf5e8"/><stop offset="1" stop-color="#cce7d6"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#85aa85" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><circle cx="99" cy="96" r="53" fill="none" stroke="#579c80" stroke-width="20"/><circle cx="99" cy="96" r="27" fill="none" stroke="#417e70" stroke-width="12"/><path d="M142 130l32 15 11-13" fill="none" stroke="#549a7b" stroke-width="15" stroke-linecap="round"/><path d="M179 127l13-8" stroke="#d8ae6d" stroke-width="12" stroke-linecap="round"/></g></svg>', 'UTF8'), true),
    (24, 24, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#f9f0e3"/><stop offset="1" stop-color="#e4d5c0"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#c49a76" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path d="M100 37v107m-38 0h76" stroke="#506875" stroke-width="11" stroke-linecap="round"/><path d="M53 26h93l-17 50H70z" fill="#e8b96d"/><path d="M71 77h58l-17 20H88z" fill="#fff0bf"/><path d="M68 108h64" stroke="#78959a" stroke-width="7"/></g></svg>', 'UTF8'), true),
    (25, 25, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#e6f4f3"/><stop offset="1" stop-color="#c7e4e5"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#75aeb0" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><path fill="#4b9c9a" d="M53 106h104v29H53z"/><path d="M106 107V30" stroke="#4e6979" stroke-width="12" stroke-linecap="round"/><path d="M88 30h37" stroke="#4e6979" stroke-width="11" stroke-linecap="round"/><path d="M63 138h84" stroke="#335366" stroke-width="9" stroke-linecap="round"/><path d="M51 146h109" stroke="#a4d5d2" stroke-width="7" stroke-linecap="round"/></g></svg>', 'UTF8'), true),
    (26, 26, convert_to('<svg xmlns="http://www.w3.org/2000/svg" width="320" height="240" viewBox="0 0 320 240"><defs><linearGradient id="b" x2="1" y2="1"><stop stop-color="#f5ecf1"/><stop offset="1" stop-color="#e5d8e8"/></linearGradient></defs><rect width="320" height="240" rx="26" fill="url(#b)"/><circle cx="265" cy="49" r="47" fill="#ffffff" opacity=".38"/><circle cx="44" cy="205" r="64" fill="#aa93b1" opacity=".12"/><ellipse cx="160" cy="199" rx="107" ry="12" fill="#34495e" opacity=".12"/><g transform="translate(60 24)"><rect x="33" y="57" width="141" height="78" rx="14" fill="#d3925f"/><path d="M44 58V43h120v15M44 133v14m119-14v14" fill="none" stroke="#485f6d" stroke-width="9"/><circle cx="84" cy="96" r="23" fill="#495f6a"/><circle cx="84" cy="96" r="12" fill="#b7c7c2"/><path d="M129 79h27m-27 14h27m-27 14h27" stroke="#f4d5a6" stroke-width="6" stroke-linecap="round"/></g></svg>', 'UTF8'), true);

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

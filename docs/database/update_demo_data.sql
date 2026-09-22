-- Run with psql -X -v ON_ERROR_STOP=1 -d database -f update_demo_data.sql
-- Existing database only. No schema changes, deletes, or dump restoration.
-- Sources checked 2026-09-22:
-- Tallinn (8 administrative districts):
-- https://www.tallinn.ee/et/linnaosade-teenindussaalid
-- Tartu (18 urban districts; excludes Maapiirkond/former Tahtvere parish):
-- https://www.tartu.ee/et/linnaosad
-- Parnu (7 named city districts, not municipal osavallad or smaller neighbourhoods):
-- Parnu City Council, 17.09.1998 no. 27, section 1.1:
-- https://www.riigiteataja.ee/akt/88913?tegevus=salvesta-link
-- Tammiste here is the CITY district, not the neighbouring village.
-- Images contain UTF-8 TEXT bytes, not a renderable image file.
-- Any error aborts the transaction. Sequence advances are not rolled back by
-- PostgreSQL; gaps are harmless, and this script never decreases a sequence.

BEGIN;
SET LOCAL search_path = public, pg_catalog;
SET LOCAL lock_timeout = '10s';
SET LOCAL statement_timeout = '60s';
LOCK TABLE public.app_user, public.role, public.profile, public.location,
    public.city, public.district, public.category, public.tool,
    public.tool_image, public.booking IN SHARE ROW EXCLUSIVE MODE;

DO $demo$
DECLARE
    districts jsonb := '{
      "Tallinn": ["Haabersti","Kesklinn","Kristiine","Lasnamäe","Mustamäe","Nõmme","Pirita","Põhja-Tallinn"],
      "Tartu": ["Annelinn","Ihaste","Jaamamõisa","Karlova","Kesklinn","Kvissentali","Maarjamõisa","Raadi-Kruusamäe","Ropka","Ropka tööstusrajoon","Ränilinn","Supilinn","Tammelinn","Tähtvere","Vaksali","Variku","Veeriku","Ülejõe"],
      "Pärnu": ["Vana-Pärnu","Ülejõe","Rääma","Tammiste","Kesklinn","Raeküla","Lodja"]
    }';
    categories text[] := ARRAY['Aiatööd','Ehitustööd','Koristamine','Muud asjad'];
    schema_query text := $catalog$
      SELECT jsonb_build_object(
        'relations', (SELECT jsonb_agg(jsonb_build_array(c.oid,c.relname,c.relkind,c.relpersistence,c.relrowsecurity,c.relforcerowsecurity,c.reloptions) ORDER BY c.oid)
          FROM pg_class c WHERE c.relnamespace='public'::regnamespace),
        'columns', (SELECT jsonb_agg(to_jsonb(a) ORDER BY a.attrelid,a.attnum)
          FROM pg_attribute a JOIN pg_class c ON c.oid=a.attrelid WHERE c.relnamespace='public'::regnamespace),
        'defaults', (SELECT jsonb_agg(to_jsonb(d) ORDER BY d.oid)
          FROM pg_attrdef d JOIN pg_class c ON c.oid=d.adrelid WHERE c.relnamespace='public'::regnamespace),
        'constraints', (SELECT jsonb_agg(to_jsonb(c) ORDER BY c.oid) FROM pg_constraint c WHERE c.connamespace='public'::regnamespace),
        'indexes', (SELECT jsonb_agg(to_jsonb(i) ORDER BY i.indexrelid)
          FROM pg_index i JOIN pg_class c ON c.oid=i.indrelid WHERE c.relnamespace='public'::regnamespace),
        'sequences', (SELECT jsonb_agg(to_jsonb(s) ORDER BY s.seqrelid)
          FROM pg_sequence s JOIN pg_class c ON c.oid=s.seqrelid WHERE c.relnamespace='public'::regnamespace),
        'triggers', (SELECT jsonb_agg(to_jsonb(t) ORDER BY t.oid)
          FROM pg_trigger t JOIN pg_class c ON c.oid=t.tgrelid WHERE c.relnamespace='public'::regnamespace)
      )
    $catalog$;
    schema_before jsonb;
    schema_after jsonb;
    protected_before jsonb;
    protected_after jsonb;
    protected_query text := $protected$
      SELECT jsonb_build_object(
        'users',(SELECT jsonb_agg(to_jsonb(x) ORDER BY id) FROM app_user x),
        'roles',(SELECT jsonb_agg(to_jsonb(x) ORDER BY id) FROM role x),
        'profiles',(SELECT jsonb_agg(to_jsonb(x) ORDER BY id) FROM profile x),
        'locations',(SELECT jsonb_agg(to_jsonb(x) ORDER BY id) FROM location x),
        'booking2',(SELECT to_jsonb(x) FROM booking x WHERE id=2),
        'cities',(SELECT jsonb_agg(to_jsonb(x) ORDER BY id) FROM city x WHERE id=1),
        'districts',(SELECT jsonb_agg(to_jsonb(x) ORDER BY id) FROM district x WHERE id IN (1,2))
      )
    $protected$;
    old_ids jsonb := '{}';
    ids jsonb;
    r record;
    item record;
    city_key integer;
    seq_name text;
    seq_last bigint;
    seq_called boolean;
    max_id bigint;
    bad boolean;
    detail text;
    join_condition text;
    nonnull_condition text;
BEGIN
    EXECUTE schema_query INTO schema_before;
    EXECUTE protected_query INTO protected_before;

    -- Fail before writing if the live data no longer matches this seed's scope.
    IF (SELECT array_agg(id ORDER BY id) FROM app_user) IS DISTINCT FROM ARRAY[1,3] THEN
        RAISE EXCEPTION 'Expected exactly app_user IDs 1 and 3; no users will be added or removed';
    END IF;
    IF NOT EXISTS (SELECT FROM city WHERE id=1 AND city_name='Tallinn')
       OR NOT EXISTS (SELECT FROM district WHERE id=1 AND city_id=1 AND district_name='Kristiine')
       OR NOT EXISTS (SELECT FROM district WHERE id=2 AND city_id=1 AND district_name='Mustamäe') THEN
        RAISE EXCEPTION 'Tallinn / Kristiine / Mustamäe IDs differ from the inspected database';
    END IF;
    IF (SELECT count(*) FROM tool WHERE id IN (1,2,3,4)) <> 4
       OR (SELECT count(*) FROM tool) > 8
       OR NOT EXISTS (SELECT FROM tool WHERE id=2 AND name='Redel' AND owner_id=1) THEN
        RAISE EXCEPTION 'Expected original tool IDs 1..4 (Redel ID 2, owner 1) and at most 8 tools';
    END IF;
    IF EXISTS (SELECT FROM category WHERE id NOT BETWEEN 1 AND 4) THEN
        RAISE EXCEPTION 'Extra category IDs conflict with the required four categories';
    END IF;
    IF NOT EXISTS (SELECT FROM booking WHERE id=2 AND tool_id=2 AND renter_id=3
                   AND status='C' AND start_date<end_date AND google_event_id IS NULL)
       OR (SELECT count(*) FROM booking)>3
       OR EXISTS (SELECT FROM booking WHERE id<>2 AND status NOT IN ('P','R'))
       OR EXISTS (SELECT FROM booking GROUP BY status HAVING count(*)>1) THEN
        RAISE EXCEPTION 'Bookings conflict with preservation of booking 2 and exactly one P/C/R';
    END IF;
    SELECT string_agg(format('tool_id=%s image_ids=%s',tool_id,image_ids),'; ')
      INTO detail FROM (SELECT tool_id,array_agg(id ORDER BY id) image_ids
                       FROM tool_image GROUP BY tool_id HAVING count(*)>1) x;
    IF detail IS NOT NULL THEN
        RAISE EXCEPTION 'Identical placeholders would violate (tool_id,image_data): %',detail;
    END IF;
    IF EXISTS (SELECT FROM district d JOIN city c ON c.id=d.city_id
               WHERE districts ? c.city_name
                 AND NOT (districts->c.city_name ? d.district_name)) THEN
        RAISE EXCEPTION 'Existing district outside the chosen official city district lists';
    END IF;
    IF EXISTS (SELECT FROM pg_trigger t JOIN pg_class c ON c.oid=t.tgrelid
               WHERE c.relnamespace='public'::regnamespace AND NOT t.tgisinternal) THEN
        RAISE EXCEPTION 'Unexpected user trigger: review its effects before seeding';
    END IF;
    IF current_setting('session_replication_role') <> 'origin'
       OR EXISTS (SELECT FROM pg_constraint WHERE connamespace='public'::regnamespace AND NOT convalidated)
       OR EXISTS (SELECT FROM pg_trigger t JOIN pg_class c ON c.oid=t.tgrelid
                  WHERE c.relnamespace='public'::regnamespace AND t.tgisinternal AND t.tgenabled NOT IN ('O','A')) THEN
        RAISE EXCEPTION 'Integrity constraints must be validated and enabled';
    END IF;
    -- Evaluate actual booking CHECK expressions on an R candidate, without DML.
    FOR r IN SELECT conname,pg_get_expr(conbin,conrelid) expr FROM pg_constraint
             WHERE conrelid='booking'::regclass AND contype='c' LOOP
        EXECUTE format('SELECT (%s) IS FALSE FROM jsonb_populate_record(NULL::public.booking,
                       (SELECT to_jsonb(b) FROM booking b WHERE id=2) || ''{"status":"R"}''::jsonb)',r.expr) INTO bad;
        IF bad THEN RAISE EXCEPTION 'Booking status R rejected by actual CHECK %',r.conname; END IF;
    END LOOP;

    FOR r IN SELECT unnest(ARRAY['app_user','role','profile','location','city','district','category','tool','tool_image','booking']) AS name LOOP
        EXECUTE format('SELECT jsonb_agg(id ORDER BY id) FROM public.%I',r.name) INTO ids;
        old_ids := old_ids || jsonb_build_object(r.name,coalesce(ids,'[]'::jsonb));
    END LOOP;
    -- Default-generated IDs must be safe. No sequence is decreased or redefined.
    FOR r IN SELECT unnest(ARRAY['city','district','tool','tool_image','booking']) AS name LOOP
        seq_name := pg_get_serial_sequence('public.'||r.name,'id');
        IF seq_name IS NULL THEN RAISE EXCEPTION 'Missing ID sequence for %',r.name; END IF;
        EXECUTE format('SELECT last_value,is_called FROM %s',seq_name) INTO seq_last,seq_called;
        EXECUTE format('SELECT coalesce(max(id),0) FROM public.%I',r.name) INTO max_id;
        IF seq_last + (CASE WHEN seq_called THEN 1 ELSE 0 END) <= max_id THEN
            RAISE EXCEPTION 'Sequence % is behind existing IDs; review before seeding',seq_name;
        END IF;
    END LOOP;

    FOR r IN SELECT key,value FROM jsonb_each(districts) ORDER BY key LOOP
        INSERT INTO city(city_name) SELECT r.key WHERE NOT EXISTS (SELECT FROM city WHERE city_name=r.key);
        SELECT id INTO STRICT city_key FROM city WHERE city_name=r.key;
        INSERT INTO district(city_id,district_name)
          SELECT city_key,v FROM jsonb_array_elements_text(r.value) AS x(v)
          WHERE NOT EXISTS (SELECT FROM district d WHERE d.city_id=city_key AND d.district_name=v)
          ORDER BY v;
    END LOOP;

    -- Temporary names allow even permuted category names under the UNIQUE constraint.
    IF EXISTS (SELECT FROM category WHERE category_name LIKE '__demo_category_%') THEN
        RAISE EXCEPTION 'Reserved temporary category name already exists';
    END IF;
    UPDATE category SET category_name='__demo_category_'||id
      WHERE category_name IS DISTINCT FROM categories[id];
    FOR city_key IN 1..4 LOOP
        INSERT INTO category(id,category_name) VALUES(city_key,categories[city_key])
          ON CONFLICT(id) DO UPDATE SET category_name=EXCLUDED.category_name
          WHERE category.category_name IS DISTINCT FROM EXCLUDED.category_name;
    END LOOP;

    UPDATE tool t SET category_id=v.category_id,name=v.name,description=v.description,
      updated_at=CURRENT_TIMESTAMP
    FROM (VALUES
      (1,2,'Akutrell','18 V akutrell, kaks akut ja laadija. Sobib puurimiseks ja kruvide keeramiseks.'),
      (3,3,'Tolmuimeja','1200 W tolmuimeja koos põrandaotsiku ja praootsikuga. Sobib kodu koristamiseks.'),
      (4,1,'Hekikäärid','Käsitsi kasutatavad hekikäärid, tera pikkus 25 cm. Sobivad heki pügamiseks.')
    ) v(id,category_id,name,description)
    WHERE t.id=v.id AND (t.category_id,t.name,t.description) IS DISTINCT FROM (v.category_id,v.name,v.description);
    -- Redel ID 2 (including owner, status and description) remains unchanged.
    FOR item IN SELECT * FROM (VALUES
      (1,1,'Muruniiduk','Elektriline muruniiduk, lõikelaius 32 cm ja 30-liitrine kogumiskast.'),
      (1,3,'Survepesur','130-baarine survepesur koos 6-meetrise voolikuga. Sobib terrassi ja aia puhastamiseks.'),
      (1,4,'Matkatelk','Kahekohaline veekindel matkatelk koos vaiade ja kandekotiga.'),
      (3,4,'Projektor','Full HD projektor HDMI-sisendiga. Kaasas toitejuhe ja kaugjuhtimispult.')
    ) v(owner_id,category_id,name,description) LOOP
        IF (SELECT count(*) FROM tool WHERE name=item.name)>1 THEN
            RAISE EXCEPTION 'Ambiguous demo tool name: %',item.name;
        END IF;
        INSERT INTO tool(owner_id,category_id,name,description,status)
          SELECT item.owner_id,item.category_id,item.name,item.description,'A'
          WHERE NOT EXISTS (SELECT FROM tool WHERE name=item.name);
    END LOOP;

    UPDATE tool_image SET image_data=convert_to('<picture placeholder>','UTF8'),is_main=true
      WHERE image_data IS DISTINCT FROM convert_to('<picture placeholder>','UTF8') OR NOT is_main;
    INSERT INTO tool_image(tool_id,image_data,is_main)
      SELECT t.id,convert_to('<picture placeholder>','UTF8'),true FROM tool t
      WHERE NOT EXISTS (SELECT FROM tool_image i WHERE i.tool_id=t.id) ORDER BY t.id;

    INSERT INTO booking(tool_id,renter_id,start_date,end_date,status,owner_message,google_event_id)
      SELECT 1,3,DATE '2026-10-02',DATE '2026-10-04','P',NULL,NULL
      WHERE NOT EXISTS (SELECT FROM booking WHERE status='P');
    INSERT INTO booking(tool_id,renter_id,start_date,end_date,status,owner_message,google_event_id)
      SELECT 4,1,DATE '2026-10-05',DATE '2026-10-07','R','Soovitud kuupäevadel ei saa tööriista välja laenata.',NULL
      WHERE NOT EXISTS (SELECT FROM booking WHERE status='R');

    -- Final assertions, still before COMMIT.
    IF (SELECT count(*) FROM app_user)<>2 OR (SELECT count(*) FROM category)<>4
       OR EXISTS (SELECT FROM category WHERE category_name IS DISTINCT FROM categories[id])
       OR (SELECT count(*) FROM tool)<>8
       OR EXISTS (SELECT FROM category c LEFT JOIN tool t ON t.category_id=c.id GROUP BY c.id HAVING count(t.id)<>2)
       OR (SELECT count(DISTINCT owner_id) FROM tool)<>2 THEN
        RAISE EXCEPTION 'User/category/tool counts or category mapping failed';
    END IF;
    FOR r IN SELECT key,value FROM jsonb_each(districts) LOOP
        IF NOT EXISTS (SELECT FROM city WHERE city_name=r.key)
           OR (SELECT count(*) FROM district d JOIN city c ON c.id=d.city_id WHERE c.city_name=r.key)<>jsonb_array_length(r.value)
           OR EXISTS (SELECT FROM jsonb_array_elements_text(r.value) x(v) WHERE NOT EXISTS (
               SELECT FROM district d JOIN city c ON c.id=d.city_id WHERE c.city_name=r.key AND d.district_name=v)) THEN
            RAISE EXCEPTION 'Incomplete district list for %',r.key;
        END IF;
    END LOOP;
    IF EXISTS (SELECT FROM district GROUP BY city_id,district_name HAVING count(*)>1)
       OR (SELECT count(*) FROM tool_image)<>8
       OR EXISTS (SELECT FROM tool_image WHERE image_data<>convert_to('<picture placeholder>','UTF8') OR NOT is_main)
       OR EXISTS (SELECT FROM tool t LEFT JOIN tool_image i ON i.tool_id=t.id GROUP BY t.id HAVING count(i.id)<>1) THEN
        RAISE EXCEPTION 'District duplicates or placeholder/main image validation failed';
    END IF;
    IF (SELECT array_agg(status::text ORDER BY status) FROM booking) IS DISTINCT FROM ARRAY['C','P','R']
       OR (SELECT count(DISTINCT tool_id) FROM booking)<>3
       OR EXISTS (SELECT FROM booking b JOIN tool t ON t.id=b.tool_id WHERE b.renter_id=t.owner_id)
       OR EXISTS (SELECT FROM booking WHERE start_date>=end_date OR google_event_id IS NOT NULL) THEN
        RAISE EXCEPTION 'Booking status/tool/renter/date/calendar validation failed';
    END IF;
    -- Verify NOT NULL (also reject empty required text), every CHECK and every FK
    -- against actual catalog definitions, including untouched records.
    FOR r IN SELECT c.oid,c.relname,a.attname,a.atttypid FROM pg_class c
             JOIN pg_attribute a ON a.attrelid=c.oid
             WHERE c.relnamespace='public'::regnamespace AND c.relkind='r'
               AND a.attnum>0 AND NOT a.attisdropped AND a.attnotnull LOOP
        EXECUTE format('SELECT EXISTS(SELECT FROM public.%I WHERE %I IS NULL%s)',r.relname,r.attname,
          CASE WHEN r.atttypid IN ('text'::regtype,'varchar'::regtype,'bpchar'::regtype)
            THEN format(' OR btrim(%I::text)=''''',r.attname) ELSE '' END) INTO bad;
        IF bad THEN RAISE EXCEPTION 'Required field %.% is missing/empty',r.relname,r.attname; END IF;
    END LOOP;
    FOR r IN SELECT conname,conrelid,pg_get_expr(conbin,conrelid) expr FROM pg_constraint
             WHERE connamespace='public'::regnamespace AND contype='c' LOOP
        EXECUTE format('SELECT EXISTS(SELECT FROM %s WHERE (%s) IS FALSE)',r.conrelid::regclass,r.expr) INTO bad;
        IF bad THEN RAISE EXCEPTION 'CHECK validation failed: %',r.conname; END IF;
    END LOOP;
    FOR r IN SELECT * FROM pg_constraint WHERE connamespace='public'::regnamespace AND contype='f' LOOP
        SELECT string_agg(format('child.%I=parent.%I',a.attname,b.attname),' AND ' ORDER BY k.n),
               string_agg(format('child.%I IS NOT NULL',a.attname),' AND ' ORDER BY k.n)
          INTO join_condition,nonnull_condition
          FROM unnest(r.conkey,r.confkey) WITH ORDINALITY k(child_col,parent_col,n)
          JOIN pg_attribute a ON a.attrelid=r.conrelid AND a.attnum=k.child_col
          JOIN pg_attribute b ON b.attrelid=r.confrelid AND b.attnum=k.parent_col;
        EXECUTE format('SELECT EXISTS(SELECT FROM %s child WHERE %s AND NOT EXISTS(SELECT FROM %s parent WHERE %s))',
                       r.conrelid::regclass,nonnull_condition,r.confrelid::regclass,join_condition) INTO bad;
        IF bad THEN RAISE EXCEPTION 'Foreign key validation failed: %',r.conname; END IF;
    END LOOP;
    FOR r IN SELECT key,value FROM jsonb_each(old_ids) LOOP
        EXECUTE format('SELECT coalesce(jsonb_agg(id ORDER BY id),''[]''::jsonb) FROM public.%I',r.key) INTO ids;
        IF NOT ids @> r.value THEN RAISE EXCEPTION 'Existing IDs were lost in %',r.key; END IF;
    END LOOP;
    EXECUTE protected_query INTO protected_after;
    IF protected_before IS DISTINCT FROM protected_after THEN
        RAISE EXCEPTION 'Protected users/roles/profiles/locations/booking/district IDs changed';
    END IF;
    EXECUTE schema_query INTO schema_after;
    IF schema_before IS DISTINCT FROM schema_after THEN
        RAISE EXCEPTION 'Schema, constraints, indexes, triggers or sequence definitions changed';
    END IF;

    -- Category 4 is the only explicit new ID. Advance only if actually needed.
    seq_name := pg_get_serial_sequence('public.category','id');
    EXECUTE format('SELECT last_value,is_called FROM %s',seq_name) INTO seq_last,seq_called;
    SELECT max(id) INTO max_id FROM category;
    IF seq_last<max_id OR (seq_last=max_id AND NOT seq_called) THEN
        PERFORM setval(seq_name::regclass,greatest(seq_last,max_id),true);
    END IF;
    RAISE NOTICE 'Verified: 2 users; districts Tallinn=8 Tartu=18 Pärnu=7; 4 categories; 8 tools/images; bookings C/P/R; all IDs, FKs and schema preserved';
END
$demo$;
SET CONSTRAINTS ALL IMMEDIATE;
COMMIT;

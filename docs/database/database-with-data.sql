-- Impordi kogu fail tühja PostgreSQL 17+ andmebaasi database.
-- IntelliJ juhend: IMPORT_DATABASE.md
BEGIN;

--
-- PostgreSQL database dump
--


-- Dumped from database version 17.11
-- Dumped by pg_dump version 17.11

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: app_user; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.app_user (
    id integer NOT NULL,
    first_name character varying(100) NOT NULL,
    last_name character varying(100) NOT NULL,
    google_sub character varying(255) NOT NULL,
    role_id integer NOT NULL,
    status character(1) DEFAULT 'A'::bpchar NOT NULL,
    CONSTRAINT app_user_status_check CHECK ((status = ANY (ARRAY['A'::bpchar, 'B'::bpchar])))
);


--
-- Name: app_user_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.app_user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: app_user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.app_user_id_seq OWNED BY public.app_user.id;


--
-- Name: booking; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.booking (
    id integer NOT NULL,
    tool_id integer NOT NULL,
    renter_id integer NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    status character(1) DEFAULT 'P'::bpchar NOT NULL,
    owner_message character varying(500),
    google_event_id character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT booking_period_check CHECK ((start_date < end_date)),
    CONSTRAINT booking_status_check CHECK ((status = ANY (ARRAY['P'::bpchar, 'C'::bpchar])))
);


--
-- Name: booking_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.booking_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: booking_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.booking_id_seq OWNED BY public.booking.id;


--
-- Name: category; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.category (
    id integer NOT NULL,
    category_name character varying(100) NOT NULL
);


--
-- Name: category_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.category_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: category_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.category_id_seq OWNED BY public.category.id;


--
-- Name: city; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.city (
    id integer NOT NULL,
    city_name character varying(100) NOT NULL
);


--
-- Name: city_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.city_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: city_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.city_id_seq OWNED BY public.city.id;


--
-- Name: district; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.district (
    id integer NOT NULL,
    city_id integer NOT NULL,
    district_name character varying(100) NOT NULL
);


--
-- Name: district_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.district_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: district_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.district_id_seq OWNED BY public.district.id;


--
-- Name: location; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.location (
    id integer NOT NULL,
    district_id integer NOT NULL,
    street_name character varying(150) NOT NULL,
    house_number character varying(20) NOT NULL,
    apartment_number character varying(20),
    lng numeric(10,7),
    lat numeric(10,7)
);


--
-- Name: location_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.location_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: location_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.location_id_seq OWNED BY public.location.id;


--
-- Name: profile; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.profile (
    id integer NOT NULL,
    location_id integer NOT NULL,
    user_id integer NOT NULL,
    email character varying(254) NOT NULL,
    phone character varying(32) NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


--
-- Name: profile_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.profile_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: profile_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.profile_id_seq OWNED BY public.profile.id;


--
-- Name: role; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.role (
    id integer NOT NULL,
    role_name character varying(20) NOT NULL
);


--
-- Name: role_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.role_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.role_id_seq OWNED BY public.role.id;


--
-- Name: tool; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tool (
    id integer NOT NULL,
    owner_id integer NOT NULL,
    category_id integer NOT NULL,
    name character varying(150) NOT NULL,
    description character varying(2000),
    status character(1) DEFAULT 'A'::bpchar NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT tool_status_check CHECK ((status = ANY (ARRAY['A'::bpchar, 'U'::bpchar])))
);


--
-- Name: tool_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tool_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tool_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tool_id_seq OWNED BY public.tool.id;


--
-- Name: tool_image; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.tool_image (
    id integer NOT NULL,
    tool_id integer NOT NULL,
    image_data bytea NOT NULL,
    is_main boolean DEFAULT false NOT NULL
);


--
-- Name: tool_image_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.tool_image_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: tool_image_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.tool_image_id_seq OWNED BY public.tool_image.id;


--
-- Name: app_user id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user ALTER COLUMN id SET DEFAULT nextval('public.app_user_id_seq'::regclass);


--
-- Name: booking id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking ALTER COLUMN id SET DEFAULT nextval('public.booking_id_seq'::regclass);


--
-- Name: category id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.category ALTER COLUMN id SET DEFAULT nextval('public.category_id_seq'::regclass);


--
-- Name: city id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.city ALTER COLUMN id SET DEFAULT nextval('public.city_id_seq'::regclass);


--
-- Name: district id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.district ALTER COLUMN id SET DEFAULT nextval('public.district_id_seq'::regclass);


--
-- Name: location id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location ALTER COLUMN id SET DEFAULT nextval('public.location_id_seq'::regclass);


--
-- Name: profile id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile ALTER COLUMN id SET DEFAULT nextval('public.profile_id_seq'::regclass);


--
-- Name: role id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role ALTER COLUMN id SET DEFAULT nextval('public.role_id_seq'::regclass);


--
-- Name: tool id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tool ALTER COLUMN id SET DEFAULT nextval('public.tool_id_seq'::regclass);


--
-- Name: tool_image id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tool_image ALTER COLUMN id SET DEFAULT nextval('public.tool_image_id_seq'::regclass);


--
-- Data for Name: app_user; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.app_user (id, first_name, last_name, google_sub, role_id, status) VALUES (1, 'Marko', 'Tamm', 'demo-marko-tamm', 1, 'A');
INSERT INTO public.app_user (id, first_name, last_name, google_sub, role_id, status) VALUES (3, 'Liis', 'Kask', 'demo-liis-kask', 2, 'A');


--
-- Data for Name: booking; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.booking (id, tool_id, renter_id, start_date, end_date, status, owner_message, google_event_id, created_at, updated_at) VALUES (2, 2, 3, '2026-09-18', '2026-09-21', 'C', 'Palun tagasta redel 21. septembril enne kella 18.', NULL, '2026-09-18 11:00:00', '2026-09-18 11:00:00');


--
-- Data for Name: category; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.category (id, category_name) VALUES (1, 'Elektritööriistad');
INSERT INTO public.category (id, category_name) VALUES (2, 'Redelid');
INSERT INTO public.category (id, category_name) VALUES (3, 'Aiatööriistad');


--
-- Data for Name: city; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.city (id, city_name) VALUES (1, 'Tallinn');


--
-- Data for Name: district; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.district (id, city_id, district_name) VALUES (1, 1, 'Kristiine');
INSERT INTO public.district (id, city_id, district_name) VALUES (2, 1, 'Mustamäe');


--
-- Data for Name: location; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.location (id, district_id, street_name, house_number, apartment_number, lng, lat) VALUES (1, 1, 'Teddre', '28', NULL, NULL, NULL);
INSERT INTO public.location (id, district_id, street_name, house_number, apartment_number, lng, lat) VALUES (3, 2, 'Sõpruse pst', '120', '8', NULL, NULL);


--
-- Data for Name: profile; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.profile (id, location_id, user_id, email, phone, created_at, updated_at) VALUES (1, 1, 1, 'email@Gmail.com', '56565656', '2026-09-18 10:00:00', '2026-09-18 10:00:00');
INSERT INTO public.profile (id, location_id, user_id, email, phone, created_at, updated_at) VALUES (3, 3, 3, 'liis.kask@example.com', '55501002', '2026-09-18 10:30:00', '2026-09-18 10:30:00');


--
-- Data for Name: role; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.role (id, role_name) VALUES (1, 'admin');
INSERT INTO public.role (id, role_name) VALUES (2, 'customer');


--
-- Data for Name: tool; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.tool (id, owner_id, category_id, name, description, status, created_at, updated_at) VALUES (1, 1, 1, 'Akutrell', '18 V akutrell, kaks akut ja laadija. Heas korras.', 'A', '2026-09-18 10:15:00', '2026-09-18 10:15:00');
INSERT INTO public.tool (id, owner_id, category_id, name, description, status, created_at, updated_at) VALUES (3, 3, 1, 'Tikksaag', '500 W elektriline tikksaag koos kolme saelehega. Sobib puidu lõikamiseks.', 'A', '2026-09-18 10:40:00', '2026-09-18 10:40:00');
INSERT INTO public.tool (id, owner_id, category_id, name, description, status, created_at, updated_at) VALUES (4, 3, 3, 'Hekikäärid', 'Käsitsi kasutatavad hekikäärid, tera pikkus 25 cm. Heas korras.', 'A', '2026-09-18 10:45:00', '2026-09-18 10:45:00');
INSERT INTO public.tool (id, owner_id, category_id, name, description, status, created_at, updated_at) VALUES (2, 1, 2, 'Redel', 'Alumiiniumist kokkupandav redel, töökõrgus 3 meetrit.', 'U', '2026-09-18 10:20:00', '2026-09-18 11:00:00');


--
-- Data for Name: tool_image; Type: TABLE DATA; Schema: public; Owner: -
--

INSERT INTO public.tool_image (id, tool_id, image_data, is_main) VALUES (1, 1, '\x3c73766720786d6c6e733d22687474703a2f2f7777772e77332e6f72672f323030302f737667222077696474683d2232343022206865696768743d22323430222076696577426f783d223020302032343020323430223e3c726563742077696474683d2232343022206865696768743d22323430222072783d223136222066696c6c3d2223663166356639222f3e3c706174682066696c6c3d22233235363365622220643d224d3530203630683130357634354835307a204d3835203130356833357636304838357a222f3e3c706174682066696c6c3d22233333343135352220643d224d313535203730683235763235682d32357a204d3138302037386833307639682d33307a204d3730203135356836357631354837307a222f3e3c7465787420783d223132302220793d223231352220746578742d616e63686f723d226d6964646c652220666f6e742d66616d696c793d2273616e732d73657269662220666f6e742d73697a653d223230222066696c6c3d2223306631373261223e416b757472656c6c3c2f746578743e3c2f7376673e', true);
INSERT INTO public.tool_image (id, tool_id, image_data, is_main) VALUES (2, 3, '\x3c73766720786d6c6e733d22687474703a2f2f7777772e77332e6f72672f323030302f737667222077696474683d2232343022206865696768743d22323430222076696577426f783d223020302032343020323430223e3c726563742077696474683d2232343022206865696768743d22323430222072783d223136222066696c6c3d2223663166356639222f3e3c706174682066696c6c3d22233064393438382220643d224d3635203930683130307636304836357a222f3e3c706174682066696c6c3d226e6f6e6522207374726f6b653d222330643934383822207374726f6b652d77696474683d2231352220643d224d3835203930563535683535763335222f3e3c70617468207374726f6b653d222333333431353522207374726f6b652d77696474683d22382220643d224d35352031353568313235204d31353020313330763435222f3e3c7465787420783d223132302220793d223231352220746578742d616e63686f723d226d6964646c652220666f6e742d66616d696c793d2273616e732d73657269662220666f6e742d73697a653d223230222066696c6c3d2223306631373261223e54696b6b736161673c2f746578743e3c2f7376673e', true);
INSERT INTO public.tool_image (id, tool_id, image_data, is_main) VALUES (3, 4, '\x3c73766720786d6c6e733d22687474703a2f2f7777772e77332e6f72672f323030302f737667222077696474683d2232343022206865696768743d22323430222076696577426f783d223020302032343020323430223e3c726563742077696474683d2232343022206865696768743d22323430222072783d223136222066696c6c3d2223663166356639222f3e3c672066696c6c3d226e6f6e6522207374726f6b652d77696474683d223130223e3c70617468207374726f6b653d22233634373438622220643d224d36352033356c31303520313335204d3137352033354c373020313730222f3e3c70617468207374726f6b653d22236561353830632220643d224d313435203134306c3235203330204d3935203134306c2d3235203330222f3e3c2f673e3c636972636c652063783d22313230222063793d223130352220723d2239222066696c6c3d2223333334313535222f3e3c7465787420783d223132302220793d223231352220746578742d616e63686f723d226d6964646c652220666f6e742d66616d696c793d2273616e732d73657269662220666f6e742d73697a653d223230222066696c6c3d2223306631373261223e48656b696bc3a4c3a47269643c2f746578743e3c2f7376673e', true);
INSERT INTO public.tool_image (id, tool_id, image_data, is_main) VALUES (4, 2, '\x3c73766720786d6c6e733d22687474703a2f2f7777772e77332e6f72672f323030302f737667222077696474683d2232343022206865696768743d22323430222076696577426f783d223020302032343020323430223e3c726563742077696474683d2232343022206865696768743d22323430222072783d223136222066696c6c3d2223663166356639222f3e3c67207374726f6b653d222336343734386222207374726f6b652d77696474683d223130222066696c6c3d226e6f6e65223e3c7061746820643d224d3730203137354c3930203335204d313730203137354c313530203335204d3836203630683638204d3832203930683736204d373820313230683834204d373420313530683932222f3e3c2f673e3c7465787420783d223132302220793d223231352220746578742d616e63686f723d226d6964646c652220666f6e742d66616d696c793d2273616e732d73657269662220666f6e742d73697a653d223230222066696c6c3d2223306631373261223e526564656c3c2f746578743e3c2f7376673e', true);


--
-- Name: app_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.app_user_id_seq', 3, true);


--
-- Name: booking_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.booking_id_seq', 2, true);


--
-- Name: category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.category_id_seq', 4, true);


--
-- Name: city_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.city_id_seq', 1, true);


--
-- Name: district_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.district_id_seq', 3, true);


--
-- Name: location_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.location_id_seq', 3, true);


--
-- Name: profile_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.profile_id_seq', 3, true);


--
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.role_id_seq', 2, true);


--
-- Name: tool_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.tool_id_seq', 4, true);


--
-- Name: tool_image_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

SELECT pg_catalog.setval('public.tool_image_id_seq', 4, true);


--
-- Name: app_user app_user_google_sub_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT app_user_google_sub_unique UNIQUE (google_sub);


--
-- Name: app_user app_user_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT app_user_pk PRIMARY KEY (id);


--
-- Name: booking booking_google_event_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking
    ADD CONSTRAINT booking_google_event_unique UNIQUE (google_event_id);


--
-- Name: booking booking_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking
    ADD CONSTRAINT booking_pk PRIMARY KEY (id);


--
-- Name: category category_name_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_name_unique UNIQUE (category_name);


--
-- Name: category category_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.category
    ADD CONSTRAINT category_pk PRIMARY KEY (id);


--
-- Name: city city_name_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.city
    ADD CONSTRAINT city_name_unique UNIQUE (city_name);


--
-- Name: city city_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.city
    ADD CONSTRAINT city_pk PRIMARY KEY (id);


--
-- Name: district district_city_name_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.district
    ADD CONSTRAINT district_city_name_unique UNIQUE (city_id, district_name);


--
-- Name: district district_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.district
    ADD CONSTRAINT district_pk PRIMARY KEY (id);


--
-- Name: location location_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location
    ADD CONSTRAINT location_pk PRIMARY KEY (id);


--
-- Name: profile profile_email_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_email_unique UNIQUE (email);


--
-- Name: profile profile_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_pk PRIMARY KEY (id);


--
-- Name: profile profile_user_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_user_unique UNIQUE (user_id);


--
-- Name: role role_name_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_name_unique UNIQUE (role_name);


--
-- Name: role role_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.role
    ADD CONSTRAINT role_pk PRIMARY KEY (id);


--
-- Name: tool_image tool_image_data_unique; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tool_image
    ADD CONSTRAINT tool_image_data_unique UNIQUE (tool_id, image_data);


--
-- Name: tool_image tool_image_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tool_image
    ADD CONSTRAINT tool_image_pk PRIMARY KEY (id);


--
-- Name: tool tool_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tool
    ADD CONSTRAINT tool_pk PRIMARY KEY (id);


--
-- Name: tool_image_one_main_unique; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX tool_image_one_main_unique ON public.tool_image USING btree (tool_id) WHERE (is_main = true);


--
-- Name: app_user app_user_role_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.app_user
    ADD CONSTRAINT app_user_role_fk FOREIGN KEY (role_id) REFERENCES public.role(id) ON DELETE RESTRICT;


--
-- Name: booking booking_renter_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking
    ADD CONSTRAINT booking_renter_fk FOREIGN KEY (renter_id) REFERENCES public.app_user(id) ON DELETE RESTRICT;


--
-- Name: booking booking_tool_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.booking
    ADD CONSTRAINT booking_tool_fk FOREIGN KEY (tool_id) REFERENCES public.tool(id) ON DELETE RESTRICT;


--
-- Name: district district_city_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.district
    ADD CONSTRAINT district_city_fk FOREIGN KEY (city_id) REFERENCES public.city(id) ON DELETE RESTRICT;


--
-- Name: location location_district_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.location
    ADD CONSTRAINT location_district_fk FOREIGN KEY (district_id) REFERENCES public.district(id) ON DELETE RESTRICT;


--
-- Name: profile profile_location_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_location_fk FOREIGN KEY (location_id) REFERENCES public.location(id) ON DELETE RESTRICT;


--
-- Name: profile profile_user_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.profile
    ADD CONSTRAINT profile_user_fk FOREIGN KEY (user_id) REFERENCES public.app_user(id) ON DELETE RESTRICT;


--
-- Name: tool tool_category_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tool
    ADD CONSTRAINT tool_category_fk FOREIGN KEY (category_id) REFERENCES public.category(id) ON DELETE RESTRICT;


--
-- Name: tool_image tool_image_tool_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tool_image
    ADD CONSTRAINT tool_image_tool_fk FOREIGN KEY (tool_id) REFERENCES public.tool(id) ON DELETE CASCADE;


--
-- Name: tool tool_owner_fk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.tool
    ADD CONSTRAINT tool_owner_fk FOREIGN KEY (owner_id) REFERENCES public.app_user(id) ON DELETE RESTRICT;


--
-- PostgreSQL database dump complete
--

COMMIT;

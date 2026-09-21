-- Ainult andmebaasi struktuur (ilma andmeteta), genereeritud database-with-data.sql põhjal.
-- Kasutamiseks tühjas PostgreSQL 17+ andmebaasis database.
BEGIN;

--
-- PostgreSQL database dump (schema-only)
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
-- Name: app_user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

--
-- Name: booking_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

--
-- Name: category_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

--
-- Name: city_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

--
-- Name: district_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

--
-- Name: location_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

--
-- Name: profile_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

--
-- Name: role_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

--
-- Name: tool_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

--
-- Name: tool_image_id_seq; Type: SEQUENCE SET; Schema: public; Owner: -
--

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

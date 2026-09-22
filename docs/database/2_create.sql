CREATE TABLE role (
    id serial PRIMARY KEY,
    role_name varchar(20) NOT NULL UNIQUE
);

CREATE TABLE app_user (
    id serial PRIMARY KEY,
    role_id integer NOT NULL REFERENCES role (id),
    first_name varchar(100) NOT NULL,
    last_name varchar(100) NOT NULL,
    google_sub varchar(255) NOT NULL UNIQUE,
    status char(1) NOT NULL DEFAULT 'A' CHECK (status IN ('A', 'B'))
);

CREATE TABLE city (
    id serial PRIMARY KEY,
    city_name varchar(100) NOT NULL UNIQUE
);

CREATE TABLE district (
    id serial PRIMARY KEY,
    city_id integer NOT NULL REFERENCES city (id),
    district_name varchar(100) NOT NULL,
    CONSTRAINT district_city_name_unique UNIQUE (city_id, district_name)
);

CREATE TABLE location (
    id serial PRIMARY KEY,
    district_id integer NOT NULL REFERENCES district (id),
    street_name varchar(150) NOT NULL,
    house_number varchar(20) NOT NULL,
    apartment_number varchar(20),
    lng decimal(10, 7),
    lat decimal(10, 7)
);

CREATE TABLE profile (
    id serial PRIMARY KEY,
    user_id integer NOT NULL UNIQUE REFERENCES app_user (id),
    location_id integer NOT NULL REFERENCES location (id),
    email varchar(254) NOT NULL UNIQUE,
    phone varchar(32) NOT NULL,
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp
);

CREATE TABLE category (
    id serial PRIMARY KEY,
    category_name varchar(100) NOT NULL UNIQUE
);

CREATE TABLE tool (
    id serial PRIMARY KEY,
    owner_id integer NOT NULL REFERENCES app_user (id),
    category_id integer NOT NULL REFERENCES category (id),
    name varchar(150) NOT NULL,
    description varchar(2000),
    status char(1) NOT NULL DEFAULT 'A' CHECK (status IN ('A', 'U')),
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp
);

CREATE TABLE tool_image (
    id serial PRIMARY KEY,
    tool_id integer NOT NULL REFERENCES tool (id) ON DELETE CASCADE,
    image_data bytea NOT NULL,
    is_main boolean NOT NULL DEFAULT false,
    CONSTRAINT tool_image_data_unique UNIQUE (tool_id, image_data)
);

CREATE UNIQUE INDEX tool_image_one_main_unique ON tool_image (tool_id) WHERE is_main = true;

CREATE TABLE booking (
    id serial PRIMARY KEY,
    tool_id integer NOT NULL REFERENCES tool (id),
    renter_id integer NOT NULL REFERENCES app_user (id),
    start_date date NOT NULL,
    end_date date NOT NULL,
    status char(1) NOT NULL DEFAULT 'P' CHECK (status IN ('P', 'C', 'R')),
    owner_message varchar(500),
    google_event_id varchar(255) UNIQUE,
    created_at timestamp NOT NULL DEFAULT current_timestamp,
    updated_at timestamp NOT NULL DEFAULT current_timestamp,
    CONSTRAINT booking_period_check CHECK (start_date <= end_date)
);

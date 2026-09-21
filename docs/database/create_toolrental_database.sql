-- Created by Redgate Data Modeler (https://datamodeler.redgate-platform.com)
-- Last modification date: 2026-09-18 08:44:17.911

-- tables
-- Table: app_user
CREATE TABLE app_user (
    id serial  NOT NULL,
    first_name varchar(100)  NOT NULL,
    last_name varchar(100)  NOT NULL,
    google_sub varchar(255)  NOT NULL,
    role_id int  NOT NULL,
    status char(1)  NOT NULL DEFAULT 'A',
    CONSTRAINT app_user_google_sub_unique UNIQUE (google_sub) NOT DEFERRABLE  INITIALLY IMMEDIATE,
    CONSTRAINT app_user_status_check CHECK (( status IN ( 'A' , 'B' ) )) NOT DEFERRABLE INITIALLY IMMEDIATE,
    CONSTRAINT app_user_pk PRIMARY KEY (id)
);

-- Table: booking
CREATE TABLE booking (
    id serial  NOT NULL,
    tool_id int  NOT NULL,
    renter_id int  NOT NULL,
    start_date date  NOT NULL,
    end_date date  NOT NULL,
    status char(1)  NOT NULL DEFAULT 'P',
    owner_message varchar(500)  NULL,
    google_event_id varchar(255)  NULL,
    created_at timestamp  NOT NULL DEFAULT current_timestamp,
    updated_at timestamp  NOT NULL DEFAULT current_timestamp,
    CONSTRAINT booking_google_event_unique UNIQUE (google_event_id) NOT DEFERRABLE  INITIALLY IMMEDIATE,
    CONSTRAINT booking_period_check CHECK (( start_date < end_date )) NOT DEFERRABLE INITIALLY IMMEDIATE,
    CONSTRAINT booking_status_check CHECK (( status IN ( 'P' , 'C' ) )) NOT DEFERRABLE INITIALLY IMMEDIATE,
    CONSTRAINT booking_pk PRIMARY KEY (id)
);

-- Table: category
CREATE TABLE category (
    id serial  NOT NULL,
    category_name varchar(100)  NOT NULL,
    CONSTRAINT category_name_unique UNIQUE (category_name) NOT DEFERRABLE  INITIALLY IMMEDIATE,
    CONSTRAINT category_pk PRIMARY KEY (id)
);

-- Table: city
CREATE TABLE city (
    id serial  NOT NULL,
    city_name varchar(100)  NOT NULL,
    CONSTRAINT city_name_unique UNIQUE (city_name) NOT DEFERRABLE  INITIALLY IMMEDIATE,
    CONSTRAINT city_pk PRIMARY KEY (id)
);

-- Table: district
CREATE TABLE district (
    id serial  NOT NULL,
    city_id int  NOT NULL,
    district_name varchar(100)  NOT NULL,
    CONSTRAINT district_city_name_unique UNIQUE (city_id, district_name) NOT DEFERRABLE  INITIALLY IMMEDIATE,
    CONSTRAINT district_pk PRIMARY KEY (id)
);

-- Table: location
CREATE TABLE location (
    id serial  NOT NULL,
    district_id int  NOT NULL,
    street_name varchar(150)  NOT NULL,
    house_number varchar(20)  NOT NULL,
    apartment_number varchar(20)  NULL,
    lng decimal(10,7)  NULL,
    lat decimal(10,7)  NULL,
    CONSTRAINT location_pk PRIMARY KEY (id)
);

-- Table: profile
CREATE TABLE profile (
    id serial  NOT NULL,
    location_id int  NOT NULL,
    user_id int  NOT NULL,
    email varchar(254)  NOT NULL,
    phone varchar(32)  NOT NULL,
    created_at timestamp  NOT NULL DEFAULT current_timestamp,
    updated_at timestamp  NOT NULL DEFAULT current_timestamp,
    CONSTRAINT profile_email_unique UNIQUE (email) NOT DEFERRABLE  INITIALLY IMMEDIATE,
    CONSTRAINT profile_user_unique UNIQUE (user_id) NOT DEFERRABLE  INITIALLY IMMEDIATE,
    CONSTRAINT profile_pk PRIMARY KEY (id)
);

-- Table: role
CREATE TABLE role (
    id serial  NOT NULL,
    role_name varchar(20)  NOT NULL,
    CONSTRAINT role_name_unique UNIQUE (role_name) NOT DEFERRABLE  INITIALLY IMMEDIATE,
    CONSTRAINT role_pk PRIMARY KEY (id)
);

-- Table: tool
CREATE TABLE tool (
    id serial  NOT NULL,
    owner_id int  NOT NULL,
    category_id int  NOT NULL,
    name varchar(150)  NOT NULL,
    description varchar(2000)  NULL,
    status char(1)  NOT NULL DEFAULT 'A',
    created_at timestamp  NOT NULL DEFAULT current_timestamp,
    updated_at timestamp  NOT NULL DEFAULT current_timestamp,
    CONSTRAINT tool_status_check CHECK (( status IN ( 'A' , 'U' ) )) NOT DEFERRABLE INITIALLY IMMEDIATE,
    CONSTRAINT tool_pk PRIMARY KEY (id)
);

-- Table: tool_image
CREATE TABLE tool_image (
    id serial  NOT NULL,
    tool_id int  NOT NULL,
    image_data bytea  NOT NULL,
    is_main boolean  NOT NULL DEFAULT false,
    CONSTRAINT tool_image_data_unique UNIQUE (tool_id, image_data) NOT DEFERRABLE  INITIALLY IMMEDIATE,
    CONSTRAINT tool_image_pk PRIMARY KEY (id)
);

CREATE UNIQUE INDEX tool_image_one_main_unique on tool_image (tool_id ASC)
    WHERE is_main = true;

-- foreign keys
-- Reference: app_user_role_fk (table: app_user)
ALTER TABLE app_user ADD CONSTRAINT app_user_role_fk
    FOREIGN KEY (role_id)
    REFERENCES role (id)
    ON DELETE  RESTRICT  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: booking_renter_fk (table: booking)
ALTER TABLE booking ADD CONSTRAINT booking_renter_fk
    FOREIGN KEY (renter_id)
    REFERENCES app_user (id)
    ON DELETE  RESTRICT  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: booking_tool_fk (table: booking)
ALTER TABLE booking ADD CONSTRAINT booking_tool_fk
    FOREIGN KEY (tool_id)
    REFERENCES tool (id)
    ON DELETE  RESTRICT  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: district_city_fk (table: district)
ALTER TABLE district ADD CONSTRAINT district_city_fk
    FOREIGN KEY (city_id)
    REFERENCES city (id)
    ON DELETE  RESTRICT  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: location_district_fk (table: location)
ALTER TABLE location ADD CONSTRAINT location_district_fk
    FOREIGN KEY (district_id)
    REFERENCES district (id)
    ON DELETE  RESTRICT  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: profile_location_fk (table: profile)
ALTER TABLE profile ADD CONSTRAINT profile_location_fk
    FOREIGN KEY (location_id)
    REFERENCES location (id)
    ON DELETE  RESTRICT  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: profile_user_fk (table: profile)
ALTER TABLE profile ADD CONSTRAINT profile_user_fk
    FOREIGN KEY (user_id)
    REFERENCES app_user (id)
    ON DELETE  RESTRICT  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: tool_category_fk (table: tool)
ALTER TABLE tool ADD CONSTRAINT tool_category_fk
    FOREIGN KEY (category_id)
    REFERENCES category (id)
    ON DELETE  RESTRICT  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: tool_image_tool_fk (table: tool_image)
ALTER TABLE tool_image ADD CONSTRAINT tool_image_tool_fk
    FOREIGN KEY (tool_id)
    REFERENCES tool (id)
    ON DELETE  CASCADE  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- Reference: tool_owner_fk (table: tool)
ALTER TABLE tool ADD CONSTRAINT tool_owner_fk
    FOREIGN KEY (owner_id)
    REFERENCES app_user (id)
    ON DELETE  RESTRICT  
    NOT DEFERRABLE 
    INITIALLY IMMEDIATE
;

-- End of file.


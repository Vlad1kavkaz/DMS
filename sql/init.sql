-- =========================================================
-- DATABASE: db_ics_cogs
-- SCHEMA  : sc_cogs
-- SUBJECT : composition and state of russian observation satellites
-- =========================================================

-- CREATE DATABASE usually must be run separately by a superuser or a user
-- with CREATEDB privilege.
-- CREATE DATABASE db_ics_cogs;

-- Connect to database db_ics_cogs before running the rest.

CREATE SCHEMA IF NOT EXISTS sc_cogs;

-- =========================================================
-- SEQUENCES
-- =========================================================

CREATE SEQUENCE IF NOT EXISTS sc_cogs.sq_ni_id_organization
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS sc_cogs.sq_ni_id_satellite_series
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS sc_cogs.sq_ni_id_satellite
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS sc_cogs.sq_ni_id_orbit
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS sc_cogs.sq_ni_id_payload
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS sc_cogs.sq_ni_id_cross_satellite_payload
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 2147483647
    CACHE 1;

-- =========================================================
-- TABLE: tb_organization
-- =========================================================

CREATE TABLE IF NOT EXISTS sc_cogs.tb_organization (
                                                       ni_id_organization INTEGER NOT NULL
                                                       DEFAULT nextval('sc_cogs.sq_ni_id_organization'),
    cv_organization_name VARCHAR(200) NOT NULL,
    cv_organization_short_name VARCHAR(100),
    cv_organization_type VARCHAR(50) NOT NULL,
    cv_country_code VARCHAR(2) NOT NULL DEFAULT 'RU',
    cv_website VARCHAR(255),
    dt_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dt_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ni_id_organization
    PRIMARY KEY (ni_id_organization),

    CONSTRAINT cnu_cv_organization_name
    UNIQUE (cv_organization_name),

    CONSTRAINT cnc_cv_organization_type
    CHECK (
              cv_organization_type IN (
              'operator',
              'owner',
              'manufacturer',
              'agency',
              'other'
                                      )
    )
    );

ALTER SEQUENCE sc_cogs.sq_ni_id_organization
    OWNED BY sc_cogs.tb_organization.ni_id_organization;

-- =========================================================
-- TABLE: tb_satellite_series
-- =========================================================

CREATE TABLE IF NOT EXISTS sc_cogs.tb_satellite_series (
                                                           ni_id_satellite_series INTEGER NOT NULL
                                                           DEFAULT nextval('sc_cogs.sq_ni_id_satellite_series'),
    cv_satellite_series_code VARCHAR(50) NOT NULL,
    cv_satellite_series_name VARCHAR(200) NOT NULL,
    ct_description TEXT,
    dt_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dt_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ni_id_satellite_series
    PRIMARY KEY (ni_id_satellite_series),

    CONSTRAINT cnu_cv_satellite_series_code
    UNIQUE (cv_satellite_series_code),

    CONSTRAINT cnu_cv_satellite_series_name
    UNIQUE (cv_satellite_series_name)
    );

ALTER SEQUENCE sc_cogs.sq_ni_id_satellite_series
    OWNED BY sc_cogs.tb_satellite_series.ni_id_satellite_series;

-- =========================================================
-- TABLE: tb_satellite
-- =========================================================

CREATE TABLE IF NOT EXISTS sc_cogs.tb_satellite (
                                                    ni_id_satellite INTEGER NOT NULL
                                                    DEFAULT nextval('sc_cogs.sq_ni_id_satellite'),
    ni_id_satellite_series INTEGER,
    cv_satellite_name VARCHAR(200) NOT NULL,
    cv_satellite_code VARCHAR(100),
    cv_international_designator VARCHAR(50),
    ni_norad_catalog_number INTEGER,
    cv_satellite_purpose VARCHAR(50) NOT NULL,
    cv_satellite_status VARCHAR(50) NOT NULL,
    dt_launch_date DATE,
    dt_operation_start_date DATE,
    dt_decommission_date DATE,
    ni_id_operator_organization INTEGER,
    ni_id_owner_organization INTEGER,
    ni_id_manufacturer_organization INTEGER,
    ct_description TEXT,
    ct_notes TEXT,
    dt_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dt_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ni_id_satellite
    PRIMARY KEY (ni_id_satellite),

    CONSTRAINT cnu_cv_international_designator
    UNIQUE (cv_international_designator),

    CONSTRAINT cnu_ni_norad_catalog_number
    UNIQUE (ni_norad_catalog_number),

    CONSTRAINT fk_tb_satellite_series_tb_satellite_ni_id_satellite_series
    FOREIGN KEY (ni_id_satellite_series)
    REFERENCES sc_cogs.tb_satellite_series (ni_id_satellite_series),

    CONSTRAINT fk_tb_organization_tb_satellite_ni_id_operator_organization
    FOREIGN KEY (ni_id_operator_organization)
    REFERENCES sc_cogs.tb_organization (ni_id_organization),

    CONSTRAINT fk_tb_organization_tb_satellite_ni_id_owner_organization
    FOREIGN KEY (ni_id_owner_organization)
    REFERENCES sc_cogs.tb_organization (ni_id_organization),

    CONSTRAINT fk_tb_organization_tb_satellite_ni_id_manufacturer_organization
    FOREIGN KEY (ni_id_manufacturer_organization)
    REFERENCES sc_cogs.tb_organization (ni_id_organization),

    CONSTRAINT cnc_cv_satellite_purpose
    CHECK (
              cv_satellite_purpose IN (
              'meteorology',
              'hydrology',
              'remote_sensing',
              'climate_monitoring',
              'ocean_monitoring',
              'ice_monitoring',
              'environment_monitoring',
              'multi_purpose',
              'other'
                                      )
    ),

    CONSTRAINT cnc_cv_satellite_status
    CHECK (
              cv_satellite_status IN (
              'planned',
              'active',
              'reserve',
              'inactive',
              'lost',
              'retired'
                                     )
    ),

    CONSTRAINT cnc_dt_satellite_dates
    CHECK (
              dt_decommission_date IS NULL
              OR dt_launch_date IS NULL
              OR dt_decommission_date >= dt_launch_date
          )
    );

ALTER SEQUENCE sc_cogs.sq_ni_id_satellite
    OWNED BY sc_cogs.tb_satellite.ni_id_satellite;

-- =========================================================
-- TABLE: tb_orbit
-- =========================================================

CREATE TABLE IF NOT EXISTS sc_cogs.tb_orbit (
                                                ni_id_orbit INTEGER NOT NULL
                                                DEFAULT nextval('sc_cogs.sq_ni_id_orbit'),
    ni_id_satellite INTEGER NOT NULL,
    cv_orbit_type VARCHAR(50) NOT NULL,
    n_inclination_deg NUMERIC(6,3),
    dt_valid_from DATE,
    dt_valid_to DATE,
    bl_is_current BOOLEAN NOT NULL DEFAULT TRUE,
    ct_notes TEXT,
    dt_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dt_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ni_id_orbit
    PRIMARY KEY (ni_id_orbit),

    CONSTRAINT fk_tb_satellite_tb_orbit_ni_id_satellite
    FOREIGN KEY (ni_id_satellite)
    REFERENCES sc_cogs.tb_satellite (ni_id_satellite)
    ON DELETE CASCADE,

    CONSTRAINT cnc_cv_orbit_type
    CHECK (
              cv_orbit_type IN (
              'geostationary',
              'highly_elliptical',
              'polar'
                               )
    ),

    CONSTRAINT cnc_n_inclination_deg
    CHECK (
              n_inclination_deg IS NULL
              OR (n_inclination_deg >= 0 AND n_inclination_deg <= 180)
    ),

    CONSTRAINT cnc_dt_orbit_valid_range
    CHECK (
              dt_valid_to IS NULL
              OR dt_valid_from IS NULL
              OR dt_valid_to >= dt_valid_from
          )
    );

ALTER SEQUENCE sc_cogs.sq_ni_id_orbit
    OWNED BY sc_cogs.tb_orbit.ni_id_orbit;

-- =========================================================
-- TABLE: tb_geo_orbit
-- =========================================================

CREATE TABLE IF NOT EXISTS sc_cogs.tb_geo_orbit (
                                                    ni_id_orbit INTEGER NOT NULL,
                                                    n_station_longitude_deg NUMERIC(7,3) NOT NULL,
    n_nominal_altitude_km NUMERIC(10,3),
    n_orbital_period_min NUMERIC(10,3),

    CONSTRAINT pk_ni_id_geo_orbit
    PRIMARY KEY (ni_id_orbit),

    CONSTRAINT fk_tb_orbit_tb_geo_orbit_ni_id_orbit
    FOREIGN KEY (ni_id_orbit)
    REFERENCES sc_cogs.tb_orbit (ni_id_orbit)
    ON DELETE CASCADE,

    CONSTRAINT cnc_n_station_longitude_deg
    CHECK (
              n_station_longitude_deg >= -180
              AND n_station_longitude_deg <= 180
          ),

    CONSTRAINT cnc_n_nominal_altitude_km
    CHECK (
              n_nominal_altitude_km IS NULL
              OR n_nominal_altitude_km >= 0
          ),

    CONSTRAINT cnc_n_geo_orbital_period_min
    CHECK (
              n_orbital_period_min IS NULL
              OR n_orbital_period_min > 0
          )
    );

-- =========================================================
-- TABLE: tb_heo_orbit
-- =========================================================

CREATE TABLE IF NOT EXISTS sc_cogs.tb_heo_orbit (
                                                    ni_id_orbit INTEGER NOT NULL,
                                                    n_eccentricity NUMERIC(10,7) NOT NULL,
    n_perigee_altitude_km NUMERIC(10,3) NOT NULL,
    n_apogee_altitude_km NUMERIC(10,3) NOT NULL,
    n_orbital_period_min NUMERIC(10,3),

    CONSTRAINT pk_ni_id_heo_orbit
    PRIMARY KEY (ni_id_orbit),

    CONSTRAINT fk_tb_orbit_tb_heo_orbit_ni_id_orbit
    FOREIGN KEY (ni_id_orbit)
    REFERENCES sc_cogs.tb_orbit (ni_id_orbit)
    ON DELETE CASCADE,

    CONSTRAINT cnc_n_eccentricity
    CHECK (
              n_eccentricity >= 0
              AND n_eccentricity < 1
          ),

    CONSTRAINT cnc_n_heo_altitudes
    CHECK (
              n_perigee_altitude_km >= 0
              AND n_apogee_altitude_km >= n_perigee_altitude_km
          ),

    CONSTRAINT cnc_n_heo_orbital_period_min
    CHECK (
              n_orbital_period_min IS NULL
              OR n_orbital_period_min > 0
          )
    );

-- =========================================================
-- TABLE: tb_polar_orbit
-- =========================================================

CREATE TABLE IF NOT EXISTS sc_cogs.tb_polar_orbit (
                                                      ni_id_orbit INTEGER NOT NULL,
                                                      n_mean_altitude_km NUMERIC(10,3) NOT NULL,
    n_orbital_period_min NUMERIC(10,3),
    bl_is_sun_synchronous BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_ni_id_polar_orbit
    PRIMARY KEY (ni_id_orbit),

    CONSTRAINT fk_tb_orbit_tb_polar_orbit_ni_id_orbit
    FOREIGN KEY (ni_id_orbit)
    REFERENCES sc_cogs.tb_orbit (ni_id_orbit)
    ON DELETE CASCADE,

    CONSTRAINT cnc_n_mean_altitude_km
    CHECK (
              n_mean_altitude_km >= 0
          ),

    CONSTRAINT cnc_n_polar_orbital_period_min
    CHECK (
              n_orbital_period_min IS NULL
              OR n_orbital_period_min > 0
          )
    );

-- =========================================================
-- TABLE: tb_payload
-- =========================================================

CREATE TABLE IF NOT EXISTS sc_cogs.tb_payload (
                                                  ni_id_payload INTEGER NOT NULL
                                                  DEFAULT nextval('sc_cogs.sq_ni_id_payload'),
    cv_payload_code VARCHAR(50),
    cv_payload_name VARCHAR(200) NOT NULL,
    cv_payload_type VARCHAR(50) NOT NULL,
    ni_id_manufacturer_organization INTEGER,
    ct_description TEXT,
    dt_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dt_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ni_id_payload
    PRIMARY KEY (ni_id_payload),

    CONSTRAINT fk_tb_organization_tb_payload_ni_id_manufacturer_organization
    FOREIGN KEY (ni_id_manufacturer_organization)
    REFERENCES sc_cogs.tb_organization (ni_id_organization),

    CONSTRAINT cnc_cv_payload_type
    CHECK (
              cv_payload_type IN (
              'radiometer',
              'spectrometer',
              'imager',
              'radar',
              'relay',
              'scanner',
              'other'
                                 )
    )
    );

ALTER SEQUENCE sc_cogs.sq_ni_id_payload
    OWNED BY sc_cogs.tb_payload.ni_id_payload;

-- =========================================================
-- TABLE: tb_cross_satellite_payload
-- =========================================================

CREATE TABLE IF NOT EXISTS sc_cogs.tb_cross_satellite_payload (
                                                                  ni_id_cross_satellite_payload INTEGER NOT NULL
                                                                  DEFAULT nextval('sc_cogs.sq_ni_id_cross_satellite_payload'),
    ni_id_satellite INTEGER NOT NULL,
    ni_id_payload INTEGER NOT NULL,
    bl_is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    ct_notes TEXT,
    dt_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ni_id_cross_satellite_payload
    PRIMARY KEY (ni_id_cross_satellite_payload),

    CONSTRAINT fk_tb_satellite_tb_cross_satellite_payload_ni_id_satellite
    FOREIGN KEY (ni_id_satellite)
    REFERENCES sc_cogs.tb_satellite (ni_id_satellite)
    ON DELETE CASCADE,

    CONSTRAINT fk_tb_payload_tb_cross_satellite_payload_ni_id_payload
    FOREIGN KEY (ni_id_payload)
    REFERENCES sc_cogs.tb_payload (ni_id_payload),

    CONSTRAINT cnu_ni_id_satellite_ni_id_payload
    UNIQUE (ni_id_satellite, ni_id_payload)
    );

ALTER SEQUENCE sc_cogs.sq_ni_id_cross_satellite_payload
    OWNED BY sc_cogs.tb_cross_satellite_payload.ni_id_cross_satellite_payload;

-- =========================================================
-- INDEXES
-- =========================================================

CREATE INDEX IF NOT EXISTS ix_tb_satellite_ni_id_satellite_series
    ON sc_cogs.tb_satellite (ni_id_satellite_series);

CREATE INDEX IF NOT EXISTS ix_tb_satellite_ni_id_operator_organization
    ON sc_cogs.tb_satellite (ni_id_operator_organization);

CREATE INDEX IF NOT EXISTS ix_tb_satellite_cv_satellite_status
    ON sc_cogs.tb_satellite (cv_satellite_status);

CREATE INDEX IF NOT EXISTS ix_tb_satellite_cv_satellite_purpose
    ON sc_cogs.tb_satellite (cv_satellite_purpose);

CREATE INDEX IF NOT EXISTS ix_tb_orbit_ni_id_satellite
    ON sc_cogs.tb_orbit (ni_id_satellite);

CREATE INDEX IF NOT EXISTS ix_tb_orbit_cv_orbit_type
    ON sc_cogs.tb_orbit (cv_orbit_type);

CREATE INDEX IF NOT EXISTS ix_tb_orbit_bl_is_current
    ON sc_cogs.tb_orbit (bl_is_current);

CREATE INDEX IF NOT EXISTS ix_tb_cross_satellite_payload_ni_id_satellite
    ON sc_cogs.tb_cross_satellite_payload (ni_id_satellite);

CREATE INDEX IF NOT EXISTS ix_tb_cross_satellite_payload_ni_id_payload
    ON sc_cogs.tb_cross_satellite_payload (ni_id_payload);

-- =========================================================
-- OPTIONAL BUSINESS RULE:
-- only one current orbit per satellite
-- =========================================================

CREATE UNIQUE INDEX IF NOT EXISTS uix_tb_orbit_current_satellite
    ON sc_cogs.tb_orbit (ni_id_satellite)
    WHERE bl_is_current = TRUE;

-- =========================================================
-- PATCH: satellite photo
-- =========================================================

ALTER TABLE sc_cogs.tb_satellite
    ADD COLUMN IF NOT EXISTS bt_photo BYTEA;
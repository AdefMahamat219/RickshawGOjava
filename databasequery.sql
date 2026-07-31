CREATE DATABASE rickshawgo;
USE rickshawgo;

-- Ride History Table
CREATE TABLE ride_history (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    from_loc    VARCHAR(100),
    to_loc      VARCHAR(100),
    distance_km DOUBLE,
    fare_bdt    DOUBLE,
    is_night    BOOLEAN,
    is_peak     BOOLEAN,
    ride_time   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Saved Locations Table
CREATE TABLE saved_locations (
    id       INT AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(100),
    node_id  VARCHAR(50)
);

-- Settings Table
CREATE TABLE settings (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    base_rate           DOUBLE DEFAULT 15.0,
    per_km_rate         DOUBLE DEFAULT 10.0,
    night_multiplier    DOUBLE DEFAULT 1.5,
    peak_multiplier     DOUBLE DEFAULT 1.2,
    night_start_hour    INT DEFAULT 21,
    night_end_hour      INT DEFAULT 6
);

-- Insert default settings
INSERT INTO settings (base_rate, per_km_rate, night_multiplier, peak_multiplier)
VALUES (15.0, 10.0, 1.5, 1.2);
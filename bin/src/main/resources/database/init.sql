CREATE DATABASE IF NOT EXISTS config_db;
USE config_db;

CREATE TABLE IF NOT EXISTS filaments (
    id_filament BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    color VARCHAR(255) NOT NULL,
    price_per_kg DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_name_color (name, color),
    CONSTRAINT chk_price_per_kg_positive CHECK (price_per_kg > 0),
    CONSTRAINT chk_status_valid CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS printing_config (
    id_printing_config BIGINT AUTO_INCREMENT PRIMARY KEY,
    electricity_price_kwh DECIMAL(10, 2) NOT NULL,
    printer_consumption_kwh DECIMAL(10, 2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_electricity_price_positive CHECK (electricity_price_kwh > 0),
    CONSTRAINT chk_printer_consumption_positive CHECK (printer_consumption_kwh > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO printing_config (electricity_price_kwh, printer_consumption_kwh) 
VALUES (150.00, 0.5);

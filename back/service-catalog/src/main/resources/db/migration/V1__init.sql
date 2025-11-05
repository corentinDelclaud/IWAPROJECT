CREATE TABLE IF NOT EXISTS service (
        id_service SERIAL PRIMARY KEY,
        game VARCHAR(50) NOT NULL,
        service_type VARCHAR(50) NOT NULL,
        description TEXT,
        price DECIMAL(10, 2) NOT NULL,
        is_unique BOOLEAN DEFAULT FALSE,
        is_available BOOLEAN DEFAULT TRUE,
        id_provider INT NOT NULL
    );

    CREATE INDEX IF NOT EXISTS idx_service_game ON service(game);
    CREATE INDEX IF NOT EXISTS idx_service_type ON service(service_type);
    CREATE INDEX IF NOT EXISTS idx_service_provider ON service(id_provider);
DO $$ 
BEGIN
    CREATE ROLE archiveuser WITH INHERIT LOGIN PASSWORD ':dbuserpwd';
EXCEPTION
    WHEN duplicate_object THEN 
        RAISE NOTICE 'Role already exists, skipping creation';
END $$;
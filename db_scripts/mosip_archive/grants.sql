\c mosip_archive

DO $$ 
BEGIN
    BEGIN
        EXECUTE 'GRANT CONNECT ON DATABASE mosip_archive TO archiveuser';
    EXCEPTION WHEN DUPLICATE_OBJECT THEN
        -- Permission already exists, do nothing
    END;

    BEGIN
        EXECUTE 'GRANT USAGE ON SCHEMA archive TO archiveuser';
    EXCEPTION WHEN DUPLICATE_OBJECT THEN
        -- Permission already exists, do nothing
    END;

    BEGIN
        EXECUTE 'GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES ON ALL TABLES IN SCHEMA archive TO archiveuser';
    EXCEPTION WHEN DUPLICATE_OBJECT THEN
        -- Permission already exists, do nothing
    END;
END $$;

ALTER DEFAULT PRIVILEGES IN SCHEMA archive 
    GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON TABLES TO archiveuser;
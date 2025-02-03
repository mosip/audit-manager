SELECT 'CREATE DATABASE mosip_archive
    ENCODING = ''UTF8''
    LC_COLLATE = ''en_US.UTF-8''
    LC_CTYPE = ''en_US.UTF-8''
    TABLESPACE = pg_default
    OWNER = postgres
    TEMPLATE = template0;'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'mosip_archive')\gexec

-- Add a comment to the database
COMMENT ON DATABASE mosip_archive IS 'mosip_archive database is used to store the archived data from source dbs';

\c mosip_archive postgres

CREATE SCHEMA IF NOT EXISTS archive;
ALTER SCHEMA archive OWNER TO postgres;
ALTER DATABASE mosip_archive SET search_path TO archive,pg_catalog,public;
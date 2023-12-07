\c mosip_audit

REASSIGN OWNED BY postgres TO sysadmin;

GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA audit TO sysadmin;

--This only need to execute if no rows added after upgradescripts for host_ip column---------------
ALTER TABLE audit.app_audit_log ALTER COLUMN host_ip TYPE varchar(50) USING host_ip::varchar;
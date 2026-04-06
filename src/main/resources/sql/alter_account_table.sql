-- Alter table Account to add IMAP fields
ALTER TABLE Account ADD COLUMN imap_host VARCHAR(255);
ALTER TABLE Account ADD COLUMN imap_port INT;
ALTER TABLE Account ADD COLUMN imap_secure BOOLEAN;
ALTER TABLE Account ADD COLUMN connection_error TEXT;
ALTER TABLE Account ADD COLUMN is_imap_active BOOLEAN DEFAULT FALSE;
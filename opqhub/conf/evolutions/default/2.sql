# --- !Ups
ALTER TABLE person ADD COLUMN enable_sms_alerts tinyint(1) default 0;
ALTER TABLE person ADD COLUMN enable_email_alerts tinyint(1) default 0;
ALTER TABLE person ADD COLUMN itic_region_email_threshold integer default 1;
ALTER TABLE person ADD COLUMN itic_region_sms_threshold integer default 1;
ALTER TABLE person ADD COLUMN enable_email_summary_notifications tinyint(1) default 0;
ALTER TABLE person ADD COLUMN enable_email_aert_notifications tinyint(1) default 0;
ALTER TABLE person ADD COLUMN email_notify_daily tinyint(1) default 0;
ALTER TABLE person ADD COLUMN email_notify_weekly tinyint(1) default 0;

#constraint ck_person_itic_region_email_threshold check (itic_region_email_threshold in (0,1,2));
#constraint ck_person_itic_region_sms_threshold check (itic_region_sms_threshold in (0,1,2));

# --- !Downs
ALTER TABLE person DROP COLUMN enable_sms_alerts;
ALTER TABLE person DROP COLUMN enable_email_alerts;
ALTER TABLE person DROP COLUMN itic_region_email_threshold;
ALTER TABLE person DROP COLUMN itic_region_sms_threshold;
ALTER TABLE person DROP COLUMN enable_email_summary_notifications;
ALTER TABLE person DROP COLUMN enable_email_aert_notifications;
ALTER TABLE person DROP COLUMN email_notify_daily;
ALTER TABLE person DROP COLUMN email_notify_weekly;
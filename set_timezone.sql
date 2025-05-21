-- Set timezone to GMT+8
PRAGMA timezone = '+08:00';

-- Verify the timezone setting
SELECT datetime('now', 'localtime') as current_time; 
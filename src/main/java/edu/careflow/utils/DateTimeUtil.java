package edu.careflow.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTimeUtil {
    // Using Asia/Singapore for GMT+8
    private static final ZoneId zoneId = ZoneId.of("Asia/Singapore");

    /**
     * Convert LocalDateTime to Timestamp for database storage
     * @param dateTime The LocalDateTime to convert
     * @return Timestamp in the specified timezone
     */
    public static Timestamp toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return Timestamp.valueOf(dateTime.atZone(zoneId).toLocalDateTime());
    }

    /**
     * Convert Timestamp from database to LocalDateTime in specified timezone
     * @param timestamp The Timestamp from database
     * @return LocalDateTime in specified timezone
     */
    public static LocalDateTime fromTimestamp(Timestamp timestamp) {
        if (timestamp == null) return null;
        return timestamp.toLocalDateTime().atZone(zoneId).toLocalDateTime();
    }

    /**
     * Get current LocalDateTime in specified timezone
     * @return Current LocalDateTime in specified timezone
     */
    public static LocalDateTime now() {
        return LocalDateTime.now(zoneId);
    }
} 
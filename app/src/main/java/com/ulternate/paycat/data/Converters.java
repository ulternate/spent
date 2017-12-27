package com.ulternate.paycat.data;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Define converter methods to convert data between data types.
 *
 * This enables the use of data types not supported by the Room Persistence
 * Library (like Dates).
 */

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

}

package com.ulternate.paycat.data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

/**
 * Data holder for the application.
 *
 * Extends RoomDatabase and serves as the access point for the application data.
 */

@Database(entities = {Transaction.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase{

    private static final String DATABASE_NAME = "app-database";
    private static AppDatabase instance;

    public abstract TransactionDao transactionDao();

    /**
     * Get the AppDatabase as a Singleton.
     *
     * @param context: the Context from the activity/thread accessing the database.
     * @return: an AppDatabase instance.
     */
    public static AppDatabase getAppDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME)
                    .build();
        }

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }
}

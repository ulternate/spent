package com.ulternate.paycat.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Data holder for the application.
 *
 * Extends RoomDatabase and serves as the access point for the application data.
 */

@Database(entities = {Transaction.class}, version = 2)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase{

    private static final String DATABASE_NAME = "app-database";
    private static AppDatabase instance;

    public abstract TransactionDao transactionDao();

    /**
     * Migration from version 1 to version 2.
     * Adds two columns, "originalDescription" and "paymentApp"
     */
    private static final Migration MIGRATION1_2 = new Migration(1, 2) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add the new columns to the database.
            database.execSQL("ALTER TABLE transactions "
            + " ADD COLUMN originalDescription TEXT");

            database.execSQL("ALTER TABLE transactions "
            + " ADD COLUMN paymentApp TEXT");
        }
    };

    /**
     * Get the AppDatabase as a Singleton.
     *
     * @param context: the Context from the activity/thread accessing the database.
     * @return an AppDatabase instance.
     */
    public static AppDatabase getAppDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class,
                    DATABASE_NAME)
                    .addMigrations(MIGRATION1_2)
                    .build();
        }

        return instance;
    }

    public static void destroyInstance() {
        instance = null;
    }
}

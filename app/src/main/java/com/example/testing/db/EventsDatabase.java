package com.example.testing.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Events.class}, version = 3)
public abstract class EventsDatabase extends RoomDatabase {

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {

        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Events ADD COLUMN TriggerTimeValue INTEGER");
            database.execSQL("ALTER TABLE Events ADD COLUMN ResetTimeValue INTEGER");
        }
    };
    private static volatile EventsDatabase INSTANCE;

    public static EventsDatabase getInstance(Context context) {
        synchronized (EventsDatabase.class) {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, EventsDatabase.class, "events-db")
                        .addMigrations(MIGRATION_2_3)
                        .build();
            }
        }

        return INSTANCE;
    }

    abstract EventsDao eventsDao();
}

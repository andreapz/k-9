package com.tiscali.appmail.mailstore.migrations;


import android.database.sqlite.SQLiteDatabase;


class MigrationTo52 {
    public static void addMoreMessagesColumnToFoldersTable(SQLiteDatabase db) {
        // old tiscali mail version already include this change
        // db.execSQL("ALTER TABLE folders ADD more_messages TEXT default \"unknown\"");
    }
}

package com.tiscali.appmail.mailstore.migrations;


import com.tiscali.appmail.Account;
import com.tiscali.appmail.mail.Folder;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;


class MigrationTo50 {
    public static void foldersAddNotifyClassColumn(SQLiteDatabase db,
            MigrationsHelper migrationsHelper) {
        try {
            db.execSQL("ALTER TABLE folders ADD notify_class TEXT default '"
                    + Folder.FolderClass.INHERITED.name() + "'");
        } catch (SQLiteException e) {
            if (!e.getMessage().startsWith("duplicate column name:")) {
                throw e;
            }
        }

        ContentValues cv = new ContentValues();
        cv.put("notify_class", Folder.FolderClass.INHERITED.name());

        Account account = migrationsHelper.getAccount();
        db.update("folders", cv, "name = ?", new String[] {account.getInboxFolderName()});
    }
}

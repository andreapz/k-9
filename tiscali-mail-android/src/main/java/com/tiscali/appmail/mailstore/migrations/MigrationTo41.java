package com.tiscali.appmail.mailstore.migrations;


import com.tiscali.appmail.Account;
import com.tiscali.appmail.K9;
import com.tiscali.appmail.helper.Utility;
import com.tiscali.appmail.mail.Folder;
import com.tiscali.appmail.preferences.Storage;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;


class MigrationTo41 {
    public static void db41FoldersAddClassColumns(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE folders ADD integrate INTEGER");
            db.execSQL("ALTER TABLE folders ADD top_group INTEGER");
            db.execSQL("ALTER TABLE folders ADD poll_class TEXT");
            db.execSQL("ALTER TABLE folders ADD push_class TEXT");
            db.execSQL("ALTER TABLE folders ADD display_class TEXT");
        } catch (SQLiteException e) {
            if (!e.getMessage().startsWith("duplicate column name:")) {
                throw e;
            }
        }
    }

    public static void db41UpdateFolderMetadata(SQLiteDatabase db,
            MigrationsHelper migrationsHelper) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT id, name FROM folders", null);
            while (cursor.moveToNext()) {
                try {
                    int id = cursor.getInt(0);
                    String name = cursor.getString(1);
                    update41Metadata(db, migrationsHelper, id, name);
                } catch (Exception e) {
                    Log.e(K9.LOG_TAG, " error trying to ugpgrade a folder class", e);
                }
            }
        } catch (SQLiteException e) {
            Log.e(K9.LOG_TAG,
                    "Exception while upgrading database to v41. folder classes may have vanished",
                    e);
        } finally {
            Utility.closeQuietly(cursor);
        }
    }

    private static void update41Metadata(SQLiteDatabase db, MigrationsHelper migrationsHelper,
            int id, String name) {
        Storage storage = migrationsHelper.getStorage();
        Account account = migrationsHelper.getAccount();
        String accountUuid = account.getUuid();

        Folder.FolderClass displayClass = Folder.FolderClass.NO_CLASS;
        Folder.FolderClass syncClass = Folder.FolderClass.INHERITED;
        Folder.FolderClass pushClass = Folder.FolderClass.INHERITED;
        boolean inTopGroup = false;
        boolean integrate = false;
        if (account.getInboxFolderName().equals(name)) {
            displayClass = Folder.FolderClass.INHERITED;
            syncClass = Folder.FolderClass.INHERITED;
            pushClass = Folder.FolderClass.INHERITED;
            inTopGroup = true;
            integrate = true;
        }

        try {
            displayClass = Folder.FolderClass.valueOf(storage
                    .getString(accountUuid + "." + name + ".displayMode", displayClass.name()));
            syncClass = Folder.FolderClass.valueOf(
                    storage.getString(accountUuid + "." + name + ".syncMode", syncClass.name()));
            pushClass = Folder.FolderClass.valueOf(
                    storage.getString(accountUuid + "." + name + ".pushMode", pushClass.name()));
            inTopGroup = storage.getBoolean(accountUuid + "." + name + ".inTopGroup", inTopGroup);
            integrate = storage.getBoolean(accountUuid + "." + name + ".integrate", integrate);
        } catch (Exception e) {
            Log.e(K9.LOG_TAG, " Throwing away an error while trying to upgrade folder metadata", e);
        }

        if (displayClass == Folder.FolderClass.NONE) {
            displayClass = Folder.FolderClass.NO_CLASS;
        }
        if (syncClass == Folder.FolderClass.NONE) {
            syncClass = Folder.FolderClass.INHERITED;
        }
        if (pushClass == Folder.FolderClass.NONE) {
            pushClass = Folder.FolderClass.INHERITED;
        }

        db.execSQL(
                "UPDATE folders SET integrate = ?, top_group = ?, poll_class=?, push_class =?, display_class = ? WHERE id = ?",
                new Object[] {integrate, inTopGroup, syncClass, pushClass, displayClass, id});
    }
}

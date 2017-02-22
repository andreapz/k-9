package com.tiscali.appmail.provider;

import com.tiscali.appmail.R;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AlertDialog;

/**
 * Created by Annalisa Sini on 17/02/2017.
 */

public class TiscaliSearchRecentSuggestionsProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY =
            "com.tiscali.appmail.provider.TiscaliSearchRecentSuggestionsProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public TiscaliSearchRecentSuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    public static void saveSearch(Context context, String searchQuery) {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(context,
                TiscaliSearchRecentSuggestionsProvider.AUTHORITY,
                TiscaliSearchRecentSuggestionsProvider.MODE);
        suggestions.saveRecentQuery(searchQuery, null);
    }

    public static void showCancelSearchHistoryDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(null);
        builder.setMessage(context.getString(R.string.settings_clear_recent_dialog_message));
        builder.setPositiveButton(R.string.notification_action_delete,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelSearchHistory(context);
                    }
                });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    private static void cancelSearchHistory(Context context) {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(context, AUTHORITY, MODE);
        suggestions.clearHistory();
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

        class Wrapper extends CursorWrapper {
            Wrapper(Cursor c) {
                super(c);
            }

            public String getString(int columnIndex) {
                if (columnIndex != -1
                        && columnIndex == getColumnIndex(SearchManager.SUGGEST_COLUMN_ICON_1))
                    return ("android.resource://" + getContext().getPackageName()
                            + "/drawable/ic_history_black_24dp");

                return super.getString(columnIndex);
            }
        }

        return new Wrapper(super.query(uri, projection, selection, selectionArgs, sortOrder));
    }
}

package com.tiscali.appmail.provider;

import android.content.SearchRecentSuggestionsProvider;

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
}

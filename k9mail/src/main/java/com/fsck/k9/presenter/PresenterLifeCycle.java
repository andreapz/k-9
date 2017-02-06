package com.fsck.k9.presenter;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created by andreaputzu on 12/01/17.
 */

public interface PresenterLifeCycle {

    void onResume();

    void onPause();

    void onDetach();

    void onCreateView();

    void onCreateOptionsMenu(Menu menu, MenuInflater inflater);

    boolean onPrepareOptionsMenu(Menu menu);

    boolean onOptionsItemSelected(MenuItem item);

    void onSaveInstanceState(Bundle outState);

    void setStartInstanceState(Bundle savedInstanceState);

    void onNewIntent(Intent intent);

}

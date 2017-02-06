package com.fsck.k9.activity;

import com.fsck.k9.R;
import com.fsck.k9.helper.CaptivePortalHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by thomascastangia on 02/02/17.
 */

public class CaptivPortalActivity extends K9Activity {

    public static final int REQUEST_CODE_DUMMY = 65000; // 2^16bit int value for a dummy request
                                                        // code

    /**
     * Start the CaptivePortalActivity, optionally for results with the REQUEST_CODE_DUMMY request
     * code
     *
     * @param context the context used to start the ChildActivity. Pass here an Activity if you want
     *        to start it for result with a dummy code.
     */
    public static void startActivity(Context context) {
        if (context != null) {
            Intent intent = new Intent(context, CaptivPortalActivity.class);

            if (context instanceof Activity) {
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // avoid many instances
                ((Activity) context).startActivityForResult(intent, REQUEST_CODE_DUMMY);
            } else {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_captive_portal);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }


    @Override
    protected void onStop() {
        CaptivePortalHelper.getInstance(this).setLoginWebViewAsClosed();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.captive_portal_activity, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_close) {
            finish(); // close
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

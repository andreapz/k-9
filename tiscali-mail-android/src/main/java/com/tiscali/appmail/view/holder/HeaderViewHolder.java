package com.tiscali.appmail.view.holder;

import com.tiscali.appmail.R;
import com.tiscali.appmail.ui.CapitalizedTextView;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by andreaputzu on 21/12/16.
 */

public class HeaderViewHolder extends RecyclerView.ViewHolder {

    public ImageView mSettingsIv;
    public TextView mAccountTv;
    public TextView mAccountDisplayNameTv;
    public ImageView mExpandMenuIconIv;
    public RelativeLayout mAccountContainer;
    public CapitalizedTextView mSectionName;

    public HeaderViewHolder(View itemView) {

        super(itemView);
        mSettingsIv = (ImageView) itemView.findViewById(R.id.settings);
        mExpandMenuIconIv = (ImageView) itemView.findViewById(R.id.expand_menu);
        mAccountDisplayNameTv = (TextView) itemView.findViewById(R.id.account_display_name);
        mAccountTv = (TextView) itemView.findViewById(R.id.account);
        mAccountContainer = (RelativeLayout) itemView.findViewById(R.id.account_container);
        mSectionName = (CapitalizedTextView) itemView.findViewById(R.id.section_name);
    }
}

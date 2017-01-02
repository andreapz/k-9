package com.fsck.k9.view.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fsck.k9.R;

/**
 * Created by andreaputzu on 21/12/16.
 */

public class HeaderViewHolder extends RecyclerView.ViewHolder {

    public ImageView mSettingsIv;
    public TextView mAccountTv;

    public HeaderViewHolder(View itemView) {

        super(itemView);
        mSettingsIv = (ImageView) itemView.findViewById(R.id.settings);
        mAccountTv = (TextView) itemView.findViewById(R.id.account);
    }
}
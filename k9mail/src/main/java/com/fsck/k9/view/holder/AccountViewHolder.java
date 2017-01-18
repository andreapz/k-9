package com.fsck.k9.view.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fsck.k9.R;

/**
 * Created by Annalisa Sini on 16/01/2017.
 */

public class AccountViewHolder extends RecyclerView.ViewHolder {
    public TextView mAccountNameTv;
    public ImageView mAccountIconIv;

    public AccountViewHolder(View itemView) {
        super(itemView);
        mAccountNameTv = (TextView) itemView.findViewById(R.id.account_name);
        mAccountIconIv = (ImageView) itemView.findViewById(R.id.account_icon);
    }

}

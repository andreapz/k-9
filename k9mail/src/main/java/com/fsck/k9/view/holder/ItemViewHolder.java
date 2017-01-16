package com.fsck.k9.view.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fsck.k9.R;

/**
 * Created by andreaputzu on 21/12/16.
 */

public class ItemViewHolder extends RecyclerView.ViewHolder {
    public RelativeLayout mItemContainerRl;
    public TextView mItemTitleTv;
    public TextView mItemActionTv;
    public ImageView mItemIconIv;
    public ImageView mItemToggleIv;

    public ItemViewHolder(View itemView) {
        super(itemView);
        mItemContainerRl = (RelativeLayout) itemView.findViewById(R.id.item_container);
        mItemTitleTv = (TextView) itemView.findViewById(R.id.folder_name);
        mItemActionTv = (TextView) itemView.findViewById(R.id.item_action);
        mItemIconIv = (ImageView) itemView.findViewById(R.id.item_icon);
        mItemToggleIv = (ImageView) itemView.findViewById(R.id.item_toggle);
    }
}

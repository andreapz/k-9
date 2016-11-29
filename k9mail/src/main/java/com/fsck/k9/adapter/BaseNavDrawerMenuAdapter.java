package com.fsck.k9.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fsck.k9.R;

/**
 * Created by Annalisa Sini on 22/11/2016.
 */

public abstract class BaseNavDrawerMenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int HEADER = 0;
    public static final int ITEM = 1;

    Context mContext;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, final int type) {

        View view;
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (type) {
            case HEADER:
                view = inflater.inflate(R.layout.nav_drawer_menu_header, parent, false);
                return new HeaderViewHolder(view);
            case ITEM:
                view = inflater.inflate(R.layout.nav_drawer_menu_item, parent, false);
                return new ItemViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public boolean hasChildren(int position) {
        return getChildrenCount(position) > 0;
    }

    protected int getCollapseIconResource() {
        return R.drawable.ic_expand_less_white_24dp;
    }

    protected int getExpandIconResource() {
        return R.drawable.ic_expand_more_white_24dp;
    }

    // abstract methods
    public abstract int getChildrenCount(int position);

    public abstract int getItemDepth(int position);

    public abstract boolean isItemExpanded(int position);
}

class ItemViewHolder extends RecyclerView.ViewHolder {
    public RelativeLayout mItemContainerRl;
    public TextView mItemTitleTv;
    public ImageView mItemIconIv;
    public ImageView mItemToggleIv;

    public ItemViewHolder(View itemView) {
        super(itemView);
        mItemContainerRl = (RelativeLayout) itemView.findViewById(R.id.item_container);
        mItemTitleTv = (TextView) itemView.findViewById(R.id.item_title);
        mItemIconIv = (ImageView) itemView.findViewById(R.id.item_icon);
        mItemToggleIv = (ImageView) itemView.findViewById(R.id.item_toggle);
    }
}

class HeaderViewHolder extends RecyclerView.ViewHolder {

    public ImageView mSettingsIv;
    public TextView mAccountTv;

    public HeaderViewHolder(View itemView) {

        super(itemView);
        mSettingsIv = (ImageView) itemView.findViewById(R.id.settings);
        mAccountTv = (TextView) itemView.findViewById(R.id.account);
    }
}

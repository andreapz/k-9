package com.fsck.k9.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.fsck.k9.R;

/**
 * Created by Annalisa Sini on 22/11/2016.
 */

public abstract class BaseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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

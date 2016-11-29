package com.fsck.k9.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.fsck.k9.Account;
import com.fsck.k9.R;
import com.fsck.k9.mailstore.LocalFolder;
import com.fsck.k9.model.NavDrawerMenuItem;

import java.util.List;

/**
 * Created by Annalisa Sini on 29/11/2016.
 */

public class MailNavDrawerManuAdapter extends BaseNavDrawerMenuAdapter {

    List<LocalFolder> mItems;

    public MailNavDrawerManuAdapter(List<LocalFolder> data, Context context) {
        this.mContext = context;

        mItems = data;
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof HeaderViewHolder) {
            // TODO
            final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.mAccountTv.setText("Name Surname");
            headerViewHolder.mAccountTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });

            headerViewHolder.mSettingsIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
        } else if (holder instanceof ItemViewHolder) {
            final LocalFolder item = getItem(position);
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            //Icon
            // TODO

            // Title
            if(item.getName() != null) {
                itemViewHolder.mItemTitleTv.setText(item.getName());
            }

            //click listener
            itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //TODO
                }
            });
        }
    }

    @Override
    public int getChildrenCount(int position) {
        return 0;
    }

    @Override
    public int getItemDepth(int position) {
        return 0;
    }

    @Override
    public boolean isItemExpanded(int position) {
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        // without header
//        return ITEM;
        // with header
        return position == 0 ? HEADER : ITEM;
    }

    private LocalFolder getItem(int position) {
        // without header
//        return mVisibleItems.get(position);
        // with header
        return mItems.get(position - 1);
    }

    @Override
    public int getItemCount() {
        // without header
//        return mVisibleItems.size();
        // with header
        return mItems.size() + 1;
    }

}

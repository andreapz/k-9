package com.fsck.k9.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.fsck.k9.Account;
import com.fsck.k9.activity.FolderInfoHolder;
import com.fsck.k9.view.holder.HeaderViewHolder;
import com.fsck.k9.view.holder.ItemViewHolder;

import java.util.List;

/**
 * Created by Annalisa Sini on 29/11/2016.
 */

public class MailNavDrawerMenuAdapter extends BaseNavDrawerMenuAdapter {

    List<FolderInfoHolder> mItems;
    MailAdapterClickListener mMailNavDrawerClickListener;
    Account mAccount;

    public MailNavDrawerMenuAdapter(Account account, List<FolderInfoHolder> data, Context context, MailAdapterClickListener settingsListener) {
        this.mContext = context;
        this.mMailNavDrawerClickListener = settingsListener;
        this.mAccount = account;
        this.mItems = data;
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
                    if(mMailNavDrawerClickListener != null) {
                        mMailNavDrawerClickListener.onSettingsClick();
                    }
                }
            });
        } else if (holder instanceof ItemViewHolder) {
            final FolderInfoHolder item = getItem(position);
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            //Icon
            // TODO

            // Title
            if(item.displayName != null) {
                itemViewHolder.mItemTitleTv.setText(item.displayName);
            }

            //click listener
            itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mMailNavDrawerClickListener.onFolderClick(mAccount, item);
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

    private FolderInfoHolder getItem(int position) {
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

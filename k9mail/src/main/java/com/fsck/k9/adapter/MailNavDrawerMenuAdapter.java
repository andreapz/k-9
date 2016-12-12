package com.fsck.k9.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.fsck.k9.Account;
import com.fsck.k9.R;
import com.fsck.k9.activity.FolderInfoHolder;
import com.fsck.k9.activity.setup.AccountSettings;
import com.fsck.k9.activity.setup.Prefs;
import com.fsck.k9.mailstore.LocalFolder;
import com.fsck.k9.model.NavDrawerMenuItem;

import java.util.List;

/**
 * Created by Annalisa Sini on 29/11/2016.
 */

public class MailNavDrawerMenuAdapter extends BaseNavDrawerMenuAdapter {

    List<FolderInfoHolder> mItems;
    SettingsListener mSettingsListener;

    public MailNavDrawerMenuAdapter(List<FolderInfoHolder> data, Context context, SettingsListener settingsListener) {
        this.mContext = context;
        this.mSettingsListener = settingsListener;
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
                    if(mSettingsListener != null) {
                        mSettingsListener.showSettings();
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

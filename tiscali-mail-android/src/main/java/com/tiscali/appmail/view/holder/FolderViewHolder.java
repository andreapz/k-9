package com.tiscali.appmail.view.holder;

import com.tiscali.appmail.R;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Annalisa Sini on 21/12/2016.
 */

public class FolderViewHolder extends RecyclerView.ViewHolder {
    public TextView mFolderNameTv;
    public TextView mNewMessageCountTv;
    public TextView mFlaggedMessageCountTv;
    public ImageView mFolderIconIv;
    public ImageView mNewMessageCountIconIv;
    public ImageView mFlaggedMessageCountIconIv;
    public View mNewMessageCountWrapperV;
    public View mFlaggedMessageCountWrapperV;

    public FolderViewHolder(View itemView) {
        super(itemView);
        mFolderNameTv = (TextView) itemView.findViewById(R.id.folder_name);
        mNewMessageCountTv = (TextView) itemView.findViewById(R.id.new_message_count);
        mFlaggedMessageCountTv = (TextView) itemView.findViewById(R.id.flagged_message_count);
        mNewMessageCountWrapperV = itemView.findViewById(R.id.new_message_count_wrapper);
        mFlaggedMessageCountWrapperV = itemView.findViewById(R.id.flagged_message_count_wrapper);
        mNewMessageCountIconIv = (ImageView) itemView.findViewById(R.id.new_message_count_icon);
        mFlaggedMessageCountIconIv =
                (ImageView) itemView.findViewById(R.id.flagged_message_count_icon);
        mFolderIconIv = (ImageView) itemView.findViewById(R.id.folder_icon);
    }
}

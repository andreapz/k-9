package com.fsck.k9.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.fsck.k9.R;
import com.fsck.k9.model.NavDrawerMenuItem;
import com.fsck.k9.view.holder.HeaderViewHolder;
import com.fsck.k9.view.holder.ItemViewHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Annalisa Sini on 21/11/2016.
 */

public class NavDrawerMenuAdapter extends BaseNavDrawerMenuAdapter {

    // data set
    private List<NavDrawerMenuItem> mItems;
    private List<NavDrawerMenuItem> mVisibleItems = new ArrayList<>();
    private HashMap<String, Boolean> mItemsOpenStatus = new HashMap<>();
    private HashMap<String, Integer> mItemsDepth = new HashMap<>();


    public NavDrawerMenuAdapter(List<NavDrawerMenuItem> data, Context context) {
        this.mContext = context;
        mItems = data;
        mVisibleItems.addAll(data);
        // init item status hash map. Elements all collapsed
        setItemsStatus(mVisibleItems, false);
        // init depth level
        setItemsDepth(mVisibleItems, -1);
    }

    private void setItemsStatus(List<NavDrawerMenuItem> items, boolean status) {
        for(NavDrawerMenuItem item : items) {
            // items initially collapsed
            mItemsOpenStatus.put(item.getSectionId(), status);
        }
    }

    private void setItemsDepth(List<NavDrawerMenuItem> items, int upperDepth) {
        for(int i=0; i<items.size(); i++) {
            NavDrawerMenuItem item = items.get(i);
            if(!mItemsDepth.containsKey(item.getSectionId())) {
                mItemsDepth.put(item.getSectionId(), upperDepth+1);
            }
        }
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof HeaderViewHolder) {
            // TODO
            final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.mAccountTv.setVisibility(View.VISIBLE);
            headerViewHolder.mAccountTv.setText("Name Surname");

            headerViewHolder.mSettingsIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //nop
                }
            });
        } else if (holder instanceof ItemViewHolder) {
            final NavDrawerMenuItem item = getItem(position);
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;

            //Icon
            if(item.getIconUrl() != null) {
                itemViewHolder.mItemIconIv.setVisibility(View.VISIBLE);
                Glide.with(mContext)
                        .load(item.getIconUrl())
                        .into(itemViewHolder.mItemIconIv);

            } else {
                itemViewHolder.mItemIconIv.setVisibility(View.GONE);
            }

            // add additionally left margin depending on depth
            if(itemViewHolder.mItemContainerRl.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) itemViewHolder.mItemContainerRl.getLayoutParams();
                int marginLeft = (int) (getItemDepth(position)*mContext.getResources().getDimension(R.dimen.margin_standard_16dp));
                p.setMargins(marginLeft, 0, 0, 0);
                itemViewHolder.mItemContainerRl.requestLayout();
            }

            // toggle icon for expandable items
            if(hasChildren(position)) {
                itemViewHolder.mItemToggleIv.setVisibility(View.VISIBLE);
                // expandable item opened
                if(isItemExpanded(position)) {
                    itemViewHolder.mItemToggleIv.setImageResource(getCollapseIconResource());
                } else {
                    itemViewHolder.mItemToggleIv.setImageResource(getExpandIconResource());
                }
            } else {
                itemViewHolder.mItemToggleIv.setVisibility(View.GONE);
            }

            if(item.getTitle() != null) {
                itemViewHolder.mItemTitleTv.setText(item.getTitle());
            }

            //click listener
            itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(hasChildren(position)) {
                        //update status for expandable item (if opened set to close, and vice versa)
                        boolean oldStatus = isItemExpanded(position);
                        boolean newStatus = !oldStatus;
                        mItemsOpenStatus.put(item.getSectionId(), newStatus);

                        // update data set for rendering
                        int childPosition = getFirstChildPosition(position);
                        if(newStatus) { // expandable item just opened
                            mVisibleItems.addAll(childPosition, item.getSections());
                            setItemsStatus(item.getSections(), false);
                            setItemsDepth(item.getSections(), getItemDepth(position));
                        } else { // expandable item just closed
                            // remove all elements with depth > item depth (consider nested children)
                            setItemsStatus(getItemsToHide(position), false);
                            mVisibleItems.removeAll(getItemsToHide(position));
                        }
                        notifyDataSetChanged();
                    } else {
                       //nop
                    }
                }
            });
        }
    }

    @Override
    public int getChildrenCount(int position) {
        return getItem(position).getSections().size();
    }

    @Override
    public int getItemDepth(int position) {
        return mItemsDepth.get(getItem(position).getSectionId());
    }

    @Override
    public boolean isItemExpanded(int position) {
        return mItemsOpenStatus.get(getItem(position).getSectionId());
    }

    private int getFirstChildPosition(int position) {
        // without header
//        return position + 1;
        //with header
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        // without header
//        return ITEM;
        // with header
        return position == 0 ? HEADER : ITEM;
    }

    private NavDrawerMenuItem getItem(int position) {
        // without header
//        return mVisibleItems.get(position);
        // with header
        return mVisibleItems.get(position - 1);
    }

    @Override
    public int getItemCount() {
        // without header
//        return mVisibleItems.size();
        // with header
        return mVisibleItems.size() + 1;
    }

    private List<NavDrawerMenuItem> getItemsToHide(int position) {
        List<NavDrawerMenuItem> itemsToHide = new ArrayList<>();
        // without header
//        int firstItemPosition = 0;
        //with header
        int firstItemPosition = 1;
        for(int i=firstItemPosition; i<getItemCount(); i++) {
            if(getItemDepth(i) > getItemDepth(position)) {
                itemsToHide.add(getItem(i));
            }
        }
        return itemsToHide;
    }
}


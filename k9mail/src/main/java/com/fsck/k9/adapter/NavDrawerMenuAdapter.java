package com.fsck.k9.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fsck.k9.R;
import com.fsck.k9.model.NavDrawerMenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Annalisa Sini on 21/11/2016.
 */

public class NavDrawerMenuAdapter extends BaseAdapter {

    public static final int HEADER = 0;
    public static final int ITEM = 1;

    Context mContext;

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
        setItemsStatus(mVisibleItems);
        // init depth level
        setItemsDepth(mVisibleItems, -1);
    }

    private void setItemsStatus(List<NavDrawerMenuItem> items) {
        // FIXME for items initially opened?
        for(NavDrawerMenuItem item : items) {
            // items initially collapsed
            mItemsOpenStatus.put(item.getSectionId(), false);
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

    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof HeaderViewHolder) {
            // TODO
            final HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            headerViewHolder.mAccountTv.setText("Name Surname");
            headerViewHolder.mAccountTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    headerViewHolder.mAccountTv.setText(" Name Surname clicked");
                }
            });
            headerViewHolder.mSettinsIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
                int marginLeft = (int) (getItemDepth(position)*mContext.getResources().getDimension(R.dimen.nav_drawer_child_margin));
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
                            setItemsStatus(item.getSections());
                            setItemsDepth(item.getSections(), getItemDepth(position));
                        } else { // expandable item just closed
                            // remove all elements with depth > item depth (consider nested children)
                            setItemsStatus(getItemsToHide(position));
                            mVisibleItems.removeAll(getItemsToHide(position));
                        }
                        notifyDataSetChanged();
                    } else {
                        // TODO
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

    public ImageView mSettinsIv;
    public TextView mAccountTv;

    public HeaderViewHolder(View itemView) {

        super(itemView);
        mSettinsIv = (ImageView) itemView.findViewById(R.id.settings);
        mAccountTv = (TextView) itemView.findViewById(R.id.account);
    }
}

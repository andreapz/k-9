package com.fsck.k9.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.fsck.k9.R;
import com.fsck.k9.api.model.TiscaliMenuItem;
import com.fsck.k9.model.NavDrawerMenuItem;

import java.util.List;

/**
 * Created by thomascastangia on 16/01/17.
 */

public class CategoryNewsAdapter extends BaseAdapter {
    public Activity context;
    public LayoutInflater inflater;
    List<TiscaliMenuItem> mNewsCategory;



    public CategoryNewsAdapter(Activity context, List<TiscaliMenuItem> Categories,
                               boolean Dialogue) {
        super();
        this.context = context;
        this.inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.mNewsCategory = Categories;


    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mNewsCategory.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mNewsCategory.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public List<TiscaliMenuItem> getSelectedItmes() {
        return mNewsCategory;
    }

    public class ViewHolder {
        public CheckBox news_button;
        public TextView news_category;
        public RelativeLayout rl_news;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final int pos = position;
        ViewHolder holder;


        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(
                    R.layout.listview_news_dialogue_row, null);

            holder.news_button = (CheckBox) convertView
                    .findViewById(R.id.toggle_news);
            holder.news_category =  (TextView) convertView
                    .findViewById(R.id.category_news);
            holder.rl_news = (RelativeLayout) convertView
                    .findViewById(R.id.row_news);
            holder.news_button.setTag(position);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.news_category.setText(mNewsCategory.get(pos).getTitle());
        final ViewHolder final_Holder = holder;
        if((Boolean) mNewsCategory.get(pos).getVisibility()){
            holder.news_button.setChecked(true);

        }else{
            holder.news_button.setChecked(false);
        }


        holder.news_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                mNewsCategory.get(pos).setVisibility(isChecked);

            }
        });



        return convertView;
    }

}

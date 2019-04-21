package com.example.zmorgan.deafradio;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecyclerStationViewAdapter extends RecyclerView.Adapter<RecyclerStationViewAdapter.MyViewHolder> {

    private boolean search;
    private Context mContext;
    private RecyclerViewClickListener mListener;
    private List<Radio> mData = new ArrayList<Radio>();
    private List<String> mDataCategories = new ArrayList<String>();
    private List<Radio> mDataSortCategories = new ArrayList<Radio>();
    private List<Radio> mDataback = new ArrayList<Radio>();
    private boolean categories = false;
    private String category = "None";
    private String query = "None";


    public RecyclerStationViewAdapter(Context mContext, List<Radio> mData, RecyclerViewClickListener mListener, boolean categories, boolean searcher) {
        //We take in a context, a list of stations, a listener, wether we display categories, searches or just stations.
        this.mContext = mContext;
        this.mDataback = mData;
        //make a copy of the data
        for (Radio r : mData){
            this.mData.add(r);
        }
        this.mListener = mListener;
        this.categories = categories;
        this.search = searcher;
        //Seperate the list by categories if we are displaying them like that
        if (categories){
            for(Radio r : mData){
                if (!mDataCategories.contains(r.getCategory())){
                    mDataCategories.add(r.getCategory());
                }
            }
        }
    }

    public void updateView(){
        notifyDataSetChanged();
    }

    public List getDataSource(){
        return mData;
    }

    public List getDataCategorySource() { return mDataSortCategories;}

    public RecyclerStationViewAdapter setCategory(String category){
        //Sets the category to view, called we a user selects a category
        this.category = category;
        for(Radio r : mData){
            if(r.getCategory() == category){
                mDataSortCategories.add(r);
            }
        }
        return this;
    }

    public void search(String query){
        //Handles searching and displaying stations which match the search text in a substring match.
        boolean newSearch = false;
        if(query == ""){
            mData = mDataback;
        }
        else {
            this.query = query.toLowerCase(Locale.getDefault());
            mData.clear();
            for (Radio r : mDataback) {
                if (r.getTitle().toLowerCase(Locale.getDefault()).contains(query)) {
                    mData.add(r);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.cardview_item_radio, parent, false);
        return new MyViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int  position) {
        //Handles displaying the items
        if(categories) {
            if(category == "None") {
                holder.tv_radio_title.setText(mDataCategories.get(position));
                if(mDataCategories.get(position).equals("User added")){
                    holder.img_radio_thumbnail.setImageResource(R.drawable.ic_favorite_black_24dp);
                }
                else if(mDataCategories.get(position).equals("Music")){
                    holder.img_radio_thumbnail.setImageResource(R.drawable.ic_audiotrack_black_24dp);
                }
                else{
                    holder.img_radio_thumbnail.setImageResource(R.drawable.ic_chat_black_24dp);
                }
            }
            else{
                holder.tv_radio_title.setText(mDataSortCategories.get(position).getTitle());
                holder.img_radio_thumbnail.setImageResource(mDataSortCategories.get(position).getThumbnail());
            }
        }
        else{
            holder.tv_radio_title.setText(mData.get(position).getTitle());
            holder.img_radio_thumbnail.setImageResource(mData.get(position).getThumbnail());
        }



    }

    @Override
    public int getItemCount() {
        if(!categories) {
            return mData.size();
        }
        else{
            if(this.category == "None") {
                return mDataCategories.size();
            }
            else{
                return mDataSortCategories.size();
            }
        }
    }

    public String getCategory(int position){
        return mDataCategories.get(position);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        //Sets the actuals views

        private RecyclerViewClickListener mListener;
        TextView tv_radio_title;
        ImageView img_radio_thumbnail;


        public MyViewHolder(View itemView, RecyclerViewClickListener listener){
            super(itemView);
            mListener = listener;
            itemView.setOnClickListener(this);

            tv_radio_title = (TextView) itemView.findViewById(R.id.radio_title_id);
            img_radio_thumbnail = (ImageView) itemView.findViewById(R.id.radio_img_id);

        }

        @Override
        public void onClick(View view) {
            mListener.onClick(view, getAdapterPosition());
        }
    }

}

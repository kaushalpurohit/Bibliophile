package com.example.myapplication;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

// The adapter class which
// extends RecyclerView Adapter
public class Adapter
        extends RecyclerView.Adapter<Adapter.MyView> {

    // List with String type
    private List<String> titleList  = new ArrayList<>();
    private List<String> imageList = new ArrayList<>();
    private List<String> pageList = new ArrayList<>();
    private List<String> sizeList = new ArrayList<>();
    private Context mContext;

    // View Holder class which
    // extends RecyclerView.ViewHolder
    public class MyView
            extends RecyclerView.ViewHolder {

        // Text View
        TextView textView;
        TextView pageText;
        TextView sizeText;
        ImageView imageView;

        // parameterised constructor for View Holder class
        // which takes the view as a parameter
        public MyView(View view)
        {
            super(view);

            // initialise TextView with id
            textView = (TextView)view
                    .findViewById(R.id.grid_text);
            pageText =  (TextView)view
                    .findViewById(R.id.page_text);
            sizeText =  (TextView)view
                    .findViewById(R.id.size_text);
            imageView = (ImageView) view.findViewById((R.id.grid_image));
        }
    }

    // Constructor for adapter class
    // which takes a list of String type
    public Adapter(Context context, List<String> image, List<String> title, List<String> page, List<String> size)
    {
        this.imageList = image;
        this.titleList = title;
        this.pageList = page;
        this.sizeList = size;
        this.mContext = context;
    }

    // Override onCreateViewHolder which deals
    // with the inflation of the card layout
    // as an item for the RecyclerView.
    @Override
    public MyView onCreateViewHolder(ViewGroup parent,
                                     int viewType)
    {

        // Inflate item.xml using LayoutInflator
        View itemView
                = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.grid_single,
                        parent,
                        false);

        // return itemView
        return new MyView(itemView);
    }

    // Override onBindViewHolder which deals
    // with the setting of different data
    // and methods related to clicks on
    // particular items of the RecyclerView.
    @Override
    public void onBindViewHolder(final MyView holder,
                                 final int position)
    {

        // Set the text of each item of
        // Recycler view with the list items
            holder.textView.setText(titleList.get(position));
            holder.pageText.setText(pageList.get(position));
            holder.sizeText.setText(sizeList.get(position));
            Picasso
                    .with(mContext)
                    .load(imageList.get(position))
                    .fit() // will explain later
                    .into(holder.imageView);
    }

    // Override getItemCount which Returns
    // the length of the RecyclerView.
    @Override
    public int getItemCount()
    {
        return titleList.size();
    }
}

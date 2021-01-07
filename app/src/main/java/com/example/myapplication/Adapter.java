package com.example.myapplication;

import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.view.LayoutInflater;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;

// The adapter class which
// extends RecyclerView Adapter
public class Adapter
        extends RecyclerView.Adapter<Adapter.MyView> {

    // List with String type
    private final List<String> titleList;
    private final List<String> imageList;
    private final List<String> linkList;
    private final Context mContext;

    // View Holder class which
    // extends RecyclerView.ViewHolder
    public static class MyView
            extends RecyclerView.ViewHolder {

        // Text View
        TextView textView;
        ImageView imageView;
        LinearLayout mainLayout;

        // parameterised constructor for View Holder class
        // which takes the view as a parameter
        public MyView(View view)
        {
            super(view);
            textView = view.findViewById(R.id.grid_text);
            imageView = view.findViewById((R.id.grid_image));
            mainLayout = view.findViewById(R.id.mainLayout);
        }
    }

    // Constructor for adapter class
    // which takes a list of String type
    public Adapter(Context context, List<String> image, List<String> title, List<String> link)
    {
        this.imageList = image;
        this.titleList = title;
        this.linkList = link;
        this.mContext = context;
    }

    // Override onCreateViewHolder which deals
    // with the inflation of the card layout
    // as an item for the RecyclerView.
    @NotNull
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
            // holder.pageText.setText(pageList.get(position));
            // holder.sizeText.setText(sizeList.get(position));
            Picasso
                    .with(mContext)
                    .load(imageList.get(position))
                    .fit() // will explain later
                    .into(holder.imageView);
            holder.mainLayout.setOnClickListener(view -> {
                Intent download = new Intent(mContext, DownloadActivity.class);
                download.putExtra("url", linkList.get(position));
                download.putExtra("title", titleList.get(position));
                mContext.startActivity(download);
            });
    }

    // Override getItemCount which Returns
    // the length of the RecyclerView.
    @Override
    public int getItemCount()
    {
        return titleList.size();
    }
}

package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// The adapter class which
// extends RecyclerView Adapter
public class MyBookAdapter extends RecyclerView.Adapter<MyBookAdapter.MyCardView> {

    // List with String type
    private List<String> detailsList  = new ArrayList<>();
    private List<String> imageList = new ArrayList<>();
    private List<String> fileList = new ArrayList<>();
    private Context mContext;

    // View Holder class which
    // extends RecyclerView.ViewHolder
    public class MyCardView
            extends RecyclerView.ViewHolder {

        // Text View
        CardView cat;
        TextView title;
        ImageView image;
        RelativeLayout mainLayout;

        // parameterised constructor for View Holder class
        // which takes the view as a parameter
        public MyCardView(View view) {
            super(view);

            // initialise TextView with id
            cat = view.findViewById(R.id.MyBooksCard);
            title = view.findViewById(R.id.myBookTitle);
            image = view.findViewById(R.id.myBookImage);
            mainLayout = view.findViewById(R.id.MyBooksLayout);

        }
    }

    // Constructor for adapter class
    // which takes a list of String type
    public MyBookAdapter(Context context, List<String> details, List<String> image, List<String> file)
    {
        this.detailsList = details;
        this.mContext = context;
        this.imageList = image;
        this.fileList = file;
    }

    // Override onCreateViewHolder which deals
    // with the inflation of the card layout
    // as an item for the RecyclerView.
    @Override
    public MyCardView onCreateViewHolder(ViewGroup parent,
                                           int viewType)
    {

        // Inflate item.xml using LayoutInflator
        View itemView
                = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.mybooks_layout,
                        parent,
                        false);

        // return itemView
        return new MyCardView(itemView);
    }

    // Override onBindViewHolder which deals
    // with the setting of different data
    // and methods related to clicks on
    // particular items of the RecyclerView.
    @Override
    public void onBindViewHolder(final MyCardView holder,
                                 final int position)
    {

        // Set the text of each item of
        // Recycler view with the list items
        holder.title.setText(detailsList.get(position).substring(0,detailsList.get(position).indexOf('.')));
        try {
            Picasso
                    .with(mContext)
                    .load(imageList.get(position))
                    .fit() // will explain later
                    .into(holder.image);
        }
        catch (IndexOutOfBoundsException e) {
            Log.i("IO", "Recycler imageView index error");
        }
        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                File file = new File(fileList.get(position));
                Uri uri = Uri.fromFile(file);
                intent.setDataAndType(uri, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);;
                try {
                    mContext.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.i("File", "No PDF reader installed!");
                }
            }
        });
    }

    // Override getItemCount which Returns
    // the length of the RecyclerView.
    @Override
    public int getItemCount()
    {
        return detailsList.size();
    }
}

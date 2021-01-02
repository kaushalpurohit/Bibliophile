package com.example.myapplication;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

// The adapter class which
// extends RecyclerView Adapter
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyButtonView> {

    // List with String type
    private List<String> detailsList  = new ArrayList<>();
    private Context mContext;

    // View Holder class which
    // extends RecyclerView.ViewHolder
    public class MyButtonView
            extends RecyclerView.ViewHolder {

        // Text View
        Button cat;

        // parameterised constructor for View Holder class
        // which takes the view as a parameter
        public MyButtonView(View view) {
            super(view);

            // initialise TextView with id
            cat = view.findViewById(R.id.catButton);
        }
    }

    // Constructor for adapter class
    // which takes a list of String type
    public CategoryAdapter(Context context, List<String> details)
    {
        this.detailsList = details;
        this.mContext = context;
    }

    // Override onCreateViewHolder which deals
    // with the inflation of the card layout
    // as an item for the RecyclerView.
    @Override
    public MyButtonView onCreateViewHolder(ViewGroup parent,
                                         int viewType)
    {

        // Inflate item.xml using LayoutInflator
        View itemView
                = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.category,
                        parent,
                        false);

        // return itemView
        return new MyButtonView(itemView);
    }

    // Override onBindViewHolder which deals
    // with the setting of different data
    // and methods related to clicks on
    // particular items of the RecyclerView.
    @Override
    public void onBindViewHolder(final MyButtonView holder,
                                 final int position)
    {

        // Set the text of each item of
        // Recycler view with the list items
        holder.cat.setText(detailsList.get(position));
    }

    // Override getItemCount which Returns
    // the length of the RecyclerView.
    @Override
    public int getItemCount()
    {
        return detailsList.size();
    }
}


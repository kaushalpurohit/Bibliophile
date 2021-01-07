package com.example.myapplication;

import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.LayoutInflater;

import org.jetbrains.annotations.NotNull;

import java.util.List;

// The adapter class which
// extends RecyclerView Adapter
public class textAdapter
        extends RecyclerView.Adapter<textAdapter.MyTextView> {

    // List with String type
    private final List<String> detailsList;

    // View Holder class which
    // extends RecyclerView.ViewHolder
    public static class MyTextView
            extends RecyclerView.ViewHolder {

        // Text View
        TextView textView;

        // parameterised constructor for View Holder class
        // which takes the view as a parameter
        public MyTextView(View view) {
            super(view);

            // initialise TextView with id
            textView = view.findViewById(R.id.details);
        }
    }

    // Constructor for adapter class
    // which takes a list of String type
    public textAdapter(List<String> details)
    {
        this.detailsList = details;
    }

    // Override onCreateViewHolder which deals
    // with the inflation of the card layout
    // as an item for the RecyclerView.
    @NotNull
    @Override
    public MyTextView onCreateViewHolder(ViewGroup parent,
                                     int viewType)
    {

        // Inflate item.xml using LayoutInflator
        View itemView
                = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.text_layout,
                        parent,
                        false);

        // return itemView
        return new MyTextView(itemView);
    }

    // Override onBindViewHolder which deals
    // with the setting of different data
    // and methods related to clicks on
    // particular items of the RecyclerView.
    @Override
    public void onBindViewHolder(final MyTextView holder,
                                 final int position)
    {

        // Set the text of each item of
        // Recycler view with the list items
        holder.textView.setText(detailsList.get(position));
    }

    // Override getItemCount which Returns
    // the length of the RecyclerView.
    @Override
    public int getItemCount()
    {
        return detailsList.size();
    }
}

package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListViewAdapter extends BaseAdapter {

    Context mContext;
    LayoutInflater inflater;
    private final List<Suggestions> suggestions;
    private final ArrayList<Suggestions> arrayList;

    public ListViewAdapter(Context context, List<Suggestions> suggestions) {
        mContext = context;
        this.suggestions = suggestions;
        inflater = LayoutInflater.from(mContext);
        this.arrayList = new ArrayList<>();
        this.arrayList.addAll(suggestions);
    }
    public static class ViewHolder {
        TextView name;
    }

    @Override
    public int getCount() {
        return suggestions.size();
    }

    @Override
    public Suggestions getItem(int position) {
        return suggestions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.list_view_items, null);
            // Locate the TextViews in listview_item.xml
            holder.name = (TextView) view.findViewById(R.id.name);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.name.setText(suggestions.get(position).getSuggestions());
        holder.name.setOnClickListener(v -> {
            SearchView searchView = ((Activity) mContext).findViewById(R.id.searchBook);
            String query = suggestions.get(position).getSuggestions();
            searchView.setQuery(query, true);
        });
        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase();
        suggestions.clear();
        if (charText.length() == 0) {
            suggestions.addAll(arrayList);
            Log.i("suggestion", arrayList.toString());
        } else {
            Log.i("suggestion", "else");
            for (int i = 0; i < arrayList.size(); i++) {
                Suggestions wp = arrayList.get(i);
                if (wp.getSuggestions().toLowerCase().contains(charText)) {
                    suggestions.add(wp);
                    Log.i("suggestion", wp.getSuggestions());
                }
            }
        }
        notifyDataSetChanged();
    }

}
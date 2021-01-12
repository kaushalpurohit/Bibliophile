package com.example.myapplication;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Search extends AppCompatActivity {
    private List<String> titleList = new ArrayList<>();
    private List<String> linkList = new ArrayList<>();
    private List<String> imageList = new ArrayList<>();
    private final List<Suggestions> searchList = new ArrayList<>();
    private ListView list;
    private ListViewAdapter adapter;
    private ShimmerFrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = findViewById(R.id.searchToolbar);
        setSupportActionBar(toolbar);
        list = (ListView) findViewById(R.id.list_view);
        addSuggestions();
        adapter = new ListViewAdapter(this, searchList);
        list.setAdapter(adapter);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        search();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public void addSuggestions() {
        String[] suggestions = new String[]{
                "The Da Vinci code", "The alchemist", "Bloodline", "Memory man", "I Feel Bad About My Neck"
                , "Broken Glass", "The Girl With the Dragon Tattoo", "Harry Potter",
                "A Little Life", "Chronicles: Volume One", "The Tipping Point", "Darkmans", "The Siege",
                "Light", "Visitation", "Bad Blood", "The Cost of Living", "Gone Girl", "Underland",
                "Small Island", "Brooklyn", "Night Watch", "Moneyball", "Atonement", "The Year of Magical Thinking"
                , "The Line of Beauty", "The Green Road", "Normal People", "Life After Life", "The Shock Doctrine"
                , "The Road", "The Sixth Extinction", "My Brilliant Friend", "Never Let Me Go", "Wolf Hall",
                "Origin", "Angels & Demons", "Inferno", "Digital Fortress", "Deception Point", "If Tomorrow Comes",
                "Nothing Lasts Forever", "The Doomsday Conspiracy", "The Silent Widow"
        };
        Arrays.sort(suggestions);
        for(String suggestion : suggestions) {
            Suggestions suggestionText = new Suggestions(suggestion);
            searchList.add(suggestionText);
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
            finish();
            return true;
    }

    public void search(){
        SearchView text = findViewById(R.id.searchBook);
        int id = text.getContext()
                .getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        EditText searchEditText = text.findViewById(id);
        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setHintTextColor(getResources().getColor(R.color.white));
        text.requestFocus();
        text.setMaxWidth(Integer.MAX_VALUE);
        text.setIconified(false);
        text.setFocusable(true);
        int hintId =  text.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
        TextView textView = (TextView) text.findViewById(hintId);
        textView.setTextColor(Color.WHITE);
        textView.setHintTextColor(Color.GRAY);
        text.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String search) {
                list.setVisibility(View.GONE);
                container = findViewById(R.id.shimmer_view_container_search);
                container.setVisibility(View.VISIBLE);
                container.startShimmer();
                RecyclerView searchRecycler = findViewById(R.id.searchRecycler);
                searchRecycler.setAdapter(null);
                searchRecycler.setVisibility(View.VISIBLE);
                int cacheSize = 10 * 1024 * 1024;
                File httpCacheDirectory = new File(getApplicationContext().getCacheDir(), "http-cache");
                Cache cache = new Cache(httpCacheDirectory, cacheSize);
                String url = "https://bookdl-api.herokuapp.com/search?book=" + search;
                OkHttpClient client = new OkHttpClient.Builder()
                        .addNetworkInterceptor(new CacheInterceptor())
                        .cache(cache)
                        .build();
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            final String myResponse = Objects.requireNonNull(response.body()).string();
                            Search.this.runOnUiThread(() -> {
                                try {
                                    for(int i=1; i<11; i++) {
                                        JSONObject res = (new JSONObject(myResponse)).getJSONObject(String.valueOf(i));
                                        String title = res.getString("Title");
                                        String link = res.getString("Link");
                                        String image = res.getString("image");
                                        titleList.add(title);
                                        linkList.add(link);
                                        imageList.add(image);
                                    }
                                    container.stopShimmer();
                                    container.setVisibility(View.GONE);
                                    initializeRecyclerView();
                                }
                                catch (Exception e){
                                    Log.i("exception", "1", e);
                                }
                            });
                        }

                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                container = findViewById(R.id.shimmer_view_container_search);
                container.stopShimmer();
                container.setVisibility(View.GONE);
                list.setVisibility(View.VISIBLE);
                RecyclerView searchRecycler= findViewById(R.id.searchRecycler);
                searchRecycler.setAdapter(null);
                searchRecycler.setVisibility(View.GONE);
                try {
                    searchRecycler.removeItemDecorationAt(0);
                }
                catch (IndexOutOfBoundsException e) {
                    Log.i("IO", "Search recycler index error");
                }
                adapter.filter(newText);
                return false;
            }
        });

    }

    public void initializeRecyclerView() {
        Adapter adapter = new Adapter(Search.this, imageList, titleList, linkList);
        RecyclerView recyclerView = findViewById(R.id.searchRecycler);
        try {
            recyclerView.removeItemDecorationAt(0);
        }
        catch (IndexOutOfBoundsException e) {
            Log.i("IO", "Search recycler index error");
        }
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(Search.this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        int spanCount = 2;
        int spacing = getResources().getDimensionPixelSize(R.dimen._34sdp);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        recyclerView.setAdapter(adapter);
        titleList = new ArrayList<>();
        linkList = new ArrayList<>();
        imageList = new ArrayList<>();
    }

}

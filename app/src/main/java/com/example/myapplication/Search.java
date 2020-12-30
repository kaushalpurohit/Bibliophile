package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SearchRecentSuggestionsProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Search extends AppCompatActivity {
    private List<String> titleList = new ArrayList<>();
    private List<String> linkList = new ArrayList<>();
    private List<String> imageList = new ArrayList<>();
    private List<String> sizeList = new ArrayList<>();
    private List<String> pageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }


    @Override
    public boolean onSupportNavigateUp() {
            finish();
            return true;
    }

    public boolean onCreateOptionsMenu(Menu menu){
        ProgressBar spinner=(ProgressBar)findViewById(R.id.loading);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        MenuItem search_item = menu.findItem(R.id.search);
        SearchView text = (SearchView) search_item.getActionView();
        text.requestFocus();
        text.setMaxWidth(Integer.MAX_VALUE);
        text.setIconified(false);
        text.setFocusable(true);
        text.setQueryHint("search");
        text.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String search) {
                RecyclerView searchRecycler = (RecyclerView) findViewById(R.id.searchRecycler);
                searchRecycler.setAdapter(null);
                spinner.setVisibility(View.VISIBLE);
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
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            final String myResponse = response.body().string();
                            Search.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        for(int i=1; i<11; i++) {
                                            JSONObject res = (new JSONObject(myResponse)).getJSONObject(String.valueOf(i));
                                            String title = res.getString("Title");
                                            String link = res.getString("Link");
                                            String image = res.getString("image");
                                            String size = res.getString("size");
                                            String page = res.getString("page");
                                            titleList.add(title);
                                            linkList.add(link);
                                            imageList.add(image);
                                            sizeList.add(size);
                                            pageList.add(page);
                                        }
                                        spinner.setVisibility(View.GONE);
                                        Adapter adapter = new Adapter(Search.this, imageList, titleList, pageList, sizeList, linkList);
                                        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.searchRecycler);
                                        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(Search.this, 2);
                                        recyclerView.setLayoutManager(mLayoutManager);
                                        recyclerView.setAdapter(adapter);
                                        titleList = new ArrayList<>();
                                        linkList = new ArrayList<>();
                                        imageList = new ArrayList<>();
                                        sizeList = new ArrayList<>();
                                        pageList = new ArrayList<>();
                                    }
                                    catch (Exception e){
                                        Log.i("exception", "1", e);
                                    }
                                }
                            });
                        }
                    }
                });
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                spinner.setVisibility(View.GONE);
                RecyclerView searchRecycler=(RecyclerView) findViewById(R.id.searchRecycler);
                searchRecycler.setAdapter(null);
                return false;
            }
        });

        return true;
    }

}

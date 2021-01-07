package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Category extends AppCompatActivity {
    private String title = "";
    private List<String> titleList = new ArrayList<>();
    private List<String> linkList = new ArrayList<>();
    private List<String> imageList = new ArrayList<>();
    private ShimmerFrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        Toolbar toolbar = findViewById(R.id.searchToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView categoryTitle = findViewById(R.id.categoryTitle);
        categoryTitle.setText(title);
        search();
        showBooks();
    }

    public void search() {
        SearchView text = findViewById(R.id.searchBook);
        text.setIconifiedByDefault(true);
        text.setQueryHint("search");
        text.setOnSearchClickListener(V -> {
            text.setIconified(true);
            Intent search = new Intent(Category.this, Search.class);
            search.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(search);
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public void showBooks(){
        container = findViewById(R.id.shimmer_view_container_search);
        container.setVisibility(View.VISIBLE);
        container.startShimmer();
        RecyclerView searchRecycler = findViewById(R.id.searchRecycler);
        searchRecycler.setAdapter(null);
        int cacheSize = 10 * 1024 * 1024;
        File httpCacheDirectory = new File(getApplicationContext().getCacheDir(), "http-cache");
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
        String url = "https://bookdl-api.herokuapp.com/search?book=" + title;
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new CacheInterceptor())
                .cache(cache)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call,@NotNull IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = Objects.requireNonNull(Objects.requireNonNull(response.body()).string());
                    Category.this.runOnUiThread(() -> {
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
    }
    public void initializeRecyclerView() {
        Adapter adapter = new Adapter(Category.this, imageList, titleList, linkList);
        RecyclerView recyclerView = findViewById(R.id.searchRecycler);
        try {
            recyclerView.removeItemDecorationAt(0);
        }
        catch (IndexOutOfBoundsException e) {
            Log.i("IO", "Search recycler index error");
        }
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(Category.this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        int spanCount = 2;
        int spacing = getResources().getDimensionPixelSize(R.dimen._34sdp);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        recyclerView.setAdapter(adapter);
        titleList = new ArrayList<>();
        linkList = new ArrayList<>();
        imageList = new ArrayList<>();
        recyclerView.setVisibility(View.VISIBLE);
    }
}
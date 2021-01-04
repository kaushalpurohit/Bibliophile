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

public class Category extends AppCompatActivity {
    private String title = "";
    private List<String> titleList = new ArrayList<>();
    private List<String> linkList = new ArrayList<>();
    private List<String> imageList = new ArrayList<>();
    private List<String> sizeList = new ArrayList<>();
    private List<String> pageList = new ArrayList<>();
    private ShimmerFrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Intent intent = getIntent();
        title = intent.getStringExtra("title");
        Toolbar toolbar = findViewById(R.id.searchToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView categoryTitle = findViewById(R.id.categoryTitle);
        categoryTitle.setText(title);
        search();
        showBooks();
    }

    public boolean search() {
        SearchView text = findViewById(R.id.searchBook);
        text.setIconifiedByDefault(true);
        text.setQueryHint("search");
        text.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                text.setIconified(true);
                Intent search = new Intent(Category.this, Search.class);
                search.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(search);
            }
        });
        return true;
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
        container = (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container_search);
        container.setVisibility(View.VISIBLE);
        container.startShimmer();
        RecyclerView searchRecycler = (RecyclerView) findViewById(R.id.searchRecycler);
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
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = response.body().string();
                    Category.this.runOnUiThread(new Runnable() {
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
                                container.stopShimmer();
                                container.setVisibility(View.GONE);
                                Adapter adapter = new Adapter(Category.this, imageList, titleList, pageList, sizeList, linkList);
                                RecyclerView recyclerView = (RecyclerView)findViewById(R.id.searchRecycler);
                                try {
                                    recyclerView.removeItemDecorationAt(0);
                                }
                                catch (IndexOutOfBoundsException e) {
                                    Log.i("IO", "Search recycler index error");
                                }
                                RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(Category.this, 2);
                                recyclerView.setLayoutManager(mLayoutManager);
                                int spanCount = 2; // 2 columns
                                int spacing = 120; // 100px
                                boolean includeEdge = true;
                                recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
                                recyclerView.setAdapter(adapter);
                                titleList = new ArrayList<>();
                                linkList = new ArrayList<>();
                                imageList = new ArrayList<>();
                                sizeList = new ArrayList<>();
                                pageList = new ArrayList<>();
                                recyclerView.setVisibility(View.VISIBLE);
                            }
                            catch (Exception e){
                                Log.i("exception", "1", e);
                            }
                        }
                    });
                }
            }
        });
    }
}
package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    private List<String> titleList = new ArrayList<>();
    private List<String> linkList = new ArrayList<>();
    private List<String> imageList = new ArrayList<>();
    private List<String> sizeList = new ArrayList<>();
    private List<String> pageList = new ArrayList<>();
    private ShimmerFrameLayout container;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SplashScreenTheme);
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_MyApplication);
        setContentView(R.layout.activity_main);
        container = (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container);
        container.startShimmer();
        createCategories();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        MenuItem search_item = menu.findItem(R.id.search);
        SearchView text = (SearchView) search_item.getActionView();
        text.setIconifiedByDefault(true);
        text.setQueryHint("search");
        text.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V){
                text.setIconified(true);
                Intent search = new Intent(MainActivity.this, Search.class);
                search.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(search);
            }
        });
        return true;
    }

    public void createCategories() {
        String url = "https://bookdl-api.herokuapp.com/home";
        int cacheSize = 10 * 1024 * 1024;
        File httpCacheDirectory = new File(getApplicationContext().getCacheDir(), "http-cache");
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
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
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                int val[] = new int[5];
                                int rec[] = new int[5];
                                for(int i=0; i<5; i++) {
                                    Log.i("id", "id/title"+(i+1));
                                    val[i] = getResources().getIdentifier("id/title"+(i+1), null, getPackageName());
                                    rec[i] = getResources().getIdentifier("id/recyclerview"+(i+1), null, getPackageName());
                                }
                                for(int i=0; i<5; i++) {
                                    JSONObject res = (new JSONObject(myResponse)).getJSONObject(String.valueOf(i));
                                    Iterator<String> keys = res.keys();
                                    String property = "";
                                    property = keys.next();
                                    TextView title1 = findViewById(val[i]);
                                    title1.setText(property);
                                    JSONObject details = res.getJSONObject(property);
                                    for (int j = 0; j < 4; j++ ) {
                                        try {
                                            JSONObject index = details.getJSONObject(String.valueOf(j));
                                            titleList.add(index.getString("title"));
                                            linkList.add(index.getString("link"));
                                            imageList.add(index.getString("image"));
                                            sizeList.add(index.getString("size"));
                                            pageList.add(index.getString("page"));
                                        }
                                        catch (Exception e) {
                                            Log.i("Exception", "Length");
                                        }
                                    }
                                    Adapter adapter = new Adapter(MainActivity.this, imageList, titleList, pageList, sizeList, linkList);
                                    RecyclerView recyclerView = (RecyclerView)findViewById(rec[i]);
                                    LinearLayoutManager RecyclerViewLayoutManager = new LinearLayoutManager(getApplicationContext());
                                    LinearLayoutManager HorizontalLayout = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
                                    recyclerView.setLayoutManager(HorizontalLayout);
                                    recyclerView.setAdapter(adapter);
                                    titleList = new ArrayList<>();
                                    imageList = new ArrayList<>();
                                    linkList = new ArrayList<>();
                                }
                                container.stopShimmer();
                                container.setVisibility(View.GONE);
                                ScrollView homeLayout = (ScrollView) findViewById(R.id.scroll_view);
                                homeLayout.setVisibility(View.VISIBLE);
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
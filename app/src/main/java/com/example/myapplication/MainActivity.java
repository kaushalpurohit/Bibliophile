package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.navigation.NavigationView;

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

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private List<String> titleList = new ArrayList<>();
    private List<String> linkList = new ArrayList<>();
    private List<String> imageList = new ArrayList<>();
    private List<String> sizeList = new ArrayList<>();
    private List<String> pageList = new ArrayList<>();
    private ShimmerFrameLayout container;
    private List<String> category = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SplashScreenTheme);
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_MyApplication);
        setContentView(R.layout.activity_main);
        container = (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container);
        container.startShimmer();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        createCategoryButtons();
        createCategories();
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE);

        if (permission == PackageManager.PERMISSION_DENIED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{WRITE_EXTERNAL_STORAGE,
                            READ_EXTERNAL_STORAGE},
                    1
            );
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        SearchView text = (SearchView) findViewById(R.id.barSearch);
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
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch(item.getItemId()){
            case R.id.about:
                Log.i("Drawer", "about");
                break;
            case R.id.books:
                Intent myBooks = new Intent(MainActivity.this, MyBooks.class);
                myBooks.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(myBooks);
        }

        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
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
                                    try {
                                        recyclerView.removeItemDecorationAt(0);
                                    }
                                    catch (IndexOutOfBoundsException e) {
                                        Log.i("IO", "Search recycler index error");
                                    }
                                    int spanCount = 2; // 2 columns
                                    int spacing = 80; // 50px
                                    boolean includeEdge = true;
                                    recyclerView.addItemDecoration(new GridSpacingItemDecoration(5, spacing, includeEdge));
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
    public void createCategoryButtons() {
        category.add("Sci-Fi");
        category.add("Horror");
        category.add("Romance");
        category.add("Suspense");
        category.add("Thriller");
        CategoryAdapter adapter = new CategoryAdapter(MainActivity.this, category);
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.category);
        LinearLayoutManager HorizontalLayout = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(HorizontalLayout);
        recyclerView.setAdapter(adapter);
    }
}
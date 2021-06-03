package com.example.myapplication;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.Manifest.permission.MANAGE_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener{

    private  List<String> titleList = new ArrayList<>();
    private  List<String> linkList = new ArrayList<>();
    private  List<String> imageList = new ArrayList<>();
    public View root;
    private ShimmerFrameLayout container;
    private  final List<String> category = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SplashScreenTheme);
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_MyApplication);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        createCategoryButtons();
        root = getWindow().getDecorView().findViewById(R.id.main);
        container = findViewById(R.id.shimmer_view_container);
        // container.setVisibility(View.VISIBLE);
        container.startShimmer();
        createCategories();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this,
                                                             WRITE_EXTERNAL_STORAGE);
        if (permission == PackageManager.PERMISSION_DENIED ){
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    new String[]{WRITE_EXTERNAL_STORAGE,
                            READ_EXTERNAL_STORAGE},
                    1
            );
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                startActivityForResult(intent, 2);
            }
        }
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
        SearchView text = findViewById(R.id.barSearch);
        text.setIconifiedByDefault(true);
        text.setQueryHint("search");
        text.setOnSearchClickListener(V -> {
            text.setIconified(true);
            Intent search = new Intent(MainActivity.this, Search.class);
            search.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(search);
        });
        return true;
    }

    @SuppressLint("NonConstantResourceId")
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
                break;
            case R.id.update:
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            checkForUpdates();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
                break;
        }
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
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = Objects.requireNonNull(response.body()).string();
                    MainActivity.this.runOnUiThread(() -> {
                        try {
                            int[] titleID = new int[5];
                            int[] recyclerID = new int[5];
                            for(int i=0; i<5; i++) {
                                Log.i("id", "id/title"+(i+1));
                                titleID[i] = getResources().getIdentifier("id/title"+(i+1), null, getPackageName());
                                recyclerID[i] = getResources().getIdentifier("id/recyclerview"+(i+1), null, getPackageName());
                            }
                            for(int i=0; i<5; i++) {
                                JSONObject res = (new JSONObject(myResponse)).getJSONObject(String.valueOf(i));
                                initializeRecyclerView(res, titleID, recyclerID, i);
                            }
                            container.stopShimmer();
                            container.setVisibility(View.GONE);
                            ScrollView homeLayout = findViewById(R.id.scroll_view);
                            homeLayout.setVisibility(View.VISIBLE);
                        }
                        catch (Exception e){
                            Log.i("exception", "1", e);
                        }
                    });
                }
            }
        });
    }

    public void initializeRecyclerView(JSONObject res,
                                       int[] val,
                                       int[] rec,
                                       int i) throws JSONException {
        Iterator<String> keys = res.keys();
        String property = keys.next();
        TextView title1 = findViewById(val[i]);
        title1.setText(property);
        JSONObject details = res.getJSONObject(property);
        for (int j = 0; j < 4; j++) {
            try {
                JSONObject index = details.getJSONObject(String.valueOf(j));
                titleList.add(index.getString("title"));
                linkList.add(index.getString("link"));
                imageList.add(index.getString("image"));
            } catch (Exception e) {
                Log.i("Exception", "Length");
            }
        }
        Adapter adapter = new Adapter(MainActivity.this, imageList, titleList, linkList);
        RecyclerView recyclerView = findViewById(rec[i]);
        LinearLayoutManager HorizontalLayout = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(HorizontalLayout);
        try {
            recyclerView.removeItemDecorationAt(0);
        } catch (IndexOutOfBoundsException e) {
            Log.i("IO", "Search recycler index error");
        }
        int spanCount = 4;
        int spacing = getResources().getDimensionPixelSize(R.dimen._28sdp);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        recyclerView.setAdapter(adapter);
        titleList = new ArrayList<>();
        imageList = new ArrayList<>();
        linkList = new ArrayList<>();
    }

    public void createCategoryButtons() {
        category.add("Sci-Fi");
        category.add("Horror");
        category.add("Romance");
        category.add("Suspense");
        category.add("Thriller");
        category.add("Mystery");
        CategoryAdapter adapter = new CategoryAdapter(MainActivity.this, category);
        RecyclerView recyclerView = findViewById(R.id.category);
        LinearLayoutManager HorizontalLayout = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(HorizontalLayout);
        recyclerView.setAdapter(adapter);
    }

    public void checkForUpdates() {
        try {
            View nav = (View) findViewById(R.id.nav_view);
            showSnackBar(nav, "Checking for updates");
            URL url = new URL("https://github.com/kaushalpurohit/Files/blob/master/version.txt?raw=true");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String content = in.readLine();
            if (content.equals(BuildConfig.VERSION_NAME)) {
                Log.i("Content", "True");
                showSnackBar(nav, "App is up to date!");
            }
            else {
                Log.i("Content", "False");
                showSnackBar(nav, "Downloading update");
                update();
            }
        }
        catch (Exception e) {
            Log.i("Content", e.toString());
        }

    }

    public void showSnackBar(View rootView, String text) {
        Snackbar snackBar = Snackbar .make(rootView, text, Snackbar.LENGTH_LONG);
        snackBar.setBackgroundTint(Color.parseColor("#2F0743"));
        snackBar.setTextColor(Color.WHITE);
        snackBar.show();
    }

    public void update() {
        String url = "https://github.com/kaushalpurohit/Files/blob/master/Bibliophile.apk?raw=true";
        Request request = new Request.Builder()
                .url(url)
                .build();
        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Failed to download file: " + response);
            }
            InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
            byte[] buff = new byte[1024 * 4];
            String fileName = "Bibliophile.apk";
            String dirPath = Environment.getExternalStorageDirectory() + File.separator + "Books";
            File file = new File(dirPath);
            if (!file.exists()) {
                boolean result = file.mkdirs();
                Log.i("File", String.valueOf(result));
            }
            String path = file.getAbsolutePath();
            File apk = new File(path, fileName);
            OutputStream output = new FileOutputStream(apk);
            while (true) {
                int read = inputStream.read(buff);
                if (read == -1) {
                    break;
                }
                output.write(buff, 0, read); // write buff
            }
            Log.i("Download", "File written!");
            output.flush();
            output.close();
            showSnackBar(root, "Installing update");
            Uri packageURI = Uri.parse("Bibliophile");
            Intent install = new Intent(Intent.ACTION_VIEW, packageURI);
            Uri uri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID +".provider",
                    apk
            );
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            Log.i("Download", uri.toString());
            install.setDataAndType(uri, "application/vnd.android.package-archive");
            startActivity(install);
        }
        catch (Exception e) {
            Log.i("IOException", e.toString());
        }
    }
}
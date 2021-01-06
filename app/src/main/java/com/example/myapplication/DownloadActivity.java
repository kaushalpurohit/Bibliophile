package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.security.spec.EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class DownloadActivity extends AppCompatActivity {
    private String link = "";
    private String title = "";
    private String download_url = "";
    private String finalDownloadUrl = "";
    private List<String> infoList = new ArrayList<>();
    private List<String> tagList = new ArrayList<>();
    private List<String> titleList = new ArrayList<>();
    private List<String> linkList = new ArrayList<>();
    private List<String> imageList = new ArrayList<>();
    private List<String> sizeList = new ArrayList<>();
    private List<String> pageList = new ArrayList<>();
    private ShimmerFrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        Toolbar toolbar = findViewById(R.id.searchToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        search();
        Intent intent = getIntent();
        link = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
        container = (ShimmerFrameLayout) findViewById(R.id.shimmer_view_container_download);
        container.startShimmer();
        checkIfFileExists();
        displayData();
        Button downloadButton = findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download(v);
            }
        });
        preview();
        share();
    }
    public void firstRun(){
        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = wmbPreference.getBoolean("FIRSTRUN", true);
        if (isFirstRun)
        {
            // Code to run once
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean("FIRSTRUN", false);
            //editor.commit();
            editor.apply();
            showcase();
        }
    }

    public void showcase(){
        ViewTarget target = new ViewTarget(findViewById(R.id.download_image));
        new ShowcaseView.Builder(this)
                .setTarget(target)
                .setContentTitle("New feature!")
                .setContentText("Tap on the image to read the book online.")
                .hideOnTouchOutside()
                .build()
        .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public void preview() {
        ImageView image = findViewById(R.id.download_image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackBar = Snackbar .make(v, "Please wait", Snackbar.LENGTH_SHORT);
                snackBar.setBackgroundTint(Color.parseColor("#2F0743"));
                snackBar.setTextColor(Color.WHITE);
                snackBar.show();
                int cacheSize = 10 * 1024 * 1024;
                File httpCacheDirectory = new File(getApplicationContext().getCacheDir(), "http-cache");
                Cache cache = new Cache(httpCacheDirectory, cacheSize);
                String url = "https://bookdl-api.herokuapp.com/download?url=" + download_url;
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
                            DownloadActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject res = new JSONObject(myResponse);
                                        //String finalDownloadUrl = res.getString("link");
                                        finalDownloadUrl = res.getString("link");
                                        Log.i("finalUrl", finalDownloadUrl);
                                        Intent preview = new Intent(DownloadActivity.this, Preview.class);
                                        preview.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        preview.putExtra("link", finalDownloadUrl);
                                        startActivity(preview);
                                    } catch (Exception e) {
                                        Log.i("exception", "1", e);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    public boolean search() {
        SearchView text = findViewById(R.id.searchBook);
        text.setIconifiedByDefault(true);
        text.setQueryHint("search");
        text.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                text.setIconified(true);
                Intent search = new Intent(DownloadActivity.this, Search.class);
                search.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(search);
            }
        });
        return true;
    }

    public void checkIfFileExists(){
        String dirPath = Environment.getExternalStorageDirectory() + File.separator + "Books" ;
        dirPath = dirPath + File.separator + title + ".pdf";
        File file = new File(dirPath);
        if (file.exists()) {
            Button downloadButton = (Button) findViewById(R.id.downloadButton);
            Button readButton = (Button) findViewById(R.id.readButton);
            downloadButton.setVisibility(View.INVISIBLE);
            readButton.setVisibility(View.VISIBLE);
            readButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.fromFile(file);
                    intent.setDataAndType(uri, "application/pdf");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);;
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Snackbar snackBar = Snackbar .make(v, "Install a PDF reader!", Snackbar.LENGTH_SHORT);
                        snackBar.setBackgroundTint(Color.parseColor("#2F0743"));
                        snackBar.setTextColor(Color.WHITE);
                        snackBar.show();
                    }
                }
            });
        }
    }


    public void displayData() {
        String url = "https://bookdl-api.herokuapp.com/download_page?url=" + link;
        TextView textView = (TextView) findViewById(R.id.download_title);
        textView.setText(title);
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
                    DownloadActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject res = new JSONObject(myResponse);
                                String image = res.getString("image");
                                Log.i("image", image);
                                ImageView imageView = (ImageView) findViewById(R.id.download_image);
                                Picasso
                                        .with(DownloadActivity.this)
                                        .load(image)
                                        .fit() // will explain later
                                        .into(imageView);
                                download_url  = res.getString("download_url");
                                JSONArray info= res.getJSONArray("info");
                                JSONArray tags = res.getJSONArray("tags");
                                for (int i = 0; i<4; i++) {
                                    infoList.add(info.getString(i));
                                }
                                for(int i = 0; i<4; i++){
                                    try{
                                        tagList.add(tags.getString(i));
                                    }
                                    catch (Exception e){
                                        Log.i("Exception", "Length");
                                    }
                                }
                                JSONObject similarBooks = res.getJSONObject("similar_books");
                                for (int i = 0; i < 7; i++ ) {
                                    try {
                                        JSONObject index = similarBooks.getJSONObject(String.valueOf(i));
                                        titleList.add(index.getString("title"));
                                        linkList.add(index.getString("url"));
                                        imageList.add(index.getString("image"));
                                        sizeList.add(index.getString("size"));
                                        pageList.add(index.getString("pages"));
                                    }
                                    catch (Exception e) {
                                        Log.i("Exception", e.toString());
                                    }
                                }
                                Adapter adapter = new Adapter(DownloadActivity.this, imageList, titleList, pageList, sizeList, linkList);
                                RecyclerView recyclerView = (RecyclerView) findViewById(R.id.similar_books);
                                try {
                                    recyclerView.removeItemDecorationAt(0);
                                }
                                catch (IndexOutOfBoundsException e) {
                                    Log.i("IO", "Search recycler index error");
                                }
                                int spanCount = 2; // 2 columns
                                int spacing = getResources().getDimensionPixelSize(R.dimen._35sdp); // 100px
                                boolean includeEdge = true;
                                recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));
                                recyclerView.setNestedScrollingEnabled(false);
                                //LinearLayoutManager VerticalLayout = new LinearLayoutManager(DownloadActivity.this, LinearLayoutManager.VERTICAL, false);
                                LinearLayoutManager HorizontalLayout = new LinearLayoutManager(DownloadActivity.this, LinearLayoutManager.HORIZONTAL, false);
                                //recyclerView.setLayoutManager(VerticalLayout);
                                textAdapter infoAdapter = new textAdapter(DownloadActivity.this, infoList);
                                CategoryAdapter tagAdapter = new CategoryAdapter(DownloadActivity.this, tagList);
                                RecyclerView infoRecyclerView = (RecyclerView) findViewById(R.id.info);
                                RecyclerView tagRecyclerView = (RecyclerView) findViewById(R.id.tag);
                                LinearLayoutManager NewVerticalLayout = new LinearLayoutManager(DownloadActivity.this, LinearLayoutManager.VERTICAL, false);
                                infoRecyclerView.setLayoutManager(NewVerticalLayout);
                                tagRecyclerView.setLayoutManager(HorizontalLayout);
                                infoRecyclerView.setAdapter(infoAdapter);
                                recyclerView.setAdapter(adapter);
                                tagRecyclerView.setAdapter(tagAdapter);
                                container.stopShimmer();
                                container.setVisibility(View.GONE);
                                androidx.core.widget.NestedScrollView homeLayout = (androidx.core.widget.NestedScrollView) findViewById(R.id.download_scroll);
                                homeLayout.setVisibility(View.VISIBLE);
                                firstRun();
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

    public void download(View v) {
        Snackbar snackBar = Snackbar .make(v, "Please wait", Snackbar.LENGTH_SHORT);
        snackBar.setBackgroundTint(Color.parseColor("#2F0743"));
        snackBar.setTextColor(Color.WHITE);
        snackBar.show();
        // Toast toast=Toast.makeText(getApplicationContext(),"Please wait",Toast.LENGTH_SHORT);
        // toast.show();
        int cacheSize = 10 * 1024 * 1024;
        File httpCacheDirectory = new File(getApplicationContext().getCacheDir(), "http-cache");
        Cache cache = new Cache(httpCacheDirectory, cacheSize);
        String url = "https://bookdl-api.herokuapp.com/download?url=" + download_url;
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
                    DownloadActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject res = new JSONObject(myResponse);
                                //String finalDownloadUrl = res.getString("link");
                                finalDownloadUrl = res.getString("link");
                                Log.i("finalUrl", finalDownloadUrl);
                                DownloadFile myTask = new DownloadFile();
                                myTask.execute(finalDownloadUrl);
                                Button cancelButton = findViewById(R.id.cancelButton);
                                cancelButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        myTask.cancel(true);
                                    }
                                });
                            } catch (Exception e) {
                                Log.i("exception", "1", e);
                            }
                        }
                    });
                }
            }
        });
    }

    public void share() {
        TextView share = findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar snackBar = Snackbar .make(v, "Please wait", Snackbar.LENGTH_SHORT);
                snackBar.setBackgroundTint(Color.parseColor("#2F0743"));
                snackBar.setTextColor(Color.WHITE);
                snackBar.show();
                // Toast toast=Toast.makeText(getApplicationContext(),"Please wait",Toast.LENGTH_SHORT);
                // toast.show();
                int cacheSize = 10 * 1024 * 1024;
                File httpCacheDirectory = new File(getApplicationContext().getCacheDir(), "http-cache");
                Cache cache = new Cache(httpCacheDirectory, cacheSize);
                String url = "https://bookdl-api.herokuapp.com/download?url=" + download_url;
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
                            DownloadActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject res = new JSONObject(myResponse);
                                        //String finalDownloadUrl = res.getString("link");
                                        finalDownloadUrl = res.getString("link");
                                        Log.i("finalUrl", finalDownloadUrl);
                                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                                        sharingIntent.setType("text/plain");
                                        String shareBody = "Hi there! I would like to share this book with you.\n\n";
                                        shareBody += finalDownloadUrl.trim();
                                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Bibliophile");
                                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                                    } catch (Exception e) {
                                        Log.i("exception", "1", e);
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    public class DownloadFile extends AsyncTask<String, Integer, String> {
        private String dirPath;
        private View rootView = getWindow().getDecorView().getRootView();
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            // Toast toast=Toast.makeText(getApplicationContext(),"Downloading",Toast.LENGTH_SHORT);
            // toast.show();
            Snackbar snackBar = Snackbar .make(rootView, "Downloading", Snackbar.LENGTH_SHORT);
            snackBar.setBackgroundTint(Color.parseColor("#2F0743"));
            snackBar.setTextColor(Color.WHITE);
            snackBar.show();
            ProgressBar progress = findViewById(R.id.progressBar);
            Button downloadButton = findViewById(R.id.downloadButton);
            Button cancelButton = findViewById(R.id.cancelButton);
            TextView downloadPercent = findViewById(R.id.percent);
            downloadPercent.setVisibility(View.VISIBLE);
            downloadButton.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            progress.setVisibility(View.VISIBLE);
        }
        @Override
        protected String doInBackground(String... Url) {
            try  {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(Url[0])
                        .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36")
                        .build();
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to download file: " + response);
                }
                InputStream inputStream = response.body().byteStream();
                byte[] buff = new byte[1024 * 4];
                int downloaded = 0;
                int length = (int) response.body().contentLength();
                String extension = response.headers().get("Content-type");
                extension = extension.substring(extension.indexOf("/") + 1);
                Log.i("extension", extension);
                String fileName = title + "." + extension;
                dirPath = Environment.getExternalStorageDirectory() + File.separator + "Books";
                File file = new File(dirPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                String path = file.getAbsolutePath();
                File mediaFile = new File(path, fileName);
                OutputStream output = new FileOutputStream(mediaFile);
                while (true) {
                    int read = inputStream.read(buff);
                    int percent = downloaded*100/length;
                    if (read == -1) {
                        break;
                    }
                    output.write(buff, 0, read);
                    //write buff
                    downloaded += read;
                    publishProgress(percent);
                    if(isCancelled()) {
                        break;
                    }
                }

                output.flush();
                output.close();
                Log.i("Download", "File written!");
            } catch (Exception e) {
                Log.i("IOException", e.toString());
            }
            return Url[0];
        }

        @Override
        protected void onPostExecute(String Url) {
            super.onPostExecute(Url);
            ProgressBar progress = findViewById(R.id.progressBar);
            Button downloadButton = findViewById(R.id.downloadButton);
            Button cancelButton = findViewById(R.id.cancelButton);
            TextView downloadPercent = findViewById(R.id.percent);
            downloadPercent.setText("0");
            progress.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);
            downloadPercent.setVisibility(View.INVISIBLE);
            checkIfFileExists();
            progress.setProgress(0);
            // Toast toast=Toast.makeText(getApplicationContext(),"Download complete. Book saved in:" + dirPath,Toast.LENGTH_LONG);
            // toast.show();
            Snackbar snackBar = Snackbar .make(rootView, "Download complete. Book saved in:" + dirPath, Snackbar.LENGTH_SHORT);
            snackBar.setBackgroundTint(Color.parseColor("#2F0743"));
            snackBar.setTextColor(Color.WHITE);
            snackBar.show();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            // Toast toast=Toast.makeText(getApplicationContext(),"Cancelled ",Toast.LENGTH_SHORT);
            // toast.show();
            Snackbar snackBar = Snackbar .make(rootView, "Cancelled", Snackbar.LENGTH_SHORT);
            snackBar.setBackgroundTint(Color.parseColor("#2F0743"));
            snackBar.setTextColor(Color.WHITE);
            snackBar.show();
            ProgressBar progress = findViewById(R.id.progressBar);
            Button downloadButton = findViewById(R.id.downloadButton);
            Button cancelButton = findViewById(R.id.cancelButton);
            TextView downloadPercent = findViewById(R.id.percent);
            downloadPercent.setText("0");
            progress.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);
            downloadButton.setVisibility(View.VISIBLE);
            downloadPercent.setVisibility(View.INVISIBLE);
            progress.setProgress(0);
        }

        @Override
        protected void onProgressUpdate(Integer... percent) {
            super.onProgressUpdate(percent);
            ProgressBar progress = findViewById(R.id.progressBar);
            TextView downloadPercent = findViewById(R.id.percent);
            progress.setProgress(percent[0]);
            downloadPercent.setText(percent[0] + "%");
        }
    }
}
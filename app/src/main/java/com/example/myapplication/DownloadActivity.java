package com.example.myapplication;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadActivity extends AppCompatActivity {
    private String link = "";
    private String title = "";
    private String download_url = "";
    private String finalDownloadUrl = "";
    public final int cacheSize = 10 * 1024 * 1024;
    public File httpCacheDirectory;
    public Cache cache = null;
    public OkHttpClient client = null;
    private final List<String> infoList = new ArrayList<>();
    private final List<String> tagList = new ArrayList<>();
    private final List<String> titleList = new ArrayList<>();
    private final List<String> linkList = new ArrayList<>();
    private final List<String> imageList = new ArrayList<>();
    private ShimmerFrameLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        Toolbar toolbar = findViewById(R.id.searchToolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        httpCacheDirectory = new File(getApplicationContext().getCacheDir(), "http-cache");
        cache = new Cache(httpCacheDirectory, cacheSize);
        client =  new OkHttpClient.Builder()
                .addNetworkInterceptor(new CacheInterceptor())
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .cache(cache)
                .build();
        search();
        Intent intent = getIntent();
        link = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
        container = findViewById(R.id.shimmer_view_container_download);
        container.startShimmer();
        checkIfFileExists();
        displayData();
        Button downloadButton = findViewById(R.id.downloadButton);
        downloadButton.setOnClickListener(this::download);
        preview();
        share();
    }
    public void firstRun(){
        SharedPreferences wmbPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = wmbPreference.getBoolean("UPDATE", true);
        if (isFirstRun)
        {
            // Code to run once
            SharedPreferences.Editor editor = wmbPreference.edit();
            editor.putBoolean("UPDATE", false);
            //editor.commit();
            editor.apply();
            showcase();
        }
    }

    public void showcase(){
        ViewTarget target = new ViewTarget(findViewById(R.id.share));
        new ShowcaseView.Builder(this)
                .setTarget(target)
                .setContentTitle("New feature!")
                .setContentText("Tap here to share the book.")
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
        image.setOnClickListener(v -> {
            showSnackBar(v, "Please wait");
            String url = "http://bookdl-api.herokuapp.com/download?url=" + link;
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call,@NotNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call,@NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        final String myResponse = Objects.requireNonNull(response.body()).string();
                        DownloadActivity.this.runOnUiThread(() -> {
                            try {
                                JSONObject res = new JSONObject(myResponse);
                                finalDownloadUrl = res.getString("link");
                                String id = res.getString("id");
                                String h = res.getString("h");
                                finalDownloadUrl = finalDownloadUrl + String.format("?id=%s&h=%s", id, h);
                                Log.i("finalUrl", finalDownloadUrl);
                                Intent preview = new Intent(DownloadActivity.this, Preview.class);
                                preview.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                preview.putExtra("link", finalDownloadUrl);
                                startActivity(preview);
                            } catch (Exception e) {
                                Log.i("exception", "1", e);
                            }
                        });
                    }
                }
            });
        });
    }

    public void search() {
        SearchView text = findViewById(R.id.searchBook);
        text.setIconifiedByDefault(true);
        text.setQueryHint("search");
        text.setOnSearchClickListener(V -> {
            text.setIconified(true);
            Intent search = new Intent(DownloadActivity.this, Search.class);
            search.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(search);
        });
    }

    public void checkIfFileExists(){
        String dirPath = Environment.getExternalStorageDirectory() + File.separator + "Books" ;
        dirPath = dirPath + File.separator + title + ".pdf";
        File file = new File(dirPath);
        if (file.exists()) {
            Button downloadButton = findViewById(R.id.downloadButton);
            Button readButton = findViewById(R.id.readButton);
            downloadButton.setVisibility(View.INVISIBLE);
            readButton.setVisibility(View.VISIBLE);
            readButton.setOnClickListener(v -> {
                Intent pdfRead = new Intent(this, PdfReader.class);
                Log.i("path", file.toString());
                pdfRead.putExtra("path", file.toString());
                try {
                    startActivity(pdfRead);
                } catch (ActivityNotFoundException e) {
                    showSnackBar(v, "Install a pdf reader!");
                }
            });
        }
    }


    public void displayData() {
        String url = "https://bookdl-api.herokuapp.com/download_page?url=" + link;
        TextView textView = findViewById(R.id.download_title);
        textView.setText(title);
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call,@NotNull IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(@NotNull Call call,@NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = Objects.requireNonNull(response.body()).string();
                    DownloadActivity.this.runOnUiThread(() -> {
                        try {
                            JSONObject res = new JSONObject(myResponse);
                            String image = res.getString("image");
                            Log.i("image", image);
                            ImageView imageView = findViewById(R.id.download_image);
                            Picasso
                                    .with(DownloadActivity.this)
                                    .load(image)
                                    .fit()
                                    .into(imageView);
                            download_url  = res.getString("download_url");
                            JSONArray info= res.getJSONArray("info");
                            JSONArray tags = res.getJSONArray("tags");
                            JSONObject similarBooks = res.getJSONObject("similar_books");
                            initializeLists(info, tags, similarBooks);
                            initializeRecyclerView();
                            firstRun();
                        }
                        catch (Exception e){
                            Log.i("exception", "1", e);
                        }
                    });
                }
            }
        });
    }

    public void initializeLists(JSONArray info, JSONArray tags, JSONObject similarBooks) {
        for (int i = 0; i<4; i++) {
            try {
                infoList.add(info.getString(i));
            }
            catch (Exception e) {
                Log.i("Exception", "Length");
            }
        }
        for(int i = 0; i<4; i++){
            try{
                tagList.add(tags.getString(i));
            }
            catch (Exception e){
                Log.i("Exception", "Length");
            }
        }
        for (int i = 0; i < 7; i++ ) {
            try {
                JSONObject index = similarBooks.getJSONObject(String.valueOf(i));
                titleList.add(index.getString("title"));
                linkList.add(index.getString("url"));
                imageList.add(index.getString("image"));
            }
            catch (Exception e) {
                Log.i("Exception", e.toString());
            }
        }
    }

    public void initializeRecyclerView(){
        Adapter adapter = new Adapter(DownloadActivity.this, imageList, titleList, linkList);
        RecyclerView recyclerView = findViewById(R.id.similar_books);
        try {
            recyclerView.removeItemDecorationAt(0);
        }
        catch (IndexOutOfBoundsException e) {
            Log.i("IO", "Search recycler index error");
        }
        int spanCount = 2;
        int spacing = getResources().getDimensionPixelSize(R.dimen._34sdp);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        recyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager HorizontalLayout = new LinearLayoutManager(DownloadActivity.this, LinearLayoutManager.HORIZONTAL, false);
        textAdapter infoAdapter = new textAdapter(infoList);
        CategoryAdapter tagAdapter = new CategoryAdapter(DownloadActivity.this, tagList);
        RecyclerView infoRecyclerView = findViewById(R.id.info);
        RecyclerView tagRecyclerView = findViewById(R.id.tag);
        LinearLayoutManager NewVerticalLayout = new LinearLayoutManager(DownloadActivity.this, LinearLayoutManager.VERTICAL, false);
        infoRecyclerView.setLayoutManager(NewVerticalLayout);
        tagRecyclerView.setLayoutManager(HorizontalLayout);
        infoRecyclerView.setAdapter(infoAdapter);
        recyclerView.setAdapter(adapter);
        tagRecyclerView.setAdapter(tagAdapter);
        container.stopShimmer();
        container.setVisibility(View.GONE);
        androidx.core.widget.NestedScrollView homeLayout = findViewById(R.id.download_scroll);
        homeLayout.setVisibility(View.VISIBLE);
    }

    public void download(View v) {
        showSnackBar(v, "Please wait");
        String url = "http://bookdl-api.herokuapp.com/download?url=" + link;
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call,@NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call,@NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String myResponse = Objects.requireNonNull(response.body()).string();
                    DownloadActivity.this.runOnUiThread(() -> {
                        try {
                            JSONObject res = new JSONObject(myResponse);
                            finalDownloadUrl = res.getString("link");
                            String id = res.getString("id");
                            String h = res.getString("h");
                            finalDownloadUrl = finalDownloadUrl + String.format("?id=%s&h=%s", id, h);
                            Log.i("finalUrl", finalDownloadUrl);
                            DownloadFile myTask = new DownloadFile();
                            myTask.execute(finalDownloadUrl);
                            Button cancelButton = findViewById(R.id.cancelButton);
                            cancelButton.setOnClickListener(v1 -> myTask.cancel(true));
                        } catch (Exception e) {
                            Log.i("exception", "1", e);
                        }
                    });
                }
            }
        });
    }

    public void share() {
        TextView share = findViewById(R.id.share);
        share.setOnClickListener(v -> {
            showSnackBar(v, "Please wait");
            String url = "https://bookdl-api.herokuapp.com/download?url=" + link;
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call,@NotNull IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NotNull Call call,@NotNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        final String myResponse = Objects.requireNonNull(response.body()).string();
                        DownloadActivity.this.runOnUiThread(() -> {
                            try {
                                JSONObject res = new JSONObject(myResponse);
                                finalDownloadUrl = res.getString("link");
                                String id = res.getString("id");
                                String h = res.getString("h");
                                finalDownloadUrl = finalDownloadUrl + String.format("?id=%s&h=%s", id, h);
                                Log.i("finalUrl", finalDownloadUrl);
                                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                sharingIntent.setType("text/plain");
                                String shareBody = "Hi there! I would like to share this book with you.\n\n";
                                shareBody += finalDownloadUrl.trim();
                                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Bibliophile");
                                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                                startActivity(Intent.createChooser(sharingIntent, "Share via"));
                            } catch (Exception e) {
                                Log.i("exception", "1", e);
                            }
                        });
                    }
                }
            });
        });
    }

    public void showSnackBar(View rootView, String text) {
        Snackbar snackBar = Snackbar .make(rootView, text, Snackbar.LENGTH_SHORT);
        snackBar.setBackgroundTint(Color.parseColor("#2F0743"));
        snackBar.setTextColor(Color.WHITE);
        snackBar.show();
    }

    public class DownloadFile extends AsyncTask<String, Integer, String> {
        private String dirPath;
        private final View rootView = getWindow().getDecorView().getRootView();

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            showSnackBar(rootView, "Downloading");
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
                        .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) " +
                                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.97 Safari/537.36")
                        .build();
                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to download file: " + response);
                }
                InputStream inputStream = Objects.requireNonNull(response.body()).byteStream();
                byte[] buff = new byte[1024 * 4];
                int downloaded = 0;
                int length = (int) Objects.requireNonNull(response.body()).contentLength();
                String extension = response.headers().get("Content-type");
                assert extension != null;
                extension = extension.substring(extension.indexOf("/") + 1);
                Log.i("extension", extension);
                String fileName = title + "." + extension;
                dirPath = Environment.getExternalStorageDirectory() + File.separator + "Books";
                File file = new File(dirPath);
                if (!file.exists()) {
                    boolean result = file.mkdirs();
                    Log.i("File", String.valueOf(result));
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
                    output.write(buff, 0, read); // write buff
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
            Button cancelButton = findViewById(R.id.cancelButton);
            TextView downloadPercent = findViewById(R.id.percent);
            downloadPercent.setText("0");
            progress.setVisibility(View.INVISIBLE);
            cancelButton.setVisibility(View.INVISIBLE);
            downloadPercent.setVisibility(View.INVISIBLE);
            checkIfFileExists();
            progress.setProgress(0);
            String text = "Download complete. Book saved in:" + dirPath;
            showSnackBar(rootView, text);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            showSnackBar(rootView, "Cancelled");
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
            String value = percent[0] + "%";
            downloadPercent.setText(value);
        }
    }
}
package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
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

public class DownloadActivity extends AppCompatActivity {
    private String link = " ";
    private String title = " ";
    private List<String> infoList = new ArrayList<>();
    private List<String> tagList = new ArrayList<>();
    private List<String> titleList = new ArrayList<>();
    private List<String> linkList = new ArrayList<>();
    private List<String> imageList = new ArrayList<>();
    private List<String> sizeList = new ArrayList<>();
    private List<String> pageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        getSupportActionBar().setTitle("Download");
        Intent intent = getIntent();
        link = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
        displayData();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        ProgressBar spinner = (ProgressBar) findViewById(R.id.progress);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        MenuItem search_item = menu.findItem(R.id.search);
        SearchView text = (SearchView) search_item.getActionView();
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
                                String download_url  = res.getString("download_url");
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
                                recyclerView.setNestedScrollingEnabled(false);
                                //LinearLayoutManager VerticalLayout = new LinearLayoutManager(DownloadActivity.this, LinearLayoutManager.VERTICAL, false);
                                LinearLayoutManager HorizontalLayout = new LinearLayoutManager(DownloadActivity.this, LinearLayoutManager.HORIZONTAL, false);
                                //recyclerView.setLayoutManager(VerticalLayout);
                                textAdapter infoAdapter = new textAdapter(DownloadActivity.this, infoList);
                                textAdapter tagAdapter = new textAdapter(DownloadActivity.this, tagList);
                                RecyclerView infoRecyclerView = (RecyclerView) findViewById(R.id.info);
                                RecyclerView tagRecyclerView = (RecyclerView) findViewById(R.id.tag);
                                LinearLayoutManager NewVerticalLayout = new LinearLayoutManager(DownloadActivity.this, LinearLayoutManager.VERTICAL, false);
                                infoRecyclerView.setLayoutManager(NewVerticalLayout);
                                tagRecyclerView.setLayoutManager(HorizontalLayout);
                                infoRecyclerView.setAdapter(infoAdapter);
                                recyclerView.setAdapter(adapter);
                                tagRecyclerView.setAdapter(tagAdapter);
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
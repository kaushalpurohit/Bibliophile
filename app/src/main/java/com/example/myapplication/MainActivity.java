package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    private String[] titleList = new String[4];
    private String[] linkList = new String[4];
    private String[] imageList = new String[4];
    private String[] sizeList = new String[4];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createCategories();
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
            public void onClick(View V){
                text.setIconified(true);
                Intent search = new Intent(MainActivity.this, Search.class);
                startActivity(search);
            }
        });
        return true;
    }

    public void createCategories() {
        String url = "https://bookdl-api.herokuapp.com/home";
        OkHttpClient client = new OkHttpClient();
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
                                int val[] = new int[4];
                                int rec[] = new int[4];
                                for(int i=0; i<4; i++) {
                                    Log.i("id", "id/title"+(i+1));
                                    val[i] = getResources().getIdentifier("id/title"+(i+1), null, getPackageName());
                                    rec[i] = getResources().getIdentifier("id/recyclerview"+(i+1), null, getPackageName());
                                }
                                for(int i=0; i<4; i++) {
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
                                            titleList[j] = index.getString("title");
                                            linkList[j] = index.getString("link");
                                            imageList[j] = index.getString("image");
                                            sizeList[j] = "";
                                        }
                                        catch (Exception e) {
                                            Log.i("Exception", "Length");
                                        }
                                    }
                                    Adapter adapter = new Adapter(MainActivity.this, imageList, titleList);
                                    RecyclerView recyclerView = (RecyclerView)findViewById(rec[i]);
                                    LinearLayoutManager RecyclerViewLayoutManager = new LinearLayoutManager(getApplicationContext());
                                    LinearLayoutManager HorizontalLayout = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
                                    recyclerView.setLayoutManager(HorizontalLayout);
                                    recyclerView.setAdapter(adapter);
                                    titleList = new String[4];
                                    imageList = new String[4];
                                }
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
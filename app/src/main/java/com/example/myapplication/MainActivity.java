package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.GridView;
import org.json.JSONObject;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    int i = 0;
    GridView grid;
    public ArrayList<String> linksList = new ArrayList<String>();
    public String[] imagesList = new String[10];
    public String[] titlesList = new String[10];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        SearchView text = (SearchView) findViewById(R.id.searchText);
        text.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String search) {
                String url = "https://bookdl-api.herokuapp.com/search?book=" + search;
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
                                        for(int i=1; i<11; i++) {
                                            JSONObject res = (new JSONObject(myResponse)).getJSONObject(String.valueOf(i));
                                            String title = res.getString("Title");
                                            String link = res.getString("Link");
                                            String image = res.getString("image");
                                            imagesList[i-1] = image;
                                            titlesList[i-1] = title;
                                            linksList.add(link);
                                        }
                                        CustomGrid adapter = new CustomGrid(MainActivity.this, titlesList, imagesList);
                                        grid=(GridView)findViewById(R.id.grid);
                                        grid.setAdapter(adapter);
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
                return false;
            }
        });
    }

    private AdapterView.OnItemClickListener listClick = new AdapterView.OnItemClickListener() {
      public void onItemClick(AdapterView parent, View v, int position, long id){
          Log.i("Item:", String.valueOf(position));
          String url = "https://bookdl-api.herokuapp.com/download?url=" + linksList.get(position);
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
                                      JSONObject res = (new JSONObject(myResponse));
                                      String url = res.getString("link");
                                      Log.i("URL:", url);
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
    };
}
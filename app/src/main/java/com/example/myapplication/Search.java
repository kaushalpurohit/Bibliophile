package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Search extends AppCompatActivity {
    int i = 0;
    public ArrayList<String> linksList = new ArrayList<String>();
    public String[] imagesList = new String[10];
    public String[] titlesList = new String[10];
    public String[] sizeList = new String[10];
    public String[] pageList = new String[10];

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }


@Override
public boolean onSupportNavigateUp() {
        finish();
        return true;
}

    public boolean onCreateOptionsMenu(Menu menu){
        ProgressBar spinner=(ProgressBar)findViewById(R.id.progress);
        getMenuInflater().inflate(R.menu.options_menu, menu);
        MenuItem search_item = menu.findItem(R.id.search);
        SearchView text = (SearchView) search_item.getActionView();
        text.requestFocus();
        text.setMaxWidth(Integer.MAX_VALUE);
        text.setIconified(false);
        text.setFocusable(true);
        text.setQueryHint("search");
        text.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String search) {
                GridView exGrid=(GridView) findViewById(R.id.grid);
                exGrid.setAdapter(null);
                spinner.setVisibility(View.VISIBLE);
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
                            Search.this.runOnUiThread(new Runnable() {
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
                                            imagesList[i-1] = image;
                                            titlesList[i-1] = title;
                                            linksList.add(link);
                                            sizeList[i-1] = size;
                                            pageList[i-1] = page;
                                        }
                                        spinner.setVisibility(View.GONE);
                                        CustomGrid adapter = new CustomGrid(Search.this, titlesList, imagesList, sizeList, pageList);
                                        GridView exGrid=(GridView) findViewById(R.id.grid);
                                        exGrid.setNumColumns(2);
                                        exGrid.setAdapter(adapter);
                                        exGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                PopUp popup = new PopUp(Search.this, imagesList[position], titlesList[position], linksList.get(position));
                                                popup.showPopupWindow(view);
                                            }
                                        });
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
                spinner.setVisibility(View.GONE);
                GridView exGrid=(GridView) findViewById(R.id.grid);
                exGrid.setAdapter(null);
                return false;
            }
        });

        return true;
    }

}

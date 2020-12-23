package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import org.json.JSONObject;
import android.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    int i = 0;
    public ArrayList<String> linksList = new ArrayList<String>();
    public String[] imagesList = new String[10];
    public String[] titlesList = new String[10];
    public String[] sizeList = new String[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        SearchView text = (SearchView) findViewById(R.id.searchText);
        ProgressBar spinner=(ProgressBar)findViewById(R.id.progress);
        spinner.setVisibility(View.GONE);
        text.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String search) {
                closeKeyboard();
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
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        for(int i=1; i<11; i++) {
                                            JSONObject res = (new JSONObject(myResponse)).getJSONObject(String.valueOf(i));
                                            String title = res.getString("Title");
                                            String link = res.getString("Link");
                                            String image = res.getString("image");
                                            String size = res.getString("size");
                                            imagesList[i-1] = image;
                                            titlesList[i-1] = title;
                                            linksList.add(link);
                                            sizeList[i-1] = size;
                                        }
                                        CustomGrid adapter = new CustomGrid(MainActivity.this, titlesList, imagesList, sizeList);
                                        ExpandableHeightGridView exGrid=(ExpandableHeightGridView) findViewById(R.id.grid);
                                        exGrid.setNumColumns(2);
                                        exGrid.setAdapter(adapter);
                                        exGrid.setExpanded(true);
                                        spinner.setVisibility(View.GONE);
                                        exGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                PopUp popup = new PopUp(MainActivity.this, imagesList[position], titlesList[position], linksList.get(position));
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
                ExpandableHeightGridView exGrid=(ExpandableHeightGridView) findViewById(R.id.grid);
                exGrid.setAdapter(null);
                return false;
            }
        });
    }

    public void closeKeyboard()
    {
        // this will give us the view
        // which is currently focus
        // in this layout
        View view = this.getCurrentFocus();
        InputMethodManager manager = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        manager
                .hideSoftInputFromWindow(
                        view.getWindowToken(), 0);

        // if nothing is currently
        // focus then this will protect
        // the app from crash
        if (view != null) {

            // now assign the system
            // service to InputMethodManager
            manager
                    .hideSoftInputFromWindow(
                            view.getWindowToken(), 0);
        }

    }
}
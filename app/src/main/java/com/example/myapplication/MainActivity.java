package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
}
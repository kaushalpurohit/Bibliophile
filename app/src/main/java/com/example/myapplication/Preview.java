package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

public class Preview  extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        Intent intent = getIntent();
        String link = intent.getStringExtra("link");
        WebView webview = findViewById(R.id.webView);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=" + link);
    }
}

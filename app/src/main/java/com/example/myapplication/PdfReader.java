package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.shockwave.pdfium.PdfDocument;

import java.io.File;
import java.util.List;

public class PdfReader extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {
    PDFView pdfView;
    private static final String TAG = PdfReader.class.getSimpleName();
    private Integer pageNumber;
    private String pdfFileName;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_reader);
        Intent intent = getIntent();
        String bookPath = intent.getStringExtra("path");
        title = intent.getStringExtra("title");
        SharedPreferences sh = getSharedPreferences("PdfReader", MODE_APPEND);
        pageNumber = sh.getInt(title, 0);
        File path = new File(bookPath);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        readPdf(path);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sh = getSharedPreferences("PdfReader", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sh.edit();
        myEdit.putInt(title, pageNumber);
        myEdit.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sh = getSharedPreferences("PdfReader", MODE_APPEND);
        pageNumber = sh.getInt(title, 0);
    }


    private void readPdf(File path) {
        boolean nightMode = false;
        int nightModeFlags =
                getApplicationContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                nightMode = true;
                getWindow().setStatusBarColor(Color.BLACK);
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                nightMode = false;
                getWindow().setStatusBarColor(Color.WHITE);
                break;
        }
        pdfFileName = "Name";
        Log.i("path", path.toString());
        pdfView.fromFile(path)
                .defaultPage(pageNumber)
                .enableSwipe(true)
                .swipeHorizontal(true)
                .onLoad(this)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .scrollHandle(new DefaultScrollHandle(this))
                .pageFling(true)
                .nightMode(nightMode)
                .load();
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }


    @Override
    public void loadComplete(int nbPages) {
        PdfDocument.Meta meta = pdfView.getDocumentMeta();
        Log.e(TAG, "title = " + meta.getTitle());
        Log.e(TAG, "author = " + meta.getAuthor());
        Log.e(TAG, "subject = " + meta.getSubject());
        Log.e(TAG, "keywords = " + meta.getKeywords());
        Log.e(TAG, "creator = " + meta.getCreator());
        Log.e(TAG, "producer = " + meta.getProducer());
        Log.e(TAG, "creationDate = " + meta.getCreationDate());
        Log.e(TAG, "modDate = " + meta.getModDate());

        printBookmarksTree(pdfView.getTableOfContents(), "-");

    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (b.hasChildren()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page);
    }
}
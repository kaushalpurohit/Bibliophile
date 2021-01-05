package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MyBooks extends AppCompatActivity {
    public ArrayList<String> paths = new ArrayList<String>();
    public ArrayList<String> fileName = new ArrayList<String>();
    public ArrayList<String> fileList = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_books);
        Toolbar toolbar = findViewById(R.id.searchToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();
        search();
    }
    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            listFiles();
            return "Done";
        }


        @Override
        protected void onPostExecute(String result) {
            MyBookAdapter adapter = new MyBookAdapter(MyBooks.this, fileName, paths, fileList);
            Log.i("File", paths.toString());
            RecyclerView recyclerView = findViewById(R.id.myBooksRecycler);
            RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(MyBooks.this, 2);
            recyclerView.setLayoutManager(mLayoutManager);
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
            recyclerView.setAdapter(adapter);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean search() {
        SearchView text = findViewById(R.id.searchBook);
        text.setIconifiedByDefault(true);
        text.setQueryHint("search");
        text.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                text.setIconified(true);
                Intent search = new Intent(MyBooks.this, Search.class);
                search.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(search);
            }
        });
        return true;
    }

    public void listFiles() {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "Books");
        String pdfPattern = ".pdf";
        File FileList[] = file.listFiles();
        if (FileList != null) {
            for (int i = 0; i < FileList.length; i++) {

                    if (FileList[i].getName().endsWith(pdfPattern)){
                        fileName.add(FileList[i].getName());
                        fileList.add(FileList[i].getAbsolutePath());
                    }
                }
            }
        createImage(fileList, fileName);
    }

    public void createImage(List<String> Files, List<String> Names) {
        for (int i = 0; i < Files.size(); i++) {
            try {
                String path = Environment.getExternalStorageDirectory() + File.separator + "Books" + File.separator + "Images" + File.separator +
                        Names.get(i).substring(0,Names.get(i).indexOf('.')) + ".png";
                File image = new File(path);
                if(image.exists()) {
                    paths.add(Uri.fromFile(image).toString());
                    Log.i("File", "Break");
                    continue;
                }
                int pageNumber = 0;
                Log.i("File", Names.get(i));
                PdfiumCore pdfiumCore = new PdfiumCore(this);
                ParcelFileDescriptor fd = getContentResolver().openFileDescriptor(Uri.fromFile(new File(Files.get(i))), "r");
                PdfDocument pdfDocument = pdfiumCore.newDocument(fd);
                pdfiumCore.openPage(pdfDocument, pageNumber);
                int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
                int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
                Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height);
                saveImage(bmp, Names.get(i));
                pdfiumCore.closeDocument(pdfDocument); // important!
            }
            catch (java.io.FileNotFoundException e) {
                Log.i("File", e.toString());
            }
            catch (java.io.IOException e){
                Log.i("IOException", e.toString());
            }
        }
        Log.i("File", paths.toString());
    }
    public final static String FOLDER = Environment.getExternalStorageDirectory() + File.separator + "Books" + File.separator + "Images";
    private void saveImage(Bitmap bmp, String name) {
        FileOutputStream out = null;
        try {
            File folder = new File(FOLDER);
            if(!folder.exists())
                folder.mkdirs();
            String path = folder + File.separator + name.substring(0,name.indexOf('.')) + ".png";
            paths.add(Uri.fromFile(new File(path)).toString());
            File file = new File(path);
            out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
        } catch (Exception e) {
            Log.i("Image", e.toString());
        } finally {
            try {
                if (out != null)
                    out.close();
            } catch (Exception e) {
                //todo with exception
            }
        }
    }
}


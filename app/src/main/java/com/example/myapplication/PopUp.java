package com.example.myapplication;

import android.app.DownloadManager;
import android.database.Cursor;
import android.net.Uri;
import java.util.concurrent.TimeUnit;
import java.io.File;

import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.view.animation.AnimationSet;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import okhttp3.OkHttpClient.Builder;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PopUp {
    private String url;
    private String image;
    private String title;
    private Context mContext;
    private long downloadID;

    //PopupWindow display method
    public PopUp(Context c, String i, String t, String url) {
        this.url = url;
        this.title = t;
        this.image = i;
        this.mContext = c;
    }

    public void showPopupWindow(final View view) {


        //Create a View object yourself through inflater
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(view.getContext().LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popupwindow, null);
        //Specify the length and width through constants
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;

        //Make Inactive Items Outside Of PopupWindow
        boolean focusable = true;
        AnimationSet mAnimationSet = new AnimationSet(false);
        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height);
        //Set the location of the window on the screen
        popupWindow.setAnimationStyle(R.style.fade_in_out);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        //Initialize the elements of our window, install the handler

        TextView test2 = popupView.findViewById(R.id.titleText);
        test2.setText(title);

        ImageView imageView = (ImageView) popupView.findViewById(R.id.image);
        Picasso
                .with(view.getContext())
                .load(image)
                .fit() // will explain later
                .into(imageView);

        Button buttonEdit = popupView.findViewById(R.id.messageButton);
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String apiUrl = "https://bookdl-api.herokuapp.com/download?url=" + url;
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30,TimeUnit.SECONDS)
                        .readTimeout(30,TimeUnit.SECONDS)
                        .build();
                Request request = new Request.Builder()
                        .url(apiUrl)
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
                            try {
                                JSONObject res = (new JSONObject(myResponse));
                                String newUrl = res.getString("link");
                                Log.i("URL", newUrl);
                                beginDownload(newUrl);
                            }
                            catch (Exception e){
                                Log.i("exception", "1", e);
                            }
                        }
                    }
                });
                //As an example, display the message

            }
        });


        //Handler for clicking on the inactive zone of the window
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                //Close the window when clicked
                popupWindow.dismiss();
                return true;
            }
        });
    }

    private void beginDownload(String url){
        String fileName = url.substring(url.lastIndexOf('=') + 1);
        fileName = fileName.substring(0,1).toUpperCase() + fileName.substring(1);
        fileName = "/storage/emulated/0/Download/the_da_vinci_code.pdf";
        File file = new File(fileName);
        Log.i("file", fileName);
        Log.i("parse", Uri.parse(url).toString());
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                .setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)// Visibility of the download Notification
                .setDestinationUri(Uri.fromFile(file))// Uri of the destination file
                .setTitle(fileName)// Title of the Download Notification
                .setDescription("Downloading")// Description of the Download Notification
                .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                .setDestinationUri(Uri.fromFile(file))
                .setAllowedOverRoaming(true);// Set if download is allowed on roaming network
        DownloadManager downloadManager= (DownloadManager) mContext.getSystemService(mContext.DOWNLOAD_SERVICE);
        downloadID = downloadManager.enqueue(request);// enqueue puts the download request in the queue.
        Log.i("ID", String.valueOf(downloadID));

        // using query method
        boolean finishDownload = false;
        int progress;
        while (!finishDownload) {
            Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downloadID));
            if (cursor.moveToFirst()) {
                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                Log.i("status", String.valueOf(status));
                switch (status) {
                    case DownloadManager.STATUS_FAILED: {
                        finishDownload = true;
                        break;
                    }
                    case DownloadManager.STATUS_PAUSED:
                        break;
                    case DownloadManager.STATUS_PENDING:
                        break;
                    case DownloadManager.STATUS_RUNNING: {
                        final long total = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        Log.i("size", String.valueOf(total));
                        if (total >= 0) {
                            final long downloaded = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                            progress = (int) ((downloaded * 100L) / total);
                            // if you use downloadmanger in async task, here you can use like this to display progress.
                            // Don't forget to do the division in long to get more digits rather than double.
                            //  publishProgress((int) ((downloaded * 100L) / total));
                        }
                        break;
                    }
                    case DownloadManager.STATUS_SUCCESSFUL: {
                        progress = 100;
                        finishDownload = true;
                        Looper.prepare();
                        Toast.makeText(mContext, "Download Completed", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        break;
                    }
                }
            }
        }
    }
}

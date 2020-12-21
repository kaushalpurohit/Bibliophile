package com.example.myapplication;

import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;

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

        //Create a window with our parameters
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        //Set the location of the window on the screen
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
                OkHttpClient client = new OkHttpClient();
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
                                Looper.prepare();
                                Toast.makeText(view.getContext(), "Downloading your book.", Toast.LENGTH_SHORT).show();
                                Looper.loop();
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
}

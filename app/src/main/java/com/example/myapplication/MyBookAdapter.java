package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// The adapter class which
// extends RecyclerView Adapter
public class MyBookAdapter extends RecyclerView.Adapter<MyBookAdapter.MyCardView> {

    // List with String type
    private List<String> detailsList  = new ArrayList<>();
    private List<String> imageList = new ArrayList<>();
    private List<String> fileList = new ArrayList<>();
    private Context mContext;

    // View Holder class which
    // extends RecyclerView.ViewHolder
    public class MyCardView
            extends RecyclerView.ViewHolder {

        // Text View
        CardView cat;
        TextView title;
        ImageView image;
        RelativeLayout mainLayout;
        RelativeLayout deleteIcon;

        // parameterised constructor for View Holder class
        // which takes the view as a parameter
        public MyCardView(View view) {
            super(view);

            // initialise TextView with id
            cat = view.findViewById(R.id.MyBooksCard);
            title = view.findViewById(R.id.myBookTitle);
            image = view.findViewById(R.id.myBookImage);
            mainLayout = view.findViewById(R.id.MyBooksLayout);
            deleteIcon = view.findViewById(R.id.delete);
        }
    }

    // Constructor for adapter class
    // which takes a list of String type
    public MyBookAdapter(Context context, List<String> details, List<String> image, List<String> file)
    {
        this.detailsList = details;
        this.mContext = context;
        this.imageList = image;
        this.fileList = file;
    }

    // Override onCreateViewHolder which deals
    // with the inflation of the card layout
    // as an item for the RecyclerView.
    @Override
    public MyCardView onCreateViewHolder(ViewGroup parent,
                                           int viewType)
    {

        // Inflate item.xml using LayoutInflator
        View itemView
                = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.mybooks_layout,
                        parent,
                        false);

        // return itemView
        return new MyCardView(itemView);
    }

    // Override onBindViewHolder which deals
    // with the setting of different data
    // and methods related to clicks on
    // particular items of the RecyclerView.
    @Override
    public void onBindViewHolder(final MyCardView holder,
                                 final int position) {

        // Set the text of each item of
        // Recycler view with the list items
        holder.title.setText(detailsList.get(position).substring(0, detailsList.get(position).indexOf('.')));
        try {
            Picasso
                    .with(mContext)
                    .load(imageList.get(position))
                    .fit() // will explain later
                    .into(holder.image);
        } catch (IndexOutOfBoundsException e) {
            Log.i("IO", "Recycler imageView index error");
        }
        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                File file = new File(fileList.get(position));
                Uri uri = Uri.fromFile(file);
                intent.setDataAndType(uri, "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                ;
                try {
                    mContext.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.i("File", "No PDF reader installed!");
                }
            }
        });
        delete(holder, position);
    }

    public void delete(MyCardView holder, int position) {
        holder.deleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Alert!");

                //Setting message manually and performing action on button click
                builder.setMessage("Do you want to delete this book?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                    String path = Environment.getExternalStorageDirectory() +
                                            File.separator + "Books" + File.separator ;
                                    String filePath = path + detailsList.get(position);
                                    String imagePath = path + File.separator + "Images" + File.separator +
                                            detailsList.get(position).substring(0, detailsList.get(position).indexOf('.'))
                                            + ".png";
                                    File file = new File(filePath);
                                    File image = new File(imagePath);
                                    if(image.exists()){
                                        boolean imageResult = image.delete();
                                    };
                                    boolean result = file.delete();
                                    RecyclerView recyclerView = ((Activity) mContext).findViewById(R.id.myBooksRecycler);
                                    detailsList.remove(position);
                                    imageList.remove(position);
                                    fileList.remove(position);
                                    recyclerView.removeViewAt(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, detailsList.size());
                                    notifyDataSetChanged();
                                    if(result) {
                                        Log.i("Delete", "Deleted book.");
                                    }
                                    else {
                                        Log.i("Delete", "Couldn't delete book.");
                                    }
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                dialog.cancel();
                            }
                        });
                //Creating dialog box
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle("Bibliophile");
                alert.show();
                int[] buttons = new int[] {AlertDialog.BUTTON_POSITIVE, AlertDialog.BUTTON_NEGATIVE, AlertDialog.BUTTON_NEUTRAL};
                for (int i : buttons) {
                    Button b = null;
                    try {
                        b = alert.getButton(i);
                        b.setBackgroundColor(Color.TRANSPARENT);
                        b.setTextColor(mContext.getResources().getColor(R.color.purple_200));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
        // Override getItemCount which Returns
        // the length of the RecyclerView.
        @Override
        public int getItemCount ()
        {
            return detailsList.size();
        }
}

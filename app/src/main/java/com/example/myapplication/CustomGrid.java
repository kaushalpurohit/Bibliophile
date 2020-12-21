package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnimationSet;
import com.squareup.picasso.Picasso;

public class CustomGrid extends BaseAdapter{
    private Context mContext;
    private String[] web;
    private String[] Imageid;

    public CustomGrid(Context c,String[] web,String[] Imageid ) {
        mContext = c;
        this.Imageid = Imageid;
        this.web = web;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return web.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {

            grid = inflater.inflate(R.layout.grid_single, null);
            TextView textView = (TextView) grid.findViewById(R.id.grid_text);
            AnimationSet mAnimationSet = new AnimationSet(false);
            ImageView imageView = (ImageView)grid.findViewById(R.id.grid_image);
            Animation fadeInAnimation = AnimationUtils.loadAnimation(grid.getContext(), android.R.anim.fade_in);
            fadeInAnimation.setStartOffset(1000);
            mAnimationSet.addAnimation(fadeInAnimation);
            textView.setText(web[position]);
            Log.i("message", String.valueOf(position));
            Picasso
                    .with(mContext)
                    .load(Imageid[position])
                    .fit() // will explain later
                    .into(imageView);
            imageView.startAnimation(mAnimationSet);
        } else {
            grid = (View) convertView;
        }

        return grid;
    }
}
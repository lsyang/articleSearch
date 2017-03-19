package com.codepath.nytimessearch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by lsyang on 3/18/17.
 */

public class ArticleArrayAdapter extends ArrayAdapter<Article> {
    public ArticleArrayAdapter(Context context, List<Article> articles){
        super(context, android.R.layout.simple_list_item_1, articles);
    }


    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get data for position
        Article article = this.getItem(position);
        // check to see if existing view being reused
        // not using a recycler view -> inflate the layout
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_article_result, parent, false);
        }
        // find the image view
        ImageView imageView = (ImageView) convertView.findViewById(R.id.ivImage);

        // clear out recycled image from converView from last time
        imageView.setImageResource(0);

        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
        tvTitle.setText(article.getHeadline());

        String thumbnail = article.getThumbnail();
        if (!TextUtils.isEmpty(thumbnail)) {
            Picasso.with(getContext()).load(thumbnail).into(imageView);
        }
        return convertView;
    }
}

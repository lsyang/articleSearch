package com.codepath.nytimessearch.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.codepath.nytimessearch.Article;
import com.codepath.nytimessearch.ArticleArrayAdapter;
import com.codepath.nytimessearch.FilterFragment;
import com.codepath.nytimessearch.FilterFragment.FilterDialogListener;
import com.codepath.nytimessearch.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity implements FilterDialogListener {

    GridView gvResults;
    ArrayList<Article> articles;
    ArticleArrayAdapter adapter;

    int year = 2016;
    int month = 1;
    int day = 1;
    int sortOrder = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUpViews();

    }

    public void setUpViews() {
        gvResults = (GridView) findViewById(R.id.gvResults);
        articles = new ArrayList<Article>();
        adapter = new ArticleArrayAdapter(this, articles);
        gvResults.setAdapter(adapter);

        // hoook up listner
        gvResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // create an intent ti display article
                Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
                // get article to display
                Article article = articles.get(position);
                // pass in that article intent
                i.putExtra("article", article);
                startActivity(i);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // create an intent and pass on the position and text
            FragmentManager fm = getSupportFragmentManager();
            FilterFragment editItemFragment = FilterFragment.newInstance(year, month, day, sortOrder);
            editItemFragment.show(fm, "fragment_edit_name");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search, menu);

        // configure search button
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onArtcileSearch(query);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public void onArtcileSearch(String query) {
        query = "android";

        AsyncHttpClient client = new AsyncHttpClient();
        String url = "https://api.nytimes.com/svc/search/v2/articlesearch.json";

        SimpleDateFormat outputFmt = new SimpleDateFormat("yyyyMMdd");
        String beginDate = "Begin Date: " + outputFmt.format(new Date(year - 1900, month, day));
        String sort = getResources().getStringArray(R.array.sort_order)[sortOrder];

        RequestParams params = new RequestParams();
        params.put("api-key", "e66de07ef7534c4696080e6d4c75413a");
        params.put("q", query);
        params.put("page", 0);
//        params.put("begin_date", beginDate);
//        params.put("sort", sort);

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray articleJsonResults = null;
                try {
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    adapter.addAll(Article.fromJsonArray(articleJsonResults));
                    Log.d("success", "onSuccess: " + articles);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                    JSONObject errorResponse) {
                Log.d("DEBUG", "onFailure: " + errorResponse);
            }
        });
    }

    @Override
    public void onFinishEditDialog(int year, int month, int day, int sortOrder) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.sortOrder = sortOrder;
        Log.d("filter", "onFinishEditDialog: ");
    }
}

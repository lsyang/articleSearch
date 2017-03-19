package com.codepath.nytimessearch.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.nytimessearch.model.Article;
import com.codepath.nytimessearch.adapter.ArticleAdapter;
import com.codepath.nytimessearch.listener.EndlessRecyclerViewScrollListener;
import com.codepath.nytimessearch.fragment.FilterFragment;
import com.codepath.nytimessearch.fragment.FilterFragment.FilterDialogListener;
import com.codepath.nytimessearch.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity implements FilterDialogListener {

    RecyclerView gvResults;
    ArrayList<Article> articles;
    ArticleAdapter adapter;
    private EndlessRecyclerViewScrollListener scrollListener;

    String query = "";
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
        //setUpScrolling();

    }

    public void setUpViews() {
        gvResults = (RecyclerView) findViewById(R.id.gvResults);
        articles = new ArrayList<Article>();
        adapter = new ArticleAdapter(this, articles);
        gvResults.setAdapter(adapter);
        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        gvResults.setLayoutManager(gridLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadNextDataFromApi(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        gvResults.addOnScrollListener(scrollListener);
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
                onArtcileSearch(query, 0);
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



    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

    public void loadNextDataFromApi (int page) {
        onArtcileSearch(query, page);
    }

    public void onArtcileSearch(String query, int page) {

        if (!isOnline()) {
            Toast toast = Toast.makeText(getApplicationContext(), "Error: Network is unavaliable, please try again later", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (!this.query.equals(query)) {
            Log.d("DEBUG", "onArtcileSearch: ");
            // 1. First, clear the array of data
            articles.clear();
            // 2. Notify the adapter of the update
            adapter.notifyDataSetChanged(); // or notifyItemRangeRemoved
            // 3. Reset endless scroll listener when performing a new search
            scrollListener.resetState();
        }

        this.query = query;

        AsyncHttpClient client = new AsyncHttpClient();
        String url = "https://api.nytimes.com/svc/search/v2/articlesearch.json";

        SimpleDateFormat outputFmt = new SimpleDateFormat("yyyyMMdd");
        String beginDate = outputFmt.format(new Date(year - 1900, month, day));
        String sort = getResources().getStringArray(R.array.sort_order)[sortOrder];

        RequestParams params = new RequestParams();
        params.put("api-key", "e66de07ef7534c4696080e6d4c75413a");
        params.put("q", query);
        params.put("page", page);
        params.put("begin_date", beginDate);
        params.put("sort", sort);

        client.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                JSONArray articleJsonResults = null;
                try {
                    articleJsonResults = response.getJSONObject("response").getJSONArray("docs");
                    articles.addAll(Article.fromJsonArray(articleJsonResults));
                    adapter.notifyDataSetChanged();
                    Log.d("DEBUG", "onSuccess: " + articles);
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                    JSONObject errorResponse) {
                Toast toast = Toast.makeText(getApplicationContext(), "Error: " + errorResponse, Toast.LENGTH_SHORT);
                toast.show();
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

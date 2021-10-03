package com.codepath.apps.restclienttemplate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {

    // tag to keep track of success/failure
    public static final String TAG = "TimelineActivity";

    // to pass into startActivityForResult; any value if unique throughout app
    private final int REQUEST_CODE = 20;

    // instance variable to be used in multiple methods
    TwitterClient client;

    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        // get instance of TwitterClient from TwitterClient.java
        client = TwitterApp.getRestClient(this);

        swipeContainer = findViewById(R.id.swipeContainer);
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "fetching new data!");
                populateHomeTimeline();
            }
        });

        // Find the recycler view
        rvTweets = findViewById(R.id.rvTweets);
        // Initialize the list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);
        // Recycler view setup: layout manager and the adapter
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setAdapter(adapter);

        populateHomeTimeline();
    }

    // Override onCreateOptionsMenu method to reference menu_main.xml resource file for inflation of app bar
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        // Boolean, so return true for menu to be displayed
        return true;
    }

    // Override onOptionsItemSelected to be notified of and handle clicks on app bar
    // Use android:onClick in menu_main.xml or onOptionsItemSelected method in Java file
    @Override
    // Pass in selected MenuItem and compare id of item to compose id to handle appropriately
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // If compose icon has been selected
        if (item.getItemId() == R.id.compose) {
//            Toast.makeText(this, "Compose!", Toast.LENGTH_SHORT).show();
            // Navigate to the compose activity from this activity
            Intent intent = new Intent(this, ComposeActivity.class);
            // Return newly composed tweet data to from child (Compose) to parent (Timeline) activity
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        }
        // Launch child (compose) activity
        return super.onOptionsItemSelected(item);
    }

    // Check if user has composed new tweet and handle accordingly
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        // ensure requestCode is same as defined above, data is from child activity
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Get data (tweet object) from the intent
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // Update RecyclerView with tweet
            // Modify data source (list) of tweets
            tweets.add(0, tweet);
            // Update adapter
            adapter.notifyItemInserted(0);
            // Scroll to very top
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void populateHomeTimeline() {
        // use TwitterClient to call API method
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                // info-level log
                Log.i(TAG, "onSuccess! " + json.toString());
                JSONArray jsonArray = json.jsonArray;
                // if json is parsed successfully, modify tweets defined above
                try {
                    // methods defined in TweetsAdapter.java
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(jsonArray));
                    // call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                // error-level log
                Log.e(TAG, "onFailure! " + response, throwable);
            }
        });
    }
}
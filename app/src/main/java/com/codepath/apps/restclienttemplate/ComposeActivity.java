package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    // set as constant
    public static final String TAG = "ComposeActivity";
    public static final int MAX_TWEET_LENGTH = 280;

    // Reference elements from activity_compose.xml
    EditText etCompose;
    Button btnTweet;
    TextInputLayout textInputLayout;

    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        client = TwitterApp.getRestClient(this);

        // Reference instances of elements from activity_compose.xml
        etCompose = findViewById(R.id.etCompose);
        btnTweet = findViewById(R.id.btnTweet);
        textInputLayout = findViewById(R.id.textInputLayout);

        textInputLayout.setCounterMaxLength(MAX_TWEET_LENGTH);

        // Set click listener on button
        btnTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Error handling if tweet is too long or short
                String tweetContent = etCompose.getText().toString();
                if (tweetContent.isEmpty()) {
                    // Optimally use Android Snackbar, rather than Toast
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tweetContent.length() > MAX_TWEET_LENGTH) {
                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet is too long", Toast.LENGTH_SHORT).show();
                    return;
                }
//                Toast.makeText(ComposeActivity.this, tweetContent, Toast.LENGTH_SHORT).show();
                // On click, make an API call to Twitter to publish the tweet
                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish tweet");
                        try {
                            // json object returned from API is a tweet model
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Published tweet says: " + tweet.body);
                            // create empty intent
                            Intent intent = new Intent();
                            // pass in data, convert to parcelable object to pass between activities
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            // set result code (shows API call succeeded) and bundle data for response
                            setResult(RESULT_OK, intent);
                            // close activity, pass data to parent
                            finish();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet", throwable);
                    }
                });

            }
        });
    }
}
//composeactivity pass back tweet object when API call succeeds, subactivity upon
//completion pass result to parent by creating new empty intent and put in data
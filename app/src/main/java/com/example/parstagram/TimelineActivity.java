package com.example.parstagram;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class TimelineActivity extends AppCompatActivity {

    public Toolbar toolbar;
    public RecyclerView rvTimeline;
    public final String TAG = "Timeline";
    protected PostAdapter adapter;
    protected List<Post> allPosts;
    private SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        // Find the toolbar view inside the activity layout
        toolbar = findViewById(R.id.my_toolbar);
        rvTimeline = findViewById(R.id.rvTimeline);
        // Sets the Toolbar to act as the ActionBar for this Activity window.
        // Make sure the toolbar exists in the activity and is not null
        setSupportActionBar(toolbar);
        //initialize all posts after this
        //queryPosts();
        allPosts = new ArrayList<>();
        adapter = new PostAdapter(this, allPosts);
        rvTimeline.setAdapter(adapter);
        // set the layout manager on the recycler view
        rvTimeline.setLayoutManager(new LinearLayoutManager(this));
        // query posts from Parstagram
        swipeContainer = findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(0);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        queryPosts();
        Log.e(TAG, "Query done");
        for (Post post : allPosts) {
            Log.i(TAG, "Post, " + post.getDescription() + ", username: " + post.getUser().getUsername());
        }
    }

    private void fetchTimelineAsync(int i) {
        adapter.clear();
        allPosts.clear();
        // ...the data has come back, add new items to your adapter...
        queryPosts();
        for (Post post : allPosts) {
            Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getUser().getUsername());
        }
        //adapter.addAll(allPosts);
        // Now we call setRefreshing(false) to signal refresh has finished
        swipeContainer.setRefreshing(false);
    }

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        /*if(item.getItemId() == R.id.composeButton){
            Toast.makeText(this, "Compose!", Toast.LENGTH_SHORT).show();
            return true;
        }*/
        if(item.getItemId() == R.id.menuLogout){
            Log.e(TAG, "Logging out of " + ParseUser.getCurrentUser());
            ParseUser.logOutInBackground();
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
            //Log.e(TAG, "User now: " + ParseUser.getCurrentUser());
        }
        if(item.getItemId() == R.id.menuCreate){
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }
        if(item.getItemId() == R.id.menuHome){
            rvTimeline.smoothScrollToPosition(0);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void queryPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // include data referred by user key
        query.include(Post.KEY_USER);
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                // for debugging purposes let's print every post description to logcat
                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getDescription() + ", username: " + post.getUser().getUsername());
                }

                // save received posts to list and notify adapter of new data
                allPosts.addAll(posts);
                //adapter.addAll(allPosts);
                adapter.notifyDataSetChanged();
            }

        });
    }
}
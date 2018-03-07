package com.mwano.lauren.popular_movies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.mwano.lauren.popular_movies.model.Movie;
import com.mwano.lauren.popular_movies.utils.JsonUtils;
import com.mwano.lauren.popular_movies.utils.MovieApi;
import com.mwano.lauren.popular_movies.utils.NetworkUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler,
        NavigationView.OnNavigationItemSelectedListener {

    private static final String POPULAR = "popular";
    private static final String TOP_RATED = "top_rated";
    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private MovieAdapter mMovieAdapter;
    ArrayList<Movie> movies;
    private DrawerLayout mDrawer;
    private Toolbar mToolbar;



    // TODO Add Error message on screen when no internet connection

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get reference to RecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);
        // Set GridLinearLayout to RecyclerView
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        // Create new Adapter and set to RecyclerView in layout
        mMovieAdapter = new MovieAdapter(this, movies, this);
        mRecyclerView.setAdapter(mMovieAdapter);

        // Instantiate the navigation drawer
        mDrawer = (DrawerLayout)
                findViewById(R.id.drawer_layout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        // Set toolbar as action bar
        setSupportActionBar(mToolbar);

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, mDrawer, mToolbar,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close);
        if (mDrawer != null) {
           mDrawer.addDrawerListener(toggle);
        }
        toggle.syncState();

        NavigationView navigationView = (NavigationView)
                findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(this);

        loadMovieData(POPULAR);
        setTitle("Popular Movies App");
        //Log.i(TAG, String.valueOf(movies));
        }
    }

    @Override
    public void onClick(Movie currentMovie) {
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent (context, destinationClass);
        intentToStartDetailActivity.putExtra("movie", currentMovie);
        startActivity(intentToStartDetailActivity);
    }

    /**
     * Create an AsyncTask for the http connection and JSON parsing
     */
    public class FetchMoviesTask extends AsyncTask <String, Void, ArrayList<Movie>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }

            String movieSort = params[0];
            URL movieRequestUrl = MovieApi.buildUrl(movieSort);

            try {
               switch (movieSort) {
                   case POPULAR:
                       movieRequestUrl = MovieApi.buildUrl(POPULAR);
                       break;
                   case TOP_RATED:
                       movieRequestUrl = MovieApi.buildUrl(TOP_RATED);
                       break;
                   // Default exception
                   default:
                       throw new UnsupportedOperationException("Unknown url: " + movieRequestUrl);               }

                String jsonResponse = NetworkUtils.httpConnect(movieRequestUrl);
                // Log.i(TAG, jsonResponse);
                // jsonResponse ok.  OK connection
                return JsonUtils.parseMovieJson(jsonResponse);

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> movies) {
            super.onPostExecute(movies);
            if (movies != null) {
                mMovieAdapter.setMovieData(movies);
            } else {
                Log.i(TAG, "Error displaying movies");
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Context context = MainActivity.this;
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.nav_popular:
                // Handle the camera import action (for now display a toast).
                drawer.closeDrawer(GravityCompat.START);
                loadMovieData(POPULAR);
                setTitle("Popular Movies");
                return true;
            case R.id.nav_top_rated:
                // Handle the gallery action (for now display a toast).
                drawer.closeDrawer(GravityCompat.START);
                loadMovieData(TOP_RATED);
                setTitle("Top Rated Movies");
                return true;
            case R.id.nav_favourite:
                // Handle the slideshow action (for now display a toast).
                drawer.closeDrawer(GravityCompat.START);
                Toast.makeText(context, getString(R.string.toast_favourites), Toast.LENGTH_LONG).show();
                return true;
            default:
                return false;
        }
    }

    private void loadMovieData(String sortMode) {
        new FetchMoviesTask().execute(sortMode);
    }

}
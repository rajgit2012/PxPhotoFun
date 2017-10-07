package com.workshop.pxphotofun;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.workshop.pxphotofun.service.Photo;
import com.workshop.pxphotofun.service.PhotoApiService;
import com.workshop.pxphotofun.service.PhotoSearchResults;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.text.CollationElementIterator;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;


public class PhotoListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final int NUM_COLUMNS = 1;

    private RecyclerView mPxListView;
    private MenuItem searchMenuItem;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_px_list);

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(PhotoApiService.API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final PhotoApiService service = retrofit.create(PhotoApiService.class);

        mPxListView = (RecyclerView) findViewById(R.id.px_list);
        mPxListView.setClickable(true);
        mPxListView.setHasFixedSize(true);
        mPxListView.setLayoutManager(new GridLayoutManager(this, NUM_COLUMNS,
          GridLayoutManager.VERTICAL, false));

        /*mPxListView.setLayoutManager(new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL));*/

        final Call<PhotoSearchResults> results = service.searchPhotos("summer");
        results.enqueue(new Callback<PhotoSearchResults>() {
            @Override
            public void onResponse(Response<PhotoSearchResults> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    System.out.println("Server return " + response.body());
                    success(response.body());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                failure(t.getMessage());
            }
        });

        checkForUpdates();
    }

    public void success(PhotoSearchResults results) {
        mPxListView.setAdapter(new PxListAdapter(this, results.photos));
    }

    public void failure(String error) {
        Toast.makeText(this, "Failed to load photo list: " + error, Toast.LENGTH_LONG).show();
    }


    private static class PxListAdapter extends RecyclerView.Adapter<AppViewHolder> {
        private final LayoutInflater mInflater;
        private final Picasso mPicasso;
        private final List<Photo> mPhotos;
        private TextView name;

        public PxListAdapter(Context context, List<Photo> photos) {
            mInflater = LayoutInflater.from(context);
            mPicasso = Picasso.with(context);
            mPhotos = photos;
        }

        @Override
        public int getItemCount() {
            return mPhotos.size();
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup parent, int position) {
            final View view = mInflater.inflate(R.layout.px_frame, parent, false);
            return new AppViewHolder(view);
        }

        @Override
        public void onBindViewHolder(AppViewHolder holder, int position) {
            mPicasso.load(mPhotos.get(position).image_url)
//                    .placeholder(R.drawable.placeholder)
                    .into(holder.image);


            holder.name.setText(mPhotos.get(position).name);
            holder.title.setText(mPhotos.get(position).description);

        }
    }

    private static class AppViewHolder extends RecyclerView.ViewHolder {
        public final ImageView image;
        public final TextView name;
        public final TextView title;

        //name

        public AppViewHolder(View view) {
            super(view);
            this.image = (ImageView) view.findViewById(R.id.image);
            this.name = (TextView) view.findViewById(R.id.txtDesc);
            this.title = (TextView) view.findViewById(R.id.txtTitle);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager)
                getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchMenuItem.getActionView();

        searchView.setSearchableInfo(searchManager.
                getSearchableInfo(getComponentName()));
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        Toast.makeText(this, "Data "+ newText, Toast.LENGTH_SHORT).show();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        // ... your own onResume implementation
        checkForCrashes();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterManagers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterManagers();
    }

    private void checkForCrashes() {
        CrashManager.register(this);
    }

    private void checkForUpdates() {
        // Remove this for store builds!
        UpdateManager.register(this);
    }

    private void unregisterManagers() {
        UpdateManager.unregister();
    }
}

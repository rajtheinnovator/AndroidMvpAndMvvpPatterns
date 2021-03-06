package com.enpassio.androidmvpandmvvmpatterns.mvvmbyabhi.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.enpassio.androidmvpandmvvmpatterns.R;
import com.enpassio.androidmvpandmvvmpatterns.mvvmbyabhi.data.model.Article;
import com.enpassio.androidmvpandmvvmpatterns.mvvmbyabhi.viewmodel.ArticlesViewModel;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;

public class MainActivityMvvmAbhi extends AppCompatActivity {

    private ImageButton button;
    private ArticlesViewModel articlesViewModel;
    /* Adapters for inflating different recyclerview */
    private ArticlePagedListAdapterMvvmAbhi mNewsAdapter;
    /* Set layout managers on those recycler views */
    private LinearLayoutManager newslayoutmanager;
    private RecyclerView mNewsrecyclerView;
    private EditText searchQueryEditText;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mUsersSearchQuery;
    private ShimmerFrameLayout mShimmerViewContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_mvvm_abhi);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mNewsrecyclerView = findViewById(R.id.main_recycler_view);
        searchQueryEditText = findViewById(R.id.search_query_edit_text);

        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setVisibility(View.GONE);
        mShimmerViewContainer = findViewById(R.id.shimmer_view_container);
        mShimmerViewContainer.setVisibility(View.VISIBLE);
        mShimmerViewContainer.startShimmer();
        /*
         * Setup layout manager for items with orientation
         * Also supports `LinearLayoutManager.HORIZONTAL`
         */
        newslayoutmanager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        /* Attach layout manager to the RecyclerView */
        mNewsrecyclerView.setLayoutManager(newslayoutmanager);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mNewsAdapter = new ArticlePagedListAdapterMvvmAbhi(this, fragmentManager);
        mNewsrecyclerView.setAdapter(mNewsAdapter);

        button = findViewById(R.id.main_button);
        articlesViewModel = ViewModelProviders.of(this).get(ArticlesViewModel.class);
        //ob loading, show news from India
        mUsersSearchQuery = "India";
        observeNewsFromViewModel();

        //get users input and query for topic from news-api
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsersSearchQuery = searchQueryEditText.getText().toString().trim();
                if (mUsersSearchQuery.isEmpty()) {
                    Toast.makeText(MainActivityMvvmAbhi.this, "Please input a news topic to search", Toast.LENGTH_SHORT).show();
                } else {
                    observeNewsFromViewModel();
                }
            }
        });

        //refresh news from news-api
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mUsersSearchQuery == null || mUsersSearchQuery.isEmpty()) {
                    Toast.makeText(MainActivityMvvmAbhi.this, "Showing top news from India", Toast.LENGTH_SHORT).show();
                    mUsersSearchQuery = "India";
                }
                observeNewsFromViewModel();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.favorite:
                startActivity(new Intent(this, FavoriteActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void observeNewsFromViewModel() {
        articlesViewModel.getArticleLiveData(mUsersSearchQuery)
                .observe(MainActivityMvvmAbhi.this, new Observer<PagedList<Article>>() {
                    @Override
                    public void onChanged(@Nullable PagedList<Article> pagedLists) {

                        if (pagedLists != null && !pagedLists.isEmpty()) {
                            ArrayList<Article> articles = new ArrayList<>();
                            articles.addAll(pagedLists);
                            mNewsAdapter.setArticlesList(articles);
                            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
                            mSwipeRefreshLayout.setRefreshing(false);
                            mShimmerViewContainer.setVisibility(View.GONE);
                            mShimmerViewContainer.stopShimmer();
                            mNewsAdapter.submitList(pagedLists);
                        }
                    }
                });
    }
}

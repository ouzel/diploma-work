package com.example.recycleapp.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recycleapp.R;
import com.example.recycleapp.data.FavoritesManager;
import com.example.recycleapp.data.model.Post;
import com.example.recycleapp.ui.AddPostDialog;
import com.example.recycleapp.ui.adapter.PostAdapter;
import com.example.recycleapp.viewmodel.NewsViewModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kwabenaberko.newsapilib.NewsApiClient;
import com.kwabenaberko.newsapilib.models.Article;
import com.kwabenaberko.newsapilib.models.request.EverythingRequest;
import com.kwabenaberko.newsapilib.models.response.ArticleResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NewsFragment extends Fragment implements AddPostDialog.AddPostListener {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private boolean isLoading = false;
    private boolean showingFavorites = false;
    private boolean filteringNearby = false;
    private int currentPage = 1;
    private static final int PAGE_SIZE = 20;
    private static final String API_KEY = "f6cc0e7a98fe4a889ab85a0afce32b7f";
    private FavoritesManager favoritesManager;
    private long lastRequestTime = 0;
    private static final long REQUEST_DELAY = 5000;
    private SwitchCompat switchFilterNearby;
    private double currentLatitude = 55.7558;
    private double currentLongitude = 37.6173;
    private static final double DEFAULT_RADIUS_KM = 5.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        favoritesManager = new FavoritesManager(requireContext());

        recyclerView = view.findViewById(R.id.recycler_view_posts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList, favoritesManager);
        recyclerView.setAdapter(postAdapter);

        view.findViewById(R.id.btn_add_post).setOnClickListener(v -> showAddPostDialog());

        switchFilterNearby = view.findViewById(R.id.switch_filter_nearby);
        switchFilterNearby.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filteringNearby = isChecked;
            updateDisplayedPosts();
        });

        view.findViewById(R.id.filter_favorites).setOnClickListener(v -> {
            showingFavorites = !showingFavorites;
            updateDisplayedPosts();
        });

        setupPagination();
        loadAllPosts();

        return view;
    }

    private void setupPagination() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!isLoading && dy > 0) {
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null && layoutManager.findLastVisibleItemPosition() >= postList.size() - 1) {
                        currentPage++;
                        loadNewsApiPosts(currentPage);
                    }
                }
            }
        });
    }

    private void loadAllPosts() {
        postList.clear();
        currentPage = 1;
        loadFirebasePosts();
        loadNewsApiPosts(currentPage);
    }

    private void loadNewsApiPosts(int page) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRequestTime < REQUEST_DELAY) {
            return;
        }
        lastRequestTime = currentTime;
        isLoading = true;

        NewsApiClient newsApiClient = new NewsApiClient(API_KEY);
        newsApiClient.getEverything(
                new EverythingRequest.Builder()
                        .q("environment")
                        .page(page)
                        .pageSize(PAGE_SIZE)
                        .language("ru")
                        .build(),
                new NewsApiClient.ArticlesResponseCallback() {
                    @Override
                    public void onSuccess(ArticleResponse response) {
                        List<Post> newsApiPosts = new ArrayList<>();
                        for (Article article : response.getArticles()) {
                            Post newPost = new Post(
                                    article.getTitle() != null ? article.getTitle() : "No Title",
                                    article.getDescription() != null ? article.getDescription() : "No Description",
                                    article.getAuthor() != null ? article.getAuthor() : "Unknown Author",
                                    null,
                                    0,
                                    formatPublishedAt(article.getPublishedAt())
                            );
                            newsApiPosts.add(newPost);
                        }

                        postList.addAll(newsApiPosts);
                        updateDisplayedPosts();
                        isLoading = false;
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("NewsAPI", "Error loading posts: " + throwable.getMessage());
                        isLoading = false;
                    }
                }
        );
    }

    private void loadFirebasePosts() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference postsRef = database.getReference("posts");

        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Post> firebasePosts = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String title = postSnapshot.child("title").getValue(String.class);
                    String text = postSnapshot.child("text").getValue(String.class);
                    String author = postSnapshot.child("author").getValue(String.class);
                    String gpsLocation = postSnapshot.child("gpsLocation").getValue(String.class);
                    String publishedAt = postSnapshot.child("publishedAt").getValue(String.class);
                    Integer likes = postSnapshot.child("likes").getValue(Integer.class);
                    if (likes == null) likes = 0;

                    firebasePosts.add(new Post(title, text, author, gpsLocation, likes, publishedAt));
                }

                postList.addAll(firebasePosts);
                updateDisplayedPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Error loading posts", error.toException());
            }
        });
    }

    private void updateDisplayedPosts() {
        List<Post> filteredPosts = new ArrayList<>(postList);

        if (showingFavorites) {
            filteredPosts.retainAll(favoritesManager.getFavoritePosts());
        }

        if (filteringNearby) {
            filteredPosts = filterNearbyPosts(filteredPosts, DEFAULT_RADIUS_KM);
        }

        postAdapter.updatePosts(filteredPosts);
    }

    private List<Post> filterNearbyPosts(List<Post> posts, double radiusKm) {
        List<Post> nearbyPosts = new ArrayList<>();
        for (Post post : posts) {
            if (post.getGpsLocation() != null) {
                String[] latLng = post.getGpsLocation().split(",");
                if (latLng.length == 2) {
                    try {
                        double postLatitude = Double.parseDouble(latLng[0]);
                        double postLongitude = Double.parseDouble(latLng[1]);
                        double distance = calculateDistance(currentLatitude, currentLongitude, postLatitude, postLongitude);
                        if (distance <= radiusKm) {
                            nearbyPosts.add(post);
                        }
                    } catch (NumberFormatException e) {
                        Log.e("FilterNearby", "Invalid GPS format: " + post.getGpsLocation(), e);
                    }
                }
            }
        }
        return nearbyPosts;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private String formatPublishedAt(String publishedAt) {
        if (publishedAt == null || publishedAt.isEmpty()) {
            return "Не указано";
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy", Locale.getDefault());
            Date date = inputFormat.parse(publishedAt);
            return outputFormat.format(date);
        } catch (Exception e) {
            return "Не указано";
        }
    }

    private void showAddPostDialog() {
        AddPostDialog dialog = new AddPostDialog();
        dialog.show(getChildFragmentManager(), "AddPostDialog");
    }

    @Override
    public void onPostAdded(Post post) {
        postList.add(0, post);
        updateDisplayedPosts();
    }
}





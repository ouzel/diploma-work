package com.example.recycleapp.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.recycleapp.data.model.Post;
import com.example.recycleapp.data.model.RecyclingPoint;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {

    private static final String PREF_NAME = "favorites_prefs";
    private static final String KEY_FAVORITES = "favorites_list";
    private static final String KEY_FAVORITES_POINTS = "favorites_points_list";
    private SharedPreferences sharedPreferences;
    private Gson gson;

    public FavoritesManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public List<Post> getFavoritePosts() {
        String json = sharedPreferences.getString(KEY_FAVORITES, null);
        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<Post>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void addPostToFavorites(Post post) {
        List<Post> favorites = getFavoritePosts();
        if (!favorites.contains(post)) {
            favorites.add(post);
            saveFavorites(favorites);
        }
    }

    public void removePostFromFavorites(Post post) {
        List<Post> favorites = getFavoritePosts();
        if (favorites.contains(post)) {
            favorites.remove(post);
            saveFavorites(favorites);
        }
    }

    private void saveFavorites(List<Post> favorites) {
        String json = gson.toJson(favorites);
        sharedPreferences.edit().putString(KEY_FAVORITES, json).apply();
    }

    // Методы для работы с избранными пунктами RecyclingPoint
    public List<RecyclingPoint> getFavoritePoints() {
        String json = sharedPreferences.getString(KEY_FAVORITES_POINTS, null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<List<RecyclingPoint>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void addPointToFavorites(RecyclingPoint point) {
        List<RecyclingPoint> favorites = getFavoritePoints();
        if (!favorites.contains(point)) {
            favorites.add(point);
            saveFavoritePoints(favorites);
        }
    }

    public void removePointFromFavorites(RecyclingPoint point) {
        List<RecyclingPoint> favorites = getFavoritePoints();
        if (favorites.contains(point)) {
            favorites.remove(point);
            saveFavoritePoints(favorites);
        }
    }

    private void saveFavoritePoints(List<RecyclingPoint> favorites) {
        String json = gson.toJson(favorites);
        sharedPreferences.edit().putString(KEY_FAVORITES_POINTS, json).apply();
    }
}


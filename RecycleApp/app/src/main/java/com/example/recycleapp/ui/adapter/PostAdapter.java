package com.example.recycleapp.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recycleapp.R;
import com.example.recycleapp.data.FavoritesManager;
import com.example.recycleapp.data.model.Post;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final List<Post> postList;
    private final Context context;
    private final FavoritesManager favoritesManager; // Менеджер избранных постов

    public PostAdapter(Context context, List<Post> postList, FavoritesManager favoritesManager) {
        this.context = context;
        this.postList = new ArrayList<>(postList); // Копируем список, чтобы избежать побочных эффектов
        this.favoritesManager = favoritesManager;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);

        // Установка текста
        holder.postTitle.setText(post.getTitle());
        holder.postAuthor.setText(post.getAuthor());
        holder.postText.setText(post.getText());
        holder.postPublishedAt.setText(post.getPublishedAt() != null ? post.getPublishedAt() : "Не указано");

        // Установка GPS-локации
        if (post.getGpsLocation() != null && !post.getGpsLocation().isEmpty()) {
            holder.gpsLocation.setText(post.getGpsLocation());
            holder.gpsLocation.setVisibility(View.GONE);
        } else {
            holder.gpsLocation.setVisibility(View.GONE);
        }

        // Установка правильной иконки "Like"
        updateLikeButton(holder, post);

        // Обработка клика на кнопку "Like"
        holder.likeButton.setOnClickListener(v -> {
            if (favoritesManager.getFavoritePosts().contains(post)) {
                favoritesManager.removePostFromFavorites(post);
            } else {
                favoritesManager.addPostToFavorites(post);
            }
            notifyItemChanged(holder.getAdapterPosition()); // Обновляем только измененный элемент
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    // Метод обновления списка постов
    public void updatePosts(List<Post> newPosts) {
        postList.clear();
        postList.addAll(newPosts);
        notifyDataSetChanged();
    }

    // Метод для установки иконки "Like"
    private void updateLikeButton(PostViewHolder holder, Post post) {
        if (favoritesManager.getFavoritePosts().contains(post)) {
            holder.likeButton.setImageResource(R.drawable.like_up);
        } else {
            holder.likeButton.setImageResource(R.drawable.like_down);
        }
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView postTitle;
        TextView postAuthor;
        TextView postText;
        TextView gpsLocation;
        TextView postPublishedAt;
        ImageButton likeButton;
        ImageButton commentButton;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            postTitle = itemView.findViewById(R.id.tv_post_title);
            postAuthor = itemView.findViewById(R.id.tv_post_author);
            postText = itemView.findViewById(R.id.tv_post_text);
            gpsLocation = itemView.findViewById(R.id.tv_gps_location);
            postPublishedAt = itemView.findViewById(R.id.tv_post_time);
            likeButton = itemView.findViewById(R.id.btn_like);
            commentButton = itemView.findViewById(R.id.btn_comment);
        }
    }
}


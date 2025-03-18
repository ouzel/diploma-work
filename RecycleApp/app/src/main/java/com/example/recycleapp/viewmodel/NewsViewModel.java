package com.example.recycleapp.viewmodel;

import androidx.lifecycle.ViewModel;

import com.example.recycleapp.data.model.Post;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewsViewModel extends ViewModel {

    private final DatabaseReference postsRef;

    public NewsViewModel() {
        postsRef = FirebaseDatabase.getInstance().getReference("posts");
    }

    public void addPost(Post post) {
        String postId = postsRef.push().getKey();
        if (postId != null) {
            postsRef.child(postId).setValue(post);
        }
    }
}

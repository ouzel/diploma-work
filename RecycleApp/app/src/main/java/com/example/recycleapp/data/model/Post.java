package com.example.recycleapp.data.model;

import java.util.Objects;

public class Post {
    private String title; // Заголовок поста
    private String text;  // Текст поста
    private String author; // Автор поста
    private String gpsLocation;
    private int likes;
    private String publishedAt; // Время публикации


    public Post(String title, String text, String author, String gpsLocation, int likes) {
        this.title = title;
        this.text = text;
        this.author = author;
        this.gpsLocation = gpsLocation;
        this.likes = likes;
    }

    public Post(String title, String text, String author, String gpsLocation, int likes, String publishedAt) {
        this.title = title;
        this.text = text;
        this.author = author;
        this.gpsLocation = gpsLocation;
        this.likes = likes;
        this.publishedAt = publishedAt;
    }

    // Геттеры и сеттеры для нового функционала
    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGpsLocation() {
        return gpsLocation;
    }

    public void setGpsLocation(String gpsLocation) {
        this.gpsLocation = gpsLocation;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Post post = (Post) o;
        return Objects.equals(title, post.title) &&
                Objects.equals(text, post.text) &&
                Objects.equals(author, post.author) &&
                Objects.equals(publishedAt, post.publishedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, text, author, publishedAt);
    }
}


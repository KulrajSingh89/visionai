package com.oracle.cloud.sdk.samples;

public class ObjectListResponse {
    private int id;
    private String name;
    private String title;
    private String imageUrl;

    public int getId() {
        return id;
    }

    public ObjectListResponse(int id, String name, String title, String imageUrl) {
        this.id = id;
        this.name = name;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "ObjectListResponse{" + "id=" + id + ", name='" + name + '\'' + ", title='" + title + '\'' + ", imageUrl='" + imageUrl + '\'' + '}';
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }


}

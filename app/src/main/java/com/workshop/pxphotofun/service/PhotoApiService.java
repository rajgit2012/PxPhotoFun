package com.workshop.pxphotofun.service;

import com.workshop.pxphotofun.BuildConfig;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public interface PhotoApiService {
    String API_URL = "https://api.500px.com/";
    String CONSUMER_KEY = "ONRy6AqHPetj3eFczUAh6OOyvzJK7SEBbcGuyNw3";//BuildConfig.PX_API_KEY;

    @GET("/v1/photos/search?image_size=4&consumer_key=" + CONSUMER_KEY)
    Call<PhotoSearchResults> searchPhotos(@Query("term") String query);
}

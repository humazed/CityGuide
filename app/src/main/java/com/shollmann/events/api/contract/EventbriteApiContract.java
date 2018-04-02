package com.shollmann.events.api.contract;

import com.shollmann.events.api.model.PaginatedEvents;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface EventbriteApiContract {
    @GET("/v3/events/search/?&organizer.id=17150638933")
    Call<PaginatedEvents> getEvents(
            @Query("q") String query,
            @Query("location.latitude") double latitude,
            @Query("location.longitude") double longitude,
//            @Query("categories") String categories,
            @Query("page") int pageNumber);

    //&organizer.id=17145795263
    @GET("/v3/events/search/?location.within=100km&organizer.id=17150638933")
    Call<PaginatedEvents> getEventsWithLocation(
            @Query("location.latitude") double latitude,
            @Query("location.longitude") double longitude,
            @Query("page") int pageNumber);

}

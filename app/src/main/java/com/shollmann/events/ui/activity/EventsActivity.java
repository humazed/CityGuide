package com.shollmann.events.ui.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.shollmann.events.R;
import com.shollmann.events.api.EventbriteApi;
import com.shollmann.events.api.baseapi.CallId;
import com.shollmann.events.api.baseapi.CallOrigin;
import com.shollmann.events.api.baseapi.CallType;
import com.shollmann.events.api.model.Event;
import com.shollmann.events.api.model.PaginatedEvents;
import com.shollmann.events.helper.Constants;
import com.shollmann.events.helper.PreferencesHelper;
import com.shollmann.events.helper.ResourcesHelper;
import com.shollmann.events.ui.EventbriteApplication;
import com.shollmann.events.ui.adapter.EventAdapter;
import com.shollmann.events.ui.event.LoadMoreEvents;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EventsActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private static final String TAG = EventsActivity.class.getSimpleName();

    private static final int NO_FLAGS = 0;
    private static final int ACCESS_COARSE_LOCATION_PERMISSION_REQUEST = 7001;

    private Toolbar toolbar;
    private TextView txtNoResults;
    private TextView txtWaitForResults;
    private SearchView searchView;
    private MenuItem menuSearch;
    private CoordinatorLayout coordinatorLayout;
    private EventbriteApi eventbriteApi;
    private Location location;
    private RecyclerView recyclerEvents;
    private EventAdapter eventAdapter;
    private PaginatedEvents lastPageLoaded;
    private String currentQuery;
    private CallId getEventsCallId;

    public static int PLACE_PICKER_REQUEST = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        eventbriteApi = EventbriteApplication.getApplication().getApiEventbrite();

        findViews();
        setupTaskDescription();
        setupToolbar();
        setupRecyclerView();
        checkForLocationPermission();


    }

    private void setupRecyclerView() {
        recyclerEvents.setHasFixedSize(true);

        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(new ArrayList<Event>());
        recyclerEvents.setAdapter(eventAdapter);

        recyclerEvents.setVisibility(View.GONE);
    }

    private void updateEventsList(List<Event> eventList) {
        eventAdapter.add(eventList);
        eventAdapter.notifyDataSetChanged();

        if (recyclerEvents.getVisibility() != View.VISIBLE) {
            recyclerEvents.setVisibility(View.VISIBLE);
        }
    }

    private void checkForLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_COARSE_LOCATION_PERMISSION_REQUEST);

        } else {
            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            getEvents(null);
        }
    }

    private void getEvents(String query) {
        Snackbar.make(coordinatorLayout, R.string.getting_events, Snackbar.LENGTH_SHORT).show();
        getEventsCallId = new CallId(CallOrigin.HOME, CallType.GET_EVENTS);
        Callback<PaginatedEvents> callback = generateGetEventsCallback();
        eventbriteApi.registerCallback(getEventsCallId, callback);

        eventbriteApi.getEvents(query, 0, 0,
                lastPageLoaded, getEventsCallId, callback);
        PreferencesHelper.setLastSearch(query);
    }

    private void getEvents(double lat, double lon) {
        Log.d(TAG, "getEvents() called with: " + "lat = [" + lat + "], lon = [" + lon + "]");
        Snackbar.make(coordinatorLayout, R.string.getting_events, Snackbar.LENGTH_SHORT).show();
        getEventsCallId = new CallId(CallOrigin.HOME, CallType.GET_EVENTS_BY_LOCATION);
        Callback<PaginatedEvents> callback = generateGetEventsCallback();
        eventbriteApi.registerCallback(getEventsCallId, callback);

        eventbriteApi.getEventsWithLocation(lat, lon, lastPageLoaded, getEventsCallId, callback);

//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("https://api.github.com/")
//                .build();
//
//        EventbriteApiContract api = retrofit.create(EventbriteApiContract.class);
//https://www.eventbriteapi.com/v3/events/search/?location.latitude=30.055245&location.longitude=31.2901943&token=VBEQ2ZP7SOEWDHH3PVOI
//https://www.eventbriteapi.com/v3/events/search/?location.latitude=30.055245&location.longitude=31.2901943&token=VBEQ2ZP7SOEWDHH3PVOI
//        PreferencesHelper.setLastSearch("");
//        eventAdapter.notifyDataSetChanged();
    }

    private Callback<PaginatedEvents> generateGetEventsCallback() {
        return new Callback<PaginatedEvents>() {
            @Override
            public void onResponse(Call<PaginatedEvents> call, Response<PaginatedEvents> response) {
                Log.d(TAG, "onResponse() called with: " + "call = [" + call + "], response = [" + response.body().getEvents() + "]");
                PaginatedEvents paginatedEvents = response.body();
                if (paginatedEvents.getEvents().isEmpty()) {
                    eventAdapter.setKeepLoading(false);
                    if (eventAdapter.getItemCount() == 0) {
                        txtNoResults.setVisibility(View.VISIBLE);
                        return;
                    }
                }
                txtNoResults.setVisibility(View.GONE);
                updateEventsList(paginatedEvents.getEvents());
                lastPageLoaded = paginatedEvents;
            }

            @Override
            public void onFailure(Call<PaginatedEvents> call, Throwable t) {
                handleGetEventsFailure();
            }
        };
    }

    private void handleGetEventsFailure() {
        txtWaitForResults.setVisibility(View.GONE);
        txtNoResults.setVisibility(View.VISIBLE);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void findViews() {
        toolbar = findViewById(R.id.toolbar);
        txtNoResults = findViewById(R.id.home_txt_no_results);
        txtWaitForResults = findViewById(R.id.home_txt_wait_first_time);
        coordinatorLayout = findViewById(R.id.home_coordinator_layout);
        recyclerEvents = findViewById(R.id.home_events_recycler);
    }

    private void setupTaskDescription() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bitmap icon = BitmapFactory.decodeResource(ResourcesHelper.getResources(),
                    R.mipmap.ic_launcher);
            ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(ResourcesHelper.getString(R.string.app_name), icon, ResourcesHelper.getResources().getColor(R.color.colorPrimary));
            this.setTaskDescription(taskDescription);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_search, menu);
        menuSearch = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(menuSearch);
        EditText edtSearch = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        ImageView searchClose = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClose.setImageResource(R.drawable.ic_action_close);
        edtSearch.setTextColor(Color.WHITE);
        edtSearch.setHintTextColor(Color.WHITE);
        searchView.setOnQueryTextListener(this);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_location:
                resetSearch();
                try {
                    startActivityForResult(new PlacePicker.IntentBuilder().build(this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    Log.e(TAG, "onOptionsItemSelected: ", e);
                }
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                if (place != null) {
                    getEvents(location.getLatitude(), location.getLongitude());
                    Log.d(TAG, "onOptionsItemSelected getEvents()");
                } else {
                    Toast.makeText(EventsActivity.this, "Error", Toast.LENGTH_LONG).show();
                }
            }

        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        resetSearch();
        currentQuery = query.trim();
        getEvents(currentQuery);
        searchView.setQuery(Constants.EMPTY_STRING, false);
        searchView.setIconified(true);
        hideKeyboard();
        return true;
    }

    private void resetSearch() {
        lastPageLoaded = null;
        eventAdapter.reset();
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public void hideKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) EventbriteApplication.getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), NO_FLAGS);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(LoadMoreEvents event) {
        getEvents(currentQuery);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        eventbriteApi.unregisterCallback(getEventsCallId);
        EventBus.getDefault().unregister(this);
    }
}

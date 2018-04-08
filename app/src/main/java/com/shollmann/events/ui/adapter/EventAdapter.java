package com.shollmann.events.ui.adapter;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shollmann.events.R;
import com.shollmann.events.api.model.Event;
import com.shollmann.events.helper.DateUtils;
import com.shollmann.events.ui.EventbriteApplication;
import com.shollmann.events.ui.activity.EventsActivity;
import com.shollmann.events.ui.event.LoadMoreEvents;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import saschpe.android.customtabs.CustomTabsHelper;
import saschpe.android.customtabs.WebViewFallback;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private static final String TAG = EventAdapter.class.getSimpleName();

    private EventsActivity context;
    private List<Event> listEvents;
    private boolean isKeepLoading;

    public EventAdapter(EventsActivity eventsActivity, ArrayList<Event> listEvents) {
        this.context = eventsActivity;
        this.listEvents = listEvents;
        this.isKeepLoading = true;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventAdapter.EventViewHolder holder, int position) {
        if (isKeepLoading && position == listEvents.size() - 10) {
            EventBus.getDefault().post(new LoadMoreEvents());
        }

        Log.d(TAG, "onBindViewHolder() called with: " + "holder = [" + holder + "], position = [" + position + "]");

        holder.setEvent(listEvents.get(position));
    }

    @Override
    public int getItemCount() {
        return listEvents.size();
    }

    public void add(List<Event> eventList) {
        listEvents.addAll(eventList);
    }

    public void setKeepLoading(boolean keepLoading) {
        isKeepLoading = keepLoading;
    }

    public void reset() {
        isKeepLoading = true;
        listEvents.clear();
        notifyDataSetChanged();
    }


    class EventViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView txtTitle;
        private TextView txtDate;
        private TextView txtIsFree;
        private ImageView imgCover;
        private CardView eventCard;

        public EventViewHolder(View view) {
            super(view);
            txtTitle = view.findViewById(R.id.event_txt_title);
            txtDate = view.findViewById(R.id.event_txt_date);
            txtIsFree = view.findViewById(R.id.event_txt_address);
            txtTitle = view.findViewById(R.id.event_txt_title);
            imgCover = view.findViewById(R.id.event_img_cover);
            eventCard = view.findViewById(R.id.event_cardview);
        }

        public void setEvent(Event event) {
            Log.d(TAG, "setEvent() called with: " + "event = [" + event + "]");

            txtTitle.setText(event.getName().getText());
            txtDate.setText(DateUtils.getEventDate(event.getStart().getLocal()));
            txtIsFree.setText(event.getIsFree() ? R.string.free : R.string.paid);
            if (event.getLogo() != null) {
                Picasso.with(EventbriteApplication.getApplication()).load(event.getLogo().getUrl()).into(imgCover);
            }

            final Event event1 = event;
            eventCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "event.getId() = " + event1.getId());


                    CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                            .addDefaultShareMenuItem()
                            .setToolbarColor(context.getResources().getColor(R.color.colorPrimary))
                            .setShowTitle(true)
                            .build();

                    // This is optional but recommended
                    CustomTabsHelper.addKeepAliveExtra(context, customTabsIntent.intent);

                    // This is where the magic happens...
                    CustomTabsHelper.openCustomTab(context, customTabsIntent,
                            Uri.parse(event1.getUrl()),
                            new WebViewFallback());

                    Log.d(TAG, "event.getId() = " + event1.getUrl());

                }
            });
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick() called with: " + "v = [" + v + "]");
            Toast.makeText(context, "ffff", Toast.LENGTH_LONG).show();
        }
    }
}

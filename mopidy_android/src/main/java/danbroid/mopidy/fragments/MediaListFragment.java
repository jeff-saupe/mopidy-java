package danbroid.mopidy.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.LinkedList;
import java.util.List;

import danbroid.mopidy.R;
import danbroid.mopidy.glide.GlideApp;
import danbroid.mopidy.interfaces.MainView;
import danbroid.mopidy.interfaces.MediaContentView;


/**
 * Created by dan on 25/08/17.
 */

@EFragment(resName = "refreshable_list")
public class MediaListFragment extends MediaFragment implements MediaContentView {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MediaListFragment.class);
	private String mediaId;

	@ViewById(resName = "busy_indicator")
	protected View busyIndicator;


	private final ContentObserver contentObserver = new ContentObserver(null) {
		@Override
		public void onChange(boolean selfChange) {
			onContentChanged();
		}
	};

	protected void onContentChanged() {
		log.debug("onContentChanged()");
	}


	public static MediaListFragment getInstance(String mediaID) {
		return MediaListFragment_.builder().arg(ARG_MEDIA_ID, mediaID).build();
	}

	@ViewById(resName = "spinner")
	protected View spinner;

	@ViewById(resName = "empty_text")
	protected TextView emptyText;

	@ViewById(resName = "recycler_view")
	protected RecyclerView recyclerView;

	@ViewById(resName = "swipe_refresh")
	protected SwipeRefreshLayout swipeRefreshLayout;

	protected String mediaID;

	protected RecyclerView.Adapter<MediaItemViewHolder> adapter;


	private final BroadcastReceiver connectivityChangeListener = new BroadcastReceiver() {
		private boolean oldOnline = false;

		@Override
		public void onReceive(Context context, Intent intent) {
			log.debug("onReceive(): {}", intent);
//			boolean isOnline = NetworkHelper.isOnline(context);
			//Toast.makeText(context,"Online: " + isOnline,Toast.LENGTH_SHORT).show();
			//		log.warn("isOnline: {}", isOnline);

			// We don't care about network changes while this fragment is not associated
			// with a media ID (for example, while it is being initialized)
	 /*   if (mediaId != null) {
	      boolean isOnline = NetworkHelper.isOnline(context);
        if (isOnline != oldOnline) {
          oldOnline = isOnline;
          checkForUserVisibleErrors(false);
          if (isOnline) {
            adapter.notifyDataSetChanged();
          }
        }
      }*/
		}
	};


	@Override
	protected void onPlaybackStateChanged(PlaybackStateCompat state) {
		super.onPlaybackStateChanged(state);

	}

	@Override
	protected void onMetadataChanged(MediaMetadataCompat metadata) {
		super.onMetadataChanged(metadata);


		//adapter.notifyDataSetChanged();
	}

	private List<MediaBrowserCompat.MediaItem> data = new LinkedList<>();


	class MediaItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		ImageView imageView;
		TextView titleView;
		TextView descriptionView;
		private MediaBrowserCompat.MediaItem item;

		public MediaItemViewHolder(View itemView) {
			super(itemView);
			imageView = itemView.findViewById(R.id.icon);
			titleView = itemView.findViewById(R.id.title);
			descriptionView = itemView.findViewById(R.id.description);
			itemView.setOnClickListener(this);
		}

		public void bind(MediaBrowserCompat.MediaItem mediaItem) {
			this.item = mediaItem;
			MediaDescriptionCompat info = item.getDescription();
			titleView.setText(info.getTitle());
			descriptionView.setText(info.getDescription());


			Uri iconURI = mediaItem.getDescription().getIconUri();

			if (iconURI == null) {
				imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_folder));

			} else {
				GlideApp.with(MediaListFragment.this)
						.load(iconURI)
						.fallback(R.drawable.ic_folder)
						.transition(DrawableTransitionOptions.withCrossFade())
						.apply(RequestOptions.bitmapTransform(new RoundedCorners(8)))
						.into(imageView);
			}

		}

		@Override
		public void onClick(View view) {
			getMainView().onMediaItemSelected(item);
		}
	}



	protected void init() {
		super.init();
		mediaId = getArguments().getString(ARG_MEDIA_ID, null);
		log.trace("init() :{}", mediaId);
		setEmptyText(R.string.msg_loading);


		getActivity().setTitle(mediaId);
		swipeRefreshLayout.setEnabled(false);


		adapter = new RecyclerView.Adapter<MediaItemViewHolder>() {
			@Override
			public long getItemId(int position) {
				return data.get(position).getMediaId().hashCode();
			}

			@Override
			public MediaItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
				return new MediaItemViewHolder(getLayoutInflater().inflate(R.layout.media_list_item, viewGroup, false));
			}

			@Override
			public void onBindViewHolder(MediaItemViewHolder viewHolder, int position) {
				viewHolder.bind(data.get(position));
			}

			@Override
			public int getItemCount() {
				return data.size();
			}
		};

		adapter.setHasStableIds(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
		recyclerView.setAdapter(adapter);
	}

	public void setEmptyText(@StringRes int msg_id) {
		setEmptyText(getString(msg_id));
	}

	public void setEmptyText(CharSequence msg) {
		emptyText.setText(msg);
		spinner.setVisibility(View.VISIBLE);
		recyclerView.setVisibility(View.INVISIBLE);
	}

	public String getMediaId() {
		return getArguments().getString(ARG_MEDIA_ID, null);
	}


	@Override
	public void onConnected() {
		super.onConnected();

		if (isDetached()) {
			log.trace("isDetached");
			return;
		}

		if (getActivity() == null) {
			log.trace("activity is null");
			return;
		}

		mediaId = getMediaId();
		if (mediaId == null) {
			mediaId = getMainView().getMediaBrowser().getRoot();
		}

		log.trace("onConnected() mediaID:{}", mediaId);


		// Unsubscribing before subscribing is required if this mediaId already has a subscriber
		// on this MediaBrowser instance. Subscribing to an already subscribed mediaId will replace
		// the callback, but won't trigger the initial callback.onChildrenLoaded.
		//
		// This is temporary: A bug is being fixed that will make subscribe
		// consistently call onChildrenLoaded initially, no matter if it is replacing an existing
		// subscriber or not. Currently this only happens if the mediaID has no previous
		// subscriber or if the media content changes on the service side, so we need to
		// unsubscribe first.
		MediaBrowserCompat mediaBrowser = getMainView().getMediaBrowser();
		mediaBrowser.unsubscribe(mediaId);
//		mainView.getMediaBrowser().


		mediaBrowser.subscribe(mediaId, subscriptionCallback);


	}

	@Override
	public boolean onBackButton() {
		log.error("onBackButton(): " + mediaId);
		return false;
	}


	@Override
	public void onStart() {
		super.onStart();
		// Registers BroadcastReceiver to track network connection changes.
		this.getActivity().registerReceiver(connectivityChangeListener,
				new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

	}

	@Override
	public void onStop() {
		super.onStop();
		MediaBrowserCompat mediaBrowser = getMainView().getMediaBrowser();
		if (mediaBrowser != null && mediaBrowser.isConnected() && mediaId != null) {
			mediaBrowser.unsubscribe(mediaId);
		}

		getActivity().unregisterReceiver(connectivityChangeListener);
	}

	private final MediaBrowserCompat.SubscriptionCallback subscriptionCallback =
			new MediaBrowserCompat.SubscriptionCallback() {
				@Override
				public void onChildrenLoaded(@NonNull String parentId,
				                             @NonNull List<MediaBrowserCompat.MediaItem> children) {
					try {
						log.warn("onChildrenLoaded(), parentId: {} size: {}", parentId,
								children.size());
						MediaListFragment.this.data = children;
						//TODO  checkForUserVisibleErrors(children.isEmpty());


						if (!data.isEmpty()) {
							adapter.notifyDataSetChanged();
							spinner.setVisibility(View.INVISIBLE);
							recyclerView.setVisibility(View.VISIBLE);
						}
					} catch (Throwable t) {
						log.error("Error on childrenloaded", t);
					}
				}

				@Override
				public void onError(@NonNull String id) {
					log.error("subscriptionCallback::onError(): id: {}", id);
					//TODO Toast.makeText(getActivity(), R.string.error_loading_media, Toast.LENGTH_LONG).show();
				}
			};

	@Override
	protected void onMopidyConnected() {
		log.info("onMopidyConnected(): {}", getMediaId());
	}
}


package danbroid.mopidy.service;

import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import danbroid.mopidy.AndroidMopidyConnection;
import danbroid.mopidy.BuildConfig;
import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.model.Artist;
import danbroid.mopidy.model.Base;
import danbroid.mopidy.model.Image;
import danbroid.mopidy.model.Playlist;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;
import danbroid.mopidy.util.MediaIds;
import danbroid.mopidy.util.MopidyServerFinder;
import danbroid.mopidy.util.UIResponseHandler;

/**
 * Created by dan on 12/01/18.
 */
@EBean(scope = EBean.Scope.Singleton)
public class MopidyContentManager {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MopidyContentManager.class);

	@Bean
	protected AndroidMopidyConnection conn;
	@Bean
	protected MopidyServerFinder discoveryHelper;

	public void onLoadChildren(@NonNull String parentId, @NonNull MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("onLoadChildren(): {}", parentId);

		switch (parentId) {
			case MediaIds.TRACKLIST:
				loadTracklist(result);
				return;
			case MediaIds.PLAYLISTS:
				loadPlaylists(result);
				return;
		}

		int i = parentId.indexOf(':');

		if (i > -1) {

			String prefix = parentId.substring(0, i);
			String data = parentId.substring(i + 1);

			switch (prefix) {
				case MediaIds.PLAYLISTS:
					loadM3U(data, result);
					return;
			}
		}

		loadMopidy(parentId, result);
	}


	public interface Result<T> {
		void onResult(T result);
	}


	public void getTracks(String mediaID, final Result<List<String>> result) {
		log.debug("getTracks(): {}", mediaID);


		if (mediaID.startsWith(MediaIds.M3U)) {
			conn.getPlaylists().getItems(mediaID).call(new ResponseHandler<Ref[]>() {
				@Override
				public void onResponse(CallContext context, Ref[] refs) {
					LinkedList<String> uris = new LinkedList<>();
					for (Ref ref : refs) {
						if (ref.getType().equals(Ref.TYPE_TRACK)) {
							uris.add(ref.getUri());
						}
					}
					result.onResult(uris);
				}
			});
			return;
		}

		getTracksRecursive(mediaID, result);
	}

	@UiThread
	public void getTracksRecursive(String mediaID, Result<List<String>> result) {
		getTracksRecursive(mediaID, new LinkedList<String>(), new LinkedList<String>(), result);
	}


	private void getTracksRecursive(final String mediaID, final List<String> directories, final List<String> uris, final Result<List<String>> result) {

		directories.add(mediaID);

		conn.getLibrary().browse(mediaID).call(new UIResponseHandler<Ref[]>() {
			@Override
			public void onUIResponse(CallContext context, Ref[] refs) {
				directories.remove(mediaID);

				for (Ref ref : refs) {
					if (ref.getType().equals(Ref.TYPE_TRACK)) {
						uris.add(ref.getUri());
					} else if (ref.getType().equals(Ref.TYPE_DIRECTORY)) {
						getTracksRecursive(ref.getUri(), directories, uris, result);
					}
				}

				if (directories.isEmpty()) {
					log.debug("finished");
					result.onResult(uris);
				}
			}
		});
	}


	protected void loadTracklist(final MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("loadTracklist()");
		result.detach();
		conn.getTrackList().getTlTrackList().call(new UIResponseHandler<TlTrack[]>() {
			@Override
			public void onUIResponse(CallContext context, TlTrack[] tracks) {
				result.sendResult(toMediaItems(tracks));
			}
		});
	}

	protected void loadPlaylists(final MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("loadPlaylists()");
		result.detach();
		conn.getPlaylists().asList()
				.call(new ResponseHandler<Ref[]>() {
					@Override
					public void onResponse(CallContext context, Ref[] playlists) {
						log.trace("playlist count: " + playlists.length);
						result.sendResult(toMediaItems(playlists));
					}
				});
	}

	protected void loadRoot(MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("loadRoot()");
		List<MediaBrowserCompat.MediaItem> items = new LinkedList<>();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			for (String serviceName : discoveryHelper.getServices().keySet()) {
				log.error("FOUND SERVICE: " + serviceName);

				NsdServiceInfo service = discoveryHelper.getServices().get(serviceName);

				MediaDescriptionCompat.Builder desc = new MediaDescriptionCompat.Builder();

				desc.setTitle(service.getServiceName());

				String host = service.getHost().toString();
				int port = service.getPort();

				if (host.startsWith("/")) host = host.substring(1);
				String description = host + ":" + port;

				desc.setDescription(description);
				desc.setSubtitle(description);
				String id = MediaIds.profileID("ws://" + host + ":" + port + "/mopidy/ws");

				desc.setMediaId(id);

				items.add(new MediaBrowserCompat.MediaItem(desc.build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
			}
		}

		if (BuildConfig.DEBUG) { //TODO REMOVE THIS SECTION

			MediaDescriptionCompat.Builder desc = new MediaDescriptionCompat.Builder();
			desc.setTitle("Dan");
			desc.setMediaId(MediaIds.profileID("ws://192.168.1.2:6680/mopidy/ws"));
			desc.setDescription("dans mopidy server");
			desc.setSubtitle("dans mopidy server");
			items.add(new MediaBrowserCompat.MediaItem(desc.build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));

			desc = new MediaDescriptionCompat.Builder();
			desc.setTitle("Rip");
			desc.setMediaId(MediaIds.profileID("ws://192.168.1.4:6680/mopidy/ws"));
			desc.setDescription("rip mopidy server");
			desc.setSubtitle("Rip mopidy server");
			items.add(new MediaBrowserCompat.MediaItem(desc.build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));

		}


		result.sendResult(items);
	}

	protected void loadMopidy(String id, @NonNull final MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("loadMopidy(): {}", id);

		id = MediaIds.extractChildID(id);

		//id of the root folder in mopidy is null
		if (id.equals(MediaIds.ROOT)) id = null;
		else if (id.startsWith(MediaIds.M3U)) {
			loadM3U(id, result);
			return;
		}

		result.detach();

		conn.getLibrary().browse(id).call(new ResponseHandler<Ref[]>() {
			@Override
			public void onResponse(CallContext context, Ref[] refs) {
				resolveImages(refs, result);
			}
		});

	}

	protected void loadM3U(String mediaID, final MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("loadM3U(): {}", mediaID);
		result.detach();
		conn.getPlaylists().getItems(mediaID).call(new ResponseHandler<Ref[]>() {
			@Override
			public void onResponse(CallContext context, Ref[] refs) {
				result.sendResult(toMediaItems(refs));
			}
		});
	}

	protected void resolveImages(final Ref[] refs, @NonNull final MediaBrowserServiceCompat.Result<List<MediaBrowserCompat.MediaItem>> result) {
		log.trace("resolveImages()");
		HashSet<String> uris = new HashSet<>();
		for (Ref ref : refs) {
			if (ref.getType().equals(Ref.TYPE_TRACK)) {
				if (!uris.contains(ref.getUri())) uris.add(ref.getUri());
			}
		}

		if (uris.isEmpty()) {
			result.sendResult(toMediaItems(refs));
			return;
		}

		conn.getLibrary().getImages(uris.toArray(new String[]{})).call(new ResponseHandler<Map<String, Image[]>>() {
			@Override
			public void onResponse(CallContext context, Map<String, Image[]> imgMap) {
				for (Ref ref : refs) {
					if (ref.getType().equals(Ref.TYPE_TRACK)) {
						if (imgMap.containsKey(ref.getUri())) {
							Image images[] = imgMap.get(ref.getUri());
							if (images.length > 0)
								ref.setExtra(images[0]);
						}
					}
				}
				result.sendResult(toMediaItems(refs));

			}
		});
	}


	public List<MediaBrowserCompat.MediaItem> toMediaItems(Base[] items) {
		LinkedList<MediaBrowserCompat.MediaItem> list = new LinkedList<>();
		for (Base item : items)
			list.add(toMediaItem(item));
		return list;
	}

	public MediaBrowserCompat.MediaItem toMediaItem(MediaDescriptionCompat.Builder md, Ref ref) {

		String name = ref.getName();
		md.setTitle(name);
		String uri = ref.getUri();
		md.setMediaId(uri);

		md.setDescription(uri);
		md.setSubtitle(uri);

		if (ref.getExtra() != null) {
			Image img = (Image) ref.getExtra();
			md.setIconUri(Uri.parse(img.getUri()));
		}

		int flags = ref.getType().equals(Ref.TYPE_TRACK) ? MediaBrowserCompat.MediaItem.FLAG_PLAYABLE :
				MediaBrowserCompat.MediaItem.FLAG_BROWSABLE;

		return new MediaBrowserCompat.MediaItem(md.build(), flags);
	}

	public MediaBrowserCompat.MediaItem toMediaItem(String mediaID, MediaDescriptionCompat.Builder md, Track track) {

		//log.error("toMediaItem(): " + mediaID + " album: " + track.getAlbum().getName());
		md.setMediaId(mediaID);
		md.setTitle(track.getName());


		String album = null;
		String comment = null;
		String artists = null;

		if (track.getAlbum() != null)
			album = track.getAlbum().getName();

		if (track.getArtists() != null && track.getArtists().length > 0) {
			for (Artist a : track.getArtists()) {
				artists = artists == null ? a.getName() : artists + "," + a.getName();
			}
		}

		if (track.getComment() != null) {
			comment = track.getComment();
		}

		String subTitle = album;
		if (artists != null)
			subTitle = subTitle == null ? artists : subTitle + " - " + artists;

		if (comment != null)
			comment = comment.trim();


		md.setSubtitle(subTitle);
		md.setDescription(comment);

		if (track.getAlbum() != null && track.getAlbum().getImages() != null
				&& track.getAlbum().getImages().length > 0) {
			md.setIconUri(Uri.parse(track.getAlbum().getImages()[0]));
		}


		return new MediaBrowserCompat.MediaItem(md.build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
	}


	public MediaBrowserCompat.MediaItem toMediaItem(MediaDescriptionCompat.Builder md, TlTrack tlTrack) {
		return toMediaItem(MediaIds.trackListID(tlTrack.getTlid()), md, tlTrack.getTrack());
	}

	public MediaBrowserCompat.MediaItem toMediaItem(Base item) {
		MediaDescriptionCompat.Builder md = new MediaDescriptionCompat.Builder();

		if (item instanceof Ref)
			return toMediaItem(md, (Ref) item);
		else if (item instanceof TlTrack)
			return toMediaItem(md, (TlTrack) item);
		else if (item instanceof Playlist)
			return toMediaItem(md, (Playlist) item);

		throw new IllegalArgumentException("Unhandled base type: " + item);

	}

	private MediaBrowserCompat.MediaItem toMediaItem(MediaDescriptionCompat.Builder md, Playlist playlist) {
		md.setTitle(playlist.getName());
		md.setMediaId(playlist.getUri());
		md.setSubtitle(playlist.getUri());
		return new MediaBrowserCompat.MediaItem(md.build(), MediaBrowserCompat.MediaItem.FLAG_BROWSABLE);
	}
}

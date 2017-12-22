package danbroid.mopidy.app.util;

import android.net.Uri;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SupposeUiThread;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.app.MopidyConnection;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.lastfm.LastFMCall;
import danbroid.mopidy.model.Image;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.model.Track;
import danbroid.mopidy.util.UIResponseHandler;

/**
 * Created by dan on 11/12/17.
 */
@EBean(scope = EBean.Scope.Singleton)
public class ImageResolver {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImageResolver.class);

	static {
		LastFMCall.API_KEY = "00ed568fa493d39e290ae42713b9094f";
	}

	@Bean
	MopidyConnection conn;

	public static final Image MISSING_IMAGE = new Image();
	private Map<String, Image> CACHE = new HashMap<>();

	@SupposeUiThread
	public void resolveImages(final Ref refs[], final ResponseHandler<Ref[]> handler) {
		HashSet<String> imageUris = new HashSet<>();
		for (Ref ref : refs) {
			if (ref.getExtra() == null && ref.getType().equals(Ref.TYPE_TRACK)) {
				String uri = ref.getUri();
				if (CACHE.containsKey(uri)) {
					ref.setExtra(CACHE.get(uri));
				} else {
					imageUris.add(ref.getUri());
				}
			}
		}

		if (imageUris.isEmpty()) {
			handler.onResponse(null, refs);
			return;
		}

		conn.getLibrary().getImages(
				imageUris.toArray(new String[]{})).call(
				new UIResponseHandler<Map<String, Image[]>>() {
					@Override
					public void onUIResponse(CallContext context, Map<String, Image[]> result) {
						resolveImages(refs, result, handler);
					}
				}
		);
	}

	private void resolveImages(Ref[] refs, Map<String, Image[]> result, ResponseHandler<Ref[]> handler) {
		for (String uri : result.keySet()) {
			Image images[] = result.get(uri);
			CACHE.put(uri, images.length == 0 ? MISSING_IMAGE : images[0]);
		}

		for (Ref ref : refs) {
			if (ref.getType().equals(Ref.TYPE_TRACK)) {
				if (CACHE.containsKey(ref.getUri())) {
					ref.setExtra(CACHE.get(ref.getUri()));
				} else {
					ref.setExtra(MISSING_IMAGE);
					log.trace("cant find image for: {}", ref.getUri());
				}
			}
		}
		handler.onResponse(null, refs);
	}

	@SupposeUiThread
	public void findCover(Track track, ResponseHandler<String> handler) {
		String artist = null, album = null;

		if (track.getAlbum() != null && track.getAlbum().getImages() != null &&
				track.getAlbum().getImages().length > 0) {
			String albumCover = track.getAlbum().getImages()[0];
			log.trace("albumCover: " + albumCover + " conn.url: " + conn.getUrl());
			if (albumCover.startsWith("/images/")) {
				Uri connURL = Uri.parse(conn.getUrl());
				log.debug("path " + connURL.getPath());
				log.debug("host " + connURL.getHost());
				log.debug("port " + connURL.getPort());

			}
			handler.onResponse(null, albumCover);
			return;
		}
	}
}

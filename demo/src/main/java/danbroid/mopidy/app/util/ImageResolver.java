package danbroid.mopidy.app.util;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SupposeUiThread;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.app.MopidyConnection;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.model.Image;
import danbroid.mopidy.model.Ref;

/**
 * Created by dan on 11/12/17.
 */
@EBean(scope = EBean.Scope.Singleton)
public class ImageResolver {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImageResolver.class);

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
				new ResponseHandler<Map<String, Image[]>>() {
					@Override
					public void onResponse(CallContext context, Map<String, Image[]> result) {
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
				ref.setExtra(CACHE.containsKey(ref.getUri()) ? CACHE.get(ref.getUri()) : MISSING_IMAGE);
			}
		}
		handler.onResponse(null, refs);
	}
}

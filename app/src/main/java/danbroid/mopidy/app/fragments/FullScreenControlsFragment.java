package danbroid.mopidy.app.fragments;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.app.BuildConfig;
import danbroid.mopidy.app.R;
import danbroid.mopidy.app.interfaces.MainView;
import danbroid.mopidy.app.util.FlingDetector;
import danbroid.mopidy.app.util.NavBarColours;
import danbroid.mopidy.fragments.MediaControlsFragment;
import danbroid.mopidy.glide.GlideApp;
import danbroid.mopidy.lastfm.Album;
import danbroid.mopidy.lastfm.AlbumSearch;
import danbroid.mopidy.lastfm.Response;
import jp.wasabeef.blurry.Blurry;

/**
 * Created by dan on 18/12/17.
 */
@EFragment(R.layout.fullscreen_controls)
public class FullScreenControlsFragment extends MediaControlsFragment {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FullScreenControlsFragment.class);

	@ViewById(R.id.image)
	ImageView image;

	@ViewById(R.id.blurred_image)
	ImageView blurredImage;


	protected void init() {
		super.init();
		getView().setOnTouchListener(new FlingDetector(getContext()) {
			@Override
			protected boolean onFlingDown(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				close();
				return true;
			}
		});
	}


	@Click(R.id.chevron_down)
	public void close() {
		MediaControllerCompat controller = getController();
		if (BuildConfig.DEBUG) {
			log.debug("controller: " + controller);
			log.debug("metadata: " + controller.getMetadata());
			log.debug("sessionREady: " + controller.isSessionReady());
		}


		((MainView) getMainView()).hideFullControls();
	}


	public static FullScreenControlsFragment newInstance() {
		return FullScreenControlsFragment_.builder().build();
	}

	@UiThread
	public void displayImage(String url) {
		log.trace("displayImage(): {}", url);
		if (url == null || getActivity() == null || !isResumed()) return;

		GlideApp.with(this).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
			@Override
			public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
				//Blurry.with(getContext()).radius(25).sampling(2)
				if (getActivity() == null || !isResumed()) return;
				if (resource != null) {
					log.trace("size: " + resource.getWidth());

					int c = 70;
					int width = resource.getWidth();
					Blurry.with(getContext()).radius(width > c ? width / c : 12).sampling(2)
							.from(resource).into(blurredImage);

					NavBarColours.configureNavBarColours(getActivity(), resource);
				}
			}
		});

		GlideApp.with(getContext()).load(url).transition(DrawableTransitionOptions.withCrossFade())
				.apply(RequestOptions.bitmapTransform(new RoundedCorners(8))).into(image);

	}

	@Override
	public void onMetadataChanged(MediaMetadataCompat metadata) {
		super.onMetadataChanged(metadata);

		if (metadata == null) return;

		Uri imageURI = metadata.getDescription().getIconUri();

		if (imageURI != null) {
			displayImage(imageURI.toString());
			return;
		}

		String album = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM);
		String artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
		if (artist == null)
			artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST);
		log.trace("album: {} artist: {}", album, artist);

		if (artist != null || album != null) {
			log.trace("performing album search: album: {} artist: {}", album, artist);
			new AlbumSearch() {
				@Override
				protected void onResponse(Response response) {
					if (response.album != null) {
						String image = response.album.getImage(Album.ImageSize.DEFAULT);
						displayImage(image);
					}
				}
			}.artist(artist).album(album).callAsync();
		}
	}

}

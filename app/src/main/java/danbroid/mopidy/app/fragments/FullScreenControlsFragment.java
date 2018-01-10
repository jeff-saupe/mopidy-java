package danbroid.mopidy.app.fragments;

import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.app.R;
import danbroid.mopidy.app.util.FlingDetector;
import danbroid.mopidy.app.util.NavBarColours;
import danbroid.mopidy.fragments.MediaFragment;
import danbroid.mopidy.glide.GlideApp;
import danbroid.mopidy.lastfm.Album;
import danbroid.mopidy.lastfm.AlbumSearch;
import danbroid.mopidy.lastfm.Response;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;
import jp.wasabeef.blurry.Blurry;

/**
 * Created by dan on 18/12/17.
 */
@EFragment(R.layout.fullscreen_controls)
public class FullScreenControlsFragment extends MediaFragment {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FullScreenControlsFragment.class);

	@ViewById(R.id.image)
	ImageView image;

	@ViewById(R.id.blurred_image)
	ImageView blurredImage;

	@ViewById(R.id.line1)
	TextView line1;

	@ViewById(R.id.line2)
	TextView line2;


	protected void init() {
		getView().setOnTouchListener(new FlingDetector(getContext()) {
			@Override
			protected boolean onFlingDown(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				close();
				return true;
			}
		});
	}


	protected void displayTrack(TlTrack tlTrack) {

		if (tlTrack == null) return;

		Track track = tlTrack.getTrack();
		String album = null, artist = null, title = null, albumCover = null;
		String description = "";

		title = track.getName();

		line1.setText(title);


		if (track.getArtists() != null) {
			if (track.getArtists().length > 0)
				artist = track.getArtists()[0].getName();
		}

		if (track.getAlbum() != null) {
			album = track.getAlbum().getName();
			if (track.getAlbum().getImages() != null
					&& track.getAlbum().getImages().length > 0)
				albumCover = track.getAlbum().getImages()[0];
		}


		if (album != null) description += album + " ";
		if (artist != null) description += artist;
		line2.setText(description);

		if (albumCover != null) {
			displayImage(albumCover);
			return;
		}


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


	@Click(R.id.chevron_down)
	public void close() {
		//TODO 	getMainView().hideFullControls();
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
}

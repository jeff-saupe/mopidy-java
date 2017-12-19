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

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import danbroid.mopidy.app.R;
import danbroid.mopidy.app.util.FlingDetector;
import danbroid.mopidy.app.util.GlideApp;
import danbroid.mopidy.app.util.NavBarColours;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;
import jp.wasabeef.blurry.Blurry;

/**
 * Created by dan on 18/12/17.
 */
@EFragment(R.layout.fullscreen_controls)
public class FullScreenControlsFragment extends PlaybackFragment {
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
		super.init();
		getView().setOnTouchListener(new FlingDetector(getContext()) {
			@Override
			protected boolean onFlingDown(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				close();
				return true;
			}
		});
	}

	@Override
	protected void displayTrack(TlTrack tlTrack) {
		log.trace("displayTrack() {} ", tlTrack.getTrack());
		super.displayTrack(tlTrack);

		Track track = tlTrack.getTrack();
		String album = null, artist = null, title = null;

		title = track.getName();

		line1.setText(title);

		if (track.getAlbum() != null) {
			album = track.getAlbum().getName();
			String images[] = track.getAlbum().getImages();
			if (images != null && images.length > 0)
				setImage(images[0]);
		}

		if (track.getArtists() != null) {
			if (track.getArtists().length > 0)
				artist = track.getArtists()[0].getName();
		}

		String description = "";
		if (album != null) description += album + " ";
		if (artist != null) description += artist;
		line2.setText(description);
	}

	@Click(R.id.chevron_down)
	public void close() {
		getMainView().hideFullControls();
	}


	public static FullScreenControlsFragment newInstance() {
		return FullScreenControlsFragment_.builder().build();
	}

	public void setImage(String url) {
		GlideApp.with(this).asBitmap().load(url).into(new SimpleTarget<Bitmap>() {
			@Override
			public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
				//Blurry.with(getContext()).radius(25).sampling(2)
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

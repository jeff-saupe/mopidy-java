package danbroid.mopidy.app.activities;

import android.content.ComponentName;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import danbroid.mopidy.activities.MopidyActivity;
import danbroid.mopidy.app.R;
import danbroid.mopidy.app.fragments.ContentListFragment;
import danbroid.mopidy.app.fragments.FullScreenControlsFragment;
import danbroid.mopidy.app.interfaces.ContentView;
import danbroid.mopidy.app.interfaces.MainPrefs_;
import danbroid.mopidy.app.service.MopidyService_;
import danbroid.mopidy.app.ui.AddServerDialog_;
import danbroid.mopidy.app.util.MopidyUris;
import danbroid.mopidy.interfaces.MainView;
import danbroid.mopidy.service.AbstractMopidyService;

@OptionsMenu(R.menu.menu_main)
@EActivity(R.layout.activity_main)
public class MainActivity extends MopidyActivity implements MainView, FragmentManager.OnBackStackChangedListener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MainActivity.class);

	@ViewById(R.id.toolbar)
	Toolbar toolbar;

	@Pref
	MainPrefs_ prefs;

	@ViewById(R.id.fab)
	FloatingActionButton fab;


	public void init() {
		super.init();

		setSupportActionBar(toolbar);

		getSupportFragmentManager().addOnBackStackChangedListener(this);

		hideFullControls();
	}

	@Override
	protected Class<? extends AbstractMopidyService> getServiceClass() {
		return MopidyService_.class;
	}


	@OptionsItem(R.id.action_home)
	public void actionHome() {

		//TODO showContent(MopidyUris.URI_SERVERS);
	}




	public void setContent(ContentView content) {
		log.trace("setContent() uri:{}", content.getUri());

		getSupportFragmentManager().beginTransaction()
				.setCustomAnimations(
						R.animator.slide_in_from_right, R.animator.slide_out_to_left,
						R.animator.slide_in_from_left, R.animator.slide_out_to_right)
				.replace(R.id.content_container, (Fragment) content)
				.addToBackStack(null)
				.commit();
	}

	public void showFAB(@DrawableRes int icon) {
		fab.setImageResource(icon);
		fab.setVisibility(View.VISIBLE);
	}

	public void hideFAB() {
		fab.setVisibility(View.GONE);
	}

	@Click(R.id.fab)
	void onFabClicked() {

	}

	public void showAddServerDialog() {
		AddServerDialog_.getInstance_(this).show(this);
	}


	private long lastTimeBackPressed = 0;

	@OptionsItem(android.R.id.home)
	@Override
	public void onBackPressed() {
		FragmentManager fm = getSupportFragmentManager();

		Fragment fragment = fm.findFragmentById(R.id.full_controls);

		if (fm.findFragmentById(R.id.full_controls) != null) {
			hideFullControls();
			return;
		}

		if (fm.getBackStackEntryCount() > 1) {
			super.onBackPressed();
			return;
		}

		long time = System.currentTimeMillis();
		if ((time - lastTimeBackPressed) < 2000) {
			finish();
			return;
		}
		lastTimeBackPressed = time;
		Toast.makeText(this, getString(R.string.msg_press_back_to_exit), Toast.LENGTH_SHORT).show();
	}


	public void showFullControls() {
		log.debug("showFullControls()");

		final View view = findViewById(R.id.full_controls);
		view.setVisibility(View.VISIBLE);
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_up);
		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				findViewById(R.id.coordinator_layout).setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.full_controls, FullScreenControlsFragment.newInstance()).commit();
		view.setAnimation(anim);
		view.animate();
	}


	public void hideFullControls() {
		log.trace("hideFullControls()");

		Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_down);
		anim.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				FragmentManager fm = getSupportFragmentManager();
				FragmentTransaction ft = fm.beginTransaction();
				Fragment fullControls = fm.findFragmentById(R.id.full_controls);
				if (fullControls != null) {
					ft.remove(fullControls);
				}
				ft.commit();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});

		findViewById(R.id.coordinator_layout).setVisibility(View.VISIBLE);
		View fullControls = findViewById(R.id.full_controls);
		fullControls.setAnimation(anim);
		fullControls.animate();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
			getWindow().setNavigationBarColor(getResources().getColor(R.color.colorPrimaryDark));
		}

	}


	@Override
	public void onBackStackChanged() {
		getSupportActionBar()
				.setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 1);

	}


}

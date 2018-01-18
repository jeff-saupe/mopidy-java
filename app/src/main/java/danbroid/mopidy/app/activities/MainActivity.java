package danbroid.mopidy.app.activities;

import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import danbroid.mopidy.activities.MopidyActivity;
import danbroid.mopidy.app.R;
import danbroid.mopidy.app.fragments.FullScreenControlsFragment;
import danbroid.mopidy.app.fragments.LibraryTab;
import danbroid.mopidy.app.fragments.PlaylistsTab;
import danbroid.mopidy.app.fragments.TracklistTab;
import danbroid.mopidy.app.interfaces.MainView;
import danbroid.mopidy.app.service.MopidyService_;
import danbroid.mopidy.app.ui.AddServerDialog_;
import danbroid.mopidy.fragments.MediaFragment;
import danbroid.mopidy.interfaces.MediaContentView;
import danbroid.mopidy.interfaces.MopidyPrefs_;
import danbroid.mopidy.service.AbstractMopidyService;
import danbroid.mopidy.service.MopidyClient;

@OptionsMenu(R.menu.menu_main)
@EActivity(R.layout.activity_main)
public class MainActivity extends MopidyActivity implements MainView, FragmentManager.OnBackStackChangedListener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MainActivity.class);

	@ViewById(R.id.toolbar)
	Toolbar toolbar;

	@Pref
	MopidyPrefs_ prefs;

	@ViewById(R.id.fab)
	FloatingActionButton fab;

	@ViewById(R.id.bottom_controls)
	View bottomControls;

	@ViewById(R.id.view_pager)
	ViewPager viewPager;

	@ViewById(R.id.tab_layout)
	TabLayout tabLayout;

	abstract class TabInfo {
		final String title;
		Fragment fragment;

		TabInfo(String title) {
			this.title = title;
		}

		Fragment getFragment() {
			return fragment = createFragment();
		}

		abstract Fragment createFragment();

		public String getTitle() {
			return title;
		}
	}

	private TabInfo tabs[] = {};

	public void init() {
		super.init();

		setSupportActionBar(toolbar);

		getSupportFragmentManager().addOnBackStackChangedListener(this);

		hideFullControls();

		tabs = new TabInfo[]{
				new TabInfo(getString(R.string.lbl_library)) {
					@Override
					Fragment createFragment() {
						return LibraryTab.newInstance();
					}
				},
				new TabInfo(getString(R.string.lbl_tracklist)) {
					@Override
					Fragment createFragment() {
						return TracklistTab.newInstance();
					}
				},
				new TabInfo(getString(R.string.lbl_playlists)) {
					@Override
					Fragment createFragment() {
						return PlaylistsTab.newInstance();
					}
				},
		};

		FragmentPagerAdapter pageAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public Fragment getItem(int i) {
				return tabs[i].getFragment();
			}

			@Override
			public int getCount() {
				return tabs.length;
			}

			@Override
			public CharSequence getPageTitle(int position) {
				return tabs[position].getTitle();
			}
		};

		viewPager.setAdapter(pageAdapter);

		tabLayout.setupWithViewPager(viewPager);


	}

	@Override
	protected void onConnected() {
		String lastConnection = prefs.lastConnectionURL().getOr(null);
		log.info("onConnected() lastConnection: {}", lastConnection);
		if (lastConnection != null) {
			new MopidyClient.Connect(this, lastConnection) {
				@Override
				protected void onSuccess(String version) {
					actionHome();
				}
			}.call();
		}
	}

/*	public void showBottomControls(boolean animate) {
		log.trace("showBottomControls(): animate: {}", animate);

		if (bottomControls == null) {
			log.warn("showBottomControls() no controls found");
			return;
		}

		if (bottomControls.getVisibility() != View.VISIBLE) {

			log.error("showing bottom controls");
			if (!animate) {
				bottomControls.setVisibility(View.VISIBLE);
				CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) contentContainer.getLayoutParams();
				params.bottomMargin += bottomControls.getHeight();
				log.error("set bottom margin to " + params.bottomMargin);
				contentContainer.setLayoutParams(params);
				return;
			}
			//shall slide it up from the bottom
			Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

			slideUp.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					//need to reset the padding on the main content to allow space for the bottom controls

					CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) contentContainer.getLayoutParams();
					params.bottomMargin += bottomControls.getHeight();
					contentContainer.setLayoutParams(params);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}
			});

			bottomControls.setAnimation(slideUp);
			bottomControls.setVisibility(View.VISIBLE);
			bottomControls.animate();
		} else {
			log.error("bottom controls already visible");
		}
	}

	public void hideBottomControls() {
		log.trace("hideBottomControls()");

		int height = bottomControls.getHeight();
		bottomControls.setVisibility(View.GONE);
		CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) contentContainer.getLayoutParams();
		params.bottomMargin -= height;
		contentContainer.setLayoutParams(params);
	}*/


	@Override
	protected Class<? extends AbstractMopidyService> getServiceClass() {
		return MopidyService_.class;
	}


	@OptionsItem(R.id.action_home)
	public void actionHome() {
		viewPager.setCurrentItem(0);
	}


	@Override
	public void setContent(MediaFragment content) {

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

		fragment = tabs[tabLayout.getSelectedTabPosition()].fragment;
		if (fragment != null && fragment instanceof MediaContentView) {
			if (((MediaContentView) fragment).onBackButton()) return;
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

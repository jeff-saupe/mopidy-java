package danbroid.mopidy.app.activities;

import android.net.nsd.NsdServiceInfo;
import android.support.annotation.DrawableRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import danbroid.mopidy.app.R;
import danbroid.mopidy.app.content.ContentProvider;
import danbroid.mopidy.app.fragments.ContentListFragment;
import danbroid.mopidy.app.fragments.TestFragment_;
import danbroid.mopidy.app.interfaces.ContentView;
import danbroid.mopidy.app.interfaces.MainPrefs_;
import danbroid.mopidy.app.interfaces.MainView;
import danbroid.mopidy.app.ui.AddServerDialog_;
import danbroid.mopidy.app.util.MopidyServerDiscovery;
import danbroid.mopidy.model.Ref;

@OptionsMenu(R.menu.menu_main)
@EActivity(R.layout.activity_main)
public class MainActivity extends AppCompatActivity implements MainView, MopidyServerDiscovery.Listener, FragmentManager.OnBackStackChangedListener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MainActivity.class);


	@ViewById(R.id.toolbar)
	Toolbar toolbar;

	@Bean
	ContentProvider contentProvider;

	@Bean
	MopidyServerDiscovery serverDiscovery;

	@Pref
	MainPrefs_ prefs;

	@AfterViews
	void init() {
		log.info("init()");
		setSupportActionBar(toolbar);

		if (getContent() == null) {
			showContent(ContentProvider.URI_SERVERS);
		}

		serverDiscovery.setListener(this);
		getSupportFragmentManager().addOnBackStackChangedListener(this);


		hideFullControls();
		hideBottomControls();
	}

	public void showContent(String uri) {
		log.trace("showContent(): {}", uri);
		setContent(ContentListFragment.newInstance(uri));
	}

	@Override
	protected void onResume() {
		super.onResume();
		serverDiscovery.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		serverDiscovery.stop();
	}

	@Override
	public ContentView getContent() {
		return (ContentView) getSupportFragmentManager().findFragmentById(R.id.content_container);
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
		String uri = getContent().getUri();
		switch (uri) {
			case ContentProvider.URI_SERVERS:
				addServer();
				break;
		}
	}

	public void addServer() {
		log.debug("addServer()");
		AddServerDialog_.getInstance_(this).show(this);
	}

	@Override
	public void onItemSelected(Ref ref) {
		log.info("onItemSelected(): {}", ref);

		if (ref.getType().equals(Ref.TYPE_DIRECTORY)) {
			String uri = ref.getUri();
			showContent(uri);
		}
	}

	@Override
	public void onItemLongClicked(final Ref ref, View v) {
		PopupMenu popupMenu = new PopupMenu(this, v);
		popupMenu.getMenu().add("Remove: " + ref.getName())
				.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						Set<String> servers = prefs.servers().get();
						String host = ref.getUri().substring(ContentProvider.URI_SERVER.length());
						if (servers.contains(host)) {
							servers.remove(host);
							prefs.edit().servers().put(servers).apply();
							getContent().refresh();
						}
						return true;
					}
				});
		popupMenu.show();
	}

	@Override
	public void browse(String uri, ContentView contentView) {
		log.debug("browse(): {}", uri);

		if (ContentProvider.URI_SERVERS.equals(uri)) {

			ArrayList<Ref> servers = new ArrayList<>();

			for (NsdServiceInfo serviceInfo : serverDiscovery.getServerInfo().values()) {
				Ref ref = new Ref();
				ref.setType(Ref.TYPE_DIRECTORY);
				ref.setName(serviceInfo.getServiceName());
				ref.setUri(ContentProvider.URI_SERVER + serviceInfo.getHost().toString().substring(1) + ":" + serviceInfo.getPort());
				servers.add(ref);


			}
			for (String s : prefs.servers().get()) {
				log.warn("SERVER: " + s);
				String parts[] = s.split(":");
				if (parts.length == 2) {

					Ref ref = new Ref();
					ref.setType(Ref.TYPE_DIRECTORY);
					ref.setName(s);
					ref.setUri(ContentProvider.URI_SERVER + s);
					servers.add(ref);

				}
			}
			contentView.setContent(servers.toArray(new Ref[]{}));

			return;
		}


		contentProvider.browse(uri, contentView);
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
		Toast.makeText(this, "Press back again to exit.", Toast.LENGTH_SHORT).show();
	}

	@Override
	protected void onStart() {
		super.onStart();
		contentProvider.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		contentProvider.stop();
	}


	@Override
	public void onServerListChanged(Map<String, NsdServiceInfo> servers) {
		ContentView contentView = getContent();
		if (contentView != null) {
			if (contentView.getUri().equals(ContentProvider.URI_SERVERS)) {
				contentView.refresh();
			}
		}
	}

	@ViewById(R.id.fab)
	FloatingActionButton fab;

	public void showBottomControls(boolean animate) {
		log.trace("showBottomControls(): animate: {}", animate);
		final View bottomControls = findViewById(R.id.bottom_controls);

		if (bottomControls == null) {
			log.warn("showBottomControls() no controls found");
			return;
		}

		if (bottomControls.getVisibility() != View.VISIBLE) {

			if (!animate) {
				bottomControls.setVisibility(View.VISIBLE);
				View content = findViewById(R.id.content_container);
				if (content != null)
					ViewCompat.setPaddingRelative(content, 0, 0, 0, bottomControls.getHeight());
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
					View content = findViewById(R.id.content_container);
					if (content != null) {
						ViewCompat.setPaddingRelative(content, 0, 0, 0, bottomControls.getHeight());
					}

					((CoordinatorLayout.LayoutParams) fab.getLayoutParams()).bottomMargin =
							getResources().getDimensionPixelSize(R.dimen.fab_margin) + bottomControls.getHeight();


				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}
			});

			bottomControls.setAnimation(slideUp);
			bottomControls.setVisibility(View.VISIBLE);
			bottomControls.animate();
		}
	}

	public void hideBottomControls() {
		log.trace("hideBottomControls()");
		View bottomControls = findViewById(R.id.bottom_controls);
		if (bottomControls == null) {
			log.warn("hideBottomControls() controls not found");
			return;
		}
		int height = bottomControls.getHeight();
		bottomControls.setVisibility(View.GONE);
		View content = findViewById(R.id.content_container);
		if (content != null)
			ViewCompat.setPaddingRelative(content, 0, 0, 0, -height);


		((CoordinatorLayout.LayoutParams) fab.getLayoutParams()).bottomMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
	}


	@Click(R.id.chevron_up)
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
		ft.replace(R.id.full_controls, TestFragment_.builder().build()).commit();


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

/*TODO		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
			getWindow().setNavigationBarColor(getResources().getColor(R.color.primary_dark));
		}*/

	}

	@OptionsItem(R.id.action_test1)
	void test1() {
		showFullControls();
	}

	@OptionsItem(R.id.action_test2)
	void test2() {
		hideFullControls();
	}

	@OptionsItem(R.id.action_test3)
	void test3() {
		showBottomControls(true);
	}

	@OptionsItem(R.id.action_test4)
	void test4() {
		hideBottomControls();
	}

	@Override
	public void onBackStackChanged() {
		getSupportActionBar()
				.setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 1);
		ContentView contentView = getContent();
		if (contentView == null) return;

		switch (contentView.getUri()) {
			case ContentProvider.URI_SERVERS:
				showFAB(R.drawable.ic_add);
				break;
			default:
				hideFAB();
				break;
		}

	}


}

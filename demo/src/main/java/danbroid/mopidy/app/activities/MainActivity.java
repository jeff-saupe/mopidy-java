package danbroid.mopidy.app.activities;

import android.net.Uri;
import android.net.nsd.NsdServiceInfo;
import android.support.annotation.DrawableRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.Map;
import java.util.Set;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.app.MopidyConnection;
import danbroid.mopidy.app.R;
import danbroid.mopidy.app.content.ContentProvider;
import danbroid.mopidy.app.fragments.ContentListFragment;
import danbroid.mopidy.app.fragments.TestFragment_;
import danbroid.mopidy.app.interfaces.ContentView;
import danbroid.mopidy.app.interfaces.MainPrefs_;
import danbroid.mopidy.app.interfaces.MainView;
import danbroid.mopidy.app.ui.AddServerDialog_;
import danbroid.mopidy.app.util.MopidyServerDiscovery;
import danbroid.mopidy.app.util.MopidyUris;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.model.TlTrack;

@OptionsMenu(R.menu.menu_main)
@EActivity(R.layout.activity_main)
public class MainActivity extends PlaybackActivity implements MainView, MopidyServerDiscovery.Listener, FragmentManager.OnBackStackChangedListener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MainActivity.class);


	@ViewById(R.id.toolbar)
	Toolbar toolbar;

	@Bean
	ContentProvider contentProvider;

	@Bean
	MopidyConnection conn;

	@Bean
	MopidyServerDiscovery serverDiscovery;

	@Pref
	MainPrefs_ prefs;


	@AfterViews
	void init() {
		log.info("init()");

		setSupportActionBar(toolbar);

		if (getContent() == null) {
			showContent(MopidyUris.URI_ROOT);
		}

		serverDiscovery.setListener(this);

		getSupportFragmentManager().addOnBackStackChangedListener(this);


		hideFullControls();

		conn.getVersion(null);

	}


	public void showContent(Uri uri) {
		log.trace("showContent(): {}", uri);
		setContent(ContentListFragment.newInstance(uri));
	}

	@Override
	public void onResume() {
		super.onResume();
		serverDiscovery.start();
	}

	@Override
	public void onPause() {
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
		Uri uri = getContent().getUri();

		switch (MopidyUris.match(uri)) {
			case MopidyUris.MATCH_SERVERS:
				showAddServerDialog();
				break;
		}
	}

	public void showAddServerDialog() {
		AddServerDialog_.getInstance_(this).show(this);
	}

	@Override
	public void onItemSelected(Ref ref) {
		log.info("onItemSelected(): {}", ref);


		switch (ref.getType()) {
			case Ref.TYPE_DIRECTORY:
				showContent(Uri.parse(ref.getUri()));
				break;
			case Ref.TYPE_TRACK:
				playTrack(ref);
				break;
			default:
				log.error("Unhandled ref: {}", ref);
				break;
		}


	}

	public void playTrack(Ref ref) {
		log.info("playTrack(): {}", ref);
		Uri uri = Uri.parse(ref.getUri());
		switch (MopidyUris.match(uri)) {
			case MopidyUris.MATCH_TRACKLIST_ITEM:
				int tlid = Integer.parseInt(uri.getLastPathSegment());
				playTrack(tlid);

				break;
			default:
				addAndPlay(ref.getUri());
				break;
		}
	}


	@UiThread
	public void addAndPlay(String uri) {
		log.debug("addAndPlay(): {}", uri);
		conn.getTrackList().add(uri, new ResponseHandler<TlTrack[]>() {
			@Override
			public void onResponse(CallContext context, TlTrack[] result) {
				if (result.length > 0) {
					playTrack(result[0].getTlid());
				}
			}
		});
	}

	@UiThread
	public void playTrack(int tlid) {
		log.trace("playTrack(): tlid: {}", tlid);
		conn.getPlayback()
				.play(tlid, null, new ResponseHandler<Void>() {
					@Override
					public void onResponse(CallContext context, Void result) {
					}
				});
	}

	@Override
	public void onItemLongClicked(final Ref ref, View v) {
		log.debug("onItemLongClicked(): {} uri:{}", ref.getName(), ref.getUri());
		final String address = Uri.decode(Uri.parse(ref.getUri()).getLastPathSegment());
		PopupMenu popupMenu = new PopupMenu(this, v);
		popupMenu.getMenu().add("Remove: " + ref.getName())
				.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						Set<String> servers = prefs.servers().get();


						if (servers.contains(address)) {
							servers.remove(address);
							prefs.edit().servers().put(servers).apply();
							getContent().refresh();
						}
						return true;
					}
				});
		popupMenu.show();
	}

	@Override
	public void browse(Uri uri, ContentView contentView) {
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
		Toast.makeText(this, getString(R.string.msg_press_back_to_exit), Toast.LENGTH_SHORT).show();
	}


	@Override
	protected void onStop() {
		super.onStop();
		conn.stop();
	}


	@Override
	public void onServerListChanged(Map<String, NsdServiceInfo> servers) {
		ContentView contentView = getContent();
		if (contentView != null) {
		/*TODO 	if (contentView.getUri().equals(ContentProvider.URI_SERVERS)) {
				contentView.refresh();
			}*/
		}
	}

	@ViewById(R.id.fab)
	FloatingActionButton fab;


	@Override
	public void onConnect() {
		getSupportActionBar().setSubtitle(conn.getUrl());
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


	@Override
	public void onBackStackChanged() {
		getSupportActionBar()
				.setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount() > 1);
		ContentView contentView = getContent();
		if (contentView == null) return;
		switch (MopidyUris.match(contentView.getUri())) {
			case MopidyUris.MATCH_SERVERS:
				showFAB(R.drawable.ic_add);
				break;
			default:
				hideFAB();
				break;
		}
	}


}

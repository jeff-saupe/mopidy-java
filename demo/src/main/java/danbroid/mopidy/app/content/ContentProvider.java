package danbroid.mopidy.app.content;

import android.net.Uri;
import android.net.nsd.NsdServiceInfo;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.app.MopidyConnection;
import danbroid.mopidy.app.interfaces.ContentView;
import danbroid.mopidy.app.interfaces.MainPrefs_;
import danbroid.mopidy.app.util.MopidyServerDiscovery;
import danbroid.mopidy.app.util.MopidyUris;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.model.Ref;
import danbroid.mopidy.model.TlTrack;

/**
 * Created by dan on 10/12/17.
 */
@EBean(scope = EBean.Scope.Singleton)
public class ContentProvider {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContentProvider.class);


	String mopidy_host;
	int mopidy_port;


	@Bean
	MopidyConnection conn;


	@Pref
	MainPrefs_ prefs;

	@Bean
	MopidyServerDiscovery serverDiscovery;

	public void browse(Uri uri, ContentView contentView) {
		log.trace("browse(): {}", uri);

		switch (MopidyUris.match(uri)) {

			case MopidyUris.MATCH_ROOT:
				browseRoot(contentView);
				break;

			case MopidyUris.MATCH_SERVERS:
				browseServers(contentView);
				break;
			case MopidyUris.MATCH_MOPIDY_URI:
				log.error("MATCHED MOPIDY URI"); //TODO implement
				break;

			case MopidyUris.MATCH_SERVER:
				browseServer(uri, contentView);
				break;


			case MopidyUris.MATCH_TRACKLIST:
				browseTracklist(contentView);
				break;

			default:

				browseDirectory(uri.toString(), contentView);

				break;
		}
/*
		if (URI_ROOT.equals(uri)) {
			browseRoot(contentView);
		} else if (URI_SERVERS.equals(uri)) {
			browseServers(contentView);
		} else if (uri.startsWith(URI_SERVER)) {
			browseServer(uri, contentView);
		} else {
			browseDirectory(uri, contentView);
		}
*/


	}

	public void browseTracklist(final ContentView contentView) {
		log.trace("browseTracklist()");
		conn.getTrackList().getTlTrackList(new ResponseHandler<TlTrack[]>() {
			@Override
			public void onResponse(CallContext context, TlTrack[] result) {
				List<Ref> refs = new LinkedList<>();
				for (TlTrack track : result) {
					Ref ref = new Ref();
					ref.setType(Ref.TYPE_TRACK);
					ref.setName(track.getTrack().getName());
					ref.setUri(MopidyUris.uriTracklistItem(track.getTlid()).toString());
					refs.add(ref);
				}
				log.debug("tracklist length: {}", result.length);
				contentView.setContent(refs.toArray(new Ref[]{}));
			}
		});
	}

	private void browseServer(Uri uri, ContentView contentView) {
		log.debug("browseServer(): {}", uri);

		String parts[] = Uri.decode(uri.getLastPathSegment()).split(":");
		String host = parts[0];
		int port = Integer.parseInt(parts[1]);


		log.info("connecting to {}:{}", host, port);
		this.mopidy_host = host;
		this.mopidy_port = port;

		conn.start(mopidy_host, mopidy_port);


		browseTopDirectory(contentView);
	}

	public void browseRoot(ContentView contentView) {
		log.debug("browseRoot()");
		ArrayList<Ref> content = new ArrayList<>();

		Ref ref;

		ref = new Ref();
		ref.setType(Ref.TYPE_DIRECTORY);
		ref.setName("Servers");
		ref.setUri(MopidyUris.URI_SERVERS.toString());
		content.add(ref);

		contentView.setContent(content.toArray(new Ref[]{}));
	}

	public void browseServers(ContentView contentView) {
		log.debug("browseServers()");

		log.trace("server count: " + prefs.servers().get().size());
		ArrayList<Ref> servers = new ArrayList<>();

		for (NsdServiceInfo serviceInfo : serverDiscovery.getServerInfo().values()) {
			Ref ref = new Ref();
			ref.setType(Ref.TYPE_DIRECTORY);
			log.trace("discovered server: {} ", serviceInfo.getServiceName());
			ref.setName(serviceInfo.getServiceName());
			String host = serviceInfo.getHost().toString().substring(1);
			int port = serviceInfo.getPort();
			ref.setUri(MopidyUris.uriServer(host, port).toString());
			servers.add(ref);
		}

		for (String address : prefs.servers().get()) {
			log.trace("prefs server address: {} ", address);
			Ref ref = new Ref();
			ref.setType(Ref.TYPE_DIRECTORY);
			ref.setName(address);
			ref.setUri(MopidyUris.uriServer(address).toString());
			servers.add(ref);
		}

		contentView.setContent(servers.toArray(new Ref[]{}));
	}

	private void browseTopDirectory(final ContentView view) {
		log.error("browseTopDirectory()");

		conn.getLibrary().browse(null, new ResponseHandler<Ref[]>() {
			@Override
			public void onResponse(CallContext context, Ref[] refs) {
				refs = Arrays.copyOf(refs, refs.length + 1);
				Ref tracklist = new Ref();
				tracklist.setType(Ref.TYPE_DIRECTORY);
				tracklist.setName("Tracklist");
				tracklist.setUri(MopidyUris.URI_TRACKLIST.toString());
				refs[refs.length - 1] = tracklist;
				view.setContent(refs);
			}
		});

	}


	private void browseDirectory(String uri, final ContentView view) {
		log.error("browseDirectory(): {}", uri);

		conn.getLibrary().browse(uri, new ResponseHandler<Ref[]>() {
			@Override
			public void onResponse(CallContext context, final Ref[] refs) {
				view.setContent(refs);
			}
		});

	}


}


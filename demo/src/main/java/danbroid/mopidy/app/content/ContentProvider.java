package danbroid.mopidy.app.content;

import android.net.Uri;

import org.androidannotations.annotations.EBean;

import danbroid.mopidy.CallContext;
import danbroid.mopidy.MopidyConnection;
import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.app.interfaces.ContentView;
import danbroid.mopidy.model.Ref;

/**
 * Created by dan on 10/12/17.
 */
@EBean(scope = EBean.Scope.Singleton)
public class ContentProvider {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContentProvider.class);

	public static final String URI_SERVERS = "content://servers";
	public static final String URI_SERVER = "content://server";
	public static final String URI_CONTENT = "content://";

	String mopidy_host;
	int mopidy_port;

	MopidyConnection conn;

	public void browse(String uri, ContentView view) {
		log.trace("browse(): {}", uri);


		if (uri.startsWith(URI_SERVER)) {
			uri = uri.substring(URI_SERVER.length() + 1);

			String parts[] = uri.split(":");
			String host = parts[0];
			int port = Integer.parseInt(parts[1]);

			if (mopidy_host == null || !host.equals(mopidy_host) || mopidy_port != port) {
				log.info("connecting to {}:{}", host, port);
				this.mopidy_host = host;
				this.mopidy_port = port;
				if (conn != null)
					conn.stop();

				conn = new MopidyConnection(mopidy_host, mopidy_port);
				conn.start();
			}


			uri = "";
		}

		if (uri.startsWith(URI_CONTENT)) {
			uri = uri.substring(URI_CONTENT.length());
			uri = Uri.decode(uri);
		}


		browseDirectory(uri,view);


	}

	private void browseDirectory(String uri, final ContentView view) {
		log.error("browseDirectory(): {}", uri);

		if (uri.length() == 0) uri = null;

		conn.call(conn.getCore().getLibrary().browse(uri).setHandler(new ResponseHandler<Ref[]>() {
			@Override
			public void onResponse(CallContext context, Ref[] result) {
				for (Ref ref : result) {
					log.error(ref.getUri());
					ref.setUri(URI_CONTENT+Uri.encode(ref.getUri()));
				}
				view.setContent(result);
			}
		}));
	}

	public void start() {
		if (mopidy_host != null) {
			conn = new MopidyConnection(mopidy_host, mopidy_port);
			conn.start();
		}
	}

	public void stop() {
		if (conn != null) {
			conn.stop();
			conn = null;
		}
	}
}

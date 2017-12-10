package danbroid.mopidy.app.content;

import org.androidannotations.annotations.EBean;

import danbroid.mopidy.MopidyConnection;
import danbroid.mopidy.ResponseHandler;
import danbroid.mopidy.app.interfaces.ContentView;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.model.Ref;

/**
 * Created by dan on 10/12/17.
 */
@EBean(scope = EBean.Scope.Singleton)
public class ContentProvider {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ContentProvider.class);

	public static final String URI_SERVERS = "server://all";
	public static final String URI_SERVER = "server://";

	String mopidy_host;
	int mopidy_port;

	MopidyConnection conn;

	public void browse(String uri, ContentView view) {
		log.trace("browse(): {}", uri);


		if (uri.startsWith(URI_SERVER)) {
			uri = uri.substring(URI_SERVER.length());

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


		browseDirectory(uri, view);


	}

	private void browseDirectory(String uri, final ContentView view) {
		log.trace("browseDirectory(): {}", uri);

		if (uri.length() == 0) uri = null;


		conn.getLibrary().browse(uri, new ResponseHandler<Ref[]>() {
			@Override
			public void onResponse(CallContext context, final Ref[] refs) {
				view.setContent(refs);
			}
		});

	}

	public MopidyConnection getConnection() {
		return conn;
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


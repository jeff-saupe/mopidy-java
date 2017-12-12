package danbroid.mopidy;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import danbroid.mopidy.api.History;
import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.model.Image;
import danbroid.mopidy.model.TlTrack;
import danbroid.mopidy.model.Track;

/**
 * Created by dan on 13/12/17.
 */
public class Api {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Api.class);
	private static MopidyConnection conn;


	@Before
	public void setup() {
		if (conn == null) {
			ResourceBundle props = PropertyResourceBundle.getBundle("connection");
			String host = props.getString("host");
			int port = Integer.parseInt(props.getString("port"));
			log.debug("connecting to {}:{}", host, port);
			conn = new MopidyConnection(host, port);
			conn.start();
		}
	}

	@AfterClass
	public static void tearDown() throws Exception {
		if (conn != null) {
			synchronized (conn) {
				if (conn.getQueueSize() != 0) {
					conn.wait(10000);
				}
				conn.wait(500);

			}
		}
		log.debug("tearDown()");
		conn.stop();
		conn = null;
	}


	@Test
	public void getSchemes() {
		log.trace("getSchemes()");

		conn.getUriScemes(new ResponseHandler<String[]>() {
			@Override
			public void onResponse(CallContext context, String result[]) {
				for (String scheme : result) {
					log.debug("scheme: {}", scheme);
				}
			}
		});
	}


	@Test
	public void getHistoryLength() {
		log.debug("getHistoryLength()");
		conn.getHistory().getLength(new ResponseHandler<Integer>() {
			@Override
			public void onResponse(CallContext context, Integer result) {
				log.debug("history length: " + result);
			}
		});
	}

	@Test
	public void getHistory() {
		log.debug("getHistory()");
		conn.getHistory().getHistory(new ResponseHandler<History.HistoryItem[]>() {
			@Override
			public void onResponse(CallContext context, History.HistoryItem[] result) {
				for (History.HistoryItem item : result) {
					log.debug("item: {}", item);
				}
			}
		});
	}

	@Test
	public void getImages() {
		log.debug("getImages()");
		conn.getLibrary().getImages(new String[]{"rnz:news"}, new ResponseHandler<Map<String, Image[]>>() {
			@Override
			public void onResponse(CallContext context, Map<String, Image[]> result) {
				for (String uri : result.keySet()) {
					for (Image img : result.get(uri)) {
						log.debug("uri:{} has image:{}", uri, img.getUri());
					}
				}
			}
		});
	}

	@Test
	public void setMute() {
		conn.getMixer().setMute(false, new ResponseHandler<Boolean>() {
			@Override
			public void onResponse(CallContext context, Boolean result) {
				log.debug("success: " + result);
			}
		});
	}

	@Test
	public void getMute() {
		conn.getMixer().getMute(new ResponseHandler<Boolean>() {
			@Override
			public void onResponse(CallContext context, Boolean result) {
				log.debug("mute: " + result);
			}
		});
	}

	@Test
	public void getVolume() {
		conn.getMixer().getVolume(new ResponseHandler<Integer>() {
			@Override
			public void onResponse(CallContext context, Integer result) {
				log.debug("volume: " + result);
			}
		});
	}

	@Test
	public void getCurrentTlTrack() {
		conn.getPlayback().getCurrentTlTrack(new ResponseHandler<TlTrack>() {
			@Override
			public void onResponse(CallContext context, TlTrack result) {
				log.debug("tltrack: {}", result);
			}
		});
	}

	@Test
	public void getCurrentTLID() {
		conn.getPlayback().getCurrentTLID(new ResponseHandler<Integer>() {
			@Override
			public void onResponse(CallContext context, Integer result) {
				log.debug("tlid: {}", result);
			}
		});
	}

	@Test
	public void getCurrentTrack() {
		conn.getPlayback().getCurrentTrack(new ResponseHandler<Track>() {
			@Override
			public void onResponse(CallContext context, Track result) {
				log.debug("current track: {}", result);
			}
		});
	}


}

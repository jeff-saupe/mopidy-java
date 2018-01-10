package danbroid.mopidy.transport;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by dan on 21/12/17.
 */
public class WebSocketTransport extends Transport {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebSocketTransport.class);
	public static final int ERROR_CLOSE_CALLED = 1001;

	private WebSocket socket;

	@Override
	public void send(String request) {
		socket.send(request);
	}

	public WebSocketTransport(Callback callback) {
		super(callback);
	}


	@Override
	protected synchronized void open() {
		if (socket != null) throw new IllegalArgumentException("socket already exists");

		OkHttpClient client = new OkHttpClient.Builder()
				.readTimeout(5, TimeUnit.SECONDS)
				.retryOnConnectionFailure(true)
				.connectTimeout(5, TimeUnit.SECONDS).build();


		Request request = new Request.Builder().url(url).build();

		this.socket = client.newWebSocket(request, new WebSocketListener() {
			@Override
			public void onMessage(WebSocket webSocket, String text) {
				WebSocketTransport.this.onMessage(text);
			}
		});
	}

	@Override
	public synchronized void close() {
		if (socket == null) return;
		socket.close(ERROR_CLOSE_CALLED, "close() called");
		socket = null;
	}


}

package danbroid.mopidy;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketExtension;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import danbroid.mopidy.transport.Transport;

/**
 * Created by dan on 19/01/18.
 */
@EBean
public class AndroidWebSocket extends Transport {
	private static final Logger log = LoggerFactory.getLogger(AndroidWebSocket.class);
	private WebSocket socket;


	@Override
	public void send(String request) {
		socket.sendText(request);
	}

	@Override
	protected synchronized void open() {
		connect();
		try {
			wait(5000);
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
		}

	}

	@Background(serial = "websocket")
	protected void connect() {
		try {
			socket = new WebSocketFactory().createSocket(url);
			socket.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE);
			socket.connect();
			socket.addListener(new WebSocketAdapter() {
				@Override
				public void onTextMessage(WebSocket websocket, String text) throws Exception {
					callback.onMessage(text);
				}

				@Override
				public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed) throws Exception {
					log.error("onMessageDecompressionError(): ", cause);
				}

				@Override
				public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
					super.onBinaryFrame(websocket, frame);
				}
			});
		} catch (Exception e) {
			callback.onError(e);
		}

		synchronized (this) {
			notify();
		}
	}

	@Background(serial = "websocket")
	@Override
	public void close() {
		socket.sendClose(ERROR_CLOSE_CALLED);
	}
}

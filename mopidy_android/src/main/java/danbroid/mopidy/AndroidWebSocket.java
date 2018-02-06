package danbroid.mopidy;

import android.os.Handler;
import android.os.Looper;

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
	private static final long RECONNECT_INTERVAL = 10000;
	private WebSocket socket;

	private Handler handler = null;


	public AndroidWebSocket() {
		super(null);
	}

	private final Handler.Callback handlerCallback = msg -> {
		open();
		return true;
	};

	@Override
	public void send(String request) {
		if (BuildConfig.DEBUG){
			if (socket == null){
				log.error("socket is null, cant send: {}",request);
				return;
			}
		}
		socket.sendText(request);
	}


	@Background(serial = "websocket")
	public void open() {
		log.trace("open()");
		try {
			if (socket != null) {
				log.trace("closing existing socket..");
				socket.sendClose();
				socket = null;
			}

			if (handler == null) {
				handler = new Handler(Looper.getMainLooper(), handlerCallback);
			}


			socket = new WebSocketFactory().createSocket(url);
			socket.addExtension(WebSocketExtension.PERMESSAGE_DEFLATE);

			socket.addListener(new WebSocketAdapter() {
				@Override
				public void onTextMessage(WebSocket websocket, String text) throws Exception {
					callback.onMessage(text);
				}

				@Override
				public void onMessageDecompressionError(WebSocket websocket, WebSocketException cause, byte[] compressed)
						throws Exception {
					log.error("onMessageDecompressionError(): ", cause);
				}

				@Override
				public void onBinaryFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
					log.trace("onBinaryFrame(): length: " + frame.getPayload().length);
				}

				@Override
				public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
					log.error("onDisconnected()");
					setConnected(false);
				}
			});


			socket.connect();

			setConnected(true);

		} catch (Exception e) {
			setConnected(false);
			callback.onError(e);
		}

		synchronized (this) {
			notify();
		}
	}


	@Override
	protected void setConnected(boolean connected) {
		super.setConnected(connected);
		if (handler != null) {
			if (connected) {
				handler.removeMessages(0);
			} else {
				handler.sendEmptyMessageDelayed(0, RECONNECT_INTERVAL);
			}
		}
	}

	@Background(serial = "websocket")
	@Override
	public void close() {
		log.warn("close()");

		if (socket != null) {
			socket.sendClose(ERROR_CLOSE_CALLED);
			socket = null;
		}
		setConnected(false);
	}


	@Background(serial = "websocket")
	@Override
	public void reconnect() {
		if (!isConnected())
			open();
	}
}

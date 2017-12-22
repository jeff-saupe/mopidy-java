package danbroid.mopidy.transport;

/**
 * Created by dan on 21/12/17.
 */
public abstract class Transport {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Transport.class);

	protected String url;
	protected final Callback callback;

	public abstract void send(String request);

	public interface Callback {
		void onMessage(String message);
	}

	protected Transport(Callback callback) {
		this.callback = callback;
	}

	public final void connect(String url) {
		log.info("connect(): {}", url);
		this.url = url;
		open();
	}


	public String getUrl() {
		return url;
	}

	protected abstract void open();

	public abstract void close();

	protected void onMessage(String message) {
		//log.trace("onMessage(): {}", message);
		callback.onMessage(message);
	}


}

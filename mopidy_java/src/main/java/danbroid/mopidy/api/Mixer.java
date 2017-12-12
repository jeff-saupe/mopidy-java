package danbroid.mopidy.api;

import danbroid.mopidy.ResponseHandler;

/**
 * Created by dan on 13/12/17.
 */
public class Mixer extends Api {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Mixer.class);

	protected Mixer(Api parent) {
		super(parent, "mixer.");
	}

	public void getMute(ResponseHandler<Boolean> handler) {
		call(new Call(methodPrefix + "get_mute", Boolean.class)
				.setHandler(handler));
	}

	public void setMute(boolean mute, ResponseHandler<Boolean> successHandler) {
		call(new Call(methodPrefix + "set_mute", Boolean.class)
				.addParam("mute", mute)
				.setHandler(successHandler));
	}

	//Integer in range [0..100] or :class:`None` if unknown.
	public void getVolume(ResponseHandler<Integer> handler) {
		call(new Call(methodPrefix + "get_volume", Integer.class).setHandler(handler));
	}

	/**
	 * @param volume  integer [1..100] the volume to set
	 * @param handler accepts true if successful
	 */
	public void setVolume(int volume, ResponseHandler<Boolean> handler) {
		call(new Call(methodPrefix + "set_volume", Boolean.class)
				.addParam("volume", volume)
				.setHandler(handler));
	}
}

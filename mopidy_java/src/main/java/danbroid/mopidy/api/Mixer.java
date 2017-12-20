package danbroid.mopidy.api;

import danbroid.mopidy.ResponseHandler;

/**
 * Created by dan on 13/12/17.
 * Implementation complete
 */
public class Mixer extends Api {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Mixer.class);

	protected Mixer(Api parent) {
		super(parent, "mixer.");
	}

	public void getMute(ResponseHandler<Boolean> handler) {
		call(new Call<Boolean>(methodPrefix + "get_mute")
				.setResultType(Boolean.class)
				.setHandler(handler));
	}

	public void setMute(boolean mute, ResponseHandler<Boolean> successHandler) {
		call(new Call<Boolean>(methodPrefix + "set_mute")
				.addParam("mute", mute)
				.setResultType(Boolean.class)
				.setHandler(successHandler));
	}

	//Integer in range [0..100] or :class:`None` if unknown.
	public void getVolume(ResponseHandler<Integer> handler) {
		call(new Call<Integer>(methodPrefix + "get_volume").setHandler(handler)
				.setResultType(Integer.class));
	}

	/**
	 * @param volume  integer [1..100] the volume to set
	 * @param handler accepts true if successful
	 */
	public void setVolume(int volume, ResponseHandler<Boolean> handler) {
		call(new Call<Boolean>(methodPrefix + "set_volume")
				.setResultType(Boolean.class)
				.addParam("volume", volume)
				.setHandler(handler));
	}
}
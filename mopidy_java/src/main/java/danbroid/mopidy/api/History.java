package danbroid.mopidy.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.LinkedList;

import danbroid.mopidy.interfaces.CallContext;
import danbroid.mopidy.model.Ref;


/**
 * Created by dan on 13/12/17.
 */
public class History extends Api {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(History.class);

	protected History(Api parent) {
		super(parent, "history.");
	}

	//Get the number of tracks in the history.
	public Call<Integer> getLength() {
		return createCall("get_length", Integer.class);
	}


	public static class HistoryItem {
		final long timestamp;
		final Ref track;

		public HistoryItem(long timestamp, Ref track) {
			this.timestamp = timestamp;
			this.track = track;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public Ref getTrack() {
			return track;
		}

		@Override
		public String toString() {
			return "[" + timestamp + "," + track + "]";
		}
	}

	//Get the history
	public Call<HistoryItem[]> getHistory() {
		return new Call<HistoryItem[]>("get_history", getConnection()) {
			@Override
			protected HistoryItem[] parseResult(CallContext callContext, JsonElement response) {
				LinkedList<HistoryItem> result = new LinkedList<>();
				JsonArray a = response.getAsJsonArray();
				for (int i = 0; i < a.size(); i++) {
					JsonArray tuple = a.get(i).getAsJsonArray();
					long timestamp = tuple.get(0).getAsLong();
					JsonElement e = tuple.get(1);
					Ref ref = callContext.getGson().fromJson(e, Ref.class);
					result.add(new HistoryItem(timestamp, ref));
				}
				return result.toArray(new HistoryItem[]{});
			}
		}.setResultType(HistoryItem[].class);
	}
}

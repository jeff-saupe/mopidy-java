package saupe.mopidy.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.LinkedList;

import saupe.mopidy.model.Ref;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/*
 * See: https://github.com/mopidy/mopidy/blob/develop/mopidy/core/history.py
 */

@Slf4j
public class History extends Api {
    protected History(Api parent) {
        super(parent, "history.");
    }

    /**
     * Get the number of tracks in the history.
     *
     * @return Integer The history length
     */
    public Call<Integer> getLength() {
        return createCall("get_length", Integer.class);
    }

    /**
     * Get the track history.
     * <p>
     * The timestamps are milliseconds since epoch.
     *
     * @return Array of {@link HistoryItem} of the track history
     */
    public Call<HistoryItem[]> getHistory() {
        return new Call<HistoryItem[]>("get_history") {
            @Override
            protected HistoryItem[] parseResult(JsonElement response) {
                LinkedList<HistoryItem> result = new LinkedList<>();
                JsonArray a = response.getAsJsonArray();
                for (int i = 0; i < a.size(); i++) {
                    JsonArray tuple = a.get(i).getAsJsonArray();
                    long timestamp = tuple.get(0).getAsLong();
                    JsonElement e = tuple.get(1);
                    Ref ref = getGson().fromJson(e, Ref.class);
                    result.add(new HistoryItem(timestamp, ref));
                }
                return result.toArray(new HistoryItem[]{});
            }
        }.setResultType(HistoryItem[].class);
    }

    @AllArgsConstructor
    @Getter
    public static class HistoryItem {
        private final long timestamp;
        private final Ref track;

        @Override
        public String toString() {
            return "[" + timestamp + "," + track + "]";
        }
    }
}
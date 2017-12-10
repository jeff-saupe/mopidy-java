package danbroid.mopidy.app.interfaces;

import danbroid.mopidy.model.Ref;

/**
 * Created by dan on 10/12/17.
 */
public interface MainView {
	void onItemSelected(Ref ref);

	void browse(String uri, ContentView contentView);


}

package danbroid.mopidy.app.interfaces;

import danbroid.mopidy.model.Ref;

/**
 * Created by dan on 10/12/17.
 */
public interface MainView {

	ContentView getContent();

	void showFullControls();

	void hideFullControls();

	void onRefClicked(Ref ref);
}

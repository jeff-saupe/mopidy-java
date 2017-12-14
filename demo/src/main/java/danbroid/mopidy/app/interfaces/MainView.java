package danbroid.mopidy.app.interfaces;

import android.net.Uri;
import android.view.View;

import danbroid.mopidy.model.Ref;

/**
 * Created by dan on 10/12/17.
 */
public interface MainView {
	void onItemSelected(Ref ref);

	void browse(Uri uri, ContentView contentView);

	ContentView getContent();

	void onItemLongClicked(Ref ref, View v);

	void showBottomControls(boolean animate);

	void hideBottomControls();

	void showFullControls();

	void hideFullControls();

}

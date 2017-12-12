package danbroid.mopidy.app.interfaces;

import android.support.annotation.DrawableRes;
import android.view.View;

import danbroid.mopidy.model.Ref;

/**
 * Created by dan on 10/12/17.
 */
public interface MainView {
	void onItemSelected(Ref ref);

	void browse(String uri, ContentView contentView);

	ContentView getContent();

	void onItemLongClicked(Ref ref, View v);
}

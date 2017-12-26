package danbroid.mopidy.interfaces;

import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.DefaultStringSet;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

import java.util.Set;

/**
 * Created by dan on 12/12/17.
 */
@SharedPref(SharedPref.Scope.APPLICATION_DEFAULT)
public interface MopidyPrefs {

	@DefaultStringSet(value = {})
	Set<String> servers();


	String lastConnectionURL();

	String lastServerURL();

}

package danbroid.mopidy.app.util;

/**
 * Created by dan on 21/12/17.
 */
public class StringUtil {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringUtil.class);

	public static String value(Object o){
		return o == null ? "" : o.toString();
	}
}

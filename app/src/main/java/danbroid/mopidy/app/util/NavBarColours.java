package danbroid.mopidy.app.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.view.Surface;
import android.view.Window;

/**
 * Utility class for setting the colour of the nav-bar and status bar
 * so it blends well with an image.
 * Nasty and ugly and (probably) slow and only works on {@link android.os.Build.VERSION_CODES#LOLLIPOP} or greater.
 */

public class NavBarColours {

	/**
	 * Sets the colour of the navigation bar and status bar so that they match the top/bottom of the supplied
	 * image
	 *
	 * @param activity
	 * @param bitmap
	 */

	public static void configureNavBarColours(Activity activity, Bitmap bitmap) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			Window window = activity.getWindow();

			int w = bitmap.getWidth();
			int h = bitmap.getHeight();
			int offset = 2;
			int pixel = 0;
			float red, green, blue;

			int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
			boolean landscape = rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270;


			int pixels[]  = getBitmapPixels(bitmap, 0, 0, w, offset);


			red = green = blue = 0;


			for (int n = 1; n <= pixels.length; n++) {
				float N = ((float) (n - 1)) / n;
				red = red * N + ((float) Color.red(pixels[n - 1])) / n;
				green = green * N + ((float) Color.green(pixels[n - 1])) / n;
				blue = blue * N + ((float) Color.blue(pixels[n - 1])) / n;
			}
			if ((red + green + blue) > 210 * 3) {
				red *= 0.8;
				green *= 0.8;
				blue *= 0.8;
			}
			pixel = Color.rgb((int) red, (int) green, (int) blue);
			window.setStatusBarColor(pixel);

			if (landscape) {
				pixels = getBitmapPixels(bitmap, h - offset, 0, offset, h);
			} else {
				bitmap.getPixels(pixels, 0, w, 0, h - offset, w, offset);
			}


			red = green = blue = 0;
			//New average = old average * (n-1)/n + new value /n


			for (int n = 1; n <= pixels.length; n++) {
				float N = ((float) (n - 1)) / n;
				red = red * N + ((float) Color.red(pixels[n - 1])) / n;
				green = green * N + ((float) Color.green(pixels[n - 1])) / n;
				blue = blue * N + ((float) Color.blue(pixels[n - 1])) / n;
			}

			if ((red + green + blue) > 210 * 3) {
				red *= 0.8;
				green *= 0.8;
				blue *= 0.8;
			}

			pixel = Color.rgb((int) red, (int) green, (int) blue);
			window.setNavigationBarColor(pixel);
		}
	}

	public static int[] getBitmapPixels(Bitmap bitmap, int x, int y, int width, int height) {
		int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
		bitmap.getPixels(pixels, 0, bitmap.getWidth(), x, y,
				width, height);
		final int[] subsetPixels = new int[width * height];
		for (int row = 0; row < height; row++) {
			System.arraycopy(pixels, (row * bitmap.getWidth()),
					subsetPixels, row * width, width);
		}
		return subsetPixels;
	}
}

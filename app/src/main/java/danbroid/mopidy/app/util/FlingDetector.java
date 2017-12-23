package danbroid.mopidy.app.util;

import android.content.Context;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by dan on 19/12/17.
 */
public class FlingDetector implements GestureDetector.OnGestureListener, View.OnTouchListener {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FlingDetector.class);

	private final GestureDetectorCompat gestureDetector;
	private int threshold = 50;

	public FlingDetector(Context context) {
		gestureDetector = new GestureDetectorCompat(context, this);
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public int getThreshold() {
		return threshold;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		log.trace("onFling() vx: {} vy: {}", velocityX, velocityY);
		if (Math.abs(velocityX) < Math.abs(velocityY)) {
			if (velocityY < -threshold) {
				return onFlingUp(e1, e2, velocityX, velocityY);
			} else if (velocityY > threshold) {
				return onFlingDown(e1, e2, velocityX, velocityY);
			}
		} else {
			if (velocityX > threshold) {
				return onFlingRight(e1, e2, velocityX, velocityY);
			} else if (velocityX < -threshold) {
				return onFlingLeft(e1, e2, velocityX, velocityY);
			}
		}

		return false;
	}

	protected boolean onFlingRight(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return true;
	}

	protected boolean onFlingLeft(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return true;
	}


	protected boolean onFlingUp(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return true;
	}

	protected boolean onFlingDown(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return true;
	}
}

package net.reichholf.dreamdroid.video;

import android.net.Uri;
import android.view.SurfaceView;

import org.videolan.libvlc.IVLCVout;

/**
 * Created by Stephan on 26.12.2016.
 */

public interface VideoPlayer {
	void deinit();

	void attach(IVLCVout.OnNewVideoLayoutListener newVideoLayoutListener, SurfaceView surfaceView, SurfaceView subtitleSurfaceView);

	void detach();

	void setWindowSize(int width, int height);

	void playUri(Uri uri, int flags);

	void play();

	void stop();

	long getLength();

	float getPosition();

	void setPosition(float position);

	boolean isSeekable();

	int getAudioTracksCount();

	int getSubtitleTracksCount();
}

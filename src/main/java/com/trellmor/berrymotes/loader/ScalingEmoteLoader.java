package com.trellmor.berrymotes.loader;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;

import java.io.FileDescriptor;

public class ScalingEmoteLoader extends ContextWrapper implements EmoteLoader {
	/**
	 * Create new ScalingEmoteLoader instance
	 * 
	 * Automatically upscales the bitmap to the device DPI
	 * 
	 * @param ctx
	 *            Android context
	 */
	public ScalingEmoteLoader(Context ctx) {
		super(ctx);
	}

	@Override
	public Drawable fromPath(String path) {
		Drawable drawable = null;
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDensity = DisplayMetrics.DENSITY_MEDIUM;		
		
		Bitmap bitmap = BitmapFactory.decodeFile(path, options);
		if (bitmap != null) {
			drawable = new BitmapDrawable(getResources(), bitmap);
		}
		
		return drawable;
	}

	@Override
	public Drawable fromFileDescriptor(FileDescriptor fd) {
		Drawable drawable = null;

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDensity = DisplayMetrics.DENSITY_MEDIUM;

		Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd, null, options);
		if (bitmap != null) {
			drawable = new BitmapDrawable(getResources(), bitmap);
		}

		return drawable;
	}
}

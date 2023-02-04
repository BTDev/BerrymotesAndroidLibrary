/*
 * LibBerryMotes
 * 
 * Copyright (C) 2013 Daniel Triendl <trellmor@trellmor.com>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.trellmor.berrymotes;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.ParcelFileDescriptor;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.util.LruCache;

import com.trellmor.berrymotes.loader.BasicEmoteLoader;
import com.trellmor.berrymotes.loader.EmoteLoader;
import com.trellmor.berrymotes.provider.EmotesContract;
import com.trellmor.berrymotes.provider.FileContract;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * EmoteGetter
 * <p/>
 * Fetches emotes from sd-card and returns Drawables
 * <p/>
 * Decends from ImageGetter and can be used with
 * {@link android.text.Html#fromHtml(String, ImageGetter, android.text.Html.TagHandler)}
 *
 * @author Daniel
 */
public class EmoteGetter implements ImageGetter {
	private static final String TAG = EmoteGetter.class.getName();

	private final ContentResolver mResolver;

	private final String[] PROJECTION = {EmotesContract.Emote.COLUMN_NAME,
			EmotesContract.Emote.COLUMN_APNG, EmotesContract.Emote.COLUMN_DELAY};
	private static final LruCache<String, Drawable> mCache = new LruCache<>(50);
	private static final LruCache<String, AnimationEmote> mAnimationCache = new LruCache<>(10);
	private final EmoteLoader mLoader;
	private static final HashSet<String> mBlacklist = new HashSet<>();

	/**
	 * Create new {@link EmoteGetter} instance
	 *
	 * @param context Android context
	 */
	public EmoteGetter(Context context) {
		this(context, new BasicEmoteLoader());
	}

	/**
	 * Create new EmoteGetter instance
	 *
	 * @param context Android context
	 * @param loader  Emote loader instance
	 */
	public EmoteGetter(Context context, EmoteLoader loader) {
		mResolver = context.getContentResolver();
		mLoader = loader;
	}

	@Override
	public Drawable getDrawable(String source) {
		synchronized (mBlacklist) {
			if (mBlacklist.contains(source))
				return null;
		}

		Drawable d = mCache.get(source);
		if (d != null)
			return d;

		AnimationEmote ae = mAnimationCache.get(source);
		if (ae != null) {
			try {
				d = ae.newDrawable();
				d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
				return d;
			} catch (OutOfMemoryError e) {
				return null;
			}
		}

		Cursor cursor = mResolver.query(EmotesContract.Emote.CONTENT_URI,
				PROJECTION, EmotesContract.Emote.COLUMN_NAME + "=?",
				new String[]{source}, EmotesContract.Emote.COLUMN_INDEX + " ASC");

		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();

			final int POS_NAME = cursor
					.getColumnIndex(EmotesContract.Emote.COLUMN_NAME);

			if (cursor.getCount() > 1
					&& cursor.getInt(cursor
					.getColumnIndex(EmotesContract.Emote.COLUMN_APNG)) == 1) {

				// Create AnimationDrawable
				ae = new AnimationEmote();

				final int POS_DELAY = cursor.getColumnIndex(EmotesContract.Emote.COLUMN_DELAY);

				try {
					do {
						ParcelFileDescriptor fd = mResolver.openFileDescriptor(FileContract.getUriForEmote(cursor.getString(POS_NAME), false), "r");
						Drawable frame =  mLoader.fromFileDescriptor(fd.getFileDescriptor());
						if (frame != null) {
							ae.addFrame(frame, cursor.getInt(POS_DELAY));
						}
					} while (cursor.moveToNext());
					d = ae.newDrawable();
					mAnimationCache.put(source, ae);
				} catch (OutOfMemoryError | FileNotFoundException e) {
					d = null;
					Log.e(TAG, "Failed to load " + source, e);
				}
			} else {
				try {
					ParcelFileDescriptor fd = mResolver.openFileDescriptor(FileContract.getUriForEmote(cursor.getString(POS_NAME), false), "r");
					d = mLoader.fromFileDescriptor(fd.getFileDescriptor());
				} catch (OutOfMemoryError | FileNotFoundException e) {
					d = null;
					Log.e(TAG, "Failed to load " + source, e);
				}
				if (d != null) {
					mCache.put(source, d);
				}
			}
		}

		if (d == null) {
			synchronized (mBlacklist) {
				if (!mBlacklist.contains(source)) mBlacklist.add(source);
			}
		}

		if (cursor != null)
			cursor.close();

		if (d != null) {
			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		}
		return d;
	}

	private class AnimationEmote {
		private final ArrayList<AnimationEmoteFrame> mFrames = new ArrayList<>();

		void addFrame(Drawable drawable, int duration) {
			mFrames.add(new AnimationEmoteFrame(drawable, duration));
		}

		Drawable newDrawable() {
			AnimationDrawable d = new AnimationDrawable();
			for (AnimationEmoteFrame frame : mFrames) {
				d.addFrame(frame.getDrawable(), frame.getDuration());
			}
			d.setOneShot(false);
			return d;
		}

		private class AnimationEmoteFrame {
			private final Drawable mDrawable;
			private final int mDuration;

			AnimationEmoteFrame(Drawable drawable, int duration) {
				mDrawable = drawable;
				mDuration = duration;
			}

			public Drawable getDrawable() {
				return mDrawable;
			}

			int getDuration() {
				return mDuration;
			}
		}
	}
}

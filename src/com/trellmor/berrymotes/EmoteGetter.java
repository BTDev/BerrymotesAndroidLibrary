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

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.text.Html.ImageGetter;

import com.trellmor.berrymotes.provider.EmotesContract;

/**
 * EmoteGetter
 * 
 * Fetches emotes from sd-card and returns Drawables
 * 
 * Decends from ImageGetter and can be used with
 * {@link android.text.Html#fromHtml(String, ImageGetter, android.text.Html.TagHandler)}
 * 
 * @author Daniel
 * 
 */
public class EmoteGetter implements ImageGetter {
	private ContentResolver mResolver;

	private final String[] PROJECTION = { EmotesContract.Emote.COLUMN_IMAGE,
			EmotesContract.Emote.COLUMN_APNG, EmotesContract.Emote.COLUMN_DELAY };
	private LruCache<String, Drawable> mCache;
	private LruCache<String, AnimationEmode> mAnimationCache;

	/**
	 * Create new EmoteGetter instance
	 * 
	 * @param context Android context
	 */
	public EmoteGetter(Context context) {
		mResolver = context.getContentResolver();
		mCache = new LruCache<String, Drawable>(20);
		mAnimationCache = new LruCache<String, AnimationEmode>(5);
	}

	@Override
	public Drawable getDrawable(String source) {
		Drawable d = mCache.get(source);
		if (d != null)
			return d;

		AnimationEmode ae = mAnimationCache.get(source);
		if (ae != null) {
			d = ae.newDrawable();
			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		}

		Cursor cursor = mResolver.query(EmotesContract.Emote.CONTENT_URI,
				PROJECTION, EmotesContract.Emote.COLUMN_NAME + "=?",
				new String[] { source }, EmotesContract.Emote.COLUMN_INDEX
						+ " ASC");

		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToFirst();

			final int POS_IMAGE = cursor
					.getColumnIndex(EmotesContract.Emote.COLUMN_IMAGE);

			if (cursor.getCount() > 1
					&& cursor.getInt(cursor
							.getColumnIndex(EmotesContract.Emote.COLUMN_APNG)) == 1) {

				// Create AnimationDrawable
				ae = new AnimationEmode();

				final int POS_DELAY = cursor
						.getColumnIndex(EmotesContract.Emote.COLUMN_DELAY);

				do {
					String path = cursor.getString(POS_IMAGE);
					Drawable frame = Drawable.createFromPath(path);
					if (frame != null) {
						ae.addFrame(frame, cursor.getInt(POS_DELAY));
					}
				} while (cursor.moveToNext());
				mAnimationCache.put(source, ae);
				d = ae.newDrawable();
			} else {
				String file = cursor.getString(POS_IMAGE);
				d = Drawable.createFromPath(file);
				if (d != null) {
					mCache.put(source, d);
				}
			}
		}

		if (cursor != null)
			cursor.close();

		if (d != null) {
			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
		}
		return d;
	}

	private class AnimationEmode {
		private ArrayList<AnimationEmoteFrame> mFrames = new ArrayList<AnimationEmoteFrame>();

		public void addFrame(Drawable drawable, int duration) {
			mFrames.add(new AnimationEmoteFrame(drawable, duration));
		}

		public Drawable newDrawable() {
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

			public AnimationEmoteFrame(Drawable drawable, int duration) {
				mDrawable = drawable;
				mDuration = duration;
			}

			public Drawable getDrawable() {
				return mDrawable;
			}

			public int getDuration() {
				return mDuration;
			}
		}
	}
}

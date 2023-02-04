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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import androidx.annotation.NonNull;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * TextView to properly handle AnimationDrawables
 * 
 * @author Daniel
 *
 */
public class AnimationTextView extends TextView implements Drawable.Callback {
	private final Handler mHandler = new Handler();
	private final AnimationTextWatcher mTextWatcher = new AnimationTextWatcher();

	public AnimationTextView(Context context) {
		super(context);
	}

	public AnimationTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AnimationTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		handleAnimationDrawable(true);
		addTextChangedListener(mTextWatcher);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		handleAnimationDrawable(false);
		removeTextChangedListener(mTextWatcher);
	}
	
	private void handleAnimationDrawable(boolean isPlay) {
		CharSequence text = getText();
		handleAnimationDrawable(isPlay, text);
	}

	private void handleAnimationDrawable(boolean isPlay, CharSequence text) {
		if (text instanceof Spanned) {
			Spanned span = (Spanned) text;
			ImageSpan[] spans = span
					.getSpans(0, span.length(), ImageSpan.class);
			for (ImageSpan i : spans) {
				Drawable d = i.getDrawable();
				if (d instanceof AnimationDrawable) {
					AnimationDrawable ad = (AnimationDrawable) d;
					if (isPlay) {
						ad.setCallback(this);
						ad.start();
					} else {
						ad.setCallback(null);
						ad.stop();
					}
				}
			}
		}
	}

	@SuppressLint("WrongCall")
	@Override
	public void invalidateDrawable(@NonNull Drawable dr) {
		if (dr instanceof AnimationDrawable) {
			// TextView caches some state, onLayout forces it to rerender
			onLayout(true, getLeft(), getTop(), getRight(), getBottom());
			invalidate();
			return;
		}

		super.invalidateDrawable(dr);
	}

	@Override
	public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
		mHandler.postAtTime(what, when);
	}

	@Override
	public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
		mHandler.removeCallbacks(what);
	}

	private class AnimationTextWatcher implements TextWatcher {
		@Override
		public void afterTextChanged(Editable s) {
			handleAnimationDrawable(true, s);
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			handleAnimationDrawable(false, s);
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	}
}

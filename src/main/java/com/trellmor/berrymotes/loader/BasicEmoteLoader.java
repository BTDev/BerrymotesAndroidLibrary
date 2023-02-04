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
package com.trellmor.berrymotes.loader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Basic emote loaders
 * 
 * Loads the file as is
 * 
 * @author Daniel
 *
 */
public class BasicEmoteLoader implements EmoteLoader {

	@Override
	public Drawable fromPath(String path) {
		return Drawable.createFromPath(path);
	}

	@Override
	public Drawable fromFileDescriptor(FileDescriptor fd) {
		try (FileInputStream stream = new FileInputStream(fd)) {
			return Drawable.createFromStream(stream, "emote.png");
		} catch (IOException e) {
			return null;
		}
	}
}

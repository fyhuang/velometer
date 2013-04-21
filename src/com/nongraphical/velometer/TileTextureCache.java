package com.nongraphical.velometer;

import java.util.LinkedHashMap;
import java.util.Map;

import org.mapsforge.core.model.Tile;

import android.graphics.Bitmap;

public class TileTextureCache extends LinkedHashMap<Tile, GLTexture> {
	int mMaxItems;
	
	public TileTextureCache(int maxItems) {
		super(maxItems, 0.75f, true);
		mMaxItems = maxItems;
	}
	
	@Override
	protected boolean removeEldestEntry(Map.Entry eldest) {
		return size() > mMaxItems;
	}
}

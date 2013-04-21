package com.nongraphical.velometer;

import java.util.ArrayList;
import java.util.HashSet;

import org.mapsforge.core.model.Tile;
import org.mapsforge.core.util.MercatorProjection;

public class TileFrustum {
	float mLat, mLng, mRot, mViewAngle;
	int mZoomLevel;
	
	HashSet<Tile> mVisibleTiles;
	
	public TileFrustum(float lat, float lng, float rot, float view_angle, int zoom_lvl) {
		update(lat, lng, rot, view_angle, zoom_lvl);
	}
	
	public void update(float lat, float lng, float rot, float view_angle, int zoom_lvl) {
		mLat = lat;
		mLng = lng;
		mRot = rot;
		mViewAngle = view_angle;
		mZoomLevel = zoom_lvl;
		
		// TODO: calculate visible tiles
		mVisibleTiles.clear();
		long tileX = MercatorProjection.longitudeToTileX(mLng, (byte)mZoomLevel);
		long tileY = MercatorProjection.latitudeToTileY(mLat, (byte)mZoomLevel);
		mVisibleTiles.add(new Tile(tileX, tileY, (byte)mZoomLevel));
		mVisibleTiles.add(new Tile(tileX+1, tileY, (byte)mZoomLevel));
		mVisibleTiles.add(new Tile(tileX, tileY+1, (byte)mZoomLevel));
		mVisibleTiles.add(new Tile(tileX-1, tileY, (byte)mZoomLevel));
		mVisibleTiles.add(new Tile(tileX, tileY-1, (byte)mZoomLevel));
	}
	
	public HashSet<Tile> getVisibleTiles() {
		return mVisibleTiles;
	}
}

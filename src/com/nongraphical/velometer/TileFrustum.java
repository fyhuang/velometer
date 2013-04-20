package com.nongraphical.velometer;

import java.util.ArrayList;

import org.mapsforge.core.model.Tile;
import org.mapsforge.core.util.MercatorProjection;

public class TileFrustum {
	float mLat, mLng, mRot, mViewAngle;
	int mZoomLevel;
	
	public TileFrustum(float lat, float lng, float rot, float view_angle, int zoom_lvl) {
		update(lat, lng, rot, view_angle, zoom_lvl);
	}
	
	public void update(float lat, float lng, float rot, float view_angle, int zoom_lvl) {
		mLat = lat;
		mLng = lng;
		mRot = rot;
		mViewAngle = view_angle;
		mZoomLevel = zoom_lvl;
	}
	
	public ArrayList<Tile> getVisibleTiles() {
		ArrayList<Tile> tiles = new ArrayList<Tile>();
		
		long tileX = MercatorProjection.longitudeToTileX(mLng, (byte)mZoomLevel);
		long tileY = MercatorProjection.latitudeToTileY(mLat, (byte)mZoomLevel);
		Tile tile = new Tile(tileX, tileY, (byte)mZoomLevel);
		tiles.add(tile);
		
		return tiles;
	}
}

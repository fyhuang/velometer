package com.nongraphical.velometer;

import java.util.ArrayList;
import java.util.HashSet;

import javax.microedition.khronos.opengles.GL10;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewPosition;
import org.mapsforge.android.maps.mapgenerator.TileCache;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.Tile;
import org.mapsforge.core.util.MercatorProjection;

import android.graphics.Bitmap;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;

public class MapsforgeGL {
	Drawing d;
	TileTextureCache mTextureCache;
	// Rotation of view
	float mHeading;
	final float MAP_SCALE = 1.0f / 256.0f;
	
	// Imported from MapView
	MapView mMV;
	MapViewPosition mMVPos;
	
	
	public MapsforgeGL(MapView mv) {
		d = new Drawing();
		mTextureCache = new TileTextureCache(64);
		mHeading = 0.0f;
		
		if (mv == null) {
			Log.e("MapsforgeGL", "TODO MapsforgeGL must be used with a MapView");
			throw new UnsupportedOperationException("TODO MapsforgeGL must be used with a MapView");
		}
		
		mMV = mv;
		mMVPos = mv.getMapViewPosition();
	}
	
	public void init(GL10 gl) {
	}
	
	void applyCamera(GL10 gl) {
		final float DIST = 3.0f / MAP_SCALE;
		
		GeoPoint gp = mMVPos.getCenter();
		float centerx = (float)MercatorProjection.longitudeToPixelX(gp.longitude, mMVPos.getZoomLevel()),
			  centerz = (float)MercatorProjection.latitudeToPixelY(gp.latitude, mMVPos.getZoomLevel());
		float offsetx = (float)Math.sin(mHeading) * DIST,
		      offsety = DIST / 2.0f,
			  offsetz = (float)Math.cos(mHeading) * DIST;
		GLU.gluLookAt(gl, (centerx + offsetx) * MAP_SCALE, offsety * MAP_SCALE, (centerz + offsetz) * MAP_SCALE,
				centerx * MAP_SCALE, 0.0f, centerz * MAP_SCALE,
				(float)Math.sin(mHeading) * -offsety, DIST, (float)Math.cos(mHeading) * -offsety);
	}
	
	public void render(GL10 gl, int width, int height) {
		if (mMV.getMapFile() == null) return;
		
		// TODO: only supports OpenGL ES 1.0 for now
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		float aspect = ((float)width / height);
		GLU.gluPerspective(gl, 45.0f, aspect, 1.0f, 100.0f);
		
		// Render the map
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		//GLU.gluLookAt(gl, 0.0f, 1.0f, 6.0f, 0.0f, 0.0f, 0.0f, 0.0f, 6.0f, -1.0f);
		GeoPoint gp = mMVPos.getCenter();
		
		TileFrustum tf = new TileFrustum((float)gp.latitude, (float)gp.longitude, mHeading, 90.0f, mMVPos.getZoomLevel());
		HashSet<Tile> visible = tf.getVisibleTiles();
		
		d.resetMatrices(gl);
		for (Tile tile : visible) {
			renderTile(gl, tile);
		}
		
		// Schedule jobs
		mMV.startRenderingTiles();
		
		// Clean up
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPopMatrix();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
	}
	
	private void renderTile(GL10 gl, Tile tile) {
		double lat = MercatorProjection.tileYToLatitude(tile.tileY, tile.zoomLevel),
			   lng = MercatorProjection.tileXToLongitude(tile.tileX, tile.zoomLevel);
		float pz = (float)MercatorProjection.latitudeToPixelY(lat, tile.zoomLevel),
				px = (float)MercatorProjection.longitudeToPixelX(lng, tile.zoomLevel);

		GLTexture tex = mTextureCache.get(tile);
		if (tex == null) {
			// Try to load the texture from the rendered Bitmap
			Bitmap bitmap = mMV.getTileBitmap(tile);
			if (bitmap != null) {
				tex = new GLTexture(gl, bitmap);
				mTextureCache.put(tile, tex);
			}
		}
		
		if (tex != null) {
			gl.glEnable(GL10.GL_TEXTURE_2D);
			tex.bind();
		}
		else {
			gl.glDisable(GL10.GL_TEXTURE_2D);
		}
		
		//d.drawRect(gl, px, py, Tile.TILE_SIZE, Tile.TILE_SIZE);
		//d.drawRect(gl, 0.0f, 0.0f, 1.0f, 1.0f);
		gl.glLoadIdentity();
		GeoPoint gp = mMVPos.getCenter();
		float centerx = (float)MercatorProjection.longitudeToPixelX(gp.longitude, mMVPos.getZoomLevel()),
				  centerz = (float)MercatorProjection.latitudeToPixelY(gp.latitude, mMVPos.getZoomLevel());

		applyCamera(gl);
		gl.glScalef(MAP_SCALE, MAP_SCALE, MAP_SCALE);
		//gl.glTranslatef(px, 0.0f, pz);
		gl.glTranslatef(px, 0.0f, pz);
		gl.glScalef(Tile.TILE_SIZE, 1.0f, Tile.TILE_SIZE);
		d.drawTile(gl);
	}
}

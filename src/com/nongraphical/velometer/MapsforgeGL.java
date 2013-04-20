package com.nongraphical.velometer;

import java.util.ArrayList;

import javax.microedition.khronos.opengles.GL10;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.MapViewPosition;
import org.mapsforge.android.maps.mapgenerator.TileCache;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.Tile;
import org.mapsforge.core.util.MercatorProjection;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

public class MapsforgeGL {
	Drawing d;
	int mTexture;
	// Rotation of view
	float mHeading;
	
	// Imported from MapView
	MapView mMV;
	MapViewPosition mMVPos;
	
	
	public MapsforgeGL(MapView mv) {
		d = new Drawing();
		mHeading = 0.0f;
		
		if (mv == null) {
			Log.e("MapsforgeGL", "TODO MapsforgeGL must be used with a MapView");
			throw new UnsupportedOperationException("TODO MapsforgeGL must be used with a MapView");
		}
		
		mMV = mv;
		mMVPos = mv.getMapViewPosition();
	}
	
	public void init(GL10 gl) {
		int[] _textures = new int[1];
		gl.glGenTextures(1, _textures, 0);
		mTexture = _textures[0];

		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
	}
	
	public void render(GL10 gl, int width, int height) {
		if (mMV.getMapFile() == null) return;
		
		// TODO: only supports OpenGL ES 1.0 for now
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		float aspect = ((float)width / height);
		gl.glOrthof(0.0f, aspect, 1.0f, 0.0f, -1.0f, 1.0f);
		//gl.glFrustumf(-1.0f * aspect, 1.0f * aspect, -1.0f, 1.0f, 0.1f, 10.0f);
		
		// Render the map
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		
		GeoPoint gp = mMVPos.getCenter();
		TileFrustum tf = new TileFrustum((float)gp.latitude, (float)gp.longitude, mHeading, 90.0f, 16);
		ArrayList<Tile> visible = tf.getVisibleTiles();
		
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
		Bitmap bitmap = mMV.getTileBitmap(tile);
		if (bitmap == null) return;
		double lat = MercatorProjection.tileYToLatitude(tile.tileY, tile.zoomLevel),
			   lng = MercatorProjection.tileXToLongitude(tile.tileX, tile.zoomLevel);
		GeoPoint gp = mMVPos.getCenter();
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		d.drawRect(gl, 0.5f - (float)(gp.longitude - lng), 0.25f - (float)(gp.latitude - lat), 0.5f, 0.5f);
	}
}

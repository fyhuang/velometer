package com.nongraphical.velometer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

public class Drawing {
	private final FloatBuffer mRectVertices;
	private final FloatBuffer mTileVertices;
	private final FloatBuffer mRectTexCoords;
	
	public Drawing() {
		// Set up buffers
		final float[] rectVerticesData = {
				// X, Y, Z
				0.0f, 0.0f, 0.0f,
				0.0f, 1.0f, 0.0f,
				1.0f, 0.0f, 0.0f,
				1.0f, 1.0f, 0.0f
		};
		
		final float[] tileVerticesData = {
				// X, Y, Z
				0.0f, 0.0f, 0.0f,
				0.0f, 0.0f, 1.0f,
				1.0f, 0.0f, 0.0f,
				1.0f, 0.0f, 1.0f
		};
		
		final float[] rectTexCoordsData = {
				// U, V
				0.0f, 0.0f,
				0.0f, 1.0f,
				1.0f, 0.0f,
				1.0f, 1.0f
		};
		
		final int FLOAT_BYTES = Float.SIZE / 8;
		
		mRectVertices = ByteBuffer.allocateDirect(rectVerticesData.length * FLOAT_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mRectVertices.put(rectVerticesData).position(0);
		
		mTileVertices = ByteBuffer.allocateDirect(tileVerticesData.length * FLOAT_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTileVertices.put(tileVerticesData).position(0);
		
		mRectTexCoords = ByteBuffer.allocateDirect(rectTexCoordsData.length * FLOAT_BYTES)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mRectTexCoords.put(rectTexCoordsData).position(0);
	}
	
	public void drawRect(GL10 gl, float x, float y, float w, float h,
			float tx, float ty, float tw, float th) {
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mRectVertices);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mRectTexCoords);
		
		gl.glMatrixMode(GL10.GL_TEXTURE);
		gl.glLoadIdentity();
		gl.glTranslatef(tx, ty, 0.0f);
		gl.glScalef(tw, th, 1.0f);
		
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(x, y, 0.0f);
		gl.glScalef(w, h, 1.0f);
		
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	}
	
	public void drawRect(GL10 gl, float x, float y, float w, float h) {
		drawRect(gl, x, y, w, h, 0.0f, 0.0f, 1.0f, 1.0f);
	}
	
	public void drawTile(GL10 gl) {
		// Draw a flat XZ rect, don't modify matrices
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mTileVertices);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mRectTexCoords);
		
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);
	}
	
	public void resetMatrices(GL10 gl) {
		gl.glMatrixMode(GL10.GL_TEXTURE);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
}

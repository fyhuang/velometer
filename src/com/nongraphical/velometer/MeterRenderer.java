package com.nongraphical.velometer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;

public class MeterRenderer {
	Drawing d;
	MainActivity mContext;
	int mDigitsTexture;

	final float HALF_PIXEL = (1.0f / 64.0f) / 2.0f;
	final float DIGIT_UV_SIZE = (1.0f) / 4.0f;
	float[] mTexCoords;
	
	final int NUM_SEGMENTS = 127;
	final float TOTAL_ANGLE = (float)(1.25 * Math.PI);
	final float START_ANGLE = (float)(0.375 * Math.PI);
	final FloatBuffer mMeterVerts;
	
	public MeterRenderer(MainActivity c, Drawing drawing) {
		mContext = c;
		d = drawing;
		
		mTexCoords = new float[20];
		for (int i = 0; i < 10; i++) {
			float tex_u = (i % 4) * DIGIT_UV_SIZE - HALF_PIXEL,
				  tex_v = (i / 4) * DIGIT_UV_SIZE + HALF_PIXEL;
			mTexCoords[i*2] = tex_u;
			mTexCoords[i*2+1] = tex_v;
		}
		
		float[] meterVertData = new float[NUM_SEGMENTS * 2 * 3];
		for (int i = 0; i < NUM_SEGMENTS; i++) {
			float angle = -( (i / (float)(NUM_SEGMENTS-1)) * TOTAL_ANGLE + START_ANGLE );
			
			meterVertData[i*2*3] = (float)Math.sin(angle);
			meterVertData[i*2*3 + 1] = (float)Math.cos(angle);
			meterVertData[i*2*3 + 2] = 0.0f;
			
			meterVertData[i*2*3 + 3] = (float)Math.sin(angle) * 0.9f;
			meterVertData[i*2*3 + 4] = (float)Math.cos(angle) * 0.9f;
			meterVertData[i*2*3 + 5] = 0.0f;
		}
		
		mMeterVerts = ByteBuffer.allocateDirect(meterVertData.length * (Float.SIZE/8))
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		mMeterVerts.put(meterVertData).position(0);
	}
	
	public void load(GL10 gl) {
		int[] _textures = new int[1];
		gl.glGenTextures(1, _textures, 0);
		mDigitsTexture = _textures[0];
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mDigitsTexture);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		
		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.digits);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();
	}
	
	public float getCharWidth(float scale) {
		return scale * 0.68f;
	}
	
	public void drawDigitString(GL10 gl, String chars, float x_base, float y_base, float scale) {
		final float char_width = getCharWidth(scale);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mDigitsTexture);
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		for (int i = 0; i < chars.length(); i++) {
			char c = chars.charAt(i);
			int tex_ix = c - '0';
			
			d.drawRect(gl, x_base + (i*char_width), y_base - scale, scale, scale,
					mTexCoords[tex_ix*2], mTexCoords[tex_ix*2+1],
					DIGIT_UV_SIZE, DIGIT_UV_SIZE);
			//		0.0f, 0.0f, 1.0f, 1.0f);
		}
	}
	
	public void drawKphMeter(GL10 gl, int value, int maxValue) {
		final float METER_SCALE = 0.15f,
				CENTER_X = 0.2f, CENTER_Y = 0.45f,
				TEXT_SCALE = 0.1f;
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisable(GL10.GL_TEXTURE_2D);

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mMeterVerts);
		
		d.resetMatrices(gl);
		gl.glTranslatef(CENTER_X, CENTER_Y, 0.0f);
		gl.glScalef(METER_SCALE, METER_SCALE, METER_SCALE);
		
		// Background arc
		gl.glColor4f(0.4f, 0.4f, 0.4f, 1.0f);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, NUM_SEGMENTS * 2);
		
		// Current value arc
		float ratio = (float)value / maxValue;
		if (ratio < 0.5f) {
			gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		}
		else {
			float ip = (ratio - 0.5f) * 2.0f;
			gl.glColor4f((1-ip)*1.0f + ip*0.6f,
					(1-ip)*1.0f + ip*0.1f,
					(1-ip)*1.0f + ip*0.1f, 1.0f);
		}
		
		int num = (int)(ratio * NUM_SEGMENTS);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, num * 2);
		
		// Digits
		drawDigitString(gl, String.format("%02d", value),
				CENTER_X - getCharWidth(TEXT_SCALE),
				CENTER_Y + (TEXT_SCALE / 4.0f), TEXT_SCALE);
	}
	
	public void drawRpmMeter(GL10 gl, int value, int targetValue) {
		final float METER_SCALE = 0.15f,
				CENTER_X = mContext.getRenderer().getWidth() - 0.2f,
				CENTER_Y = 0.45f, TEXT_SCALE = 0.1f;
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glDisable(GL10.GL_TEXTURE_2D);

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mMeterVerts);
		
		d.resetMatrices(gl);
		gl.glTranslatef(CENTER_X, CENTER_Y, 0.0f);
		gl.glScalef(METER_SCALE, METER_SCALE, METER_SCALE);
		
		// Background arc
		gl.glColor4f(0.4f, 0.4f, 0.4f, 1.0f);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, NUM_SEGMENTS * 2);
		
		// Current value arc
		float dist = (float)Math.abs(value - targetValue);
		float displ = (float)(Math.atan(dist / 8.0f) / Math.PI) * 2.0f;
		gl.glColor4f((1-displ)*1.0f + displ*0.6f,
				(1-displ)*1.0f + displ*0.2f,
				(1-displ)*1.0f + displ*0.2f, 1.0f);
		
		if (value < targetValue) {
			gl.glScalef(-1.0f, 1.0f, 1.0f);
		}
		
		int num = (int)(displ * NUM_SEGMENTS / 2);
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, NUM_SEGMENTS - 1, num * 2);

		// Digits
		drawDigitString(gl, String.format("%02d", value),
				CENTER_X - getCharWidth(TEXT_SCALE),
				CENTER_Y + (TEXT_SCALE / 4.0f), TEXT_SCALE);
	}
}

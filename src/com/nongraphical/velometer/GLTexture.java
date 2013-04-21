package com.nongraphical.velometer;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

public class GLTexture {
	int mTexture;
	GL10 gl;
	
	public GLTexture(GL10 gl) {
		int[] _textures = new int[1];
		this.gl = gl;
		gl.glGenTextures(1, _textures, 0);
		mTexture = _textures[0];
	}
	
	public GLTexture(GL10 gl, Bitmap bitmap) {
		this(gl);
		
		assert mTexture != 0;

		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
	}
	
	@Override
	protected void finalize() throws Throwable {
		try {
			int[] _textures = new int[1];
			_textures[0] = mTexture;
			gl.glDeleteTextures(1, _textures, 0);
		}
		catch (Throwable t) {
			throw t;
		}
		finally {
			super.finalize();
		}
	}
	
	public void bind() {
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture);
	}
}

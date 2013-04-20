package com.nongraphical.velometer;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.core.model.GeoPoint;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class MainActivity extends MapActivity
{
	private GLSurfaceView mGLView;
	private VelometerRenderer mRenderer;
	private MapView mMapView;
	private MapsforgeGL mMapGL;
	private int mCurrSpeed, mCurrRPM;
	
	public MainActivity() {
		mCurrSpeed = 0;
		mCurrRPM = 0;
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        // Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);*/
        

        setContentView(R.layout.main);
        
        // Initialize MapView
        mMapView = new MapView(this);
        mMapView.setClickable(true);
        mMapView.setBuiltInZoomControls(true);
        mMapView.getMapScaleBar().setShowMapScaleBar(true);
        
        File sdcardDir = Environment.getExternalStorageDirectory();
        mMapView.setMapFile(new File(sdcardDir.getPath() + "/california.map"));
        
        mMapView.getMapViewPosition().setCenter(new GeoPoint(37.7825, -122.408));
        mMapView.getMapViewPosition().setZoomLevel((byte)17);

        FrameLayout frame = (FrameLayout)findViewById(R.id.mainframelayout);
        frame.addView(mMapView, 0);
        mMapView.setVisibility(View.INVISIBLE);
    	findViewById(R.id.overlay_buttons_2d).setVisibility(View.INVISIBLE);

        
        // Initialize OpenGL
        mMapGL = new MapsforgeGL(mMapView);
        
        mGLView = (GLSurfaceView)findViewById(R.id.overlay_gl_view);
        mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLView.getHolder().setFormat(PixelFormat.RGBA_8888);
        
        mRenderer = new VelometerRenderer(this, mMapGL);
        mGLView.setRenderer(mRenderer);
        
        
        // Periodically update
        startUpdating();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	mGLView.onPause();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mGLView.onResume();
    }
    
    
    public void onShowMap(View view) {
    	mGLView.setVisibility(View.INVISIBLE);
    	findViewById(R.id.overlay_buttons).setVisibility(View.INVISIBLE);
    	mGLView.onPause();
    	
    	mMapView.setVisibility(View.VISIBLE);
    	findViewById(R.id.overlay_buttons_2d).setVisibility(View.VISIBLE);
    }
    public void onReturnToMain(View view) {
    	mGLView.setVisibility(View.VISIBLE);
    	findViewById(R.id.overlay_buttons).setVisibility(View.VISIBLE);
    	mGLView.onResume();
    	
    	mMapView.setVisibility(View.INVISIBLE);
    	findViewById(R.id.overlay_buttons_2d).setVisibility(View.INVISIBLE);
    }
    
    
    // Periodic updates
    final Handler timedHandler = new Handler();
    final Runnable updateStatsRunnable = new Runnable() {
    	public void run() {
    		mCurrSpeed += 1;
    		if (mCurrSpeed > 45) {
    			mCurrSpeed = 0;
    		}
    		
    		mCurrRPM += 2;
    		if (mCurrRPM > 160) {
    			mCurrRPM = 0;
    		}
    	}
    };
    
    private void startUpdating() {
        Timer timer = new Timer();
        /*timer.schedule(new TimerTask() {
        	@Override
        	public void run() {
        		timedHandler.post(updateStatsRunnable);
        	}
        }, 0, 300);*/
        
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
        	public void onLocationChanged(Location location) {
        		// Compute speeds
        		float speed_mps = location.getSpeed();
        		mCurrSpeed = Math.round(speed_mps / 1000.0f * 3600.0f); // kph
        		mCurrRPM = Math.round(speed_mps / 2.314f * 60.0f); // rpm
        		
        		// Update map
        		mMapView.getMapViewPosition().setCenter(new GeoPoint(location.getLatitude(), location.getLongitude()));
        	}

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
        		0, 0, locationListener);
    }
    
    
    public int getCurrSpeed() {
    	return mCurrSpeed;
    }
    public int getCurrRPM() {
    	return mCurrRPM;
    }
    
    public VelometerRenderer getRenderer() {
    	return mRenderer;
    }
}

class VelometerRenderer implements GLSurfaceView.Renderer
{
	private Drawing d;
	private MainActivity mContext;
	private MapsforgeGL mMapGL;
	private int mGradTexture;
	private float mWidth;
	
	private final MeterRenderer mMeterRnd;
	
	public VelometerRenderer(MainActivity c, MapsforgeGL mapgl) {
		d = new Drawing();
		mContext = c;
		mMapGL = mapgl;
		mMeterRnd = new MeterRenderer(c, d);
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		
		// Draw map
		mMapGL.render(gl, (int)(100 * mWidth), 100);
		d.resetMatrices(gl);
		
		// TODO: draw mirror
		
		// Draw sidebars
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glColor4f(0.0f, 0.0f, 0.0f, 1.0f);
		d.drawRect(gl, 0.0f, 0.0f, 0.305f, 1.0f);
		d.drawRect(gl, mWidth, 0.0f, -0.305f, 1.0f);
		
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mGradTexture);
		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		d.drawRect(gl, 0.3f, 0.0f, 0.3f, 1.0f);
		d.drawRect(gl, mWidth - 0.3f, 0.0f, -0.3f, 1.0f);
		
		// Draw meters
		//mMeterRnd.drawDigitString(gl, "12", 0.0f, 0.5f, 0.15f);
		mMeterRnd.drawKphMeter(gl, mContext.getCurrSpeed(), 45);
		mMeterRnd.drawRpmMeter(gl, mContext.getCurrRPM(), 80);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		float aspect_ratio = (float)width / (float)height;
		mWidth = aspect_ratio;
		gl.glOrthof(0.0f, mWidth, 1.0f, 0.0f, -1.0f, 1.0f);
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glDisable(GL10.GL_DEPTH_TEST);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		// Set up textures
		int[] _textures = new int[1];
		gl.glGenTextures(1, _textures, 0);
		mGradTexture = _textures[0];
		
		gl.glBindTexture(GL10.GL_TEXTURE_2D, mGradTexture);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		
		Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.black_grad);
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();
		
		// Load other renderers
		mMeterRnd.load(gl);
		mMapGL.init(gl);
	}
	
	public float getWidth() {
		return mWidth;
	}
}

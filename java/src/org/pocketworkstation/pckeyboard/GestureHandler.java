package org.pocketworkstation.pckeyboard;

import java.util.ArrayList;

import algorithms.DataFilter;
import algorithms.FastConvexHull;
import algorithms.RamerDouglasPeuckerFilter;
import android.gesture.GestureStroke;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

/* Class to handle swipe gestures
 * receive gesture position/velocity information from LatinKeyboardBaseView
 * 
 * by Jackie Kay
*/
public class GestureHandler {
	private static final String TAG = "GestureHandler";
    private Path    mPath;
    private float	mX;
    private float	mY;
    private static final float TOUCH_TOLERANCE = 4;
    private Canvas mCanvas;
    private Paint mPaint;
    private GestureStroke curStroke;
    private ArrayList<PointF> mGPoints;
    private ArrayList<int[]> charCodes;
    private long curt;
    private Keyboard mKeyboard;
    private KeyDetector mKeyDetector;
    private String curString = "";
    private FastConvexHull convexHuller = new FastConvexHull();
    private float epsilon = 5f;
    private RamerDouglasPeuckerFilter filterer = new RamerDouglasPeuckerFilter(epsilon);

    public class KeyBundle{
    	int keyCode;
    	int[] adjCodes;
    	float x;
    	float y;
    	
    	public KeyBundle(int k, int[] a, float x, float y){
    		this.keyCode = k;
    		this.adjCodes = a;
    		this.x = x;
    		this.y = y;
    	}
    	
    }
    
    public GestureHandler(Keyboard k, KeyDetector d){
    	setPaint();
    	mPath = new Path();
    	mKeyboard = k;
    	mKeyDetector = d;
    }
    
    public void setCanvas(Canvas c){
    	mCanvas = c;
    }
    
    public void setKeyboard(Keyboard k){
    	mKeyboard = k;
    }
    
    public ArrayList<PointF> getCurPoints(){
    	return mGPoints;
    }
    
    public ArrayList<int[]> getCurCodes(){
    	return charCodes;
    }
    
    public String getCurString(){
    	return curString;
    }
    
    public void clearCurString(){
    	curString = "";
    }
    
    public Path getPath(){
    	return mPath;
    }
    
    public void drawPath(Canvas c){
    	c.drawPath(mPath, mPaint);
    }
	
    private void setPaint(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(3);
    }
    
    private void touch_start(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        mGPoints = new ArrayList<PointF>();
        mGPoints.add(new PointF(x, y));
    }
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
            mX = x;
            mY = y;
        }
        mGPoints.add(new PointF(x, y));
    }
    
    private void touch_up() {
        mPath.lineTo(mX, mY);
        // commit the path to our offscreen
        mCanvas.drawPath(mPath, mPaint);
        // kill this so we don't double draw
        mPath.reset();
        curString = guessWord(mGPoints);
    }
    
    private String guessWord(ArrayList<PointF> mGPoints){
    	//use convex hull algorithm to get corners
    	mGPoints = filterer.filter(mGPoints);
    	if(mGPoints.size() > 1){
    		mGPoints = convexHuller.execute(mGPoints);
    		charCodes = new ArrayList<int[]>();
    		for(PointF point : mGPoints){
    			charCodes.add(mKeyboard.getNearestKeys((int) point.x, (int) point.y));
    		}
        	return pointsToString(mGPoints);
    	}
    	return "";
    }
    
    
    
    // Translate points into a string
    private String pointsToString(ArrayList<PointF> mGPoints){
    	String guess = "";
    	String prev = "";
    	String first = "";
    	for(PointF point : mGPoints){
    		String letter = getKeyFromPosition(point.x, point.y);
    		if(letter.length() > 0){
        		if (first == "" && letter != ""){
        			first = letter;
        		} else {
                	if(Character.isUpperCase(letter.charAt(0)) && mKeyboard.getShiftState() != Keyboard.SHIFT_CAPS_LOCKED
                			&& Character.isUpperCase(first.charAt(0))){
                		letter = letter.toLowerCase();
                	}
        		}

        		if(!letter.equals(prev))
        			guess += letter;
    		}
    		prev = letter;
    	}
    	return guess;
    }
    
    private String getKeyFromPosition(float x, float y){
    	int[] allKeys = null;
    	int primaryIndex = mKeyDetector.getKeyIndexAndNearbyCodes((int) x, (int) y, allKeys);
    	if(primaryIndex > 0 && primaryIndex < mKeyboard.getKeys().size()){
        	String letter = mKeyboard.getKeys().get(primaryIndex).getCaseLabel();
        	if (letter != null && letter.length() == 1)
        		return letter;
    	}

    	return "";
    }
	
	public Canvas processMotionEvent(MotionEvent event, Canvas c){
        float x = event.getX();
        float y = event.getY();
        mCanvas = c;
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                break;
        }
        return mCanvas;
	}
	
}

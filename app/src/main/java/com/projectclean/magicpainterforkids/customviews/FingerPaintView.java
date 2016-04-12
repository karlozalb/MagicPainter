package com.projectclean.magicpainterforkids.customviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.LightingColorFilter;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.projectclean.magicpainterforkids.activities.PaintActivity;
import com.projectclean.magicpainterforkids.utils.BitmapUtils;
import com.projectclean.magicpainterforkids.utils.ScreenUtils;

import java.util.LinkedList;

/**
 * Created by Carlos Albaladejo Pérez on 12/03/2016.
 */
public class FingerPaintView extends View {

    private Paint mBitmapPaint;
    private Path mPath;
    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Paint       mPaint;
    private MaskFilter  mEmboss;
    private MaskFilter mBlur;

    private int mCurrentTextureId = -1;

    //Default Colors
    public static int BLUE = 0xFF0082c6;
    public static int DARK_BLUE = 0xFF00488c;
    public static int YELLOW = 0xFFffd800;
    public static int RED = 0xFFff4520;
    public static int ORANGE = 0xFFff7927;
    public static int VIOLET = 0xFF7e1fff;
    public static int GREEN = 0xFF00c365;
    public static int PINK = 0xFF00c365;
    public static int BLACK = 0xFF000000;
    public static int BROWN = 0xFF744f00;

    //private LinkedList<Path> mPaths;
    private LinkedList<PathAndPaint> mPaths;
    private LinkedList<Bitmap> mStamps;

    private int mCurrentColor,mCurrentPencilSize;

    private boolean mFillMode;

    private LinkedList<DrawingOperation> mUndo,mRedo;
    private boolean mEraseMode;

    //Pencil mode
    private boolean mPencilMode;
    private Bitmap mOriginalPencilBitmap,mPencilBitmap;
    private float mPencilScale;
    private final int BASE_PENCIL_WIDTH_AND_HEIGHT = 256;

    private boolean mOptimizedDrawMode;

    //Image state
    private int mPathCount,mBitmapsCount;

    private int mOldWidth,mOldHeight;

    private final int SAMPLE_SIZE = 1;
    private Rect mDstRectangle,mSrcRectangle;

    private Context mContext;

    public FingerPaintView(Context context) {
        super(context);
        mContext = context;
        initializeView();
    }

    public FingerPaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initializeView();
    }

    public FingerPaintView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initializeView();
    }

    public FingerPaintView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        initializeView();
    }

    private void initializeView(){
        setLayerType(View.LAYER_TYPE_SOFTWARE, mPaint);

        mPaths = new LinkedList<PathAndPaint>();
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        mCurrentColor = RED;
        mCurrentPencilSize = ScreenUtils.getPixelsFromDp((PaintActivity)mContext,3);

        createPaint(mEraseMode);

        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 },
                0.4f, 6, 3.5f);

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);

        //mPaths = new LinkedList<>();

        mUndo = new LinkedList<>();
        mRedo = new LinkedList<>();
    }

    public void createPaint(boolean perase){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(mCurrentColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(ScreenUtils.getPixelsFromDp((PaintActivity)mContext,mCurrentPencilSize));
        if (mPencilMode) mPaint.setColorFilter(new LightingColorFilter(mCurrentColor, 1));
        if (perase) mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mOldWidth = w;
        mOldHeight = h;

        if (mBitmap == null) {

            if (mCurrentTextureId != -1) {
                setBipmapFromResource(w,h);
            } else {

                mDstRectangle = new Rect(0,0,mOldWidth,mOldHeight);
                mSrcRectangle = new Rect(0,0,mOldWidth/SAMPLE_SIZE,mOldHeight/SAMPLE_SIZE);

                mBitmap = Bitmap.createBitmap(w/SAMPLE_SIZE, h/SAMPLE_SIZE, Bitmap.Config.ARGB_8888);
            }
        }

        mCanvas = new Canvas(mBitmap);
    }

    public void setBipmapFromResource(int w, int h){
        Bitmap texture = BitmapFactory.decodeResource(getResources(), mCurrentTextureId);

        if (w > h && texture.getHeight() > texture.getWidth() || w < h && texture.getHeight() < texture.getWidth()) {
            Matrix m = new Matrix();
            m.postRotate(90);

            texture = Bitmap.createBitmap(texture, 0, 0, texture.getWidth(), texture.getHeight(), m, true);
        }

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(texture, w, h, true);
        mBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mOptimizedDrawMode) {
            mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

            batchDraw(mCanvas);
        }

        mCanvas.drawPath(mPath, mPaint);

        if (mSrcRectangle == null || mDstRectangle == null){
            mDstRectangle = new Rect(0,0,mOldWidth,mOldHeight);
            mSrcRectangle = new Rect(0,0,mOldWidth/SAMPLE_SIZE,mOldHeight/SAMPLE_SIZE);
        }

        canvas.drawBitmap(mBitmap, mSrcRectangle, mDstRectangle, mBitmapPaint);


        //canvas.drawBitmap(mBitmap,0,0,mBitmapPaint);
    }

    private void batchDraw(Canvas canvas){

        //if (!mOptimizedDrawMode) {
            for (int i = 0; i < mPaths.size(); i++) {
                PathAndPaint p = mPaths.get(i);
                if (p.BITMAP == null) {
                    canvas.drawPath(p.PATH, p.PAINT);
                } else {
                    canvas.drawBitmap(p.BITMAP, p.X, p.Y, p.PAINT);
                }
            }
        //}else{
          //  canvas.drawBitmap(mBitmap,0,0,mBitmapPaint);
        //}
    }

    public void setFillMode(boolean pmode){
        mFillMode = pmode;
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {

        if (!mPencilMode) {
            createPaint(mEraseMode);
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
        }else{
            createPaint(false);
            mPaths.add(new PathAndPaint(mPaint, null, mPencilBitmap, mEraseMode, (int) (x - mPencilBitmap.getWidth() / 2f), (int) (y - mPencilBitmap.getHeight() / 2)));
            mUndo.add(new DrawingOperation(mPaths.getLast()));
            ((PaintActivity)mContext).playHornSound();
        }
    }
    private void touch_move(float x, float y) {

        if (!mPencilMode) {
            mOptimizedDrawMode = true;
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
                mX = x;
                mY = y;
            }
        }
    }
    private void touch_up() {

        if (!mPencilMode) {
            mOptimizedDrawMode = false;
            mPath.lineTo(mX, mY);
            // commit the path to our offscreen
            mCanvas.drawPath(mPath, mPaint);

            mPaths.add(new PathAndPaint(mPaint,mPath,null,mEraseMode,0,0));
            // kill this so we don't double draw

            mUndo.add(new DrawingOperation(mPaths.getLast()));

            mPath = new Path();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        x /= SAMPLE_SIZE;
        y /= SAMPLE_SIZE;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
    }

    public void setBackgroundTexture(int presourceid){
        mCurrentTextureId = presourceid;
    }

    public void setBitmap(Bitmap pbitmap){
        mBitmap = pbitmap;
    }

    public Bitmap getBitmap(){
        batchDraw(mCanvas);
        return mBitmap;
    }

    public void setCurrentColor(int pcolor){
        if (pcolor == 0x00000000){
            mEraseMode = true;
        }else {
            mEraseMode = false;
            mCurrentColor = pcolor;
        }
    }

    public void setPencilSize(int psize){
        mCurrentPencilSize = psize;

        mPencilScale = 0.25f + (psize - 4f)/12f;

        if (mPencilMode){
            int width = (int)(BASE_PENCIL_WIDTH_AND_HEIGHT * mPencilScale);
            int height = (int)(BASE_PENCIL_WIDTH_AND_HEIGHT * mPencilScale);

            Log.i("MPFK","mPencilScale:"+mPencilScale+" width:"+width+" height:"+height);

            mPencilBitmap = Bitmap.createScaledBitmap(mOriginalPencilBitmap,width,height,true);
        }

        createPaint(mEraseMode);
    }

    public void paintBucket(int px,int py){
        int targetColor = mBitmap.getPixel(px,py);

        /*QueuedLinearFloodFiller filler = new QueuedLinearFloodFiller(mBitmap,targetColor,mCurrentColor);
        filler.setTolerance(new int[]{0, 0, 0, 0});

        Bitmap newFilledZone = filler.floodFill(px,py);

        mFilledZones.add(newFilledZone);

        mUndo.add(new DrawingOperation(newFilledZone));*/
        //mBitmap = filler.getImage();

        if (mBitmap.getPixel(px,py) == mCurrentColor) return;

        PixelNode initialColor = new PixelNode(px,py);
        LinkedList<PixelNode> nodes = new LinkedList<PixelNode>();
        nodes.add(initialColor);

        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();

        int[] pixels = new int[mBitmap.getWidth() * mBitmap.getHeight()];

        mBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int w,e;

        int index = py * width + px;

        LinkedList<PixelNode> borders = new LinkedList<PixelNode>();

        while (nodes.size() > 0){
            PixelNode n = nodes.removeFirst();
            w = n.X;
            e = n.X;

            while (pixels[n.Y * width + w] == targetColor) w--;
            while (pixels[n.Y * width + e] == targetColor) e++;

            borders.add(new PixelNode(w,n.Y));
            borders.add(new PixelNode(e,n.Y));

            for (int i=w;i<=e;i++){
                //if (mBitmap.getPixel(i,n.Y - 1) == targetColor) nodes.add(new PixelNode(i,n.Y - 1));
                //7if (mBitmap.getPixel(i,n.Y + 1) == targetColor) nodes.add(new PixelNode(i,n.Y + 1));

                if (pixels[(n.Y - 1) * width + i] == targetColor) nodes.add(new PixelNode(i,n.Y - 1));
                if (pixels[(n.Y + 1) * width + i] == targetColor) nodes.add(new PixelNode(i,n.Y + 1));

                //mCanvas.drawPoint(i, n.Y, mPaint);
                pixels[n.Y * width + i] = mCurrentColor;
                //mBitmap.setPixel(i,n.Y,mCurrentColor);
            }
        }

        mBitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        /*while (nodes.size() > 0){
            PixelNode n = nodes.removeFirst();
            w = n.X;
            e = n.X;

            while (mBitmap.getPixel(w,n.Y) == targetColor) w--;
            while (mBitmap.getPixel(e,n.Y) == targetColor) e++;

            for (int i=w;i<=e;i++){
                if (mBitmap.getPixel(i,n.Y - 1) == targetColor) nodes.add(new PixelNode(i,n.Y - 1));
                if (mBitmap.getPixel(i,n.Y + 1) == targetColor) nodes.add(new PixelNode(i,n.Y + 1));
                //mCanvas.drawPoint(i, n.Y, mPaint);
                mBitmap.setPixel(i,n.Y,mCurrentColor);
            }
        }*/
    }

    private class PixelNode{
        public int X,Y;

        public PixelNode(int px,int py){
            X = px;
            Y = py;
        }
    }

    public void undo(){
        if (mUndo.size() > 0){
            DrawingOperation dop = mUndo.removeLast();

            if (dop.getType() == dop.FINGER){
                mRedo.add(new DrawingOperation(mPaths.removeLast()));
            }else if (dop.getType() == dop.BUCKET){
                mRedo.add(new DrawingOperation(mBitmap));
                mBitmap = dop.getBitmap();
            }
            invalidate();
        }
    }

    public void redo(){
        if (mRedo.size() > 0){
            DrawingOperation dop = mRedo.removeLast();

            if (dop.getType() == dop.FINGER){
                mUndo.add(dop);
                mPaths.add(dop.getPath());
            }else if (dop.getType() == dop.BUCKET){
                mBitmap = dop.getBitmap();
                mUndo.add(dop);
            }
            invalidate();
        }
    }

    public void setPencilMode(int presource){
        mPencilMode = true;
        mOriginalPencilBitmap = BitmapFactory.decodeResource(getResources(), presource);
        setPencilSize(mCurrentPencilSize);
    }

    public void disablePencilMode(){
        if (mPencilMode) {
            mPencilMode = false;
            mOriginalPencilBitmap = mPencilBitmap = null;
        }
    }

    private boolean isBitmapChanged(){
        if (mPaths.size() != mPathCount){
            mPathCount = mPaths.size();
            return true;
        }
        return false;
    }

    public void reset(){
        mPaths.clear();
        mUndo.clear();
        mRedo.clear();
        invalidate();
    }

    public class PathAndPaint{

        public Paint PAINT;
        public Path PATH;
        public Bitmap BITMAP;
        public int X,Y;
        public boolean ERASE;

        public PathAndPaint(Paint ppaint,Path ppath,Bitmap pbitmap,boolean perase,int px,int py){
            PAINT = ppaint;
            PATH = ppath;
            ERASE = perase;
            BITMAP = pbitmap;
            X = px;
            Y = py;
        }

    }

    public class DrawingOperation{

        public int FINGER = 1,BUCKET = 0;
        private PathAndPaint mPath;
        private Bitmap mBitmap;
        private int mType;

        public DrawingOperation(PathAndPaint ppath){
            mPath = ppath;
            mType = FINGER;
        }

        public DrawingOperation(Bitmap pbitmap){
            mBitmap = pbitmap;
            mType = BUCKET;
        }

        public PathAndPaint getPath(){
            return mPath;
        }

        public Bitmap getBitmap(){
            return mBitmap;
        }

        public int getType(){
            return mType;
        }
    }

}

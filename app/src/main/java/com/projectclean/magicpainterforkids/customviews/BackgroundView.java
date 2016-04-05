package com.projectclean.magicpainterforkids.customviews;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;


/**
 * Created by Carlos Albaladejo Pérez on 12/03/2016.
 */
public class BackgroundView extends ImageView {

    private Paint mBitmapPaint;
    private Path mPath;
    private Bitmap  mBitmap;
    private Canvas  mCanvas;
    private Paint       mPaint;
    private MaskFilter  mEmboss;
    private MaskFilter mBlur;

    private int mCustomImageWidth,mCustomImageHeight;

    private int mCurrentTextureId = -1;

    private boolean mCustomBitmap;

    private int mOldWidth,mOldHeight;

    public BackgroundView(Context context) {
        super(context);
        initializeView();
    }

    public BackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeView();
    }

    public BackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeView();
    }

    public BackgroundView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initializeView();
    }

    private void initializeView(){
        mPath = new Path();
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(0xFFFF0000);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        mEmboss = new EmbossMaskFilter(new float[] { 1, 1, 1 },
                0.4f, 6, 3.5f);

        mBlur = new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mOldWidth = w;
        mOldHeight = h;

        System.out.println("w: "+w+" h: "+h+" oldw: "+oldw+" oldh: "+oldh);

        if (!mCustomBitmap) {
            if (mCurrentTextureId != -1) {
                Bitmap texture = BitmapFactory.decodeResource(getResources(), mCurrentTextureId);
                Bitmap textureTemp;

                if (w > h && texture.getHeight() > texture.getWidth() || w < h && texture.getHeight() < texture.getWidth()) {
                    Matrix m = new Matrix();
                    m.postRotate(90);

                    textureTemp = Bitmap.createBitmap(texture, 0, 0, texture.getWidth(), texture.getHeight(), m, true);
                    texture.recycle();
                    texture = textureTemp;
                }

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(texture, w, h, true);
                mBitmap = scaledBitmap.copy(Bitmap.Config.RGB_565, true);
                texture.recycle();
                scaledBitmap.recycle();
            } else {
                if (mBitmap != null) mBitmap.recycle();
                mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            }
        }else{
            setupBackground(w,h);
        }

        mCanvas = new Canvas(mBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
    }

    public void setBackgroundTexture(int presourceid){
        mCurrentTextureId = presourceid;
    }

    public void setImageBitmap(Bitmap pbitmap){
        mCustomBitmap = true;

        mBitmap.recycle();
        mBitmap = pbitmap.copy(Bitmap.Config.ARGB_8888, true);
        pbitmap.recycle();

        setupBackground(getWidth(),getHeight());
        invalidate();
    }

    public void setupBackground(int w,int h){

        if (h > w){
            int tmp = h;
            h = w;
            w = tmp;
        }

        Bitmap newBackground = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(newBackground);

        float deltaX = (w - mBitmap.getWidth()) / 2;

        c.drawBitmap(mBitmap,new Rect(0,0,mBitmap.getWidth(),mBitmap.getHeight()),new RectF(deltaX,0f,deltaX + mBitmap.getWidth(),mBitmap.getHeight()),new Paint(Paint.DITHER_FLAG));

        mBitmap.recycle();
        mBitmap = newBackground;
    }

    public Bitmap getBitmap(){
        return mBitmap;
    }

    public void reset(){
        mCustomBitmap = false;

        onSizeChanged(getWidth(),getHeight(),mOldWidth,mOldWidth);
        invalidate();
    }
}

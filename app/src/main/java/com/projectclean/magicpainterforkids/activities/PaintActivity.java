package com.projectclean.magicpainterforkids.activities;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.appodeal.ads.Appodeal;
import com.caverock.androidsvg.PreserveAspectRatio;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.projectclean.magicpainterforkids.R;
import com.projectclean.magicpainterforkids.Router;
import com.projectclean.magicpainterforkids.billingutils.IabHelper;
import com.projectclean.magicpainterforkids.billingutils.IabResult;
import com.projectclean.magicpainterforkids.customviews.BackgroundView;
import com.projectclean.magicpainterforkids.customviews.FingerPaintView;
import com.projectclean.magicpainterforkids.utils.BitmapUtils;
import com.projectclean.magicpainterforkids.utils.MarshMallowPermission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Carlos Albaladejo Pérez on 13/03/2016.
 */
public class PaintActivity extends RootActivity implements PopupMenu.OnMenuItemClickListener{

    static final int REQUEST_TAKE_PHOTO = 1;
    static final int PICK_IMAGE = 2;
    public static int IMAGE_GALLERY_REQUEST = 3;
    public static String START_WITH_BACKGROUND_CHOOSER = "Paint_SWBC";

    //Views
    private FingerPaintView mDrawingView;
    private BackgroundView mBackgroundView;
    private ImageView[] mPencilImageViews;
    private SeekBar mPencilSizeSeekBar;

    private ImageButton mSaveButton,mShareButton,mPaintBucketButton,mUndoButton,mRedoButton,mCameraButton,mPencilButton,mDeleteButton;

    private int mBackgroundId = -1;

    //Constants
    private String FOREGROUND_BITMAP = "FG_BMP";
    private String CAMERA_PICTURE_PATH = "PIC_PATH";
    private int MIN_PENCIL_SIZE = 4;

    //Current "upped" pencil.
    private ImageView mCurrentSelectedPencil;

    private boolean mPaintBucketMode;

    private String mCurrentPhotoPath;
    private MarshMallowPermission mMMPermission;

    private boolean mCameraPermissionRequested,mExternalStoragePermissionRequestedForCamera,mExternalStoragePermissionRequestedForSave;

    private boolean mIABSetupFinished,mFirstTime,chooserAlreadyShowed;

    private String mCurrentFilename;

    private MediaPlayer mBlopSound,mHornSound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paint_activity_main);

        mMMPermission = new MarshMallowPermission(this);

        mDrawingView = (FingerPaintView)findViewById(R.id.finger_paint_view);
        mBackgroundView = (BackgroundView)findViewById(R.id.background_imagev);

        if (savedInstanceState != null) {
            mDrawingView.setBitmap((Bitmap) savedInstanceState.getParcelable(FOREGROUND_BITMAP));
            mCurrentPhotoPath = savedInstanceState.getString(CAMERA_PICTURE_PATH);
        }

        mSaveButton = (ImageButton)findViewById(R.id.save_button);
        mShareButton = (ImageButton)findViewById(R.id.share_button);
        //mPaintBucketButton = (ImageButton)findViewById(R.id.paint_bucket_button);
        mCameraButton = (ImageButton)findViewById(R.id.photo_button);
        mPencilButton = (ImageButton)findViewById(R.id.pencil_button);
        mDeleteButton = (ImageButton)findViewById(R.id.delete_button);

        mUndoButton  = (ImageButton)findViewById(R.id.undo_button);
        mRedoButton = (ImageButton)findViewById(R.id.redo_button);

        mPencilSizeSeekBar = (SeekBar)findViewById(R.id.pencil_size_seekbar);
        mPencilSizeSeekBar.setMax(12);
        mPencilSizeSeekBar.setProgress(8);

        hideStatusBar();

        mBackgroundView.setBackgroundTexture(R.drawable.paper_texture_small);

        LinearLayout fLayout = (LinearLayout)findViewById(R.id.pencils_parent_layout);

        mPencilImageViews = new ImageView[fLayout.getChildCount()-1];

        /*public static int BLUE = 0xFF0082c6;
        public static int DARK_BLUE = 0xFF00488c; //No puesto!!!
        public static int YELLOW = 0xFFffd800;
        public static int RED = 0xFFff4520;
        public static int ORANGE = 0xFFff7927;
        public static int VIOLET = 0xFF7e1fff;
        public static int GREEN = 0xFF00c365;
        public static int PINK = 0xFF00c365;
        public static int BLACK = 0xFF000000;
        public static int BROWN = 0xFF744f00;*/

        int[] colors = new int[]{0xFF000000,0xFF00c365,0xFFff4520,0xFF0082c6,0xFFffd800,0xFF7e1fff,0xFF744f00,0xFFff5dee,0xFFff7927,0x00000000};

        for (int i=0;i<fLayout.getChildCount()-1;i++){
            mPencilImageViews[i] = (ImageView)fLayout.getChildAt(i);
            mPencilImageViews[i].setTag(colors[i]);
            mPencilImageViews[i].setY(mPencilImageViews[i].getLayoutParams().height / 1.75f);
        }

        setListeners();

        boolean chooser = getIntent().getBooleanExtra(START_WITH_BACKGROUND_CHOOSER,false);

        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        chooserAlreadyShowed = mIABSetupFinished = false;

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d("MPFK", "Problem setting up In-app Billing: " + result);
                    return;
                }
                mIABSetupFinished = true;
                getPurchasedProducts();
            }
        });

        if (!mNoAds){
            Appodeal.show(this, Appodeal.INTERSTITIAL);
        }

        mBlopSound = MediaPlayer.create(this, R.raw.blop);
        mHornSound = MediaPlayer.create(this, R.raw.horn);
    }

    @Override
    public void onPurchasedProductsResult() {
        boolean chooser = getIntent().getBooleanExtra(START_WITH_BACKGROUND_CHOOSER,false);
        if (chooser && !chooserAlreadyShowed){
            chooserAlreadyShowed = true;
            showBackgroundChooser();
        }
    }

    private ArrayList<String> getPurchasedBackgrounds(){
        ArrayList<String> products = new ArrayList<String>();

        if (mAnimalPack1){
            products.add("image_0001");
            products.add("image_0002");
            products.add("image_0003");
            products.add("image_0004");
            products.add("image_0005");
        }

        if (mAnimalPack2){
            products.add("image_0006");
            products.add("image_0007");
            products.add("image_0008");
            products.add("image_0009");
            products.add("image_0010");
        }

        return products;
    }

    static String stringTransform(String s, int i) {
        char[] chars = s.toCharArray();
        for(int j = 0; j<chars.length; j++)
            chars[j] = (char)(chars[j] ^ i);
        return String.valueOf(chars);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(FOREGROUND_BITMAP, mDrawingView.getBitmap());
        outState.putString(CAMERA_PICTURE_PATH, mCurrentPhotoPath);
    }

    public void setListeners(){
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playBlopSound();
                mExternalStoragePermissionRequestedForSave = true;
                if (!mMMPermission.checkPermissionForExternalStorage()){
                    mMMPermission.requestPermissionForExternalStorage();
                }else{
                    if (mCurrentFilename == null) mCurrentFilename = BitmapUtils.getUniqueFilename();
                    BitmapUtils.saveBitmapToMediaDirectory(PaintActivity.this, BitmapUtils.merge(mBackgroundView.getBitmap(), mDrawingView.getBitmap()),mCurrentFilename);
                    showSnackBarMessage(getResources().getString(R.string.document_saved),3000);
                }
            }
        });

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentFilename == null) mCurrentFilename = BitmapUtils.getUniqueFilename();
                playBlopSound();
                File f = new File(BitmapUtils.saveBitmapToMediaDirectory(PaintActivity.this, BitmapUtils.merge(mBackgroundView.getBitmap(), mDrawingView.getBitmap()),mCurrentFilename));

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.share_choose)));
            }
        });

        mUndoButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                playBlopSound();
                mDrawingView.undo();
            }
        });

        mRedoButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playBlopSound();
                mDrawingView.redo();
            }
        });

        mPencilButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playBlopSound();
                Router.showPencilChooserFragmentDialog(PaintActivity.this);
            }
        });

        mCameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                playBlopSound();
                showPopupMenu(v);
            }
        });

        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playBlopSound();
                Router.showAreYouSureDeleteFragmentDialog(PaintActivity.this);
            }
        });

        for (int i=0;i<mPencilImageViews.length;i++){
            mPencilImageViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playBlopSound();
                    mDrawingView.setCurrentColor((int)v.getTag());
                    moveViewUp((ImageView)v);
                }
            });
        }

        mPencilSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mDrawingView.setPencilSize(MIN_PENCIL_SIZE + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void moveViewUp(final ImageView pview){

        if (mCurrentSelectedPencil != null){
            ObjectAnimator objectAnimator= ObjectAnimator.ofFloat(mCurrentSelectedPencil, "translationY", mCurrentSelectedPencil.getLayoutParams().height / 3f, mCurrentSelectedPencil.getLayoutParams().height / 1.75f);
            objectAnimator.setDuration(200);
            objectAnimator.start();
        }

        mCurrentSelectedPencil = pview;

        ObjectAnimator objectAnimator= ObjectAnimator.ofFloat(pview, "translationY", pview.getY(), mCurrentSelectedPencil.getLayoutParams().height / 3f);
        objectAnimator.setDuration(200);
        objectAnimator.start();
    }

    public void showPopupMenu(View panchorview){
        PopupMenu popup = new PopupMenu(this, panchorview);
        popup.setOnMenuItemClickListener(this);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.camera_context_menu, popup.getMenu());
        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.take_photo:
                dispatchTakePictureIntent();
                return true;
            case R.id.get_from_gallery:
                dispatchPictureFromGallery();
                return true;
            case R.id.set_background:
                //Router.startImageSelectionActivity(this);
                showBackgroundChooser();
                return true;
            default:
                return false;
        }
    }

    private void showBackgroundChooser(){
        ArrayList<String> imageList = getPurchasedBackgrounds();
        Router.showBackgroundChooserFragmentDialog(this,imageList);
    }

    private void dispatchTakePictureIntent() {

        if (!mMMPermission.checkPermissionForCamera()){
            mCameraPermissionRequested = true;
            mMMPermission.requestPermissionForCamera();
        }

        if (mMMPermission.checkPermissionForCamera() && mMMPermission.checkPermissionForExternalStorage()) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                // Create the File where the photo should go
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        }
    }

    public void onResume(){
        super.onResume();


        if (Build.VERSION.SDK_INT >= 23 ) {
            if (mCameraPermissionRequested) {
                mCameraPermissionRequested = false;
                if (!mMMPermission.checkPermissionForExternalStorage()) {
                    mExternalStoragePermissionRequestedForCamera = true;
                    mMMPermission.requestPermissionForExternalStorage();
                }else{
                    dispatchTakePictureIntent();
                }
            } else if (mExternalStoragePermissionRequestedForCamera) {
                mExternalStoragePermissionRequestedForCamera = false;
                dispatchTakePictureIntent();
            } else if (mExternalStoragePermissionRequestedForSave) {
                mExternalStoragePermissionRequestedForSave = false;
                if (mCurrentFilename != null) mCurrentFilename = BitmapUtils.getUniqueFilename();
                BitmapUtils.saveBitmapToMediaDirectory(PaintActivity.this, BitmapUtils.merge(mBackgroundView.getBitmap(), mDrawingView.getBitmap()),mCurrentFilename);
            }
        }

        if (mIABSetupFinished && !mFirstTime){
            getPurchasedProducts();
        }

        mFirstTime = false;

        hideStatusBar();
    }

    private void dispatchPictureFromGallery(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, PICK_IMAGE);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void onDestroy(){
        super.onDestroy();
        mHornSound.release();
        mBlopSound.release();
        if (!mNoAds){
            Appodeal.show(this, Appodeal.INTERSTITIAL);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if (mCurrentPhotoPath != null) {
                galleryAddPic();
                setPic(null,-1);
            }
        }else if (requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            Uri selectedImage = data.getData();

            if (selectedImage != null){
                setPic(selectedImage,-1);
            }
        }else if (requestCode == IMAGE_GALLERY_REQUEST && resultCode == RESULT_OK){
            setPic(null, data.getIntExtra(ImageSelectionActivity.IMAGE_ID, 0));
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public void setPic(Uri pselectedimage,int presourceid) {
        // Get the dimensions of the View
        int targetW = mBackgroundView.getWidth();
        int targetH = mBackgroundView.getHeight();

        int virtualWidth = targetW;
        int virtualHeight = targetH;
        if (targetW < targetH){
            virtualWidth = targetH;
            virtualHeight = targetW;
        }

        Bitmap bmp = null;

        if (presourceid != -1) {
            try {
                SVG svg = SVG.getFromResource(this, presourceid);
                svg.setRenderDPI(getResources().getDisplayMetrics().xdpi);
                svg.setDocumentHeight(virtualHeight);
                svg.setDocumentWidth(virtualWidth);
                svg.setDocumentPreserveAspectRatio(PreserveAspectRatio.BOTTOM);

                bmp = Bitmap.createBitmap(virtualWidth,virtualHeight,Bitmap.Config.ARGB_8888);
                Canvas  bmcanvas = new Canvas(bmp);

                // Clear background to white
                bmcanvas.drawRGB(255, 255, 255);

                // Render our document onto our canvas
                svg.renderToCanvas(bmcanvas);
            } catch (SVGParseException e) {
                e.printStackTrace();
            }
        }else if (pselectedimage == null){
            bmp = BitmapFactory.decodeFile(mCurrentPhotoPath);
        }else{
            try {
                bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(pselectedimage));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        int photoW =  bmp.getWidth();
        int photoH = bmp.getHeight();

        float scaleFactor = 1f;

        Matrix m = new Matrix();
        if (photoH > photoW){
            int tmpHeight = photoH;
            photoH = photoW;
            photoW = tmpHeight;

            m.postRotate(90);

            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);
        }

        scaleFactor = (float)virtualHeight / (float)photoH;

        float delta = virtualWidth - (int)(photoW * scaleFactor);

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, (int)(photoW * scaleFactor), (int)(photoH * scaleFactor), true);
        //Bitmap finalBitmap = Bitmap.createBitmap(virtualWidth, virtualHeight, Bitmap.Config.ARGB_8888);

        System.out.println("virtualWidth: "+virtualWidth+" - virtualHeight:"+virtualHeight);

        //Canvas c = new Canvas(finalBitmap);
        //c.drawBitmap(bmp,new Rect(0,0,photoW,photoH),new RectF(0,0f,virtualWidth,virtualHeight),new Paint(Paint.DITHER_FLAG));
        //c.drawBitmap(scaledBitmap,delta/2,0,new Paint(Paint.DITHER_FLAG));

        mBackgroundView.setImageBitmap(scaledBitmap);
    }

    public void setCurrentPencil(int presource){
        if (presource == -1){
            mDrawingView.disablePencilMode();
        }else {
            mDrawingView.setPencilMode(presource);
        }
    }

    public void showSnackBarMessage(String pmessage,int pduration){
        Snackbar snackbar = Snackbar.make(findViewById(R.id.main_paint_layout), pmessage, pduration);

        View snackbarView = snackbar.getView();

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)snackbarView.getLayoutParams();
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        snackbarView.setLayoutParams(params);

        snackbarView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        Typeface font = Typeface.createFromAsset(getAssets(), "littledays.ttf");
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
        textView.setTypeface(font);
        snackbar.show();
    }

    public void clearCanvas(){
        mDrawingView.reset();
        mBackgroundView.reset();
        mCurrentFilename = BitmapUtils.getUniqueFilename();
    }

    public void playBlopSound(){
        if (mBlopSound.isPlaying()){
            mBlopSound.stop();
            mBlopSound.release();
            mBlopSound = MediaPlayer.create(this, R.raw.blop);
        }
        mBlopSound.start();
    }

    public void playHornSound(){
        if (mHornSound.isPlaying()){
            mHornSound.stop();
            mHornSound.release();
            mHornSound = MediaPlayer.create(this, R.raw.horn);
        }
        mHornSound.start();
    }
}

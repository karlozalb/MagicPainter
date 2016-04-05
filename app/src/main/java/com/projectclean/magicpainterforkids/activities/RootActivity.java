package com.projectclean.magicpainterforkids.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.projectclean.magicpainterforkids.billingutils.IabHelper;
import com.projectclean.magicpainterforkids.billingutils.IabResult;
import com.projectclean.magicpainterforkids.billingutils.Inventory;


/**
 * Created by Carlos Albaladejo Pérez on 13/03/2016.
 */
public abstract class RootActivity  extends AppCompatActivity {

    protected IabHelper mHelper;

    protected boolean mNoAds,mAnimalPack1,mAnimalPack2;

    protected String base64EncodedPublicKey  = PaintActivity.stringTransform("jnnenMfie@LVOLN`Pefvbaffhdfvfjnned@ldfvbfLaHmNosSqfWu`NbV~uhHpFasbsDNhNNpsMlB`s_qv^AkfR_W`EqApTPdnbCF~Q`eE]}OkLOdwvOCTvcHeOHL`~ijiilS@vdtiBlN}hADMiVHibTjRTSK@DpQ@SpM~SDTrJvuOPWww}s~RvbRf^SbmQ_mHoWf@TTpM_lDWk]c@KEMrHnju]aqkwNACspr^EhwMS]W}jmt}Bb_d]^}_kiccnevCWwdIMAisnr]IQ}ppPowaoPnFb_cuotLqsWwfWjfneo}Jis~CHrblUq_MTKPncfvfe", 0x27);

    protected void hideStatusBar(){
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        }else{
            final View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);

            decorView.setOnSystemUiVisibilityChangeListener
                    (new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            // Note that system bars will only be "visible" if none of the
                            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN & View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN & View.SYSTEM_UI_FLAG_IMMERSIVE) == 0) {
                                // TODO: The system bars are visible. Make any desired
                                // adjustments to your UI, such as showing the action bar or
                                // other navigational controls.
                                int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE;
                                decorView.setSystemUiVisibility(uiOptions);
                            } else {
                                // TODO: The system bars are NOT visible. Make any desired
                                // adjustments to your UI, such as hiding the action bar or
                                // other navigational controls.
                            }
                        }
                    });

        }
    }

    public void getPurchasedProducts(){
        IabHelper.QueryInventoryFinishedListener mGotInventoryListener
                = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result,Inventory inventory) {
                if (result.isFailure()) {
                    Toast.makeText(RootActivity.this, "Error, please check your internet connection.", Toast.LENGTH_SHORT);
                    SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                    mNoAds = sharedPref.getBoolean(ShopActivity.NO_ADS_PRODUCT, false);
                    mAnimalPack1 = sharedPref.getBoolean(ShopActivity.ANIMAL_PACK_1_PRODUCT, false);
                    mAnimalPack2 = sharedPref.getBoolean(ShopActivity.ANIMAL_PACK_2_PRODUCT, false);
                    onPurchasedProductsResult();
                }
                else {
                   SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
                   SharedPreferences.Editor editor = sharedPref.edit();
                   if (inventory.hasPurchase(ShopActivity.NO_ADS_PRODUCT)){
                       mNoAds = true;
                       editor.putBoolean(ShopActivity.NO_ADS_PRODUCT, mNoAds);
                   }
                   if (inventory.hasPurchase(ShopActivity.ANIMAL_PACK_1_PRODUCT)){
                       mAnimalPack1 = true;
                       editor.putBoolean(ShopActivity.ANIMAL_PACK_1_PRODUCT, mAnimalPack1);
                   }
                   if (inventory.hasPurchase(ShopActivity.ANIMAL_PACK_2_PRODUCT)){
                       mAnimalPack2 = true;
                       editor.putBoolean(ShopActivity.ANIMAL_PACK_2_PRODUCT, mAnimalPack2);
                   }
                   onPurchasedProductsResult();
                   editor.commit();
                }
            }
        };

        mHelper.queryInventoryAsync(mGotInventoryListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }

    public abstract void onPurchasedProductsResult();

}

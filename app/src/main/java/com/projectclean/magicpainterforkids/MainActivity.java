package com.projectclean.magicpainterforkids;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.appodeal.ads.Appodeal;
import com.projectclean.magicpainterforkids.activities.RootActivity;
import com.projectclean.magicpainterforkids.billingutils.IabHelper;
import com.projectclean.magicpainterforkids.billingutils.IabResult;
import com.projectclean.magicpainterforkids.customviews.CustomFontButton;


public class MainActivity extends RootActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CustomFontButton buttonDraw = (CustomFontButton)findViewById(R.id.main_button_draw);
        CustomFontButton buttonDrawWBackground = (CustomFontButton)findViewById(R.id.main_button_draw_with_background);
        CustomFontButton buttonShop = (CustomFontButton)findViewById(R.id.main_button_shop);
        ImageButton buttonFinish = (ImageButton)findViewById(R.id.main_button_exit);

        buttonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        buttonDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.startDrawingActivity(MainActivity.this,false);
            }
        });

        buttonDrawWBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.startDrawingActivity(MainActivity.this, true);
            }
        });

        buttonShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.startShopActivity(MainActivity.this);
            }
        });

        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d("MPFK", "Problem setting up In-app Billing: " + result);
                    return;
                }
                getPurchasedProducts();
            }
        });
    }

    @Override
    public void onPurchasedProductsResult() {
        if (!mNoAds){
            String appKey = "2828a4f2e70e62121d625edb019ebb36cf2ed80a3b08fec2";
            Appodeal.disableLocationPermissionCheck();
            Appodeal.initialize(this, appKey, Appodeal.INTERSTITIAL);
            Appodeal.setTesting(true);
        }
    }
}

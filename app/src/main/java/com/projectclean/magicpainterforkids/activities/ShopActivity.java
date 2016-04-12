package com.projectclean.magicpainterforkids.activities;

import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.projectclean.magicpainterforkids.R;
import com.projectclean.magicpainterforkids.analytics.AnalyticsApplication;
import com.projectclean.magicpainterforkids.billingutils.IabHelper;
import com.projectclean.magicpainterforkids.billingutils.IabResult;
import com.projectclean.magicpainterforkids.billingutils.Inventory;
import com.projectclean.magicpainterforkids.billingutils.Purchase;
import com.projectclean.magicpainterforkids.billingutils.SkuDetails;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Carlos Albaladejo Pérez on 21/03/2016.
 */
public class ShopActivity extends RootActivity {

    ListView mListView;
    IabHelper mHelper;
    ShopListAdapter mAdapter;

    private int PRODUCT_PURCHASE = 10001;

    public static String NO_ADS_PRODUCT = "mpfk_noads_0001";
    public static String EXTRA_PACK_1_PRODUCT = "mpfk_extra_0001";
    public static String EXTRA_PACK_2_PRODUCT = "mpfk_extra_0002";

    private String mCurrentPayload;

    private boolean mFirstTime;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        mFirstTime = true;

        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, RootActivity.base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    Log.d("MPFK", "Problem setting up In-app Billing: " + result);
                    return;
                }
                getAllProducts();
            }
        });

        mListView = (ListView)findViewById(R.id.shop_listview);
        mListView.setEmptyView(findViewById(R.id.empty_textview));

        mAdapter = new ShopListAdapter();
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                purchaseProduct(((ProductNode) mAdapter.getItem(position)).ID);
            }
        });
    }

    public void onResume(){
        super.onResume();

        if (!mFirstTime) {
            mAdapter.clear();
            getAllProducts();
        }else{
            mTracker.setScreenName(RootActivity.SHOPACTIVITY);
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        }
        mFirstTime = false;
    }

    public void purchaseProduct(String pid){

        sendBuyEventEvent(pid);

        IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
                = new IabHelper.OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result, Purchase purchase)
            {
                if (result.isFailure()) {
                    Log.d("MPFK", "Error purchasing: " + result);
                    return;
                }

                if (!verifyPurchasePayload(purchase.getDeveloperPayload())){
                    Log.d("MPFK", "Payload incorrect: " + result);
                    return;
                }

                mAdapter.clear();
                getAllProducts();
            }
        };

        String android_id = Settings.Secure.getString(this.getContentResolver(),Settings.Secure.ANDROID_ID);

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();

        mCurrentPayload = android_id + "-" + ts;

        mHelper.launchPurchaseFlow(this, pid, PRODUCT_PURCHASE,mPurchaseFinishedListener, mCurrentPayload);
    }

    private boolean verifyPurchasePayload(String ppayload){
        return ppayload.equals(mCurrentPayload);
    }

    public void getAllProducts(){
        //Get full list of products.

        IabHelper.QueryInventoryFinishedListener
                mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory)
            {
                if (result.isFailure()) {
                    // handle error
                    return;
                }

                /*if (inventory.hasPurchase(NO_ADS_PRODUCT)) {
                    mHelper.consumeAsync(inventory.getPurchase(NO_ADS_PRODUCT),null);
                }

                if (inventory.hasPurchase(EXTRA_PACK_1_PRODUCT)) {
                    mHelper.consumeAsync(inventory.getPurchase(EXTRA_PACK_1_PRODUCT),null);
                }

                if (inventory.hasPurchase(EXTRA_PACK_2_PRODUCT)) {
                    mHelper.consumeAsync(inventory.getPurchase(EXTRA_PACK_2_PRODUCT),null);
                }*/

                /*String applePrice = inventory.getSkuDetails(SKU_APPLE).getPrice();
                String bananaPrice = inventory.getSkuDetails(SKU_BANANA).getPrice();*/

                mHelper.flagEndAsync();
                getPurchasedProducts(inventory);

                // update the UI
            }
        };

        List additionalSkuList = new LinkedList();
        additionalSkuList.add(NO_ADS_PRODUCT);
        additionalSkuList.add(EXTRA_PACK_1_PRODUCT);
        additionalSkuList.add(EXTRA_PACK_2_PRODUCT);

        mHelper.queryInventoryAsync(true, additionalSkuList,mQueryFinishedListener);
    }

    public void getPurchasedProducts(final Inventory pproducts){
        IabHelper.QueryInventoryFinishedListener mGotInventoryListener
                = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result,Inventory inventory) {
                if (result.isFailure()) {
                    // handle error here
                }
                else {
                    // does the user have the premium upgrade?
                    //mIsPremium = inventory.hasPurchase(SKU_PREMIUM);
                    // update UI accordingly
                    if (!inventory.hasPurchase(NO_ADS_PRODUCT)) {
                        SkuDetails details = pproducts.getSkuDetails(NO_ADS_PRODUCT);
                        if (details != null) {
                            ProductNode newProduct = new ProductNode(NO_ADS_PRODUCT, details.getDescription(), details.getTitle(), details.getPrice(), -1);
                            mAdapter.addItem(newProduct);
                        }
                    }

                    if (!inventory.hasPurchase(EXTRA_PACK_1_PRODUCT)) {
                        SkuDetails details = pproducts.getSkuDetails(EXTRA_PACK_1_PRODUCT);
                        if (details != null) {
                            ProductNode newProduct = new ProductNode(EXTRA_PACK_1_PRODUCT, details.getDescription(), details.getTitle(), details.getPrice(), -1);
                            mAdapter.addItem(newProduct);
                        }
                    }

                    if (!inventory.hasPurchase(EXTRA_PACK_2_PRODUCT)) {
                        SkuDetails details = pproducts.getSkuDetails(EXTRA_PACK_2_PRODUCT);
                        if (details != null) {
                            ProductNode newProduct = new ProductNode(EXTRA_PACK_2_PRODUCT, details.getDescription(), details.getTitle(), details.getPrice(), -1);
                            mAdapter.addItem(newProduct);
                        }
                    }
                }
            }
        };

        mHelper.queryInventoryAsync(mGotInventoryListener);
    }

    @Override
    public void onPurchasedProductsResult() {

    }

    public void sendBuyEventEvent(String pboughtproduct){
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction("Product: " + pboughtproduct)
                .build());
    }

    public class ShopListAdapter extends BaseAdapter{

        LinkedList<ProductNode> mProducts;

        public ShopListAdapter(){
            mProducts = new LinkedList<ProductNode>();
        }

        @Override
        public int getCount() {
            return mProducts.size();
        }

        public void addItem(ProductNode pproduct){
            Log.i("MPFK","pproduct:"+pproduct.ID+" - "+pproduct.TITLE+" - "+pproduct.DESCRIPTION+" - "+pproduct.PRICE);
            mProducts.add(pproduct);
            notifyDataSetChanged();
        }

        @Override
        public Object getItem(int position) {
            return mProducts.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void clear(){
            mProducts.clear();
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ProductNode p = mProducts.get(position);

            if (convertView == null){
                convertView = ShopActivity.this.getLayoutInflater().inflate(R.layout.shop_item_remove_ads,null);

                ViewHolder v = new ViewHolder();
                v.TITLE = (TextView)convertView.findViewById(R.id.item_title);
                v.DESCRIPTION = (TextView)convertView.findViewById(R.id.item_description);
                convertView.setTag(v);
            }

            ViewHolder holder = (ViewHolder)convertView.getTag();

            holder.TITLE.setText(p.TITLE);
            holder.DESCRIPTION.setText(p.DESCRIPTION);

            return convertView;
        }
    }

    public class ViewHolder{
        public TextView TITLE;
        public TextView DESCRIPTION;
    }

    public class ProductNode{
        public String ID,DESCRIPTION,TITLE,PRICE;
        public int IMAGE_RESOURCE_ID;

        public ProductNode(String pid,String pdesc, String ptitle, String pprice,int presource){
            ID = pid;
            DESCRIPTION = pdesc;
            TITLE = ptitle;
            PRICE = pprice;
            IMAGE_RESOURCE_ID = presource;
        }
    }

}

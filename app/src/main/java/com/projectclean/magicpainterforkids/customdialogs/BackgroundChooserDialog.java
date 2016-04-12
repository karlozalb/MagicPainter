package com.projectclean.magicpainterforkids.customdialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGImageView;
import com.caverock.androidsvg.SVGParseException;
import com.projectclean.magicpainterforkids.R;
import com.projectclean.magicpainterforkids.Router;
import com.projectclean.magicpainterforkids.activities.PaintActivity;
import com.projectclean.magicpainterforkids.customviews.CustomFontButton;
import com.projectclean.magicpainterforkids.customviews.CustomFontTextView;
import com.projectclean.magicpainterforkids.utils.ScreenUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Carlos Albaladejo Pérez on 23/03/2016.
 */
public class BackgroundChooserDialog extends DialogFragment {

    public static String PRODUCT_LIST = "BC_PL";

    ImageAdapter mAdapter;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_background_chooser_dialog, null);
        v.setBackgroundColor(Color.WHITE);

        CustomFontTextView cftv = (CustomFontTextView)LayoutInflater.from(getActivity()).inflate(R.layout.dialog_title_textview, null);
        cftv.setText(getString(R.string.background_chooser));
        cftv.setBackgroundColor(Color.WHITE);

        builder.setView(v).setCustomTitle(cftv);

        mAdapter = new ImageAdapter(getActivity(),getActivity().getLayoutInflater());

        ArrayList<String> products = new ArrayList<String>();

        products.add("default_0001");
        products.add("default_0002");
        products.add("default_0003");
        products.add("default_0004");
        products.add("default_0005");
        products.add("default_0006");
        products.add("default_0007");

        if (getArguments() != null){
            ArrayList<String> ownedProducts = getArguments().getStringArrayList(PRODUCT_LIST);
            if (ownedProducts != null && ownedProducts.size() > 0)products.addAll(ownedProducts);
        }

        mAdapter.setResources(listRaw(products));

        GridView gv = (GridView) v.findViewById(R.id.pencil_gridview);
        gv.setAdapter(mAdapter);

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((PaintActivity)getActivity()).setPic(null, (int) mAdapter.getItemSVG(position));
                ((PaintActivity)getActivity()).sendBackgroundEvent(mAdapter.getItemFriendlyName(position));
                dismiss();
            }
        });

        CustomFontButton button = (CustomFontButton) v.findViewById(R.id.go_to_shop_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.startShopActivity(getActivity());
                dismiss();
            }
        });


        return builder.create();
    }

    public LinkedList<ProductNode> listRaw(ArrayList<String> products){
        LinkedList<ProductNode> resources= new LinkedList<ProductNode>();

        for (String p : products) {
            ProductNode pn =new ProductNode();
            pn.SVG_ID = this.getResources().getIdentifier(p, "raw", getActivity().getPackageName());
            pn.THUMBNAIL_ID = this.getResources().getIdentifier(p+"_jpg", "raw", getActivity().getPackageName());
            pn.FRIENDLY_NAME = p;
            resources.add(pn);
        }

        return resources;

        /*Field[] fields=R.raw.class.getFields();
        LinkedList<ProductNode> resources= new LinkedList<ProductNode>();

        for(int count=0; count < fields.length; count++){
            try {
                for (String p : products){
                    if (fields[count].getName().contains(p)){
                        resources.add(fields[count].getInt(fields[count]));
                        break;
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return resources;*/
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        private LinkedList<ProductNode> mResources;

        public ImageAdapter(Context c, LayoutInflater pinflater) {
            mContext = c;
            mInflater = pinflater;
        }

        public int getCount() {
            return mResources.size();
        }

        public void setResources(LinkedList<ProductNode> presources) {
            mResources = presources;
        }

        public Object getItem(int position) {
            return mResources.get(position).THUMBNAIL_ID;
        }

        public Object getItemSVG(int position) {
            return mResources.get(position).SVG_ID;
        }

        public String getItemFriendlyName(int position) {
            return mResources.get(position).FRIENDLY_NAME;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                convertView = new ImageView(mContext);
                int pixels = ScreenUtils.getPixelsFromDp((AppCompatActivity) mContext, 120);
                convertView.setLayoutParams(new GridView.LayoutParams(pixels, pixels));
                convertView.setBackgroundResource(R.drawable.gridview_item_background);
                convertView.setPadding(8, 8, 8, 8);
            }

            ((ImageView) convertView).setImageResource((int)mAdapter.getItem(position));

            return convertView;
        }

        // create a new ImageView for each item referenced by the Adapter
       /*public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                convertView = mInflater.inflate(R.layout.background_item_layout,null);

                SVGImageView svgView = new SVGImageView(mContext);
                int pixels = ScreenUtils.getPixelsFromDp((AppCompatActivity) mContext, 120);
                svgView.setLayoutParams(new GridView.LayoutParams(pixels, pixels));
                svgView.setBackgroundResource(R.drawable.gridview_item_background);
                svgView.setPadding(8, 8, 8, 8);

                ((FrameLayout)convertView).addView(svgView);

                ViewHolder vh = new ViewHolder();
                vh.SVG_IMAGE_VIEW = svgView;
                vh.SVG_SPINNER = (ProgressBar)convertView.findViewById(R.id.svg_spinner);

                convertView.setTag(vh);
            }

           final ViewHolder vh = (ViewHolder)convertView.getTag();
            vh.SVG_SPINNER.setVisibility(View.VISIBLE);
            vh.SVG_IMAGE_VIEW.setVisibility(View.INVISIBLE);

            AsyncTask<View, Void, SVG> task = new AsyncTask<View, Void, SVG>(){

                View view;

                @Override
                protected SVG doInBackground(View... params) {
                    view = params[0];
                    SVG svg = null;
                    try {
                        svg = SVG.getFromResource(mContext, mResources.get(position));
                    } catch (SVGParseException e) {
                        e.printStackTrace();
                    }

                    return svg;
                }

                protected void onProgressUpdate(Void... progress) {
                }

                protected void onPostExecute(SVG result) {
                    ((SVGImageView)view).setSVG(result);
                    vh.SVG_IMAGE_VIEW.setVisibility(View.VISIBLE);
                    vh.SVG_SPINNER.setVisibility(View.GONE);
                }
            }.execute(vh.SVG_IMAGE_VIEW);

            return convertView;
        }*/
    }

    public class ProductNode{
        public int SVG_ID;
        public int THUMBNAIL_ID;
        public String FRIENDLY_NAME;
    }

    public class ViewHolder{
        public SVGImageView SVG_IMAGE_VIEW;
        public ProgressBar SVG_SPINNER;
    }

}

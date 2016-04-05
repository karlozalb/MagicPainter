package com.projectclean.magicpainterforkids.customdialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

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

        CustomFontTextView cftv = (CustomFontTextView)LayoutInflater.from(getActivity()).inflate(R.layout.dialog_title_textview, null);
        cftv.setText(getString(R.string.background_chooser));

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
                ((PaintActivity)getActivity()).setPic(null, (int) mAdapter.getItem(position));
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

    public LinkedList<Integer> listRaw(ArrayList<String> products){
        Field[] fields=R.raw.class.getFields();
        LinkedList<Integer> resources= new LinkedList<Integer>();

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
        return resources;
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        private LinkedList<Integer> mResources;

        public ImageAdapter(Context c,LayoutInflater pinflater) {
            mContext = c;
            mInflater = pinflater;
        }

        public int getCount() {
            return mResources.size();
        }

        public void setResources(LinkedList<Integer> presources){
            mResources = presources;
        }

        public Object getItem(int position) {
            return mResources.get(position);
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                convertView = new SVGImageView(mContext);
                int pixels = ScreenUtils.getPixelsFromDp((AppCompatActivity) mContext, 120);
                convertView.setLayoutParams(new GridView.LayoutParams(pixels, pixels));
                convertView.setBackgroundResource(R.drawable.gridview_item_background);
                convertView.setPadding(8, 8, 8, 8);
            }

            SVG svg = null;
            try {
                svg = SVG.getFromResource(mContext, mResources.get(position));
            } catch (SVGParseException e) {
                e.printStackTrace();
            }
            ((SVGImageView)convertView).setSVG(svg);

            return convertView;
        }

    }

    public class ViewHolder{
        public SVGImageView SVG_IMAGE_VIEW;
    }

}

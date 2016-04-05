package com.projectclean.magicpainterforkids.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.projectclean.magicpainterforkids.utils.ScreenUtils;

import java.lang.reflect.Field;
import java.util.LinkedList;


/**
 * Created by Carlos Albaladejo Pérez on 21/03/2016.
 */
public class ImageSelectionActivity extends AppCompatActivity{

    public static String IMAGE_ID = "IMAGE_ID";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_selection);

        LinkedList<Integer> resources = listRaw();

        GridView gridview = (GridView) findViewById(R.id.gridview);

        final ImageAdapter imgAdapter = new ImageAdapter(this,getLayoutInflater());
        imgAdapter.setResources(resources);

        gridview.setAdapter(imgAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent result = new Intent();
                result.putExtra(IMAGE_ID,(int)imgAdapter.getItem(position));

                ImageSelectionActivity.this.setResult(RESULT_OK, result);
                ImageSelectionActivity.this.finish();
            }
        });

    }

    public LinkedList<Integer> listRaw(){
        Field[] fields=R.raw.class.getFields();

        LinkedList<Integer> resources = new LinkedList<Integer>();

        for(int count=0; count < fields.length; count++){
            try {
                if (!fields[count].getName().contains("pencil_")) {
                    resources.add(fields[count].getInt(fields[count]));
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

package com.projectclean.magicpainterforkids.customdialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.projectclean.magicpainterforkids.R;
import com.projectclean.magicpainterforkids.activities.PaintActivity;
import com.projectclean.magicpainterforkids.customviews.CustomFontTextView;

import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Created by Carlos Albaladejo Pérez on 23/03/2016.
 */
public class PencilChooserDialog extends DialogFragment {

    ImageAdapter mAdapter;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_pencil_chooser_dialog, null);

        CustomFontTextView cftv = (CustomFontTextView)LayoutInflater.from(getActivity()).inflate(R.layout.dialog_title_textview, null);
        cftv.setText(getString(R.string.pencil_chooser));

        builder.setView(v).setCustomTitle(cftv);

        mAdapter = new ImageAdapter(getActivity(),getActivity().getLayoutInflater());
        mAdapter.setResources(listRaw());

        GridView gv = (GridView) v.findViewById(R.id.pencil_gridview);
        gv.setAdapter(mAdapter);

        gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    ((PaintActivity) getActivity()).setCurrentPencil(-1);
                }else{
                    ((PaintActivity) getActivity()).setCurrentPencil((int) mAdapter.getItem(position));
                }
                dismiss();
            }
        });

        return builder.create();
    }

    public LinkedList<Integer> listRaw(){
        Field[] fields=R.raw.class.getFields();
        LinkedList<Integer> resources= new LinkedList<Integer>();

        resources.add(R.raw.disable_pencil);

        for(int count=0; count < fields.length; count++){
            try {
                if (fields[count].getName().contains("pencil_")) {
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
                convertView = mInflater.inflate(R.layout.pencil_chooser_item_gridview,null);

                ViewHolder v = new ViewHolder();
                v.IMAGE_VIEW = (ImageView)convertView.findViewById(R.id.pencil_image_view);

                v.IMAGE_VIEW.setColorFilter(Color.argb(255, 0, 0, 0));

                convertView.setTag(v);
            }

            ((ViewHolder)convertView.getTag()).IMAGE_VIEW.setImageResource(mResources.get(position));

            return convertView;
        }
    }

    public class ViewHolder{
        public ImageView IMAGE_VIEW;
    }

}

package com.zbform.penform.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.TextViewCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zbform.penform.R;
import com.zbform.penform.fragment.FormListFragment;
import com.zbform.penform.widget.LvMenuItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuItemAdapter extends ArrayAdapter<LvMenuItem>{
    private final int mIconSize;
    private LayoutInflater mInflater;
    private Context mContext;
    private List<LvMenuItem> mItems;

    public MenuItemAdapter(Context context) {
        super(context, R.layout.design_drawer_item);
        mInflater = LayoutInflater.from(context);
        mContext = context;

        mIconSize = context.getResources().getDimensionPixelSize(R.dimen.drawer_icon_size);//24dp

        mItems = new ArrayList<>(
                Arrays.asList(
                        new LvMenuItem(R.mipmap.topmenu_icn_night,
                                mContext.getResources().getString(R.string.menu_item_formlist)),
                        new LvMenuItem(R.mipmap.topmenu_icn_skin,
                                mContext.getResources().getString(R.string.menu_item_formrecord)),
                        new LvMenuItem(R.mipmap.topmenu_icn_time,
                                mContext.getResources().getString(R.string.menu_item_setting)),
                        new LvMenuItem(R.mipmap.topmenu_icn_vip,
                                mContext.getResources().getString(R.string.menu_item_upd)),
                        new LvMenuItem(R.mipmap.topmenu_icn_exit,
                                mContext.getResources().getString(R.string.menu_item_exit))));
    }


    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public LvMenuItem getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getViewTypeCount() {
        return 3;
    }

    public int getItemViewType(int position) {
        return mItems.get(position).type;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LvMenuItem item = mItems.get(position);
        switch (item.type) {
            case LvMenuItem.TYPE_NORMAL:
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.design_drawer_item, parent,
                            false);
                }
                TextView itemView = (TextView) convertView;
                itemView.setText(item.name);
                Drawable icon = mContext.getResources().getDrawable(item.icon);
                // setIconColor(icon);
                if (icon != null) {
                    icon.setBounds(0, 0, mIconSize, mIconSize);
                    TextViewCompat.setCompoundDrawablesRelative(itemView, icon, null, null, null);
                }

                break;
            case LvMenuItem.TYPE_NO_ICON:
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.design_drawer_item_subheader,
                            parent, false);
                }
                TextView subHeader = (TextView) convertView;
                subHeader.setText(item.name);
                break;
            case LvMenuItem.TYPE_SEPARATOR:
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.design_drawer_item_separator,
                            parent, false);
                }
                break;
        }

        return convertView;
    }

    public void setIconColor(Drawable icon) {
        int textColorSecondary = android.R.attr.textColorSecondary;
        TypedValue value = new TypedValue();
        if (!mContext.getTheme().resolveAttribute(textColorSecondary, value, true)) {
            return;
        }
        int baseColor = mContext.getResources().getColor(value.resourceId);
        icon.setColorFilter(baseColor, PorterDuff.Mode.MULTIPLY);
    }
}
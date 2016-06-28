package com.lee.qqinterface.adapter;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.lee.qqinterface.R;
import com.lee.qqinterface.bean.Person;
import com.lee.qqinterface.uitils.Utils;
import com.lee.qqinterface.view.FrontLayout;
import com.lee.qqinterface.view.SwipeLayout;

import java.util.ArrayList;
import java.util.HashSet;

public class HaoHanAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<Person> persons;
    public HashSet<SwipeLayout> openItems = new HashSet<>();

    public HaoHanAdapter(Context mContext, ArrayList<Person> persons) {
        this.mContext = mContext;
        this.persons = persons;

    }

    @Override
    public int getCount() {
        return persons.size();
    }

    @Override
    public Object getItem(int position) {
        return persons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (convertView == null) {
            view = View.inflate(mContext, R.layout.item_list, null);
        }
        ViewHolder mViewHolder = ViewHolder.getHolder(view);
        Person p = persons.get(position);
        String str = null;
        String currentLetter = p.getPinyin().charAt(0) + "";
        // 根据上一个首字母,决定当前是否显示字母
        if (position == 0) {
            str = currentLetter;
        } else {
            // 上一个人的拼音的首字母
            String preLetter = persons.get(position - 1).getPinyin().charAt(0) + "";
            if (!TextUtils.equals(preLetter, currentLetter)) {
                str = currentLetter;
            }
        }
        // 根据str是否为空,决定是否显示索引栏
        mViewHolder.mIndex.setVisibility(str == null ? View.GONE : View.VISIBLE);
        mViewHolder.mIndex.setText(currentLetter);
        mViewHolder.mName.setText(p.getName());
        mViewHolder.sl.setSwipeListener(swipeLayoutListener);
        mViewHolder.front.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.showToast(mContext, "click:" + position);
            }
        });
        mViewHolder.mCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return view;
    }

    SwipeLayout.SwipeListener swipeLayoutListener = new SwipeLayout.SwipeListener() {
        @Override
        public void onClose(SwipeLayout mSwipeLayout) {
            openItems.remove(mSwipeLayout);
        }

        @Override
        public void onOpen(SwipeLayout mSwipeLayout) {
            openItems.add(mSwipeLayout);
        }


        @Override
        public void onStartClose(SwipeLayout mSwipeLayout) {

        }

        @Override
        public void onStartOpen(SwipeLayout mSwipeLayout) {
            closeAllLayout();
            openItems.add(mSwipeLayout);
        }
    };

    public void closeAllLayout() {
        if (openItems.size() == 0) {
            return;
        }
        for (SwipeLayout layout : openItems) {
            layout.close();
        }
        openItems.clear();
    }

    public int getUnClosedCount() {
        return openItems.size();
    }

    static class ViewHolder {
        TextView mIndex;
        TextView mName;
        SwipeLayout sl;
        FrontLayout front;
        Button mCall;

        public static ViewHolder getHolder(View view) {
            Object tag = view.getTag();
            if (tag != null) {
                return (ViewHolder) tag;
            } else {
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.mIndex = (TextView) view.findViewById(R.id.tv_index);
                viewHolder.mName = (TextView) view.findViewById(R.id.tv_name);
                viewHolder.sl = (SwipeLayout) view.findViewById(R.id.sl);
                viewHolder.front = (FrontLayout) view.findViewById(R.id.front);
                viewHolder.mCall = (Button) view.findViewById(R.id.tv_call);
                view.setTag(viewHolder);
                return viewHolder;
            }
        }

    }

}

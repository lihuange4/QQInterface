package com.lee.qqinterface.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.CycleInterpolator;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lee.qqinterface.R;
import com.lee.qqinterface.adapter.HaoHanAdapter;
import com.lee.qqinterface.bean.Person;
import com.lee.qqinterface.uitils.Cheeses;
import com.lee.qqinterface.uitils.Utils;
import com.lee.qqinterface.view.DragLayout;
import com.lee.qqinterface.view.DragRelativeLayout;
import com.lee.qqinterface.view.QuickIndex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends Activity {

    private ListView mLeftList;
    private ListView mMainList;
    private ImageView mHeaderImage;
    private DragLayout mDragLayout;
    private QuickIndex quickIndex;//快速索引
    private ArrayList<Person> persons;
    private TextView tv_center;//索引提示
    private DragRelativeLayout mMainView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        initView();
        initData();
        

    }

    private void initView() {
        setContentView(R.layout.activity_main);
        mLeftList = (ListView) findViewById(R.id.lv_left);
        mMainList = (ListView) findViewById(R.id.lv_main);
        mHeaderImage = (ImageView) findViewById(R.id.iv_header);
        // 查找DragLayout, 设置监听
        mDragLayout = (DragLayout) findViewById(R.id.dl);

        quickIndex = (QuickIndex) findViewById(R.id.quick_index);
        tv_center = (TextView) findViewById(R.id.tv_center);//索引提示
        mMainView = (DragRelativeLayout) findViewById(R.id.rl_main);

    }

    private void initData() {
        mMainView.setDragLayout(mDragLayout);
        mLeftList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Cheeses
                .sCheeseStrings) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView mText = ((TextView) view);
                mText.setTextColor(Color.WHITE);
                return view;
            }
        });

        persons = new ArrayList<>();
        // 填充数据 , 排序
        fillAndSortData(persons);
        final HaoHanAdapter adapter = new HaoHanAdapter(MainActivity.this, persons);
        mMainList.setAdapter(adapter);
        mDragLayout.setAdapterInterface(adapter);
        //主界面设置adapter

        //监听滚动事件，快速索引随时更新
        mMainList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    adapter.closeAllLayout();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                char c = persons.get(firstVisibleItem).getPinyin().charAt(0);
                int index = c - 65;
                quickIndex.setTouchIndex(index);
            }
        });
        //设置索引变化监听
        quickIndex.setListener(new QuickIndex.OnLetterUpdateListener() {
            String str = "ABCDEFGHIJKLMNOPQRSTUVWSYZ";
            int i;
            String letter;

            @Override
            public void onLetterUpdate(String letter) {
                this.letter = letter;
                for (i = 0; i < persons.size(); i++) {
                    if (TextUtils.equals(letter, persons.get(i).getPinyin().charAt(0) + "")) {
                        showLetter(letter);
                        mMainList.setSelection(i);
                        break;
                    } else if (i == persons.size() - 1) {
                        break;
                    }
                }
            }

            //松开手时调用
            @Override
            public void onViewReleased() {
                if (i == persons.size() - 1) {
                    onLetterUpdate(str.charAt(str.indexOf(letter) - 1) + "");
                    quickIndex.setTouchIndex(letter.charAt(0) - 65 - 1);
                }
            }
        });


        //监听事件
        mDragLayout.setOnDragStatusChangeListener(new DragLayout.DragListener() {
            @Override
            public void open() {
                // Utils.showToast(MainActivity.this, "open");
                Random random = new Random();
                mLeftList.smoothScrollToPosition(random.nextInt(50));
            }

            @Override
            public void close() {
                //  Utils.showToast(MainActivity.this, "close");
                //头像抖动
                ObjectAnimator anim = ObjectAnimator.ofFloat(mHeaderImage, "translationX", Utils.dip2Dimension(13,
                        MainActivity.this));
                anim.setInterpolator(new CycleInterpolator(4));
                anim.setDuration(500);
                anim.start();
            }

            //拖拽时调用
            @Override
            public void draging(float percent) {
                //修改头像的透明度
                mHeaderImage.setAlpha(1.0f - 0.5f * percent);
            }
        });

    }

    //排序
    private void fillAndSortData(ArrayList<Person> persons) {
        // 填充数据
        for (int i = 0; i < Cheeses.NAMES.length; i++) {
            String name = Cheeses.NAMES[i];
            persons.add(new Person(name));
        }
        // 进行排序
        Collections.sort(persons);
    }

    private Handler mHandler = new Handler();

    /**
     * 显示字母
     */
    protected void showLetter(String letter) {
        tv_center.setVisibility(View.VISIBLE);
        tv_center.setText(letter);

        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tv_center.setVisibility(View.GONE);
            }
        }, 1000);

    }
}

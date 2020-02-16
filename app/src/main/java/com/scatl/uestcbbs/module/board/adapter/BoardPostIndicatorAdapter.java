package com.scatl.uestcbbs.module.board.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import androidx.viewpager.widget.ViewPager;

import com.scatl.uestcbbs.R;

import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.ColorTransitionPagerTitleView;

/**
 * author: sca_tl
 * description:
 * date: 2020/2/4 15:59
 */
public class BoardPostIndicatorAdapter extends CommonNavigatorAdapter {

    private String[] titles = new String[]{"最新", "全部", "精华"};
    private ViewPager viewPager;

    public BoardPostIndicatorAdapter(ViewPager viewPager) {
        this.viewPager = viewPager;
    }

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public IPagerTitleView getTitleView(Context context, int index) {
        ColorTransitionPagerTitleView simplePagerTitleView = new ColorTransitionPagerTitleView(context);
        simplePagerTitleView.setText(titles[index]);
        simplePagerTitleView.setTextSize(17);
        simplePagerTitleView.setNormalColor(Color.GRAY);
        simplePagerTitleView.setSelectedColor(context.getColor(R.color.colorPrimary));
        simplePagerTitleView.setOnClickListener(v -> {
            viewPager.setCurrentItem(index);
        });
        return simplePagerTitleView;
    }

    @Override
    public IPagerIndicator getIndicator(Context context) {
        LinePagerIndicator indicator = new LinePagerIndicator(context);
        indicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
        indicator.setLineHeight(7);
        indicator.setXOffset(20);
        indicator.setRoundRadius(10);
        indicator.setYOffset(10);
        indicator.setStartInterpolator(new AccelerateInterpolator());
        indicator.setEndInterpolator(new DecelerateInterpolator(2.0f));
        indicator.setColors(context.getColor(R.color.colorPrimary));

        return indicator;
    }
}
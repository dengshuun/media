package com.hwatong.media.common;
import java.lang.reflect.Field;
 
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.animation.Interpolator;
import android.widget.Scroller;
 
/**
 * ViewPager 滚动速度设置
 * 
 * @author Tercel
 * 
 */
public class ViewPagerScroller extends Scroller {
    private int mScrollDuration = 500;             // 滑动速度
 
    /**
     * 设置速度速度
     * @param duration
     */
    public void setScrollDuration(int duration){
        this.mScrollDuration = duration;
    }
     
    public ViewPagerScroller(Context context) {
        super(context);
    }
 
    public ViewPagerScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }
 
    public ViewPagerScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }
 
    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, mScrollDuration);
    }
 
    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, mScrollDuration);
    }
 
    public void initViewPagerScroll(ViewPager viewPager) {
    	if(viewPager == null) return;
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mScroller.set(viewPager, this);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void initViewPagerScroll(ViewPager viewPager,int mScrollDuration) {
    	if(viewPager == null) return;
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mScroller.set(viewPager, this);
            this.mScrollDuration = mScrollDuration;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
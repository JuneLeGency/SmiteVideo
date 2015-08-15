package lc.studio.gosmite;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import lc.studio.gosmite.fragment.CommentatorFragment_;
import lc.studio.gosmite.fragment.PlaceholderFragment_;


@EActivity(R.layout.activity_main)
public class MainActivity extends FragmentActivity {
    @ViewById
    PagerSlidingTabStrip tabs;

    @ViewById
    ViewPager pager;

    private MyPagerAdapter adapter;

    @AfterViews
    void setTabs(){
        adapter=new MyPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);
        tabs.setViewPager(pager);
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        private final String[] TITLES = { "最新视频", "热门主播" };

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLES[position];
        }

        @Override
        public int getCount() {
            return TITLES.length;
        }

        @Override
        public Fragment getItem(int position) {
            if(position==0){
                return PlaceholderFragment_.builder().build();
            }
            if(position==1){
                return CommentatorFragment_.builder().build();
            }
            else{
                return null;
            }
        }

    }

}

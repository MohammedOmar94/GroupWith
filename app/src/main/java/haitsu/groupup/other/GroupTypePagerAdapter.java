package haitsu.groupup.other;

/**
 * Created by moham on 21/08/2017.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import haitsu.groupup.fragment.InterestsGroupFragment;
import haitsu.groupup.fragment.EventsGroupFragment;

public class GroupTypePagerAdapter extends FragmentPagerAdapter {

    public GroupTypePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position ==0) {
            return new EventsGroupFragment();
        } else if (position == 1) {
            return new EventsGroupFragment();
        } else return new InterestsGroupFragment();
    }

    @Override
    public int getCount() {
        return 3;
    }
}
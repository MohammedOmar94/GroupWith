package haitsu.groupup.other.Adapters;

/**
 * Created by moham on 21/08/2017.
 */

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import haitsu.groupup.fragment.Groups.EventsGroupFragment;
import haitsu.groupup.fragment.Groups.InterestsGroupFragment;

public class GroupTypePagerAdapter extends FragmentStatePagerAdapter {

    public GroupTypePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new EventsGroupFragment();
        } else if (position == 1) {
            return new InterestsGroupFragment();
        } else return new EventsGroupFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Parcelable saveState()
    {
        return null;
    }
}
package haitsu.groupup.other.Adapters;

/**
 * Created by moham on 21/08/2017.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import haitsu.groupup.fragment.Groups.GroupsJoinedFragment;
import haitsu.groupup.fragment.Groups.MyGroupsFragment;
import haitsu.groupup.fragment.Groups.joinRequestsFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new GroupsJoinedFragment();
        } else if (position == 1) {
            return new MyGroupsFragment();
        } else if (position == 2) {
            return new joinRequestsFragment();
        } else return new GroupsJoinedFragment();
    }

    @Override
    public int getCount() {
        return 3;
    }
}
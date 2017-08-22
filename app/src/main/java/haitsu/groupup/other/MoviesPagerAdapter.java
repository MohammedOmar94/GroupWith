package haitsu.groupup.other;

/**
 * Created by moham on 21/08/2017.
 */

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import haitsu.groupup.fragment.ChatsFragment;
import haitsu.groupup.fragment.GroupsFragment;
import haitsu.groupup.fragment.MoviesGroupFragment;
import haitsu.groupup.fragment.MyGroupsFragment;

public class MoviesPagerAdapter extends FragmentPagerAdapter {

    public MoviesPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position ==0) {
            return new MoviesGroupFragment();
        } else if (position == 1) {
            return new GroupsFragment();
        } else return new GroupsFragment();
    }

    @Override
    public int getCount() {
        return 3;
    }
}
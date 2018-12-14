package com.zbform.penform.fragmentnet;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bilibili.magicasakura.utils.ThemeUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.R;
import com.zbform.penform.fragment.AttachFragment;
import com.zbform.penform.json.SearchAlbumInfo;
import com.zbform.penform.json.SearchArtistInfo;
import com.zbform.penform.json.SearchSongInfo;
import com.zbform.penform.net.BMA;
import com.zbform.penform.net.HttpUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by isaac on 2018/8/2.
 */
public class SearchTabPagerFragment extends AttachFragment {

    private ViewPager viewPager;
    private int page = 0;
    String key;
    private List searchResults = Collections.emptyList();
    FrameLayout frameLayout;
    View contentView;
    ArrayList<SearchSongInfo> songResults = new ArrayList<>();
    ArrayList<SearchArtistInfo> artistResults = new ArrayList<>();
    ArrayList<SearchAlbumInfo> albumResults = new ArrayList<>();

    public static final SearchTabPagerFragment newInstance(int page, String key) {
        SearchTabPagerFragment f = new SearchTabPagerFragment();
        Bundle bdl = new Bundle(1);
        bdl.putInt("page_number", page);
        bdl.putString("key", key);
        f.setArguments(bdl);
        return f;
    }


    private void search(final String key) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                try {

                    JsonObject jsonObject = HttpUtil.getResposeJsonObject(BMA.Search.searchMerge(key, 1, 10)).get("result").getAsJsonObject();
                    JsonObject songObject = jsonObject.get("song_info").getAsJsonObject();
                    JsonArray songArray = songObject.get("song_list").getAsJsonArray();
                    for (JsonElement o : songArray) {
                        SearchSongInfo songInfo = ZBformApplication.gsonInstance().fromJson(o, SearchSongInfo.class);
                        Log.e("songinfo", songInfo.getTitle());
                        songResults.add(songInfo);
                    }

                    JsonObject artistObject = jsonObject.get("artist_info").getAsJsonObject();
                    JsonArray artistArray = artistObject.get("artist_list").getAsJsonArray();
                    for (JsonElement o : artistArray) {
                        SearchArtistInfo artistInfo = ZBformApplication.gsonInstance().fromJson(o, SearchArtistInfo.class);
                        artistResults.add(artistInfo);
                    }

                    JsonObject albumObject = jsonObject.get("album_info").getAsJsonObject();
                    JsonArray albumArray = albumObject.get("album_list").getAsJsonArray();
                    for (JsonElement o : albumArray) {
                        SearchAlbumInfo albumInfo = ZBformApplication.gsonInstance().fromJson(o, SearchAlbumInfo.class);
                        albumResults.add(albumInfo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (mContext == null) {
                    return;
                }
                contentView = LayoutInflater.from(mContext).inflate(R.layout.fragment_net_tab, frameLayout, false);
                viewPager = (ViewPager) contentView.findViewById(R.id.viewpager);
                if (viewPager != null) {
                    Adapter adapter = new Adapter(getChildFragmentManager());
                    adapter.addFragment(SearchMusicFragment.newInstance(songResults), "单曲");
                    adapter.addFragment(SearchArtistFragment.newInstance(artistResults), "歌手");
                    adapter.addFragment(SearchAlbumFragment.newInstance(albumResults), "专辑");
                    viewPager.setAdapter(adapter);
                    viewPager.setOffscreenPageLimit(3);
                }

                TabLayout tabLayout = (TabLayout) contentView.findViewById(R.id.tabs);
                tabLayout.setupWithViewPager(viewPager);
                viewPager.setCurrentItem(page);
                tabLayout.setTabTextColors(R.color.text_color, ThemeUtils.getThemeColorStateList(mContext, R.color.theme_color_primary).getDefaultColor());
                tabLayout.setSelectedTabIndicatorColor(ThemeUtils.getThemeColorStateList(mContext, R.color.theme_color_primary).getDefaultColor());
                frameLayout.removeAllViews();
                frameLayout.addView(contentView);

            }
        }.execute();


    }


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.load_framelayout, container, false);
        frameLayout = (FrameLayout) rootView.findViewById(R.id.loadframe);
        View loadview = LayoutInflater.from(mContext).inflate(R.layout.loading, frameLayout, false);
        frameLayout.addView(loadview);


        if (getArguments() != null) {
            key = getArguments().getString("key");
        }
        search(key);


        return rootView;

    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    static class Adapter extends FragmentStatePagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }
    }
}


package com.zbform.penform.activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bilibili.magicasakura.utils.ThemeUtils;
import com.bumptech.glide.Glide;
import com.zbform.penform.R;
import com.zbform.penform.account.GlideCircleTransform;
import com.zbform.penform.adapter.MenuItemAdapter;
import com.zbform.penform.appintro.FadeAnimation;
import com.zbform.penform.banner.BannerHttpUtils;
import com.zbform.penform.dialog.CardPickerDialog;
import com.zbform.penform.fragment.MainFragment;
import com.zbform.penform.fragmentnet.TabNetPagerFragment;
import com.zbform.penform.handler.HandlerUtil;
import com.zbform.penform.info.URLUtils;
import com.zbform.penform.service.MusicPlayer;
import com.zbform.penform.settings.Activity_Settings;
import com.zbform.penform.util.ThemeHelper;
import com.zbform.penform.update.UpdateAppManager;
import com.zbform.penform.update.UpdateUtils;
import com.zbform.penform.widget.CustomViewPager;
import com.zbform.penform.widget.SplashScreen;
import com.zbform.penform.fragment.TabDevicesFragment;

import java.util.ArrayList;
import java.util.List;

public class ZBformMain extends BaseActivity implements CardPickerDialog.ClickListener {

    private static final String TAG = "ZBformMain";

    private static final String PRE_INTRODUCE_SHOWED = "show_introduce";
    private boolean mSplashShow = false;
    private ActionBar mActionBar;
    private ImageView search;
    private TextView barnet,barmusic, barfriends;
    private ArrayList<TextView> tabs = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private ListView mLvLeftMenu;
    private long time = 0;
    private SplashScreen mSplashScreen;


    public void onCreate(Bundle savedInstanceState) {

        SharedPreferences sp = getSharedPreferences(PRE_INTRODUCE_SHOWED, Context.MODE_PRIVATE);
        if (!sp.getBoolean(PRE_INTRODUCE_SHOWED, false)) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(PRE_INTRODUCE_SHOWED, true);
            editor.apply();
            Intent intent = new Intent(this, FadeAnimation.class); // Call the AppIntro java class
            startActivity(intent);
        } else {
            mSplashShow = true;
            mSplashScreen = new SplashScreen(this);
            mSplashScreen.show(R.drawable.art_login_bg,
                    SplashScreen.SLIDE_LEFT);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawableResource(R.color.background_material_light_1);

        barnet = (TextView) findViewById(R.id.bar_net);
        if (URLUtils.isMusicOn == false){
            barnet.setVisibility(View.GONE);
        }
        barmusic = (TextView) findViewById(R.id.bar_music);
        barfriends = (TextView) findViewById(R.id.bar_friends);
        search = (ImageView) findViewById(R.id.bar_search);
        drawerLayout = (DrawerLayout) findViewById(R.id.fd);
        mLvLeftMenu = (ListView) findViewById(R.id.id_lv_left_menu);

        setToolBar();
        setViewPager();
        setUpDrawer();
        if (mSplashShow) {
            HandlerUtil.getInstance(this).postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSplashScreen.removeSplashScreen();
                }
            }, 3000);
        }
    }

    private void setToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        mActionBar.setTitle("");
    }

    private void setViewPager() {
        if (URLUtils.isMusicOn){
            tabs.add(barnet);
        }
        tabs.add(barmusic);
        tabs.add(barfriends);
        final CustomViewPager customViewPager = (CustomViewPager) findViewById(R.id.main_viewpager);
        customViewPager.setOffscreenPageLimit(3);
        final MainFragment mainFragment = new MainFragment();
        final TabNetPagerFragment tabNetPagerFragment = new TabNetPagerFragment();
        final TabDevicesFragment tabDevicesFragment = new TabDevicesFragment();
        CustomViewPagerAdapter customViewPagerAdapter = new CustomViewPagerAdapter(getSupportFragmentManager());
        if (URLUtils.isMusicOn){
            customViewPagerAdapter.addFragment(tabNetPagerFragment);
        }
        customViewPagerAdapter.addFragment(mainFragment);
        customViewPagerAdapter.addFragment(tabDevicesFragment);
        customViewPager.setAdapter(customViewPagerAdapter);
        customViewPager.setCurrentItem(1);
        barmusic.setSelected(true);
        barmusic.setTextColor(Color.WHITE);
        showQuickControl(false);
        customViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switchTabs(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        barnet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customViewPager.setCurrentItem(0);
            }
        });
        barmusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (URLUtils.isMusicOn){
                    customViewPager.setCurrentItem(1);
                }else {
                    customViewPager.setCurrentItem(0);
                }

            }
        });

        barfriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (URLUtils.isMusicOn){
                    customViewPager.setCurrentItem(2);
                }else {
                    customViewPager.setCurrentItem(1);
                }
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(ZBformMain.this, NetSearchWordsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                ZBformMain.this.startActivity(intent);
            }
        });
    }


    private void setUpDrawer() {
        LayoutInflater inflater = LayoutInflater.from(this);
        mLvLeftMenu.addHeaderView(inflater.inflate(R.layout.nav_header_main, mLvLeftMenu, false));
        //圆形头像
        ImageView avtar = findViewById(R.id.top_bac);
        Glide.with(this)
                .load(R.drawable.load)
                .error(R.drawable.error)
                .transform(new GlideCircleTransform(this))
                .into(avtar);

        mLvLeftMenu.setAdapter(new MenuItemAdapter(this));
        mLvLeftMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        drawerLayout.closeDrawers();
                        break;
                    case 2:
                        CardPickerDialog dialog = new CardPickerDialog();
                        dialog.setClickListener(ZBformMain.this);
                        dialog.show(getSupportFragmentManager(), "theme");
                        drawerLayout.closeDrawers();

                        break;
                    case 3:
//                        TimingFragment fragment3 = new TimingFragment();
//                        fragment3.show(getSupportFragmentManager(), "timing");
                        Intent intent = new Intent(ZBformMain.this,Activity_Settings.class);
                        startActivity(intent);
                        drawerLayout.closeDrawers();

                        break;
                    case 4:
//                        BitSetFragment bfragment = new BitSetFragment();
//                        bfragment.show(getSupportFragmentManager(), "bitset");
                        new CheckUpdateTask().execute(URLUtils.UPDATE_URL_PATH_JSON);
                        drawerLayout.closeDrawers();

                        break;
                    case 5:
                        //侧边栏退出按钮
                        if (MusicPlayer.isPlaying()) {
                            MusicPlayer.playOrPause();
                        }
                        unbindService();
                        finish();
                        drawerLayout.closeDrawers();

                }
            }
        });
    }


    private void switchTabs(int position) {
        for (int i = 0; i < tabs.size(); i++) {
            Log.d(TAG,"tab.size" + tabs.size());
            if (position == i) {
                tabs.get(i).setSelected(true);
                tabs.get(i).setTextColor(Color.WHITE);
                if (i == 1 || i == 2){
                    showQuickControl(false);
                    Log.d(TAG,"not show quick control");
                }else {
                    showQuickControl(true);
                    Log.d(TAG,"show quick control");
                }
            } else {
                tabs.get(i).setSelected(false);
                tabs.get(i).setTextColor(Color.BLACK);
            }
        }
    }

    @Override
    public void onConfirm(int currentTheme) {
        if (ThemeHelper.getTheme(ZBformMain.this) != currentTheme) {
            ThemeHelper.setTheme(ZBformMain.this, currentTheme);
            ThemeUtils.refreshUI(ZBformMain.this, new ThemeUtils.ExtraRefreshable() {
                        @Override
                        public void refreshGlobal(Activity activity) {
                            //for global setting, just do once
                            if (Build.VERSION.SDK_INT >= 21) {
                                final ZBformMain context = ZBformMain.this;
                                ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(null, null, ThemeUtils.getThemeAttrColor(context, android.R.attr.colorPrimary));
                                setTaskDescription(taskDescription);
                                getWindow().setStatusBarColor(ThemeUtils.getColorById(context, R.color.theme_color_primary));
                            }
                        }

                        @Override
                        public void refreshSpecificView(View view) {
                        }
                    }
            );
        }
        changeTheme();
    }

    public class CheckUpdateTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            Log.d(TAG,"check update");
            String downloadUrl;
            String urlString = BannerHttpUtils.sendGetMessage(strings[0], "utf-8");
            Log.d(TAG,"urlString:" + urlString);
            downloadUrl = UpdateUtils.parseUrl(urlString);
            Log.d(TAG,"downloadUrl:" + downloadUrl);
            return downloadUrl;
        }

        @Override
        protected void onPostExecute(String downloadUrl) {
            Log.d(TAG,"downloadUrl" + downloadUrl);
            Toast.makeText(ZBformMain.this,"update available",Toast.LENGTH_SHORT).show();
            download(downloadUrl);
        }

        private void download(final String downloadUrl) {
            new AlertDialog.Builder(ZBformMain.this)
                    .setTitle("提示")
                    .setMessage("版本更新")
                    .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            UpdateAppManager.downloadApk(ZBformMain.this,downloadUrl,"版本升级","SmartBox");

                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
        }
    }
    static class CustomViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();

        public CustomViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment) {
            mFragments.add(fragment);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection
        switch (item.getItemId()) {
            case android.R.id.home: //Menu icon
                drawerLayout.openDrawer(Gravity.LEFT);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSplashScreen.removeSplashScreen();
    }

    /**
     * 双击返回桌面
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - time > 1000)) {
                Toast.makeText(this, "再按一次返回桌面", Toast.LENGTH_SHORT).show();
                time = System.currentTimeMillis();
            } else {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    fragment.onRequestPermissionsResult(requestCode,permissions,grantResults);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        moveTaskToBack(true);
        // System.exit(0);
        // finish();
    }
}
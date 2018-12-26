package com.zbform.penform.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;
import com.lidroid.xutils.bitmap.callback.DefaultBitmapLoadCallBack;
import com.pullrefresh.PtrClassicFrameLayout;
import com.pullrefresh.PtrDefaultHandler;
import com.pullrefresh.PtrFrameLayout;
import com.pullrefresh.loadmore.OnLoadMoreListener;
import com.zbform.penform.R;
import com.zbform.penform.ZBformApplication;
import com.zbform.penform.activity.FormImgActivity;
import com.zbform.penform.json.FormListInfo;
import com.zbform.penform.net.ApiAddress;
import com.zbform.penform.task.FormListTask;
//import com.zbform.penform.view.GridDividerItemDecoration;
import com.zbform.penform.task.TestTask;
import com.zbform.penform.util.BitmapHelp;
import com.zbform.penform.view.GridDividerItemDecorationEx;

import java.util.ArrayList;
import java.util.List;

public class FormListFragment extends BaseFragment implements FormListTask.OnFormTaskListener{

    public static final String TAG = FormListFragment.class.getSimpleName();
    private PtrClassicFrameLayout ptrClassicFrameLayout;
    private RecyclerView mRecyclerView;
    private RecyclerViewAdapter mAdapter;
    private List<String> mData = new ArrayList<>();
    private Handler handler = new Handler();
    private FormListTask mTask;

    private int page = 0;
    private Context mContext;
    private BitmapUtils mBitmapUtils;
    private static final ColorDrawable TRANSPARENT_DRAWABLE = new ColorDrawable(android.R.color.transparent);
    private OnFragmentChangeListener mFragmentChangeCallBack;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        Log.i(TAG, "onAttach, set fragment change listener");
        mFragmentChangeCallBack = (OnFragmentChangeListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fromlist, container, false);
        initView(view);
        initData();
        return view;
    }

    private void initView(View view) {
        ptrClassicFrameLayout = (PtrClassicFrameLayout) view.findViewById(R.id.form_grid_view_frame);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.form_recycler_view);
//        mRecyclerView.addItemDecoration(new GridDividerItemDecorationEx(mContext));
//        mRecyclerView.set
    }

    private void initData() {
//        new TestTask().execute(mContext);
        mBitmapUtils = BitmapHelp.getBitmapUtils(mContext);
        mBitmapUtils.configDefaultLoadingImage(R.drawable.no_banner);
        mBitmapUtils.configDefaultLoadFailedImage(R.drawable.no_banner);
        mBitmapUtils.configDefaultBitmapConfig(Bitmap.Config.RGB_565);
        mTask = new FormListTask();
        mTask.setOnFormTaskListener(this);

        mAdapter = new RecyclerViewAdapter(mContext);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext,2));
        ptrClassicFrameLayout.postDelayed(new Runnable() {

            @Override
            public void run() {
                ptrClassicFrameLayout.autoRefresh(true);
            }
        }, 150);

        ptrClassicFrameLayout.setPtrHandler(new PtrDefaultHandler() {

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mTask.execute(mContext);
//                        ptrClassicFrameLayout.refreshComplete();
//                        ptrClassicFrameLayout.setLoadMoreEnable(true);
                    }
                }, 1500);
            }
        });

        ptrClassicFrameLayout.setOnLoadMoreListener(new OnLoadMoreListener() {

            @Override
            public void loadMore() {
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
//                        ptrClassicFrameLayout.loadMoreComplete(true);
                        Toast.makeText(mContext, "load more complete", Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }
        });
    }

    @Override
    public void onStartGet() {

    }

    @Override
    public void onGetSuccess(List<FormListInfo.Results> results) {
        Log.i("whd","onget success");
        if (mAdapter != null && results!= null){
            Log.i("whd","onget success1="+results.size());
            mAdapter.setData(results);
            mAdapter.notifyDataSetChanged();
        }
        ptrClassicFrameLayout.refreshComplete();
    }

    @Override
    public void onGetFail() {
        ptrClassicFrameLayout.refreshComplete();

    }


    public class RecyclerViewAdapter extends RecyclerView.Adapter<ChildViewHolder> implements
            View.OnClickListener {
        private List<FormListInfo.Results> datas;
        private LayoutInflater inflater;

        public RecyclerViewAdapter(Context context) {
            super();
            inflater = LayoutInflater.from(context);
        }

        public RecyclerViewAdapter(Context context, List<FormListInfo.Results> data) {
            super();
            inflater = LayoutInflater.from(context);
            datas = data;
        }


        @NonNull
        @Override
        public ChildViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.listitem_layout, null);
            ChildViewHolder viewHolder = new ChildViewHolder(view);
            TextView viewRecord = view.findViewById(R.id.view_record);
            viewRecord.setTag(viewHolder);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ChildViewHolder holder, int position) {
            FormListInfo.Results item = datas.get(position);
            holder.formItem = item;
            holder.itemName.setText(item.getName().replace(".pdf",""));
            holder.itemContent.setTag(holder);
            holder.itemContent.setOnClickListener(this);
            holder.itemViewRecord.setOnClickListener(this);

            String url = ApiAddress.getFormImgUri(ZBformApplication.getmLoginUserId(),
                    ZBformApplication.getmLoginUserId(),item.getUuid(),item.getPage());
            mBitmapUtils.display(holder.itemImg,url,new CustomBitmapLoadCallBack(holder));
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemCount() {
            if (datas != null ) {
                return datas.size();
            }else  {
                return 0;
            }
        }

        @Override
        public void onViewRecycled(@NonNull ChildViewHolder holder) {
            super.onViewRecycled(holder);
        }

        public List<FormListInfo.Results> getData() {
            return datas;
        }

        public void setData(List<FormListInfo.Results> results) {
            datas = results;
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.item_content) {
                if (v.getTag() == null) return;
                ChildViewHolder holder = (ChildViewHolder) v.getTag();
                Log.i("whd", "item=" + holder.formItem.getName());
                Intent intent = new Intent(mContext, FormImgActivity.class);
                intent.putExtra("info",holder.formBitmap);
                startActivity(intent);
            } else if (v.getId() == R.id.view_record){
               Log.i(TAG, "onclick view record");
                if(v.getTag() != null) {
                    ChildViewHolder viewHolder = (ChildViewHolder)v.getTag();
                    String formId = viewHolder.formItem.getUuid();
                    Log.i(TAG, "form onclick formId = " + formId);
                    mFragmentChangeCallBack.onRecordListFragmentSelect(formId);
                }

            }
        }
    }

    public class CustomBitmapLoadCallBack extends DefaultBitmapLoadCallBack<ImageView> {
        private final ChildViewHolder holder;

        public CustomBitmapLoadCallBack(ChildViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onLoading(ImageView container, String uri, BitmapDisplayConfig config, long total, long current) {
//            this.holder.imgPb.setProgress((int) (current * 100 / total));
        }

        @Override
        public void onLoadCompleted(ImageView container, String uri, Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
            //super.onLoadCompleted(container, uri, bitmap, config, from);
            //override super, handle pic self
            holder.formBitmap = bitmap;
            fadeInDisplay(container, bitmap);
//            this.holder.imgPb.setProgress(100);
        }

        private void fadeInDisplay(ImageView imageView, Bitmap bitmap) {
            final TransitionDrawable transitionDrawable =
                    new TransitionDrawable(new Drawable[]{
                            TRANSPARENT_DRAWABLE,
                            new BitmapDrawable(imageView.getResources(), bitmap)
                    });
            imageView.setImageDrawable(transitionDrawable);
            transitionDrawable.startTransition(500);
        }
    }

    public class ChildViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        public View itemContent;
        public TextView itemName;
        public TextView itemViewRecord;
        public ImageView itemImg;
        public FormListInfo.Results formItem;
        public Bitmap formBitmap;

        public ChildViewHolder(View view) {
            super(view);
            itemView = view;

            itemContent= itemView.findViewById(R.id.item_content);
            itemName = (TextView) view.findViewById(R.id.form_name);
            itemImg = (ImageView) view.findViewById(R.id.form_img);
            itemViewRecord =  (TextView) view.findViewById(R.id.view_record);
            itemViewRecord.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
            itemViewRecord.getPaint().setAntiAlias(true);//抗锯
        }

    }

}

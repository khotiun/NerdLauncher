package com.bignerdranch.android.nerdlauncher;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by hotun on 23.07.2017.
 */

public class NerdLauncherFragment extends Fragment {
    private static final String TAG = "NerdLauncherFragment";

    private RecyclerView mRecyclerView;

    public static NerdLauncherFragment newInstance() {
        return new NerdLauncherFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_nerd_launcher, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_nerd_launcher_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setupAdapter();
        return v;
    }

    private void setupAdapter() {
        Intent startupIntent = new Intent(Intent.ACTION_MAIN);//в интент фильтре содержит данный экшен
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);//добавлена категория для отбора

        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(startupIntent, 0);

        Collections.sort(activities, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo a, ResolveInfo b) {//сортировка по имени приложения
                PackageManager pm = getActivity().getPackageManager();
                return String.CASE_INSENSITIVE_ORDER.compare(
                        a.loadLabel(pm).toString(),//Метка (label) - имя приложения
                        b.loadLabel(pm).toString()
                        //Метки активностей вместе с другими метаданными содержатся в объектах ResolveInfo, возвращаемых PackageManager.
                );
            }
        });
        Log.i(TAG, "Found " + activities.size() + " activities.");//показывает количество приложений в интент фильтрах
        // которых присутствует данный экшен и категория
        mRecyclerView.setAdapter(new ActivityAdapter(activities));
    }

    private class ActivityHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ResolveInfo mResolveInfo;
        private ImageView mImageView;
        private TextView mNameTextView;

        public ActivityHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            mNameTextView = (TextView) itemView.findViewById(R.id.list_item_app_tv);
            mImageView = (ImageView) itemView.findViewById(R.id.list_item_app_iv);
        }

        public void bindActivity(ResolveInfo resolveInfo) {
            mResolveInfo = resolveInfo;
            PackageManager pm = getActivity().getPackageManager();
            String appName = mResolveInfo.loadLabel(pm).toString();//получаем название приложения
            mNameTextView.setText(appName);
            Drawable appImage = mResolveInfo.loadIcon(pm);
            mImageView.setImageDrawable(appImage);
        }

        @Override
        public void onClick(View v) {//происходит запуск активности выбранного приложения
            ActivityInfo activityInfo = mResolveInfo.activityInfo;
            //получаем имя пакета и имя класса из метаданных и используем их для создания явной активности методом Intent:
            //public Intent setClassName(String packageName, String className)
            Intent i = new Intent(Intent.ACTION_MAIN)
                    .setClassName(activityInfo.applicationInfo.packageName,
                            activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//Чтобы при запуске новой активности запускалась новая задача
            startActivity(i);
        }
    }

    private class ActivityAdapter extends RecyclerView.Adapter<ActivityHolder> {

        private final List<ResolveInfo> mActivities;
        public ActivityAdapter(List<ResolveInfo> activities) {
            mActivities = activities;
        }

        @Override
        public ActivityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_app, parent, false);
            return new ActivityHolder(view);
        }

        @Override
        public void onBindViewHolder(ActivityHolder activityHolder, int position) {
            ResolveInfo resolveInfo = mActivities.get(position);
            activityHolder.bindActivity(resolveInfo);

        }

        @Override
        public int getItemCount() {
            return mActivities.size();
        }
    }
}

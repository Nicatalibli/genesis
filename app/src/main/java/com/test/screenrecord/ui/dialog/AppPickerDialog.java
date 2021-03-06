package com.test.screenrecord.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.test.screenrecord.R;
import com.test.screenrecord.adapter.Apps;
import com.test.screenrecord.adapter.AppsListFragmentAdapter;
import com.test.screenrecord.common.Const;
import com.test.screenrecord.common.PrefUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppPickerDialog extends Dialog implements AppsListFragmentAdapter.OnItemClicked {

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ArrayList<Apps> apps;

    public AppPickerDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.layout_apps_list_preference);
            progressBar = findViewById(R.id.appsProgressBar);
            recyclerView = findViewById(R.id.appsRecyclerView);

            init();
        } catch (Exception e) {
            cancel();
        }
    }

    private void init() {
        try {
            findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });

            RecyclerView.LayoutManager recyclerViewLayoutManager = new LinearLayoutManager(getContext());
            recyclerView.setLayoutManager(recyclerViewLayoutManager);

            // Generate list of installed apps and display in dialog
            new GetApps().execute();
        } catch (Exception e) {
        }
    }

    @Override
    public void onItemClick(int position) {
        Log.d(Const.TAG, "Closing dialog. received result. Pos:" + position);
        // save the selected app's package name to sharedpreference
        PrefUtils.saveStringValue(getContext(), getContext().getString(R.string.preference_app_chooser_key), apps.get(position).getPackageName());
        //dismiss dialog after saving the value
        dismiss();
    }

    class GetApps extends AsyncTask<Void, Void, ArrayList<Apps>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(ArrayList<Apps> apps) {
            super.onPostExecute(apps);
            try {
                // Hide progress bar after the apps list has been loaded
                progressBar.setVisibility(View.GONE);
                AppsListFragmentAdapter recyclerViewAdapter = new AppsListFragmentAdapter(apps);

                // set custom adapter to recycler view
                recyclerView.setAdapter(recyclerViewAdapter);

                // Set recycler view item click listener
                recyclerViewAdapter.setOnClick(AppPickerDialog.this);
            } catch (Exception e) {
            }
        }

        @Override
        protected ArrayList<Apps> doInBackground(Void... voids) {
            try {
                PackageManager pm = getContext().getPackageManager();
                apps = new ArrayList<>();

                // Get list of all installs apps including system apps and apps without any launcher activity
                List<PackageInfo> packages = pm.getInstalledPackages(0);

                for (PackageInfo packageInfo : packages) {

                    // Check if the app has launcher intent set and exclude our own app
                    if (!(getContext().getPackageName().equals(packageInfo.packageName))
                            && !(pm.getLaunchIntentForPackage(packageInfo.packageName) == null)) {

                        Apps app = new Apps(
                                packageInfo.applicationInfo.loadLabel(getContext().getPackageManager()).toString(),
                                packageInfo.packageName,
                                packageInfo.applicationInfo.loadIcon(getContext().getPackageManager())

                        );

                        // Identify the previously selected app
                        app.setSelectedApp(PrefUtils.readStringValue(getContext(), getContext().getString(R.string.preference_app_chooser_key), "none")
                                .equals(packageInfo.packageName)
                        );
                        if (pm.getLaunchIntentForPackage(packageInfo.packageName) == null)
                            Log.d(Const.TAG, packageInfo.packageName);
                        apps.add(app);
                    }
                    Collections.sort(apps);
                }
                return apps;
            } catch (Exception e) {
            }
            return null;
        }
    }
}

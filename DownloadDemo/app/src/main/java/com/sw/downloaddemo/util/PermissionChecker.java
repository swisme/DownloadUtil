package com.sw.downloaddemo.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.sw.downloaddemo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * check permission util
 */
public class PermissionChecker
{

    /**
     * checkPermission function
     *
     * @param activity              the activity uses the permission
     * @param permissions
     * @param requestCode
     * @param dialogMsgForRationale
     * @return hasPermission if false, auto-request permissions
     */
    public static boolean checkPermissions(final Activity activity, String[] permissions
            , final int requestCode, final int dialogMsgForRationale)
    {
        //Android6.0以下默认有权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        final List<String> needList = new ArrayList<>();
        boolean needShowRationale = false;
        int length = permissions.length;

        for (int i = 0; i < length; i++)
        {
            String permisson = permissions[i];
            if (ActivityCompat.checkSelfPermission(activity, permisson)
                    != PackageManager.PERMISSION_GRANTED)
            {
                needList.add(permisson);
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permisson))
                    needShowRationale = true;
            }
        }

        if (needList.size() != 0)
        {
            if (needShowRationale)
            {
                new AlertDialog.Builder(activity).setCancelable(false)
                        .setTitle(R.string.dialog_permission_title)
                        .setMessage(dialogMsgForRationale)
                        .setPositiveButton(R.string.dialog_permission_confirm, new DialogInterface.OnClickListener()
                        {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(activity, needList.toArray(new String[needList.size()]), requestCode);
                            }
                        }).create().show();
                return false;
            }

            ActivityCompat.requestPermissions(activity, needList.toArray(new String[needList.size()]), requestCode);
            return false;
        } else
        {
            return true;
        }
    }

    /**
     * the result of permission-requestion
     *
     * @param activity                the activity that uses the permissions
     * @param permissions
     * @param grantResults            request result
     * @param finishAfterCancelDialog  finish activity if refused
     * @param dialogMsgForNerverAsk   don't mention anymore?
     * @return
     */
    public static boolean[] onRequestPermissionsResult(final Activity activity, String[] permissions, int[] grantResults,
                                                       final boolean finishAfterCancelDialog, int dialogMsgForNerverAsk)
    {
        boolean result = true;
        boolean isNerverAsk = false;

        int length = grantResults.length;
        for (int i = 0; i < length; i++)
        {
            String permission = permissions[i];
            int grandResult = grantResults[i];
            if (grandResult == PackageManager.PERMISSION_DENIED)
            {
                result = false;
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
                    isNerverAsk = true;
            }
        }

        if (!result)
        {
            Toast.makeText(activity, R.string.toast_permission_denied, Toast.LENGTH_SHORT).show();
            if (isNerverAsk)
            {
                //处理NerverAsk
                new AlertDialog.Builder(activity).setCancelable(false)
                        .setTitle(R.string.dialog_permission_title)
                        .setMessage(dialogMsgForNerverAsk)
                        .setNegativeButton(R.string.dialog_permission_nerver_ask_cancel, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                                if (finishAfterCancelDialog)
                                    activity.finish();
                            }
                        })
                        .setPositiveButton(R.string.dialog_permission_nerver_ask_confirm, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);
                                activity.startActivity(intent);
                                activity.finish();
                            }
                        }).create().show();
            }
        }

        return new boolean[]{result, isNerverAsk};
    }

    public static boolean hasPermissions(@NonNull Context context, @NonNull String... perms) {
        if(Build.VERSION.SDK_INT < 23) {
            return true;
        } else {
            String[] var2 = perms;
            int var3 = perms.length;
            for(int var4 = 0; var4 < var3; ++var4) {
                String perm = var2[var4];
                boolean hasPerm = ContextCompat.checkSelfPermission(context, perm) == 0;
                if(!hasPerm) {
                    return false;
                }
            }
            return true;
        }
    }
}

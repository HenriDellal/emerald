package ru.henridellal.emerald;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.Toast;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class InstallShortcutReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent shortcutIntent = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
		if (shortcutIntent.getAction() == null) {
			shortcutIntent.setAction(Intent.ACTION_VIEW);
		}
		String name = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
		String uri = shortcutIntent.toUri(0);
		Bitmap icon = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
		
		String packageName, resourceName;
		BaseData shortcutData = null;
        if (icon != null) {
        	shortcutData = new ShortcutData(name, uri);
        } else {
            ShortcutIconResource resource = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);

            if (resource != null) {
                packageName = resource.packageName;
                resourceName = resource.resourceName;
                PackageManager pm = context.getPackageManager();
                try {
                Resources res = pm.getResourcesForApplication(packageName);
                int id = res.getIdentifier(resourceName, "png", packageName);
                Drawable d = res.getDrawable(id);
                icon = ((BitmapDrawable) d).getBitmap();
                } catch (Exception e) {
                	Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
                }
                shortcutData = new ShortcutData(name, uri, packageName, resourceName);
            } else {
                //invalid shortcut
                return;
            }
        }
        ArrayList<BaseData> shortcuts = new ArrayList<BaseData>();
        MyCache.read(context, "shortcuts", shortcuts);
        if (!shortcuts.contains(shortcutData)) {
        	shortcuts.add(shortcutData);
        	MyCache.write(context, "shortcuts", shortcuts);
        	if (icon != null) {
        		saveIcon(context, icon, shortcutData);
        	}
        }
	}
	private void saveIcon(Context context, Bitmap icon, BaseData shortcutData) {
		File file = null;
		try {
			file = MyCache.getIconFile(context, shortcutData);
			FileOutputStream out = new FileOutputStream(file);
			icon.compress(CompressFormat.PNG, 100, out);
			out.close();
		} catch (Exception e) {
		Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
			file.delete();
		}
	}
}

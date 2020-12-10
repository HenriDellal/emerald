package ru.henridellal.emerald;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.Toast;
//import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class InstallShortcutReceiver extends BroadcastReceiver {
	
	@Override
	@SuppressWarnings("deprecation")
	public void onReceive(Context context, Intent intent) {
		Intent shortcutIntent = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
		String uri = shortcutIntent.toUri(0);
		if (DatabaseHelper.hasShortcut(context, uri) || isAppLink(uri)) {
			return;
		}
		if (shortcutIntent.getAction() == null) {
			shortcutIntent.setAction(Intent.ACTION_VIEW);
		}
		String name = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
		Bitmap icon = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
		
		String packageName = null;
		String resourceName = null;
		ContentValues values = new ContentValues();
        if (icon == null) {
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
            } else {
                //invalid shortcut
                return;
            }
        }
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		icon.compress(CompressFormat.PNG, 100, out);
		values.put("icon", out.toString());
		saveIcon(context, uri, icon);
		values.put("name", name);
		values.put("uri", uri);
		values.put("package", packageName);
		values.put("resource", resourceName);
		values.put("categories", "");
		DatabaseHelper.insertShortcut(context, values);
	}
	
	private boolean isAppLink(String uri) {
		try {
			Intent intent = Intent.parseUri(uri, 0);
            if (intent.getCategories() != null && intent.getCategories().contains(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(intent.getAction())) {
            	return true;
            }
		} catch (Exception ignored) {}
		return false;
	}
	
	private void saveIcon(Context context, String uri, Bitmap icon) {
		File file = null;
		try {
			file = MyCache.getShortcutIconFile(context, uri);
			FileOutputStream out = new FileOutputStream(file);
			icon.compress(CompressFormat.PNG, 100, out);
			out.close();
		} catch (Exception e) {
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
			file.delete();
		}
	}
}

package ru.henridellal.emerald;

import org.xmlpull.v1.XmlPullParser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.preference.PreferenceManager;
//import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class IconPackManager {
	Context context;
	String iconPackName;
	private float factor = 1.0f;
	ArrayList<Bitmap> iconBacks;
	Bitmap iconMask, iconUpon;
	boolean transformDrawable = true;
	Map<String, String> iconsData;
	private Resources iconPackRes;
	public Resources getResources() {
		return iconPackRes;
	}
	public IconPackManager(Context context, String iconPack) {
		this.context = context;
		iconsData = new HashMap<String, String>();
		setIconPack(iconPack);
	}
	public Map<String, String> getIcons() {
		return iconsData;
	}
	public String getIconPackName() {
		return iconPackName;
	}
	public void setIconPack(String iconPackName) {
		this.iconPackName = iconPackName;
		setIcons();
	}
	private Bitmap loadBitmap(String drawableName) {
		
		int id = iconPackRes.getIdentifier(drawableName, "drawable", iconPackName);
		if (id > 0)
		/*
		return getActivities(null, Process.myUserHandle()).get(0).get();
		
		
		*/
			return ((BitmapDrawable)iconPackRes.getDrawable(id)).getBitmap();
		else
			return null;
	}
	/*public Bitmap resizeBitmap(Bitmap b) {
		int w = b.getWidth();
		int h = b.getHeight();
		Bitmap base = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(base);
		Bitmap bitmap = Bitmap.createScaledBitmap(b, (int)(w*factor), (int)(h*factor), false);
		base.eraseColor(0);
		//canvas.drawBitmap(b, w*(1-factor)/2, h*(1-factor)/2, new Paint(Paint.ANTI_ALIAS_FLAG));
		canvas.drawBitmap(b, 0, 0, new Paint(Paint.ANTI_ALIAS_FLAG));
		return base;
	}*/
	public Bitmap transformDrawable(Drawable d) {
		Bitmap b = ((BitmapDrawable) d).getBitmap();
		if ((iconBacks == null && iconMask == null && iconUpon == null && factor == 1.f) || !transformDrawable)
		{
			return b;
		}
		int w, h;
		Paint paint;
		if (iconBacks != null) {
			if (iconBacks.size() > 0) {
				w = iconBacks.get(0).getWidth();
				h = iconBacks.get(0).getHeight();
			} else {
				w = b.getWidth();
				h = b.getHeight();
			}
		} else {
			w = b.getWidth();
			h = b.getHeight();
		}
		Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(result);
		//Paint paint = new Paint();
		if (iconBacks != null) {
			if (iconBacks.size() > 0) {
				canvas.drawBitmap(iconBacks.get((int)(Math.random()*iconBacks.size())), 0, 0, null);
			}
		}
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int)(w*factor), (int)(h*factor), false);
		canvas.drawBitmap(scaledBitmap, w*(1-factor)/2, h*(1-factor)/2, paint);
		if (iconMask != null) {
			paint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
			canvas.drawBitmap(iconMask, 0.f, 0.f, paint);
			paint.setXfermode(null);
		}
		if (iconUpon != null) {
			canvas.drawBitmap(iconUpon, 0, 0, null);
		}
		return result;
	}
	public Bitmap getBitmap(String component) {
		if (iconsData.containsKey(component)) {
			String drawableName = iconsData.get(component);
			return loadBitmap(drawableName);
		} else {
			return null;
		}
	}
	public Map<String, String> getIconPacks() {
		Map<String, String> iconPacks = new HashMap<String, String>();
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> iconPacksInfo = pm.queryIntentActivities(new Intent("org.adw.launcher.THEMES"), PackageManager.GET_META_DATA);
		String iconPackPackage = null;
		String iconPackName = null;
		for (ResolveInfo info: iconPacksInfo) {
			iconPackPackage = info.activityInfo.packageName;
			ApplicationInfo ai = null;
			try {
				ai = pm.getApplicationInfo(iconPackPackage, PackageManager.GET_META_DATA);
				iconPackName = pm.getApplicationLabel(ai).toString();
			} catch (PackageManager.NameNotFoundException e) {}
			iconPacks.put(iconPackName, iconPackPackage);
		}
		return iconPacks;
	}
	public void setIcons() {
		iconsData = new HashMap<String, String>();
		iconBacks = null;
		iconMask = null;
		iconUpon = null;
		factor = 1.0f;
		iconPackRes = null;
		if (iconPackName.equals("default")) {
			return;
		}
		transformDrawable = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Options.PREF_TRANSFORM_DRAWABLE, true);
		String component = null;
		String drawable = null;
		PackageManager pm = context.getPackageManager();
		iconBacks = new ArrayList<Bitmap>();
		try {
			iconPackRes = pm.getResourcesForApplication(iconPackName);
		} catch (PackageManager.NameNotFoundException nameNotFound) {
		//	iconsData.put("error","name not found");
		//	Log.e("ipm", nameNotFound.toString());
		}
		try {
			int id = iconPackRes.getIdentifier("appfilter", "xml", iconPackName);
			XmlPullParser parser = iconPackRes.getXml(id);
			int parserEvent = parser.getEventType();
			
			while (parserEvent != XmlPullParser.END_DOCUMENT) {
				if (parserEvent == XmlPullParser.START_TAG){
					if (parser.getName().equals("item")) {
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							if (parser.getAttributeName(i).equals("component")) {
								component = parser.getAttributeValue(i);
								int c = component.indexOf("{");
								component = component.substring(c+1, component.length()-1);
							} else if (parser.getAttributeName(i).equals("drawable")) {
								drawable = parser.getAttributeValue(i);
							}
						}
						iconsData.put(component, drawable);
					} else if (parser.getName().equals("iconback")) {
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							iconBacks.add(loadBitmap(parser.getAttributeValue(i)));
						}
					} else if (parser.getName().equals("iconmask")) {
						if (parser.getAttributeCount() > 0 && parser.getAttributeName(0).equals("iconmask")) {
							iconMask = loadBitmap(parser.getAttributeValue(0));
						}
					} else if (parser.getName().equals("iconupon")) {
						if (parser.getAttributeCount() > 0 && parser.getAttributeName(0).equals("iconupon")) {
							iconUpon = loadBitmap(parser.getAttributeValue(0));
						}
					} else if (parser.getName().equals("scale")) {
						if (parser.getAttributeCount() > 0 && parser.getAttributeName(0).equals("factor")) {
							factor = Float.valueOf(parser.getAttributeValue(0));
						}
					}
				}
				parserEvent = parser.next();
			}
			
		} catch (Exception e) {
			//iconsData.put("error", e.toString());
		}
		/*if (res != null) {
			return;
		}*/
	}
}

package ru.henridellal.emerald;

import org.xmlpull.v1.XmlPullParser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Canvas;
import android.graphics.PorterDuffXfermode;
import android.graphics.PorterDuff.Mode;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.widget.Toast;
//import android.util.Log;

/* Imports required by Oreo adaptive icons
 * Comment them to compile with older aapt versions
*/
import android.os.Build;
import android.graphics.drawable.AdaptiveIconDrawable;


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class IconPackManager {
	private Context context;
	private String iconPackName;
	private float factor = 1.0f;
	private ArrayList<Bitmap> iconBacks;
	private Bitmap iconMask, iconUpon;
	private boolean transformDrawable = true;
	private Map<String, String> iconsData;
	private Resources iconPackRes;
	public Resources getResources() {
		return iconPackRes;
	}
	public IconPackManager(Context context, String iconPack) {
		this.context = context;
		setIconPack(iconPack);
	}
	public Map<String, String> getIcons() {
		return iconsData;
	}
	public String getIconPackName() {
		return iconPackName;
	}
	public void setIconPack(String iconPackName) {
		if (getIconPacks().containsValue(iconPackName) || "default".equals(iconPackName)) {
			this.iconPackName = iconPackName;
		} else {
			this.iconPackName = "default";
			Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
			editor.putString(Keys.ICON_PACK, this.iconPackName).commit();
		}
		setIcons();
	}
	private Bitmap loadBitmap(String drawableName) {
		int id = iconPackRes.getIdentifier(drawableName, "drawable", iconPackName);
		if (id > 0)
			return ((BitmapDrawable)iconPackRes.getDrawable(id)).getBitmap();
		else
			return null;
	}
	/* Returns Bitmap for default icon pack.
	 * Includes code for retrieving Oreo+ adaptive icons.
	 */
	public Bitmap getDefaultBitmap(Drawable d) {
		if (d instanceof BitmapDrawable) {
			return ((BitmapDrawable) d).getBitmap();
		} else if ((Build.VERSION.SDK_INT >= 26)
			&& (d instanceof AdaptiveIconDrawable)) {
				AdaptiveIconDrawable icon = ((AdaptiveIconDrawable)d);
				int w = icon.getIntrinsicWidth();
				int h = icon.getIntrinsicHeight();
				Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
				Canvas canvas = new Canvas(result);
				icon.setBounds(0, 0, w, h);
				icon.draw(canvas);
				return result;
		}
		float density = context.getResources().getDisplayMetrics().density;
		int defaultWidth = (int)(48* density);
		int defaultHeight = (int)(48* density);
		return Bitmap.createBitmap(defaultWidth, defaultHeight, Bitmap.Config.ARGB_8888);
	}
	/* transforms an Drawable object to Bitmap
	 * and adds effects from icon pack
	 */
	public Bitmap transformDrawable(Drawable d) {
		Bitmap b = getDefaultBitmap(d);
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
		if (iconBacks != null) {
			if (iconBacks.size() > 0) {
				canvas.drawBitmap(iconBacks.get((int)(Math.random()*iconBacks.size()-1)), 0, 0, null);
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
	/* used by Options class to list all icon packs available*/
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
	//sets icon from cache in ImageView
	public static void setIcon(Context c, ImageView img, BaseData a) {
		File iconFile;
		iconFile = MyCache.getOldCustomIconFile(c, a);
		if (!iconFile.exists()) {
			iconFile = MyCache.getIconFile(c, a);
		}
		if (iconFile.exists()){
			try {
				img.setImageDrawable(Drawable.createFromStream(
					new FileInputStream(iconFile), null));
			} catch (Exception e) {
				img.setImageResource(android.R.drawable.sym_def_app_icon);
			} finally {
				return;
			}
		} else {
			img.setImageResource(android.R.drawable.sym_def_app_icon);
		}
	}
	/* sets icons data */
	public void setIcons() {
		iconsData = new HashMap<String, String>();
		iconBacks = null;
		iconMask = null;
		iconUpon = null;
		factor = 1.0f;
		iconPackRes = null;
		if ("default".equals(iconPackName)) {
			return;
		}
		transformDrawable = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Keys.TRANSFORM_DRAWABLE, true);
		String component = null;
		String drawable = null;
		PackageManager pm = context.getPackageManager();
		iconBacks = new ArrayList<Bitmap>();
		try {
			iconPackRes = pm.getResourcesForApplication(iconPackName);
		} catch (PackageManager.NameNotFoundException nameNotFound) {
		
		}
		try {
			int id = iconPackRes.getIdentifier("appfilter", "xml", iconPackName);
			XmlPullParser parser = iconPackRes.getXml(id);
			int parserEvent = parser.getEventType();
			
			while (parserEvent != XmlPullParser.END_DOCUMENT) {
				if (parserEvent == XmlPullParser.START_TAG){
					final String parserName = parser.getName();
					if ("item".equals(parserName)) {
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							if ("component".equals(parser.getAttributeName(i))) {
								component = parser.getAttributeValue(i);
								int c = component.indexOf("{");
								component = component.substring(c+1, component.length()-1);
							} else if ("drawable".equals(parser.getAttributeName(i))) {
								drawable = parser.getAttributeValue(i);
							}
						}
						iconsData.put(component, drawable);
					} else if ("iconback".equals(parserName)) {
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							iconBacks.add(loadBitmap(parser.getAttributeValue(i)));
						}
					} else if ("iconmask".equals(parserName) && parser.getAttributeCount() > 0 && "iconmask".equals(parser.getAttributeName(0))) {
						iconMask = loadBitmap(parser.getAttributeValue(0));
					} else if ("iconupon".equals(parserName) && parser.getAttributeCount() > 0 && "iconupon".equals(parser.getAttributeName(0))) {
						iconUpon = loadBitmap(parser.getAttributeValue(0));
					} else if ("scale".equals(parserName) && "factor".equals(parser.getAttributeName(0))) {
						factor = Float.valueOf(parser.getAttributeValue(0));
					}
				}
				parserEvent = parser.next();
			}
			
		} catch (Exception e) {
			
		}
	}
}

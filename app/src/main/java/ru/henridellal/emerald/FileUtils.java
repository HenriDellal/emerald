package ru.henridellal.emerald;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class FileUtils {
	public static void copy(Context context, File source, File dest) {
		try {
			FileInputStream fis = new FileInputStream(source);
			FileOutputStream fos = new FileOutputStream(dest);
			FileChannel src = fis.getChannel();
			FileChannel dst = fos.getChannel();
			dst.transferFrom(src, 0, src.size());
			src.close();
			dst.close();
			fis.close();
			fos.close();
		} catch (Exception e) {
			Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
		}
	}
	public static void move(Context context, File source, File dest) {
		copy(context, source, dest);
		source.delete();
	}
}

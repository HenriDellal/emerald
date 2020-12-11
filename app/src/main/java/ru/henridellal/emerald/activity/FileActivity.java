package ru.henridellal.emerald.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.view.LayoutInflater;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;
import java.io.FileFilter;

import ru.henridellal.emerald.R;

public class FileActivity extends Activity {
	public static int GET_IMAGE = 1;
	public static String RESULT_PATH = "FileActivity.path";
	
	private File curDirectory;
	
	private File[] fileArray = null;
	private ArrayList<File> files = new ArrayList<File>();
	private FileListAdapter adapter=null;
	protected void setCurDirectory(File f) {
		curDirectory = f;
	}
	protected File getFile(int position) {
		return files.get(position);
	}
	protected void setFileList(File directory) {
		fileArray = directory.listFiles(new FileFilter() {
			private boolean isExtensionValid(File f) {
				String filePath = f.getPath();
				int i = filePath.lastIndexOf('.');
				if (i > 0 && i < filePath.length()-1) {
					return filePath.substring(i+1).equals("png");
				}
				return false;
			}
			@Override
			public boolean accept(File f) {
				if (f.isHidden()) {
					return false;
				}
				else if (f.isDirectory() || isExtensionValid(f)) {
					return true;
				} else {
					return false;
				}
			}
		});
		files.clear();
		Collections.addAll(files, fileArray);
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_loader);
		curDirectory = Environment.getExternalStorageDirectory();
		setFileList(curDirectory);
        ListView fileList = (ListView)findViewById(R.id.file_list);
        adapter = new FileListAdapter(this, R.layout.file_list_item);
        fileList.setAdapter(adapter);
    	fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
    		public void onItemClick(AdapterView parent, View v, int position, long id) {
    			File chosenFile = getFile(position);
    			if (chosenFile.isDirectory()) {
    				setCurDirectory(chosenFile);
    				setFileList(curDirectory);
    				((FileListAdapter)parent.getAdapter()).sort();
    			} else {
    				try {
    					Intent data = new Intent();
    					data.putExtra(RESULT_PATH, chosenFile.getPath());
    					setResult(RESULT_OK, data);
    					finish();
    				} catch (Exception e) {
    					Toast.makeText(FileActivity.this, ""+e, Toast.LENGTH_LONG).show();
    				}
    			}
    		}
    	});
    }

    @Override
    public void onBackPressed() {
    	if (curDirectory.equals(Environment.getExternalStorageDirectory())) {
    		finish();
		} else {
	    	try {
	    		setFileList(new File(curDirectory.getParent()));
	    		curDirectory = new File(curDirectory.getParent());
	    		adapter.sort();
	    	} catch (Exception e) {
	    		Toast.makeText(this, ""+e, Toast.LENGTH_LONG).show();
	    	}
    	}
    }
    
    public boolean hasExtension(File file, String extension) {
    	String filePath = file.getPath();
    	int i = filePath.lastIndexOf('.');
		if (i > 0 && i < filePath.length()-1) {
			return filePath.substring(i+1).equals(extension);
		}
		return false;
    }

    public class FileListAdapter extends BaseAdapter {
    	int resource;
    	Context context;

    	public void sort() {
    		Collections.sort(files, new Comparator<File>() {
    			@Override
    			public int compare(File first, File second) {
    			//additional fix
    				return first.getName().toLowerCase().compareTo(second.getName().toLowerCase());
    			}
    		});
    		notifyDataSetChanged();
    	}

    	public FileListAdapter(Context context, int resource) {
    		super();
    		this.resource = resource;
    		this.context = context;
    		sort();
    	}

    	private Bitmap getPreview(File file) {
    		if (file.isDirectory()) {
    			return ((BitmapDrawable)context.getResources().getDrawable(R.drawable.directory)).getBitmap();
    		} else if (hasExtension(file, "png")){
    			return BitmapFactory.decodeFile(file.getPath());
    		} else {
    			return ((BitmapDrawable)context.getResources().getDrawable(R.drawable.document)).getBitmap();
    		}
    	}

    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		View v;
    		if (convertView == null) {
    			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			v = inflater.inflate(resource, parent, false);
    		} else {
    			v = convertView;
    		}
    		((TextView) v.findViewById(R.id.file_name)).setText(files.get(position).getName());
    		((ImageView) v.findViewById(R.id.file_image)).setImageBitmap(getPreview(files.get(position)));
    		
    		return v;
    	}

    	@Override
		public int getCount() {
			return files.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}
	
		@Override
		public long getItemId(int position) {
			return 0;
		}
    }
}

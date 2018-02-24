package ru.henridellal.emerald;

import android.app.Activity;
import android.content.Context;
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

public class FileActivity extends Activity {
	private File curDirectory;
	private int teamType;
	
	private File[] fileArray = null;
	private ArrayList<File> files = new ArrayList<File>();
	private FileListAdapter adapter=null;
	protected void setCurDirectory(File f) {
		curDirectory = f;
	}
	protected void setFile(int position, File f) {
		files.set(position, f);
	}
	protected File getFile(int position) {
		return files.get(position);
	}
	protected void setFileList(File directory) {
		fileArray = directory.listFiles(new FileFilter() {
			private boolean isExtensionValid(File f) {
				/*String filePath = f.getPath();
				int i = filePath.lastIndexOf('.');
				if (i > 0 && i < filePath.length()-1) {
					return filePath.substring(i+1).equals("xls");
				}
				return false;*/
				return true;
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
	/*protected void sortFileList() {
		Collections.sort(new ArrayList<File>(files), new Comparator<File>() {
			public int compare(File first, File second) {
				return first.getName().compareTo(second.getName());
			}
		});
	}*/
	@Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_loader);
        /*Intent intent = getIntent();
        teamType = intent.getIntExtra(MainActivity.TEAM_TYPE, 0);*/
		//curDirectory = Environment.getExternalStorageDirectory();
		curDirectory = getCacheDir();
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
    	}
    	else {
	    	try {
	    		setFileList(new File(curDirectory.getParent()));
	    		curDirectory = new File(curDirectory.getParent());
	    		adapter.sort();
	    	} catch (Exception e) {
	    		Toast.makeText(this, ""+e, Toast.LENGTH_LONG).show();
	    	}
    	}
    }
    
    public boolean hasExtention(File file, String extension) {
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
    		} else if (hasExtention(file, "png")){
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

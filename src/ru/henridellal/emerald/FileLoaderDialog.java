package ru.henridellal.emerald;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.BaseAdapter;
import android.widget.AdapterView;
//required by permissions checker
import android.Manifest;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;
import java.io.FileFilter;

import android.preference.DialogPreference;

public class FileLoaderDialog extends ListView
	//implements DialogInterface.OnClickListener
	{
	File curDirectory;
	int mode;
	public int getMode() {
		return mode;
	}
	private FileListAdapter getListAdapter() {
		return adapter;
	}
	ListView fileList;
	File[] fileArray = null;
	ArrayList<File> files = new ArrayList<File>();
	FileListAdapter adapter;
	protected void setCurDirectory(File f) {
		curDirectory = f;
	}
	protected void setFile(int position, File f) {
		files.set(position, f);
	}
	protected File getFile(int position) {
		return files.get(position);
	}
	public ListView getFileListView(){
		return fileList;
	}
	protected void setFileList(File directory) {
		fileArray = directory.listFiles(new FileFilter() {
			private boolean isExtensionValid(File f) {
				String filePath = f.getPath();
				int i = filePath.lastIndexOf('.');
				return (i > 0 && i < filePath.length()-1) ? filePath.substring(i+1).equals("txt") : false;
			}
			@Override
			public boolean accept(File f) {
				return ((!f.isHidden()) && (f.isDirectory() || isExtensionValid(f)));
			}
		});
		files.clear();
		Collections.addAll(files, fileArray);
	}
	
    public FileLoaderDialog(DialogPreference preference, Context context, int mode)
	{
		super(context);
		// request runtime permissions (Marshmallow+)
        if (Build.VERSION.SDK_INT >= 23) {
        	if ((context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
        		|| (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)) {
        		((Options)context).requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        	}
        }
		curDirectory = Environment.getExternalStorageDirectory();
		setFileList(curDirectory);
		this.mode = mode;
		if (this.mode == 0) {
			Button button = new Button(context);
			button.setText(context.getResources().getString(R.string.save_here));
			button.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v) {
					File chosenFile = new File(curDirectory, "preferences.txt");
					((Options)getContext()).backupPrefs(chosenFile);
				}
			});
			addHeaderView(button);
		}
        adapter = new FileListAdapter(context, R.layout.file_list_item);
        setAdapter(adapter);
    	setOnItemClickListener(new AdapterView.OnItemClickListener() {
    		public void onItemClick(AdapterView parent, View v, int position, long id) {
    			File chosenFile = getFile(getMode() == 0 ? position-1: position);
    			if (chosenFile.isDirectory()) {
    				setCurDirectory(chosenFile);
    				setFileList(curDirectory);
    				((FileLoaderDialog)parent).getListAdapter().sort();
    			} else {
    				if (getMode() == 0) {
    					((Options)getContext()).backupPrefs(chosenFile);
    				} else {
    					((Options)getContext()).restorePrefs(chosenFile);
    				}
    			}
    		}
    	});
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
    		if (curDirectory.equals(Environment.getExternalStorageDirectory())) {
	    		return false;
	    	}
	    	else {
		    	try {
		    		setFileList(new File(curDirectory.getParent()));
		    		curDirectory = new File(curDirectory.getParent());
		    		adapter.sort();
		    	} catch (Exception e) {
		    		Toast.makeText(getContext(), ""+e, Toast.LENGTH_LONG).show();
		    	}
	    	}
    		return true;
    	}
    	return super.dispatchKeyEvent(event);
    }
    
    public class FileListAdapter extends BaseAdapter {
    	int resource;
    	Context context;
    	//View.OnClickListener onClickListener;
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
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		View v = null;
    		if (convertView == null) {
    			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    			v = inflater.inflate(resource, parent, false);
    		} else {
    			v = convertView;
    		}
    		((TextView) v.findViewById(R.id.file_name)).setText(files.get(position).getName());
    		((ImageView) v.findViewById(R.id.file_image)).setImageResource(files.get(position).isDirectory() ? R.drawable.directory : R.drawable.document);
    		
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

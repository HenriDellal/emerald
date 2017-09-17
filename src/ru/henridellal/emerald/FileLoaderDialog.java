package ru.henridellal.emerald;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;
import java.io.FileFilter;

public class FileLoaderDialog extends ListView
	//implements DialogInterface.OnClickListener
	{
	File curDirectory;
	int mode;
	public int getMode() {
		return mode;
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
				if (i > 0 && i < filePath.length()-1) {
					return filePath.substring(i+1).equals("txt");
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
	/*protected void sortFileList() {
		Collections.sort(new ArrayList<File>(files), new Comparator<File>() {
			public int compare(File first, File second) {
				return first.getName().compareTo(second.getName());
			}
		});
	}*/
    public FileLoaderDialog(Context context, int mode)
	{
		super(context);
        //fileList = (ListView)findViewById(R.id.file_list);
		curDirectory = Environment.getExternalStorageDirectory();
		setFileList(curDirectory);
		this.mode = mode;
		if (this.mode == 0) {
			Button button = new Button(context);
			button.setText("Save in this folder");
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
    			File chosenFile = getFile(position);
    			if (chosenFile.isDirectory()) {
    				setCurDirectory(chosenFile);
    				setFileList(curDirectory);
    				((FileListAdapter)parent.getAdapter()).sort();
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

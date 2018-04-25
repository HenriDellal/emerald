package ru.henridellal.emerald;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;

public class CategoryManagerActivity extends Activity{
	private CategoryManager cm;
	private ArrayList<String> categories, categoriesNames;
	private CategoryAdapter adapter;
	private ListView catListView;
	
	private static final int COMMAND_HIDE = 0;
	private static final int COMMAND_SET_HOME = 1;
	private static final int COMMAND_CLEAR = 2;
	private static final int COMMAND_DELETE = 3;
	private static final int COMMAND_RENAME = 4;
	private static final int COMMAND_EDIT = 5;
	
	public void updateCategoriesList() {
		categories = cm.getCategories();
		categoriesNames = new ArrayList<String>(categories.size());
		for (String category: categories) {
			categoriesNames.add(cm.getCategory(category).getRepresentName(this));
		}
	}
	
	@Override
	protected void onDestroy() {
		cm = null;
		super.onDestroy();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.categorymanager);
		cm = LauncherApp.getInstance().getCategoryManager();
		updateCategoriesList();
		adapter = new CategoryAdapter(this, 
			android.R.layout.simple_list_item_1, categoriesNames);
		catListView = (ListView)findViewById(R.id.categoryList);
		catListView.setAdapter(adapter);
		catListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int catNum, long id) {
				String chosenCat = categories.get(catNum);
				buildMenu(chosenCat);
			}

		});
		((Button)findViewById(R.id.addCategoryButton)).setOnClickListener(
			new View.OnClickListener(){
				public void onClick(View v) {
					newCategory();
				}
			});
	}
	
	private void buildMenu(final String category) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(cm.getCategory(category).getRepresentName(this));
		final boolean isHidden = cm.getCategory(category).isHidden();
		ArrayList<String> commands = new ArrayList<String>();
		final ArrayList<Integer> commandCodes = new ArrayList<Integer>();
		if (!category.equals(CategoryManager.HIDDEN) && !category.equals(cm.getHome())) {
			commands.add(isHidden ?
				getResources().getString(R.string.unhide)
				: getResources().getString(R.string.hide));
			commandCodes.add(COMMAND_HIDE);
		}
		if (!category.equals(CategoryManager.HIDDEN) && !category.equals(cm.getHome()) && !isHidden) {
			commands.add(getResources().getString(R.string.setHome));
			commandCodes.add(COMMAND_SET_HOME);
		}
		if (cm.isEditable(category)) {
			commands.add(getResources().getString(R.string.editAppList));
			commandCodes.add(COMMAND_EDIT);
			commands.add(getResources().getString(R.string.clear));
			commandCodes.add(COMMAND_CLEAR);
			if (cm.isCustom(category)) {
				commands.add(getResources().getString(R.string.rename));
				commandCodes.add(COMMAND_RENAME);
				commands.add(getResources().getString(R.string.delete));
				commandCodes.add(COMMAND_DELETE);
			}
		}
		if (commandCodes.size() == 0) {
			return;
		}
		builder.setAdapter(new ArrayAdapter<String>(this, 
			android.R.layout.simple_list_item_1, commands),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface di, int which) {
					di.dismiss();
					switch (commandCodes.get(which)) {
						case COMMAND_SET_HOME:
							cm.setHome(category);
							break;
						case COMMAND_RENAME:
							renameCategory(category);
							break;
						case COMMAND_EDIT:
							appListEditor(category);
							break;
						case COMMAND_CLEAR:
							clearCategory(category);
							break;
						case COMMAND_DELETE:
							deleteCategory(category);
							break;
						case COMMAND_HIDE:
							if (isHidden) {
								cm.unhide(category);
							} else {
								cm.hide(category);
							}
							break;
					}
					
				}
			});
		builder.create().show();
	}
	private void deleteCategory(final String category) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(cm.getCategory(category).getRepresentName(this));
		builder.setMessage(getResources().getString(R.string.delete_category_question));
		builder.setPositiveButton(android.R.string.yes, 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					cm.removeCategory(category);
					updateCategoriesList();
					adapter.update(categoriesNames);
				}
			}).setNegativeButton(android.R.string.no,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					
				}
			}).setCancelable(true);
		builder.create().show();
	}
	private void clearCategory(final String category) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(cm.getCategory(category).getRepresentName(this));
		builder.setMessage(getResources().getString(R.string.clear_category_question));
		builder.setPositiveButton(android.R.string.yes, 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					cm.clearCategory(category);
				}
			}).setNegativeButton(android.R.string.no,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					
				}
			}).setCancelable(true);
		builder.create().show();
	}
	private void renameCategory(final String category) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getText(R.string.rename).toString());
		final EditText inputBox = new EditText(this);
		inputBox.setText(category);
		builder.setView(inputBox);
		builder.setPositiveButton(android.R.string.yes, 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (cm.renameCategory(inputBox.getText().toString(), category)) {
						updateCategoriesList();
						adapter.update(categoriesNames);
					} else {
						Toast.makeText(CategoryManagerActivity.this, getResources().getString(R.string.note_rename_error), Toast.LENGTH_LONG).show();    				
					}
				}
			}).setNegativeButton(android.R.string.no,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int i) {
			
				}
			}).setCancelable(true);
		builder.create().show();
	}
	
		
	private void appListEditor(final String catName) {
		//Log.v(APP_TAG, "Open app edit window");
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(cm.getCategory(catName).getRepresentName(this));
		builder.setCancelable(true);
		builder.setNegativeButton(android.R.string.cancel,
			new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
				}
			}
		);
		//Toast.makeText(this, "Password:" +cm.getCategoryData("All").size(), Toast.LENGTH_LONG).show();
		ArrayList<AppData> data = new ArrayList<AppData>();
		MyCache.read(this, GetApps.CACHE_NAME, data);
		Collections.sort(data, AppData.NameComparator);
		final ArrayList<AppData> allApps = data;
		data = null;
		final ArrayList<AppData> categoryApps = cm.getCategoryData(catName);
		final int nApps = allApps.size();

		if (nApps > 0) {
			final String[] appNames = new String[nApps];
			final boolean[] checked = new boolean[nApps];			

			for (int i = 0; i < nApps ; i++) {
				appNames[i] = allApps.get(i).name;
				checked[i] = categoryApps.contains(allApps.get(i));
			}

			final boolean[] oldChecked = checked.clone();

			builder.setMultiChoiceItems(appNames, checked, 
				new DialogInterface.OnMultiChoiceClickListener() {							
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					}
				}
			);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					for (int i = 0 ; i < nApps ; i++) {
						if (checked[i] && ! oldChecked[i])
							cm.addToCategory(catName, allApps.get(i));
						else if (!checked[i] && oldChecked[i])
							cm.removeFromCategory(catName, allApps.get(i));
					}
				}
			});
		}
		builder.create().show();
	}
	
	private void newCategory() {
		//Log.v(APP_TAG, "new category");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.createCategory));
		final EditText inputBox = new EditText(this);
		inputBox.setInputType(InputType.TYPE_CLASS_TEXT | 
				InputType.TYPE_TEXT_FLAG_CAP_WORDS);
		builder.setView(inputBox);
		builder.setCancelable(true)
			.setPositiveButton(android.R.string.yes, 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					if (!cm.addCategory(inputBox.getText().toString())) {
						Toast.makeText(CategoryManagerActivity.this, getResources().getString(R.string.note_rename_error), Toast.LENGTH_LONG).show();
					} else {
						updateCategoriesList();
						adapter.update(categoriesNames);
					}
				}
			}).setNegativeButton(android.R.string.cancel, 
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
			
				}
			});
		builder.create().show();
	}
}

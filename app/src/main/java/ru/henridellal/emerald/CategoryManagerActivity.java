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
	
	private static final int COMMAND_SET_HOME = 1;
	private static final int COMMAND_CLEAR = 2;
	private static final int COMMAND_DELETE = 3;
	private static final int COMMAND_RENAME = 4;
	private static final int COMMAND_EDIT = 5;
	
	public void updateCategoriesList() {
		cm.loadCategoriesList();
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
		ArrayList<String> commands = new ArrayList<String>();
		final ArrayList<Integer> commandCodes = new ArrayList<Integer>();
		if (!category.equals(CategoryManager.HIDDEN) && !category.equals(cm.getHome())) {
			commands.add(getResources().getString(R.string.setHome));
			commandCodes.add(COMMAND_SET_HOME);
		}
		if (CategoryManager.isEditable(category)) {
			commands.add(getResources().getString(R.string.editAppList));
			commandCodes.add(COMMAND_EDIT);
			commands.add(getResources().getString(R.string.clear));
			commandCodes.add(COMMAND_CLEAR);
			if (CategoryManager.isCustom(category)) {
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
					DatabaseHelper.deleteCategory(CategoryManagerActivity.this, category);
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
					DatabaseHelper.clearCategory(CategoryManagerActivity.this, category);
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
					String newName = inputBox.getText().toString();
					if (DatabaseHelper.renameCategory(CategoryManagerActivity.this, category, newName)) {
						updateCategoriesList();
						adapter.update(categoriesNames);
						if (cm.getCurCategory().equals(category)) {
							cm.setCurCategory(newName);
						}
						if (cm.getHome().equals(category)) {
							cm.setHome(newName);
						}
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
		ArrayList<BaseData> data = DatabaseHelper.getEntries(this, null);
		Collections.sort(data, BaseData.NameComparator);
		
		final ArrayList<? extends BaseData> allApps = data;
		final ArrayList<? extends BaseData> categoryApps = DatabaseHelper.getEntries(this, catName);
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
							DatabaseHelper.addToCategory(CategoryManagerActivity.this, allApps.get(i), catName);
						else if (!checked[i] && oldChecked[i])
							DatabaseHelper.removeFromCategory(CategoryManagerActivity.this, allApps.get(i), catName);
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
					if (!DatabaseHelper.addCategory(CategoryManagerActivity.this, inputBox.getText().toString())) {
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
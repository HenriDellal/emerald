package ru.henridellal.emerald;

public final class Keys {
	public static final String SHOW_TUTORIAL = "show_tutorial";
	public static final String MESSAGE_SHOWN = "message_shown";
	public static final String KEEP_IN_MEMORY = "keep_in_memory";
	
	public static final String BAR_BACKGROUND = "bar_background";
	public static final String DOCK_BACKGROUND = "dock_background";
	public static final String NAV_BAR_BACKGROUND = "nav_bar_background";
	public static final String STATUS_BAR_BACKGROUND = "status_bar_background";
	public static final String APPS_WINDOW_BACKGROUND = "apps_background";
	
	public static final String ICON_PACK = "icon_pack";
	public static final String TRANSFORM_DRAWABLE = "transform_drawable";
	public static final String APP_SHORTCUT = "app_shortcut";
	public static final String PREV_APP_SHORTCUT = "prevApp_shortcut";
	public static final String TILE = "tile";
	
	public static final String ICON_SIZE = "icon_size";
	public static final String TEXT_SIZE = "text_size";
	public static final String FONT_STYLE = "font_style";
	public static final String COLUMN_WIDTH = "column_width";
	public static final String VERTICAL_SPACING = "vertical_spacing";
	
	public static final String ICON_SIZE_LANDSCAPE = "icon_size_land";
	public static final String TEXT_SIZE_LANDSCAPE = "text_size_land";
	public static final String COLUMN_WIDTH_LANDSCAPE = "column_width_land";
	public static final String VERTICAL_SPACING_LANDSCAPE = "vertical_spacing_land";
	
	public static final String DOCK_IN_LANDSCAPE = "show_dock_in_landscape";
	
	public static final String HOME = "home";
	public static final String HOME_BUTTON = "home_button";
	public static final String CATEGORY = "category";
	public static final String DIRTY = "dirty";
	public static final String ORIENTATION = "orientation";
	public static final String SEARCH_PROVIDER = "search_provider";
	public static final String THEME = "theme";
	public static final String HISTORY_SIZE = "history_size";
	public static final String PASSWORD = "password";
	
	public static final String[] BACKUP = {SHOW_TUTORIAL,
		KEEP_IN_MEMORY, BAR_BACKGROUND, DOCK_BACKGROUND,
		NAV_BAR_BACKGROUND, STATUS_BAR_BACKGROUND,
		APPS_WINDOW_BACKGROUND, ICON_PACK, TRANSFORM_DRAWABLE,
		APP_SHORTCUT, PREV_APP_SHORTCUT, TILE,
		ICON_SIZE, TEXT_SIZE, FONT_STYLE, COLUMN_WIDTH,
		VERTICAL_SPACING, ICON_SIZE_LANDSCAPE, 
		TEXT_SIZE_LANDSCAPE, COLUMN_WIDTH_LANDSCAPE,
		VERTICAL_SPACING_LANDSCAPE, DOCK_IN_LANDSCAPE,
		HOME, HOME_BUTTON, ORIENTATION, SEARCH_PROVIDER,
		THEME, HISTORY_SIZE
	};
	
	public static final String[] restart = {MESSAGE_SHOWN, BAR_BACKGROUND,
		NAV_BAR_BACKGROUND, STATUS_BAR_BACKGROUND, DOCK_BACKGROUND,
		APPS_WINDOW_BACKGROUND, TILE, THEME, ORIENTATION,
		APP_SHORTCUT, KEEP_IN_MEMORY};
}

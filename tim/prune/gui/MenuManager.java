package tim.prune.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import tim.prune.App;
import tim.prune.DataSubscriber;
import tim.prune.FunctionLibrary;
import tim.prune.GenericFunction;
import tim.prune.I18nManager;
import tim.prune.UpdateMessageBroker;
import tim.prune.config.Config;
import tim.prune.data.Photo;
import tim.prune.data.PhotoList;
import tim.prune.data.Selection;
import tim.prune.data.Track;
import tim.prune.data.TrackInfo;
import tim.prune.function.RearrangeWaypointsFunction.Rearrange;
import tim.prune.function.browser.UrlGenerator;

/**
 * Class to manage the menu bar and tool bar,
 * including enabling and disabling the items
 */
public class MenuManager implements DataSubscriber
{
	private App _app = null;
	private Track _track = null;
	private Selection _selection = null;
	private PhotoList _photos = null;

	// Menu items which need enabling/disabling
	private JMenuItem _sendGpsItem = null;
	private JMenuItem _saveItem = null;
	private JMenuItem _exportKmlItem = null;
	private JMenuItem _exportGpxItem = null;
	private JMenuItem _exportPovItem = null;
	private JMenuItem _undoItem = null;
	private JMenuItem _clearUndoItem = null;
	private JMenuItem _editPointItem = null;
	private JMenuItem _editWaypointNameItem = null;
	private JMenuItem _deletePointItem = null;
	private JMenuItem _deleteRangeItem = null;
	private JMenuItem _compressItem = null;
	private JMenuItem _deleteMarkedPointsItem = null;
	private JMenuItem _interpolateItem = null;
	private JMenuItem _averageItem = null;
	private JMenuItem _selectAllItem = null;
	private JMenuItem _selectNoneItem = null;
	private JMenuItem _selectStartItem = null;
	private JMenuItem _selectEndItem = null;
	private JMenuItem _findWaypointItem = null;
	private JMenuItem _duplicatePointItem = null;
	private JMenuItem _reverseItem = null;
	private JMenuItem _addTimeOffsetItem = null;
	private JMenuItem _addAltitudeOffsetItem = null;
	private JMenuItem _mergeSegmentsItem = null;
	private JMenu     _rearrangeMenu = null;
	private JMenuItem _cutAndMoveItem = null;
	private JMenuItem _convertNamesToTimesItem = null;
	private JCheckBoxMenuItem _mapCheckbox = null;
	private JMenuItem _show3dItem = null;
	private JMenu     _browserMapMenu = null;
	private JMenuItem _chartItem = null;
	private JMenuItem _getGpsiesItem = null;
	private JMenuItem _distanceItem = null;
	private JMenuItem _fullRangeDetailsItem = null;
	private JMenuItem _saveExifItem = null;
	private JMenuItem _connectPhotoItem = null;
	private JMenuItem _deletePhotoItem = null;
	private JMenuItem _disconnectPhotoItem = null;
	private JMenuItem _correlatePhotosItem = null;
	private JMenuItem _rearrangePhotosItem = null;
	private JMenuItem _rotatePhotoLeft = null;
	private JMenuItem _rotatePhotoRight = null;
	private JMenuItem _ignoreExifThumb = null;

	// ActionListeners for reuse by menu and toolbar
	private ActionListener _openFileAction = null;
	private ActionListener _addPhotoAction = null;
	private ActionListener _saveAction = null;
	private ActionListener _undoAction = null;
	private ActionListener _editPointAction = null;
	private ActionListener _deletePointAction = null;
	private ActionListener _deleteRangeAction = null;
	private ActionListener _selectStartAction = null;
	private ActionListener _selectEndAction = null;
	private ActionListener _connectPhotoAction = null;

	// Toolbar buttons which need enabling/disabling
	private JButton _saveButton = null;
	private JButton _undoButton = null;
	private JButton _editPointButton = null;
	private JButton _deletePointButton = null;
	private JButton _deleteRangeButton = null;
	private JButton _selectStartButton = null;
	private JButton _selectEndButton = null;
	private JButton _connectPhotoButton = null;

	/** Array of key events */
	private static final int[] KEY_EVENTS = {
		KeyEvent.VK_A, KeyEvent.VK_B, KeyEvent.VK_C, KeyEvent.VK_D, KeyEvent.VK_E,
		KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_I, KeyEvent.VK_J,
		KeyEvent.VK_K, KeyEvent.VK_L, KeyEvent.VK_M, KeyEvent.VK_N, KeyEvent.VK_O,
		KeyEvent.VK_P, KeyEvent.VK_Q, KeyEvent.VK_R, KeyEvent.VK_S, KeyEvent.VK_T,
		KeyEvent.VK_U, KeyEvent.VK_V, KeyEvent.VK_W, KeyEvent.VK_X, KeyEvent.VK_Y, KeyEvent.VK_Z};


	/**
	 * Constructor
	 * @param inApp application to call on menu actions
	 * @param inTrackInfo track info object
	 */
	public MenuManager(App inApp, TrackInfo inTrackInfo)
	{
		_app = inApp;
		_track = inTrackInfo.getTrack();
		_selection = inTrackInfo.getSelection();
		_photos = inTrackInfo.getPhotoList();
	}


	/**
	 * Create a JMenuBar containing all menu items
	 * @return JMenuBar
	 */
	public JMenuBar createMenuBar()
	{
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu(I18nManager.getText("menu.file"));
		setAltKey(fileMenu, "altkey.menu.file");
		// Open file
		JMenuItem openMenuItem = new JMenuItem(I18nManager.getText("menu.file.open"));
		setShortcut(openMenuItem, "shortcut.menu.file.open");
		_openFileAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.openFile();
			}
		};
		openMenuItem.addActionListener(_openFileAction);
		fileMenu.add(openMenuItem);
		// Add photos
		JMenuItem addPhotosMenuItem = new JMenuItem(I18nManager.getText("menu.file.addphotos"));
		_addPhotoAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.addPhotos();
			}
		};
		addPhotosMenuItem.addActionListener(_addPhotoAction);
		fileMenu.add(addPhotosMenuItem);
		fileMenu.addSeparator();
		// Load from GPS
		JMenuItem loadFromGpsMenuItem = makeMenuItem(FunctionLibrary.FUNCTION_GPSLOAD);
		setShortcut(loadFromGpsMenuItem, "shortcut.menu.file.load");
		fileMenu.add(loadFromGpsMenuItem);
		// Send to GPS
		_sendGpsItem = makeMenuItem(FunctionLibrary.FUNCTION_GPSSAVE);
		_sendGpsItem.setEnabled(false);
		fileMenu.add(_sendGpsItem);
		fileMenu.addSeparator();
		// Save
		_saveItem = new JMenuItem(I18nManager.getText("menu.file.save"), KeyEvent.VK_S);
		setShortcut(_saveItem, "shortcut.menu.file.save");
		_saveAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.saveFile();
			}
		};
		_saveItem.addActionListener(_saveAction);
		_saveItem.setEnabled(false);
		fileMenu.add(_saveItem);
		// Export - Kml
		_exportKmlItem = makeMenuItem(FunctionLibrary.FUNCTION_KMLEXPORT);
		_exportKmlItem.setEnabled(false);
		fileMenu.add(_exportKmlItem);
		// Gpx
		_exportGpxItem = makeMenuItem(FunctionLibrary.FUNCTION_GPXEXPORT);
		_exportGpxItem.setEnabled(false);
		fileMenu.add(_exportGpxItem);
		// Pov
		_exportPovItem = makeMenuItem(FunctionLibrary.FUNCTION_POVEXPORT);
		_exportPovItem.setEnabled(false);
		fileMenu.add(_exportPovItem);
		fileMenu.addSeparator();
		JMenuItem exitMenuItem = new JMenuItem(I18nManager.getText("menu.file.exit"));
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.exit();
			}
		});
		fileMenu.add(exitMenuItem);
		menubar.add(fileMenu);
		// Track menu
		JMenu trackMenu = new JMenu(I18nManager.getText("menu.track"));
		setAltKey(trackMenu, "altkey.menu.track");
		_undoItem = new JMenuItem(I18nManager.getText("menu.edit.undo"));
		setShortcut(_undoItem, "shortcut.menu.edit.undo");
		_undoAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.beginUndo();
			}
		};
		_undoItem.addActionListener(_undoAction);
		_undoItem.setEnabled(false);
		trackMenu.add(_undoItem);
		_clearUndoItem = new JMenuItem(I18nManager.getText("menu.edit.clearundo"));
		_clearUndoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.clearUndo();
			}
		});
		_clearUndoItem.setEnabled(false);
		trackMenu.add(_clearUndoItem);
		trackMenu.addSeparator();
		_compressItem = makeMenuItem(FunctionLibrary.FUNCTION_COMPRESS);
		setShortcut(_compressItem, "shortcut.menu.edit.compress");
		_compressItem.setEnabled(false);
		trackMenu.add(_compressItem);
		_deleteMarkedPointsItem = new JMenuItem(I18nManager.getText("menu.edit.deletemarked"));
		_deleteMarkedPointsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.finishCompressTrack();
			}
		});
		_deleteMarkedPointsItem.setEnabled(false);
		trackMenu.add(_deleteMarkedPointsItem);
		trackMenu.addSeparator();
		// Rearrange waypoints
		_rearrangeMenu = new JMenu(I18nManager.getText("menu.edit.rearrange"));
		_rearrangeMenu.setEnabled(false);
		JMenuItem  rearrangeStartItem = new JMenuItem(I18nManager.getText("menu.edit.rearrange.start"));
		rearrangeStartItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				FunctionLibrary.FUNCTION_REARRANGE_WAYPOINTS.rearrangeWaypoints(Rearrange.TO_START);
			}
		});
		rearrangeStartItem.setEnabled(true);
		_rearrangeMenu.add(rearrangeStartItem);
		JMenuItem rearrangeEndItem = new JMenuItem(I18nManager.getText("menu.edit.rearrange.end"));
		rearrangeEndItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				FunctionLibrary.FUNCTION_REARRANGE_WAYPOINTS.rearrangeWaypoints(Rearrange.TO_END);
			}
		});
		rearrangeEndItem.setEnabled(true);
		_rearrangeMenu.add(rearrangeEndItem);
		JMenuItem rearrangeNearestItem = new JMenuItem(I18nManager.getText("menu.edit.rearrange.nearest"));
		rearrangeNearestItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				FunctionLibrary.FUNCTION_REARRANGE_WAYPOINTS.rearrangeWaypoints(Rearrange.TO_NEAREST);
			}
		});
		rearrangeNearestItem.setEnabled(true);
		_rearrangeMenu.add(rearrangeNearestItem);
		trackMenu.add(_rearrangeMenu);
		// Get gpsies tracks
		_getGpsiesItem = makeMenuItem(FunctionLibrary.FUNCTION_GET_GPSIES);
		_getGpsiesItem.setEnabled(false);
		trackMenu.add(_getGpsiesItem);
		menubar.add(trackMenu);

		// Range menu
		JMenu rangeMenu = new JMenu(I18nManager.getText("menu.range"));
		setAltKey(rangeMenu, "altkey.menu.range");
		_selectAllItem = new JMenuItem(I18nManager.getText("menu.select.all"));
		setShortcut(_selectAllItem, "shortcut.menu.select.all");
		_selectAllItem.setEnabled(false);
		_selectAllItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_selection.selectRange(0, _track.getNumPoints()-1);
			}
		});
		rangeMenu.add(_selectAllItem);
		_selectNoneItem = new JMenuItem(I18nManager.getText("menu.select.none"));
		_selectNoneItem.setEnabled(false);
		_selectNoneItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.selectNone();
			}
		});
		rangeMenu.add(_selectNoneItem);
		rangeMenu.addSeparator();
		_selectStartItem = new JMenuItem(I18nManager.getText("menu.select.start"));
		_selectStartItem.setEnabled(false);
		_selectStartAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_selection.selectRangeStart();
			}
		};
		_selectStartItem.addActionListener(_selectStartAction);
		rangeMenu.add(_selectStartItem);
		_selectEndItem = new JMenuItem(I18nManager.getText("menu.select.end"));
		_selectEndItem.setEnabled(false);
		_selectEndAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_selection.selectRangeEnd();
			}
		};
		_selectEndItem.addActionListener(_selectEndAction);
		rangeMenu.add(_selectEndItem);
		rangeMenu.addSeparator();
		_deleteRangeItem = new JMenuItem(I18nManager.getText("menu.edit.deleterange"));
		_deleteRangeAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.deleteSelectedRange();
			}
		};
		_deleteRangeItem.addActionListener(_deleteRangeAction);
		_deleteRangeItem.setEnabled(false);
		rangeMenu.add(_deleteRangeItem);
		_reverseItem = new JMenuItem(I18nManager.getText("menu.edit.reverse"));
		_reverseItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.reverseRange();
			}
		});
		_reverseItem.setEnabled(false);
		rangeMenu.add(_reverseItem);
		_addTimeOffsetItem = makeMenuItem(FunctionLibrary.FUNCTION_ADD_TIME_OFFSET);
		_addTimeOffsetItem.setEnabled(false);
		rangeMenu.add(_addTimeOffsetItem);
		_addAltitudeOffsetItem = makeMenuItem(FunctionLibrary.FUNCTION_ADD_ALTITUDE_OFFSET);
		_addAltitudeOffsetItem.setEnabled(false);
		rangeMenu.add(_addAltitudeOffsetItem);
		_mergeSegmentsItem = new JMenuItem(I18nManager.getText("menu.edit.mergetracksegments"));
		_mergeSegmentsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.mergeTrackSegments();
			}
		});
		_mergeSegmentsItem.setEnabled(false);
		rangeMenu.add(_mergeSegmentsItem);
		rangeMenu.addSeparator();
		_interpolateItem = new JMenuItem(I18nManager.getText("menu.edit.interpolate"));
		_interpolateItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.interpolateSelection();
			}
		});
		_interpolateItem.setEnabled(false);
		rangeMenu.add(_interpolateItem);
		_averageItem = new JMenuItem(I18nManager.getText("menu.edit.average"));
		_averageItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.averageSelection();
			}
		});
		_averageItem.setEnabled(false);
		rangeMenu.add(_averageItem);
		_cutAndMoveItem = new JMenuItem(I18nManager.getText("menu.edit.cutandmove"));
		_cutAndMoveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.cutAndMoveSelection();
			}
		});
		_cutAndMoveItem.setEnabled(false);
		rangeMenu.add(_cutAndMoveItem);
		_convertNamesToTimesItem = makeMenuItem(FunctionLibrary.FUNCTION_CONVERT_NAMES_TO_TIMES);
		_convertNamesToTimesItem.setEnabled(false);
		rangeMenu.add(_convertNamesToTimesItem);
		menubar.add(rangeMenu);

		// Point menu
		JMenu pointMenu = new JMenu(I18nManager.getText("menu.point"));
		setAltKey(pointMenu, "altkey.menu.point");
		_editPointItem = new JMenuItem(I18nManager.getText("menu.edit.editpoint"));
		_editPointAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.editCurrentPoint();
			}
		};
		_editPointItem.addActionListener(_editPointAction);
		_editPointItem.setEnabled(false);
		pointMenu.add(_editPointItem);
		_editWaypointNameItem = makeMenuItem(FunctionLibrary.FUNCTION_EDIT_WAYPOINT_NAME);
		_editWaypointNameItem.setEnabled(false);
		pointMenu.add(_editWaypointNameItem);
		_deletePointItem = new JMenuItem(I18nManager.getText("menu.edit.deletepoint"));
		_deletePointAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.deleteCurrentPoint();
			}
		};
		_deletePointItem.addActionListener(_deletePointAction);
		_deletePointItem.setEnabled(false);
		pointMenu.add(_deletePointItem);
		pointMenu.addSeparator();
		// find a waypoint
		_findWaypointItem = makeMenuItem(FunctionLibrary.FUNCTION_FIND_WAYPOINT);
		_findWaypointItem.setEnabled(false);
		pointMenu.add(_findWaypointItem);
		// duplicate current point
		_duplicatePointItem = makeMenuItem(FunctionLibrary.FUNCTION_DUPLICATE_POINT);
		_duplicatePointItem.setEnabled(false);
		pointMenu.add(_duplicatePointItem);
		// paste coordinates function
		JMenuItem pasteCoordsItem = makeMenuItem(FunctionLibrary.FUNCTION_PASTE_COORDINATES);
		pointMenu.add(pasteCoordsItem);
		menubar.add(pointMenu);

		// Add view menu
		JMenu viewMenu = new JMenu(I18nManager.getText("menu.view"));
		setAltKey(viewMenu, "altkey.menu.view");
		// Turn map display on/off
		_mapCheckbox = new JCheckBoxMenuItem(
			I18nManager.getText("menu.map.showmap"), false);
		_mapCheckbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Config.setConfigBoolean(Config.KEY_SHOW_MAP, _mapCheckbox.isSelected());
				UpdateMessageBroker.informSubscribers();
			}
		});
		viewMenu.add(_mapCheckbox);
		_show3dItem = makeMenuItem(FunctionLibrary.FUNCTION_3D);
		_show3dItem.setEnabled(false);
		viewMenu.add(_show3dItem);
		// browser submenu
		_browserMapMenu = new JMenu(I18nManager.getText("menu.view.browser"));
		_browserMapMenu.setEnabled(false);
		JMenuItem googleMapsItem = new JMenuItem(I18nManager.getText("menu.view.browser.google"));
		googleMapsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.showExternalMap(UrlGenerator.MAP_SOURCE_GOOGLE);
			}
		});
		_browserMapMenu.add(googleMapsItem);
		JMenuItem openMapsItem = new JMenuItem(I18nManager.getText("menu.view.browser.openstreetmap"));
		openMapsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.showExternalMap(UrlGenerator.MAP_SOURCE_OSM);
			}
		});
		_browserMapMenu.add(openMapsItem);
		JMenuItem mapquestMapsItem = new JMenuItem(I18nManager.getText("menu.view.browser.mapquest"));
		mapquestMapsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.showExternalMap(UrlGenerator.MAP_SOURCE_MAPQUEST);
			}
		});
		_browserMapMenu.add(mapquestMapsItem);
		JMenuItem yahooMapsItem = new JMenuItem(I18nManager.getText("menu.view.browser.yahoo"));
		yahooMapsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.showExternalMap(UrlGenerator.MAP_SOURCE_YAHOO);
			}
		});
		_browserMapMenu.add(yahooMapsItem);
		JMenuItem bingMapsItem = new JMenuItem(I18nManager.getText("menu.view.browser.bing"));
		bingMapsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.showExternalMap(UrlGenerator.MAP_SOURCE_BING);
			}
		});
		_browserMapMenu.add(bingMapsItem);
		viewMenu.add(_browserMapMenu);
		// Charts
		_chartItem = makeMenuItem(FunctionLibrary.FUNCTION_CHARTS);
		_chartItem.setEnabled(false);
		viewMenu.add(_chartItem);
		// Distances
		_distanceItem = makeMenuItem(FunctionLibrary.FUNCTION_DISTANCES);
		_distanceItem.setEnabled(false);
		viewMenu.add(_distanceItem);
		// full range details
		_fullRangeDetailsItem = makeMenuItem(FunctionLibrary.FUNCTION_FULL_RANGE_DETAILS);
		_fullRangeDetailsItem.setEnabled(false);
		viewMenu.add(_fullRangeDetailsItem);
		menubar.add(viewMenu);

		// Add photo menu
		JMenu photoMenu = new JMenu(I18nManager.getText("menu.photo"));
		setAltKey(photoMenu, "altkey.menu.photo");
		addPhotosMenuItem = new JMenuItem(I18nManager.getText("menu.file.addphotos"));
		addPhotosMenuItem.addActionListener(_addPhotoAction);
		photoMenu.add(addPhotosMenuItem);
		_saveExifItem = new JMenuItem(I18nManager.getText("menu.photo.saveexif"));
		_saveExifItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.saveExif();
			}
		});
		_saveExifItem.setEnabled(false);
		photoMenu.add(_saveExifItem);
		_connectPhotoItem = new JMenuItem(I18nManager.getText("menu.photo.connect"));
		_connectPhotoAction = new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.connectPhotoToPoint();
			}
		};
		_connectPhotoItem.addActionListener(_connectPhotoAction);
		_connectPhotoItem.setEnabled(false);
		photoMenu.addSeparator();
		photoMenu.add(_connectPhotoItem);
		// disconnect photo
		_disconnectPhotoItem = new JMenuItem(I18nManager.getText("menu.photo.disconnect"));
		_disconnectPhotoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.disconnectPhotoFromPoint();
			}
		});
		_disconnectPhotoItem.setEnabled(false);
		photoMenu.add(_disconnectPhotoItem);
		_deletePhotoItem = new JMenuItem(I18nManager.getText("menu.photo.delete"));
		_deletePhotoItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				_app.deleteCurrentPhoto();
			}
		});
		_deletePhotoItem.setEnabled(false);
		photoMenu.add(_deletePhotoItem);
		// Rotate current photo
		_rotatePhotoLeft = makeMenuItem(FunctionLibrary.FUNCTION_ROTATE_PHOTO_LEFT);
		_rotatePhotoLeft.setEnabled(false);
		photoMenu.add(_rotatePhotoLeft);
		_rotatePhotoRight = makeMenuItem(FunctionLibrary.FUNCTION_ROTATE_PHOTO_RIGHT);
		_rotatePhotoRight.setEnabled(false);
		photoMenu.add(_rotatePhotoRight);
		_ignoreExifThumb = makeMenuItem(FunctionLibrary.FUNCTION_IGNORE_EXIF_THUMB);
		_ignoreExifThumb.setEnabled(false);
		photoMenu.add(_ignoreExifThumb);
		photoMenu.addSeparator();
		// correlate all photos
		_correlatePhotosItem = makeMenuItem(FunctionLibrary.FUNCTION_CORRELATE_PHOTOS);
		_correlatePhotosItem.setEnabled(false);
		photoMenu.add(_correlatePhotosItem);
		// rearrange photo points
		_rearrangePhotosItem = makeMenuItem(FunctionLibrary.FUNCTION_REARRANGE_PHOTOS);
		_rearrangePhotosItem.setEnabled(false);
		photoMenu.add(_rearrangePhotosItem);
		menubar.add(photoMenu);

		// Settings menu
		JMenu settingsMenu = new JMenu(I18nManager.getText("menu.settings"));
		setAltKey(settingsMenu, "altkey.menu.settings");
		// Set the map background
		JMenuItem mapBgItem = makeMenuItem(FunctionLibrary.FUNCTION_SET_MAP_BG);
		settingsMenu.add(mapBgItem);
		// Set kmz image size
		JMenuItem setKmzImageSizeItem = makeMenuItem(FunctionLibrary.FUNCTION_SET_KMZ_IMAGE_SIZE);
		settingsMenu.add(setKmzImageSizeItem);
		// Set program paths
		JMenuItem setPathsItem = makeMenuItem(FunctionLibrary.FUNCTION_SET_PATHS);
		settingsMenu.add(setPathsItem);
		// Set colours
		JMenuItem setColoursItem = makeMenuItem(FunctionLibrary.FUNCTION_SET_COLOURS);
		settingsMenu.add(setColoursItem);
		// Set language
		JMenuItem setLanguageItem = makeMenuItem(FunctionLibrary.FUNCTION_SET_LANGUAGE);
		settingsMenu.add(setLanguageItem);
		settingsMenu.addSeparator();
		// Save configuration
		JMenuItem saveConfigMenuItem = makeMenuItem(FunctionLibrary.FUNCTION_SAVECONFIG);
		settingsMenu.add(saveConfigMenuItem);
		menubar.add(settingsMenu);

		// Help menu
		JMenu helpMenu = new JMenu(I18nManager.getText("menu.help"));
		setAltKey(helpMenu, "altkey.menu.help");
		JMenuItem helpItem = makeMenuItem(FunctionLibrary.FUNCTION_HELP);
		setShortcut(helpItem, "shortcut.menu.help.help");
		helpMenu.add(helpItem);
		JMenuItem showKeysItem = makeMenuItem(FunctionLibrary.FUNCTION_SHOW_KEYS);
		helpMenu.add(showKeysItem);
		JMenuItem aboutItem = makeMenuItem(FunctionLibrary.FUNCTION_ABOUT);
		helpMenu.add(aboutItem);
		JMenuItem checkVersionItem = makeMenuItem(FunctionLibrary.FUNCTION_CHECK_VERSION);
		helpMenu.add(checkVersionItem);
		menubar.add(helpMenu);

		return menubar;
	}

	/**
	 * Convenience method for making a menu item using a function
	 * @param inFunction function
	 * @return menu item using localized name of function
	 */
	private static JMenuItem makeMenuItem(GenericFunction inFunction)
	{
		JMenuItem item = new JMenuItem(I18nManager.getText(inFunction.getNameKey()));
		item.addActionListener(new FunctionLauncher(inFunction));
		return item;
	}

	/**
	 * Set the alt key for the given menu
	 * @param inMenu menu to set
	 * @param inKey key to lookup to get language-sensitive altkey
	 */
	private static void setAltKey(JMenu inMenu, String inKey)
	{
		// Lookup the key in the properties
		String altKey = I18nManager.getText(inKey);
		if (altKey != null && altKey.length() == 1)
		{
			int code = altKey.charAt(0) - 'A';
			if (code >= 0 && code < 26)
			{
				// Found a valid code between A and Z
				inMenu.setMnemonic(KEY_EVENTS[code]);
			}
		}
	}

	/**
	 * Set the shortcut key for the given menu item
	 * @param inMenuItem menu item to set
	 * @param inKey key to lookup to get language-sensitive shortcut
	 */
	private static void setShortcut(JMenuItem inMenuItem, String inKey)
	{
		// Lookup the key in the properties
		String altKey = I18nManager.getText(inKey);
		if (altKey != null && altKey.length() == 1)
		{
			int code = altKey.charAt(0) - 'A';
			if (code >= 0 && code < 26)
			{
				// Found a valid code between A and Z
				inMenuItem.setAccelerator(KeyStroke.getKeyStroke(KEY_EVENTS[code], InputEvent.CTRL_DOWN_MASK));
			}
		}
	}

	/**
	 * Create a JToolBar containing all toolbar buttons
	 * @return toolbar containing buttons
	 */
	public JToolBar createToolBar()
	{
		JToolBar toolbar = new JToolBar();
		// Add text file
		JButton openFileButton = new JButton(IconManager.getImageIcon(IconManager.OPEN_FILE));
		openFileButton.setToolTipText(I18nManager.getText("menu.file.open"));
		openFileButton.addActionListener(_openFileAction);
		toolbar.add(openFileButton);
		// Add photo
		JButton addPhotoButton = new JButton(IconManager.getImageIcon(IconManager.ADD_PHOTO));
		addPhotoButton.setToolTipText(I18nManager.getText("menu.file.addphotos"));
		addPhotoButton.addActionListener(_addPhotoAction);
		toolbar.add(addPhotoButton);
		// Save
		_saveButton = new JButton(IconManager.getImageIcon(IconManager.SAVE_FILE));
		_saveButton.setToolTipText(I18nManager.getText("menu.file.save"));
		_saveButton.addActionListener(_saveAction);
		_saveButton.setEnabled(false);
		toolbar.add(_saveButton);
		// Undo
		_undoButton = new JButton(IconManager.getImageIcon(IconManager.UNDO));
		_undoButton.setToolTipText(I18nManager.getText("menu.edit.undo"));
		_undoButton.addActionListener(_undoAction);
		_undoButton.setEnabled(false);
		toolbar.add(_undoButton);
		// Edit point
		_editPointButton = new JButton(IconManager.getImageIcon(IconManager.EDIT_POINT));
		_editPointButton.setToolTipText(I18nManager.getText("menu.edit.editpoint"));
		_editPointButton.addActionListener(_editPointAction);
		_editPointButton.setEnabled(false);
		toolbar.add(_editPointButton);
		// Delete point
		_deletePointButton = new JButton(IconManager.getImageIcon(IconManager.DELETE_POINT));
		_deletePointButton.setToolTipText(I18nManager.getText("menu.edit.deletepoint"));
		_deletePointButton.addActionListener(_deletePointAction);
		_deletePointButton.setEnabled(false);
		toolbar.add(_deletePointButton);
		// Delete range
		_deleteRangeButton = new JButton(IconManager.getImageIcon(IconManager.DELETE_RANGE));
		_deleteRangeButton.setToolTipText(I18nManager.getText("menu.edit.deleterange"));
		_deleteRangeButton.addActionListener(_deleteRangeAction);
		_deleteRangeButton.setEnabled(false);
		toolbar.add(_deleteRangeButton);
		// Select start, end
		_selectStartButton = new JButton(IconManager.getImageIcon(IconManager.SET_RANGE_START));
		_selectStartButton.setToolTipText(I18nManager.getText("menu.select.start"));
		_selectStartButton.addActionListener(_selectStartAction);
		_selectStartButton.setEnabled(false);
		toolbar.add(_selectStartButton);
		_selectEndButton = new JButton(IconManager.getImageIcon(IconManager.SET_RANGE_END));
		_selectEndButton.setToolTipText(I18nManager.getText("menu.select.end"));
		_selectEndButton.addActionListener(_selectEndAction);
		_selectEndButton.setEnabled(false);
		toolbar.add(_selectEndButton);
		_connectPhotoButton = new JButton(IconManager.getImageIcon(IconManager.CONNECT_PHOTO));
		_connectPhotoButton.setToolTipText(I18nManager.getText("menu.photo.connect"));
		_connectPhotoButton.addActionListener(_connectPhotoAction);
		_connectPhotoButton.setEnabled(false);
		toolbar.add(_connectPhotoButton);
		// finish off
		toolbar.setFloatable(false);
		return toolbar;
	}


	/**
	 * Method to update menu when file loaded
	 */
	public void informFileLoaded()
	{
		// save, undo, delete enabled
		_sendGpsItem.setEnabled(true);
		_saveItem.setEnabled(true);
		_undoItem.setEnabled(true);
		_compressItem.setEnabled(true);
		_deleteMarkedPointsItem.setEnabled(false);
	}


	/**
	 * @see tim.prune.DataSubscriber#dataUpdated(tim.prune.data.Track)
	 */
	public void dataUpdated(byte inUpdateType)
	{
		boolean hasData = (_track != null && _track.getNumPoints() > 0);
		// set functions which require data
		_sendGpsItem.setEnabled(hasData);
		_saveItem.setEnabled(hasData);
		_saveButton.setEnabled(hasData);
		_exportKmlItem.setEnabled(hasData);
		_exportGpxItem.setEnabled(hasData);
		_exportPovItem.setEnabled(hasData);
		_compressItem.setEnabled(hasData);
		_deleteMarkedPointsItem.setEnabled(hasData && _track.hasMarkedPoints());
		_rearrangeMenu.setEnabled(hasData && _track.hasTrackPoints() && _track.hasWaypoints());
		_selectAllItem.setEnabled(hasData);
		_selectNoneItem.setEnabled(hasData);
		_show3dItem.setEnabled(hasData);
		_chartItem.setEnabled(hasData);
		_browserMapMenu.setEnabled(hasData);
		_distanceItem.setEnabled(hasData);
		_getGpsiesItem.setEnabled(hasData);
		_findWaypointItem.setEnabled(hasData && _track.hasWaypoints());
		// is undo available?
		boolean hasUndo = !_app.getUndoStack().isEmpty();
		_undoItem.setEnabled(hasUndo);
		_undoButton.setEnabled(hasUndo);
		_clearUndoItem.setEnabled(hasUndo);
		// is there a current point?
		boolean hasPoint = (hasData && _selection.getCurrentPointIndex() >= 0);
		_editPointItem.setEnabled(hasPoint);
		_editPointButton.setEnabled(hasPoint);
		_editWaypointNameItem.setEnabled(hasPoint);
		_deletePointItem.setEnabled(hasPoint);
		_deletePointButton.setEnabled(hasPoint);
		_selectStartItem.setEnabled(hasPoint);
		_selectStartButton.setEnabled(hasPoint);
		_selectEndItem.setEnabled(hasPoint);
		_selectEndButton.setEnabled(hasPoint);
		_duplicatePointItem.setEnabled(hasPoint);
		// are there any photos?
		boolean anyPhotos = _photos != null && _photos.getNumPhotos() > 0;
		_saveExifItem.setEnabled(anyPhotos);
		// is there a current photo?
		boolean hasPhoto = anyPhotos && _selection.getCurrentPhotoIndex() >= 0;
		// connect is available if photo and point selected, and photo has no point
		Photo currentPhoto = _photos.getPhoto(_selection.getCurrentPhotoIndex());
		boolean connectAvailable = hasPhoto && hasPoint && currentPhoto != null
			&& currentPhoto.getDataPoint() == null;
		_connectPhotoItem.setEnabled(connectAvailable);
		_connectPhotoButton.setEnabled(connectAvailable);
		_disconnectPhotoItem.setEnabled(hasPhoto && currentPhoto != null && currentPhoto.getDataPoint() != null);
		_correlatePhotosItem.setEnabled(anyPhotos && hasData);
		_rearrangePhotosItem.setEnabled(anyPhotos && hasData && _track.getNumPoints() > 1);
		_deletePhotoItem.setEnabled(hasPhoto);
		_rotatePhotoLeft.setEnabled(hasPhoto);
		_rotatePhotoRight.setEnabled(hasPhoto);
		_ignoreExifThumb.setEnabled(hasPhoto && currentPhoto != null && currentPhoto.getExifThumbnail() != null);
		// is there a current range?
		boolean hasRange = (hasData && _selection.hasRangeSelected());
		_deleteRangeItem.setEnabled(hasRange);
		_deleteRangeButton.setEnabled(hasRange);
		_interpolateItem.setEnabled(hasRange
			&& (_selection.getEnd() - _selection.getStart()) == 1);
		_averageItem.setEnabled(hasRange);
		_mergeSegmentsItem.setEnabled(hasRange);
		_reverseItem.setEnabled(hasRange);
		_addTimeOffsetItem.setEnabled(hasRange);
		_addAltitudeOffsetItem.setEnabled(hasRange);
		_convertNamesToTimesItem.setEnabled(hasRange && _track.hasWaypoints());
		_fullRangeDetailsItem.setEnabled(hasRange);
		// Is the currently selected point outside the current range?
		_cutAndMoveItem.setEnabled(hasRange && hasPoint &&
			(_selection.getCurrentPointIndex() < _selection.getStart()
				|| _selection.getCurrentPointIndex() > (_selection.getEnd()+1)));
		// Has the map been switched on/off?
		boolean mapsOn = Config.getConfigBoolean(Config.KEY_SHOW_MAP);
		if (_mapCheckbox.isSelected() != mapsOn) {
			_mapCheckbox.setSelected(mapsOn);
		}
	}


	/**
	 * Ignore action completed signals
	 * @see tim.prune.DataSubscriber#actionCompleted(java.lang.String)
	 */
	public void actionCompleted(String inMessage)
	{}
}

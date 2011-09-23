/*
 * Bibliothek - DockingFrames
 * Library built on Java/Swing, allows the user to "drag and drop"
 * panels containing any Swing-Component the developer likes to add.
 * 
 * Copyright (C) 2007 Benjamin Sigg
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Benjamin Sigg
 * benjamin_sigg@gmx.ch
 * CH - Switzerland
 */

package bibliothek.gui.dock;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import bibliothek.gui.DockController;
import bibliothek.gui.DockStation;
import bibliothek.gui.DockTheme;
import bibliothek.gui.DockUI;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.action.DefaultDockActionSource;
import bibliothek.gui.dock.action.DockAction;
import bibliothek.gui.dock.action.DockActionSource;
import bibliothek.gui.dock.action.HierarchyDockActionSource;
import bibliothek.gui.dock.action.ListeningDockAction;
import bibliothek.gui.dock.action.LocationHint;
import bibliothek.gui.dock.control.relocator.Merger;
import bibliothek.gui.dock.displayer.DisplayerCombinerTarget;
import bibliothek.gui.dock.displayer.DockableDisplayerHints;
import bibliothek.gui.dock.dockable.DockHierarchyObserver;
import bibliothek.gui.dock.dockable.DockableStateListener;
import bibliothek.gui.dock.dockable.DockableStateListenerManager;
import bibliothek.gui.dock.event.DockHierarchyEvent;
import bibliothek.gui.dock.event.DockHierarchyListener;
import bibliothek.gui.dock.event.DockStationAdapter;
import bibliothek.gui.dock.event.DockStationListener;
import bibliothek.gui.dock.event.DockableListener;
import bibliothek.gui.dock.event.DoubleClickListener;
import bibliothek.gui.dock.event.SplitDockListener;
import bibliothek.gui.dock.layout.DockableProperty;
import bibliothek.gui.dock.security.SecureContainer;
import bibliothek.gui.dock.station.Combiner;
import bibliothek.gui.dock.station.DisplayerCollection;
import bibliothek.gui.dock.station.DisplayerFactory;
import bibliothek.gui.dock.station.DockStationIcon;
import bibliothek.gui.dock.station.DockableDisplayer;
import bibliothek.gui.dock.station.DockableDisplayerListener;
import bibliothek.gui.dock.station.StationBackgroundComponent;
import bibliothek.gui.dock.station.StationChildHandle;
import bibliothek.gui.dock.station.StationDropOperation;
import bibliothek.gui.dock.station.StationPaint;
import bibliothek.gui.dock.station.split.DefaultSplitDividerStrategy;
import bibliothek.gui.dock.station.split.DefaultSplitLayoutManager;
import bibliothek.gui.dock.station.split.DockableSplitDockTree;
import bibliothek.gui.dock.station.split.Leaf;
import bibliothek.gui.dock.station.split.Node;
import bibliothek.gui.dock.station.split.Placeholder;
import bibliothek.gui.dock.station.split.PutInfo;
import bibliothek.gui.dock.station.split.Root;
import bibliothek.gui.dock.station.split.SplitDividerStrategy;
import bibliothek.gui.dock.station.split.SplitDockAccess;
import bibliothek.gui.dock.station.split.SplitDockCombinerSource;
import bibliothek.gui.dock.station.split.SplitDockFullScreenProperty;
import bibliothek.gui.dock.station.split.SplitDockGrid;
import bibliothek.gui.dock.station.split.SplitDockPathProperty;
import bibliothek.gui.dock.station.split.SplitDockPlaceholderProperty;
import bibliothek.gui.dock.station.split.SplitDockProperty;
import bibliothek.gui.dock.station.split.SplitDockStationFactory;
import bibliothek.gui.dock.station.split.SplitDockTree;
import bibliothek.gui.dock.station.split.SplitDockTreeFactory;
import bibliothek.gui.dock.station.split.SplitDropTreeException;
import bibliothek.gui.dock.station.split.SplitFullScreenAction;
import bibliothek.gui.dock.station.split.SplitLayoutManager;
import bibliothek.gui.dock.station.split.SplitNode;
import bibliothek.gui.dock.station.split.SplitNodeVisitor;
import bibliothek.gui.dock.station.split.SplitPlaceholderConverter;
import bibliothek.gui.dock.station.split.SplitPlaceholderSet;
import bibliothek.gui.dock.station.split.SplitTreeFactory;
import bibliothek.gui.dock.station.split.PutInfo.Put;
import bibliothek.gui.dock.station.support.CombinerSource;
import bibliothek.gui.dock.station.support.CombinerTarget;
import bibliothek.gui.dock.station.support.DockStationListenerManager;
import bibliothek.gui.dock.station.support.DockableShowingManager;
import bibliothek.gui.dock.station.support.PlaceholderMap;
import bibliothek.gui.dock.station.support.PlaceholderStrategy;
import bibliothek.gui.dock.station.support.PlaceholderStrategyListener;
import bibliothek.gui.dock.station.support.RootPlaceholderStrategy;
import bibliothek.gui.dock.themes.DefaultDisplayerFactoryValue;
import bibliothek.gui.dock.themes.DefaultStationPaintValue;
import bibliothek.gui.dock.themes.StationCombinerValue;
import bibliothek.gui.dock.themes.ThemeManager;
import bibliothek.gui.dock.title.ActivityDockTitleEvent;
import bibliothek.gui.dock.title.ControllerTitleFactory;
import bibliothek.gui.dock.title.DockTitle;
import bibliothek.gui.dock.title.DockTitleFactory;
import bibliothek.gui.dock.title.DockTitleRequest;
import bibliothek.gui.dock.title.DockTitleVersion;
import bibliothek.gui.dock.util.BackgroundAlgorithm;
import bibliothek.gui.dock.util.ConfiguredBackgroundPanel;
import bibliothek.gui.dock.util.DockProperties;
import bibliothek.gui.dock.util.DockUtilities;
import bibliothek.gui.dock.util.PropertyKey;
import bibliothek.gui.dock.util.PropertyValue;
import bibliothek.gui.dock.util.icon.DockIcon;
import bibliothek.gui.dock.util.property.ConstantPropertyFactory;
import bibliothek.util.Path;
import bibliothek.util.Todo;
import bibliothek.util.Todo.Compatibility;
import bibliothek.util.Todo.Priority;
import bibliothek.util.Todo.Version;

/**
 * This station shows all its children at once. The children are separated
 * by small gaps which can be moved by the user. It is possible to set
 * one child to {@link #setFullScreen(Dockable) fullscreen}, this child will
 * be shown above all other children. The user can double click on the title
 * of a child to change its fullscreen-mode.<br>
 * The station tries to register a {@link DockTitleFactory} with the
 * ID {@link #TITLE_ID}.
 * @author Benjamin Sigg
 */
public class SplitDockStation extends SecureContainer implements Dockable, DockStation {
	/** The ID under which this station tries to register a {@link DockTitleFactory} */
	public static final String TITLE_ID = "split";

	/**
	 * Describes which {@link KeyEvent} will maximize/normalize the currently
	 * selected {@link Dockable}. 
	 */
	public static final PropertyKey<KeyStroke> MAXIMIZE_ACCELERATOR = new PropertyKey<KeyStroke>("SplitDockStation maximize accelerator");

	/**
	 * Defines the behavior of a {@link DockStation}, how to react on a
	 * drop-event, how to react on resize and other things related
	 * to the layout.
	 */
	public static final PropertyKey<SplitLayoutManager> LAYOUT_MANAGER = new PropertyKey<SplitLayoutManager>("SplitDockStation layout manager",
			new ConstantPropertyFactory<SplitLayoutManager>(new DefaultSplitLayoutManager()), true);

	/**
	 * The algorithm that allows users to resize children of a {@link SplitDockStation} by
	 * grabbing a gab between two children and moving that gap around.
	 */
	public static final PropertyKey<SplitDividerStrategy> DIVIDER_STRATEGY = new PropertyKey<SplitDividerStrategy>("SplitDockStation divider strategy",
			new ConstantPropertyFactory<SplitDividerStrategy>( new DefaultSplitDividerStrategy() ), true);
	
	/** The parent of this station */
	private DockStation parent;

	/** Listener registered to the parent. When triggered it invokes other listeners */
	private VisibleListener visibleListener = new VisibleListener();

	/** The controller to which this station is registered */
	private DockController controller;

	/** The theme of this station */
	private DockTheme theme;

	/** Combiner to {@link #dropOver(Leaf, Dockable, CombinerSource, CombinerTarget) combine} some Dockables */
	private StationCombinerValue combiner;

	/** The type of titles which are used for this station */
	private DockTitleVersion title;

	/** A list of {@link DockableListener} which will be invoked when something noticable happens */
	private List<DockableListener> dockableListeners = new ArrayList<DockableListener>();

	/** All {@link DockableStateListener}s of this station */
	private DockableStateListenerManager dockableStateListeners;
	
	/** an observer ensuring that the {@link DockHierarchyEvent}s are sent properly */
	private DockHierarchyObserver hierarchyObserver;

	/** A list of {@link SplitDockListener} which will be invoked when something noticable happens */
	private List<SplitDockListener> splitListeners = new ArrayList<SplitDockListener>();

	/** The handler for events and listeners concerning the visibility of children */
	private DockableShowingManager visibility;

	/** the DockTitles which are bound to this dockable */
	private List<DockTitle> titles = new LinkedList<DockTitle>();

	/** the list of actions offered for this Dockable */
	private HierarchyDockActionSource globalSource;

	/**
	 * The list of all registered {@link DockStationListener DockStationListeners}. 
	 * This list can be used to send events to all listeners.
	 */
	protected DockStationListenerManager dockStationListeners = new DockStationListenerManager(this);

	/** Optional text for this station */
	private PropertyValue<String> titleText = new PropertyValue<String>(PropertyKey.DOCK_STATION_TITLE){
		@Override
		protected void valueChanged( String oldValue, String newValue ){
			if( oldValue == null )
				oldValue = "";
			if( newValue == null )
				newValue = "";

			for( DockableListener listener : dockableListeners.toArray(new DockableListener[dockableListeners.size()]) )
				listener.titleTextChanged(SplitDockStation.this, oldValue, newValue);
		}
	};

	/** Optional icon for this station */
	private DockIcon titleIcon;
	
	/** Optional tooltip for this station */
	private PropertyValue<String> titleToolTip = new PropertyValue<String>(PropertyKey.DOCK_STATION_TOOLTIP){
		@Override
		protected void valueChanged( String oldValue, String newValue ){
			for( DockableListener listener : dockableListeners.toArray(new DockableListener[dockableListeners.size()]) )
				listener.titleToolTipChanged(SplitDockStation.this, oldValue, newValue);
		}
	};

	/** the manager for detailed control of the behavior of this station */
	private PropertyValue<SplitLayoutManager> layoutManager = new PropertyValue<SplitLayoutManager>(LAYOUT_MANAGER){
		@Override
		protected void valueChanged( SplitLayoutManager oldValue, SplitLayoutManager newValue ){
			if( oldValue != null )
				oldValue.uninstall(SplitDockStation.this);

			if( newValue != null )
				newValue.install(SplitDockStation.this);
		}
	};
	
	/** the strategy responsible for resizing the children of this station when the user moves a gap between them */
	private PropertyValue<SplitDividerStrategy> dividerStrategy = new PropertyValue<SplitDividerStrategy>(DIVIDER_STRATEGY){
		@Override
		protected void valueChanged( SplitDividerStrategy oldValue, SplitDividerStrategy newValue ){
			if( oldValue != null ){
				oldValue.uninstall( SplitDockStation.this );
			}
			if( newValue != null && content != null ){
				newValue.install( SplitDockStation.this, getContentPane() );
			}
		}
	};

	private PropertyValue<PlaceholderStrategy> placeholderStrategyProperty = new PropertyValue<PlaceholderStrategy>(PlaceholderStrategy.PLACEHOLDER_STRATEGY){
		@Override
		protected void valueChanged( PlaceholderStrategy oldValue, PlaceholderStrategy newValue ){
			placeholderStrategy.setStrategy(newValue);
		}
	};

	/** strategy for managing placeholders */
	private RootPlaceholderStrategy placeholderStrategy = new RootPlaceholderStrategy(this);

	/** Whether the user can double click on a child to expand it. Default is <code>true</code>. */
	private boolean expandOnDoubleclick = true;

	/** expands a child of this station when the user clicks twice on the child */
	private FullScreenListener fullScreenListener = new FullScreenListener();

	/** The list of {@link Dockable Dockables} which are shown on this station */
	private List<StationChildHandle> dockables = new ArrayList<StationChildHandle>();

	/** The {@link Dockable} which has the focus */
	private Dockable frontDockable;

	/** The {@link Dockable} which is currently in fullscreen-mode. This value might be <code>null</code> */
	private StationChildHandle fullScreenDockable;

	/** An action that is added to all children. The action changes the fullscreen-mode of the child. Can be <code>null</code> */
	private ListeningDockAction fullScreenAction;

	/** Size of the gap between two children in pixel */
	private int dividerSize = 4;

	/** 
	 * Relative size of the border where a {@link Dockable} will be placed aside 
	 * another Dockable when dragging the new Dockable onto this station. Should
	 * be between 0 and 0.25f.
	 */
	private float sideSnapSize = 1 / 4f;

	/** 
	 *  Size of the border outside this station where a {@link Dockable} will still
	 *  be considered to be dropped onto this station. Measured in pixel.
	 */
	private int borderSideSnapSize = 25;

	/** 
	 * Whether the bounds of this station are slightly bigger than the station itself.
	 * Used together with {@link #borderSideSnapSize} to grab Dockables "out of the sky".
	 * The default is <code>true</code>. 
	 */
	private boolean allowSideSnap = true;

	/** Access to the private and protected methods for some friends of this station */
	private Access access = new Access();

	/** The root of the tree which determines the structure of this station */
	private Root root;

	/** Ensures that no placeholder is used twice on this station */
	private SplitPlaceholderSet placeholderSet;

	/** Whether nodes can automatically be removed from the tree or not */
	private int treeLock = 0;

	/** Information about the {@link Dockable} which is currently draged onto this station. */
	private PutInfo putInfo;

	/** A {@link StationPaint} to draw some markings onto this station */
	private DefaultStationPaintValue paint;

	/** A {@link DisplayerFactory} used to create {@link DockableDisplayer} for the children of this station */
	private DefaultDisplayerFactoryValue displayerFactory;

	/** The set of displayers currently used by this station */
	private DisplayerCollection displayers;

	/**
	 * Whether the user can resize the content.
	 */
	private boolean resizingEnabled = true;

	/** If <code>true</code>, the components are resized while the split is dragged */
	private boolean continousDisplay = false;

	/** the configurable hints for the parent of this station */
	private DockableDisplayerHints hints;
	
	/** the parent of all {@link DockableDisplayer}s */
	private Content content;
	
	/** the background algorithm of this station */
	private Background background = new Background();

	/**
	 * Constructs a new {@link SplitDockStation}. 
	 */
	public SplitDockStation(){
		this( true );
	}

	/**
	 * Creates a new {@link SplitDockStation}. 
	 * @param createFullScreenAction whether {@link #createFullScreenAction()} should be called or not
	 */
	public SplitDockStation( boolean createFullScreenAction ){
		content = new Content();
		content.setBackground( background );
		setBasePane( content );

		placeholderSet = new SplitPlaceholderSet(access);
		dockableStateListeners = new DockableStateListenerManager( this );

		paint = new DefaultStationPaintValue( ThemeManager.STATION_PAINT + ".split", this );
		combiner = new StationCombinerValue( ThemeManager.COMBINER + ".split", this );
		displayerFactory = new DefaultDisplayerFactoryValue( ThemeManager.DISPLAYER_FACTORY + ".split", this );
		
		displayers = new DisplayerCollection(this, displayerFactory);
		displayers.addDockableDisplayerListener(new DockableDisplayerListener(){
			public void discard( DockableDisplayer displayer ){
				SplitDockStation.this.discard(displayer);
			}
		});

		if( createFullScreenAction ){
			fullScreenAction = createFullScreenAction();
		}
		visibility = new DockableShowingManager(dockStationListeners);

		dividerStrategy.getValue().install( this, getContentPane() );
		
		hierarchyObserver = new DockHierarchyObserver(this);
		globalSource = new HierarchyDockActionSource(this);
		globalSource.bind();
		
		titleIcon = new DockStationIcon( "dockStation.default", this ){
			protected void changed( Icon oldValue, Icon newValue ){
				for( DockableListener listener : dockableListeners.toArray( new DockableListener[ dockableListeners.size()] )){
					listener.titleIconChanged( SplitDockStation.this, oldValue, newValue );
				}
			}
		};

		addDockStationListener(new DockStationAdapter(){
			@Override
			public void dockableAdded( DockStation station, Dockable dockable ){
				updateConfigurableDisplayerHints();
			}

			@Override
			public void dockableRemoved( DockStation station, Dockable dockable ){
				updateConfigurableDisplayerHints();
			}
		});

		placeholderStrategy.addListener(new PlaceholderStrategyListener(){
			public void placeholderInvalidated( Set<Path> placeholders ){
				removePlaceholders(placeholders);
			}
		});
		
		addHierarchyListener( new HierarchyListener(){
			public void hierarchyChanged( HierarchyEvent e ){
				if( (e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 ){
					if( getDockParent() == null ){
						dockableStateListeners.checkShowing();
					}
					
					visibility.fire();
				}
			}
		});
	}

	/**
	 * Creates a new root for this station.
	 * @param access access to the internals of this station
	 * @return the new root
	 */
	protected Root createRoot( SplitDockAccess access ){
		return new Root(access);
	}

	/**
	 * Gets the root of this station, creates a root if necessary. This
	 * method cannot be overridden while {@link #getRoot()} can. This method
	 * just returns the value of {@link #root}, makes it a read only variable.
	 * @return the root
	 * @see #getRoot()
	 */
	protected final Root root(){
		if( root == null ) {
			root = createRoot(access);
		}
		return root;
	}

	@Override
	public String toString(){
		if( root == null ) {
			return super.toString();
		}
		else {
			return root.toString();
		}
	}

	@Override
	public Dimension getMinimumSize(){
		Insets insets = getInsets();
		Dimension base = getRoot().getMinimumSize();
		if( insets != null ) {
			base = new Dimension(base.width + insets.left + insets.right, base.height + insets.top + insets.bottom);
		}
		return base;
	}

	public DockTheme getTheme(){
		return theme;
	}

	public void updateTheme(){
		DockController controller = getController();
		if( controller != null ) {
			DockTheme newTheme = controller.getTheme();
			if( newTheme != theme ) {
				theme = newTheme;
				try {
					callDockUiUpdateTheme();
				}
				catch( IOException ex ) {
					throw new RuntimeException(ex);
				}
			}
		}
	}

	/**
	 * Calls the method {@link DockUI#updateTheme(DockStation, DockFactory)}
	 * with <code>this</code> as the first argument, and an appropriate factory
	 * as the second argument.
	 * @throws IOException if the DockUI throws an exception
	 */
	protected void callDockUiUpdateTheme() throws IOException{
		DockUI.updateTheme(this, new SplitDockStationFactory());
	}

	/**
	 * Creates an {@link DockAction action} which is added to all children
	 * of this station. The action allows the user to expand a child to
	 * fullscreen. The action is also added to subchildren, but the effect
	 * does only affect direct children of this station.
	 * @return the action or <code>null</code> if this feature should be
	 * disabled, or the action is {@link #setFullScreenAction(ListeningDockAction) set later}
	 */
	protected ListeningDockAction createFullScreenAction(){
		return new SplitFullScreenAction(this);
	}

	/**
	 * Sets an {@link DockAction action} which allows to expand children. This
	 * method can only be invoked if there is not already set an action. It is
	 * a condition that {@link #createFullScreenAction()} returns <code>null</code>
	 * @param fullScreenAction the new action
	 * @throws IllegalStateException if there is already an action present
	 */
	public void setFullScreenAction( ListeningDockAction fullScreenAction ){
		if( this.fullScreenAction != null )
			throw new IllegalStateException("The fullScreenAction can only be set once");
		this.fullScreenAction = fullScreenAction;
	}

	/**
	 * Sets whether a double click on a child or its title can expand the child 
	 * to fullscreen or not.
	 * @param expandOnDoubleclick <code>true</code> if the double click should 
	 * have an effect, <code>false</code> if double clicks should be ignored.
	 */
	public void setExpandOnDoubleclick( boolean expandOnDoubleclick ){
		this.expandOnDoubleclick = expandOnDoubleclick;
	}

	/**
	 * Tells whether a child expands to fullscreen when double clicked or not.
	 * @return <code>true</code> if a double click has an effect, <code>false</code>
	 * otherwise
	 * @see #setExpandOnDoubleclick(boolean)
	 */
	public boolean isExpandOnDoubleclick(){
		return expandOnDoubleclick;
	}

	/**
	 * Enables the user to resize the children of this station. The default value
	 * of this property is <code>true</code>. Note that resizing is a core
	 * functionality of this station, disabling it should be considered
	 * carefully.
	 * @param resizingEnabled whether resizing is enabled or not
	 */
	public void setResizingEnabled( boolean resizingEnabled ){
		this.resizingEnabled = resizingEnabled;
	}

	/**
	 * Tells whether the user can drag dividiers and resize dockables in this way.
	 * @return <code>true</code> if resizing is allowed
	 */
	public boolean isResizingEnabled(){
		return resizingEnabled;
	}

	public void setDockParent( DockStation station ){
		if( this.parent != null )
			this.parent.removeDockStationListener(visibleListener);

		parent = station;

		if( station != null )
			station.addDockStationListener(visibleListener);

		hierarchyObserver.update();
	}

	public DockStation getDockParent(){
		return parent;
	}

	public void setController( DockController controller ){
		super.setController( controller );
		if( this.controller != controller ) {
			if( this.controller != null ){
				this.controller.getDoubleClickController().removeListener(fullScreenListener);
			}

			for( StationChildHandle handle : dockables ) {
				handle.setTitleRequest(null);
			}

			this.controller = controller;
			getDisplayers().setController(controller);

			if( fullScreenAction != null )
				fullScreenAction.setController(controller);

			titleIcon.setController(controller);
			titleText.setProperties(controller);
			layoutManager.setProperties(controller);
			placeholderStrategyProperty.setProperties(controller);
			paint.setController( controller );
			displayerFactory.setController( controller );
			combiner.setController( controller );
			background.setController( controller );
			dividerStrategy.setProperties( controller );
			
			if( controller != null ) {
				title = controller.getDockTitleManager().getVersion(TITLE_ID, ControllerTitleFactory.INSTANCE);
				controller.getDoubleClickController().addListener(fullScreenListener);
			}
			else
				title = null;

			for( StationChildHandle handle : dockables ) {
				handle.setTitleRequest(title);
			}

			hierarchyObserver.controllerChanged(controller);
			visibility.fire();
		}
	}

	@Override
	public DockController getController(){
		return controller;
	}

	public void addDockableListener( DockableListener listener ){
		dockableListeners.add(listener);
	}

	public void removeDockableListener( DockableListener listener ){
		dockableListeners.remove(listener);
	}

	public void addDockHierarchyListener( DockHierarchyListener listener ){
		hierarchyObserver.addDockHierarchyListener(listener);
	}

	public void removeDockHierarchyListener( DockHierarchyListener listener ){
		hierarchyObserver.removeDockHierarchyListener(listener);
	}

	public void addMouseInputListener( MouseInputListener listener ){
		// ignore
	}

	public void removeMouseInputListener( MouseInputListener listener ){
		// ignore
	}

	public boolean accept( DockStation station ){
		return true;
	}

	public boolean accept( DockStation base, Dockable neighbour ){
		return true;
	}
	
	/**
	 * Tells whether <code>next</code> can be dropped over <code>old</code>.
	 * @param old some old dockable
	 * @param next some new dockable
	 * @return <code>true</code> if combining is allowed
	 */
	protected boolean acceptable( Dockable old, Dockable next ){
		if( !old.accept( this, next )){
			return false;
		}
		if( !next.accept( this, old )){
			return false;
		}
		DockController controller = getController();
		if( controller != null ){
			return controller.getAcceptance().accept( this, old, next );
		}
		return true;
	}
	
	/**
	 * Tells whether <code>next</code> can be dropped on this station
	 * @param next some new dockable
	 * @return <code>true</code> if combining is allowed
	 */
	protected boolean acceptable( Dockable next ){
		if( !accept( next )){
			return false;
		}
		if( !next.accept( this )){
			return false;
		}
		DockController controller = getController();
		if( controller != null ){
			return controller.getAcceptance().accept( this, next );
		}
		return true;
	}
	
	public Component getComponent(){
		return this;
	}

	public DockElement getElement(){
		return this;
	}

	public boolean isUsedAsTitle(){
		return false;
	}
	
	public boolean shouldFocus(){
    	return true;
    }
	
	public boolean shouldTransfersFocus(){
		return false;
	}

	public Point getPopupLocation( Point click, boolean popupTrigger ){
		if( popupTrigger )
			return click;
		else
			return null;
	}

	public String getTitleText(){
		String text = titleText.getValue();
		if( text == null )
			return "";
		else
			return text;
	}

	/**
	 * Sets the text of the title of this dockable.
	 * @param titleText the text displayed in the title
	 */
	public void setTitleText( String titleText ){
		this.titleText.setValue(titleText);
	}

	public String getTitleToolTip(){
		return titleToolTip.getValue();
	}

	/**
	 * Sets the tooltip that should be shown on any title that is {@link #bind(DockTitle) bound}
	 * to this dockable.
	 * @param text the tooltip, can be <code>null</code>
	 */
	public void setTitleToolTip( String text ){
		titleToolTip.setValue(text);
	}

	public Icon getTitleIcon(){
		return titleIcon.value();
	}

	/**
	 * Sets an icon that is shown in the {@link DockTitle titles} of this {@link Dockable}.
	 * @param titleIcon the icon or <code>null</code>
	 */
	public void setTitleIcon( Icon titleIcon ){
		this.titleIcon.setValue( titleIcon, true );
	}
	
    /**
     * Resets the icon of this {@link SplitDockStation}, the default icon is shown again.
     */
    public void resetTitleIcon(){
    	this.titleIcon.setValue( null );
    }

	/**
	 * Sets a special {@link SplitLayoutManager} which this station has to use.
	 * @param manager the manager or <code>null</code> to return to the 
	 * manager that is specified in the {@link DockProperties} by the key
	 * {@link #LAYOUT_MANAGER}.
	 */
	public void setSplitLayoutManager( SplitLayoutManager manager ){
		layoutManager.setValue(manager);
	}

	/**
	 * Gets the layout manager which was explicitly set.
	 * @return the manager or <code>null</code>
	 * @see #setSplitLayoutManager(SplitLayoutManager)
	 * @see #getCurrentSplitLayoutManager()
	 */
	public SplitLayoutManager getSplitLayoutManager(){
		return layoutManager.getOwnValue();
	}

	/**
	 * Gets the {@link SplitLayoutManager} that is currently used by this station.
	 * @return the currently used layout manager
	 * @see #setSplitLayoutManager(SplitLayoutManager)
	 */
	public SplitLayoutManager getCurrentSplitLayoutManager(){
		return layoutManager.getValue();
	}
	
	/**
	 * Gets the strategy for creating and storing placeholders. Note that this is not the same
	 * value as was set to {@link #setPlaceholderStrategy(PlaceholderStrategy)} 
	 * @return the strategy, never <code>null</code>
	 */
	public RootPlaceholderStrategy getPlaceholderStrategy(){
		return placeholderStrategy;
	}

	/**
	 * Sets the strategy for selecting placeholders when removing {@link Dockable}s from this 
	 * station.
	 * @param strategy the new strategy or <code>null</code> to install the default strategy
	 */
	public void setPlaceholderStrategy( PlaceholderStrategy strategy ){
		placeholderStrategyProperty.setValue(strategy);
	}

	/**
	 * Every child has an invisible border whose size is determined by <code>sideSnapSize</code>.
	 * If another {@link Dockable} is dragged into that border, it is added as neighbor.
	 * Otherwise it is merged with the present child.
	 * @param sideSnapSize the relative size of the border, should be between
	 * 0 and 0.5f
	 * @throws IllegalArgumentException if the size is less than 0
	 */
	public void setSideSnapSize( float sideSnapSize ){
		if( sideSnapSize < 0 )
			throw new IllegalArgumentException("sideSnapSize must not be less than 0");

		this.sideSnapSize = sideSnapSize;
	}

	/**
	 * Gets the relative size of the invisible border of all children.
	 * @return the size
	 * @see #setSideSnapSize(float)
	 */
	public float getSideSnapSize(){
		return sideSnapSize;
	}

	/**
	 * There is an invisible border around the station. If a {@link Dockable} is 
	 * dragged inside this border, its considered to be on the station, but
	 * will be dropped aside the station (like the whole station is a neighbor
	 * of the Dockable).
	 * @param borderSideSnapSize the size of the border in pixel
	 * @throws IllegalArgumentException if the size is smaller than 0
	 */
	public void setBorderSideSnapSize( int borderSideSnapSize ){
		if( borderSideSnapSize < 0 )
			throw new IllegalArgumentException("borderSideSnapeSize must not be less than 0");

		this.borderSideSnapSize = borderSideSnapSize;
	}

	/**
	 * Gets the size of the border around the station.
	 * @return the size in pixel
	 * @see #setBorderSideSnapSize(int)
	 */
	public int getBorderSideSnapSize(){
		return borderSideSnapSize;
	}

	/**
	 * Sets the size of the divider-gap between the children of this station.
	 * @param dividerSize the size of the gap in pixel
	 * @throws IllegalArgumentException if the size is less than 0.
	 */
	public void setDividerSize( int dividerSize ){
		if( dividerSize < 0 )
			throw new IllegalArgumentException("dividerSize must not be less than 0");

		this.dividerSize = dividerSize;
		doLayout();
	}

	/**
	 * Gets the size of the divider-gap.
	 * @return the size
	 * @see #setDividerSize(int)
	 */
	public int getDividerSize(){
		return dividerSize;
	}

	/**
	 * Sets whether the dockables should be resized while the split
	 * is dragged, or not.
	 * @param continousDisplay <code>true</code> if the dockables should
	 * be resized
	 */
	public void setContinousDisplay( boolean continousDisplay ){
		this.continousDisplay = continousDisplay;
	}

	/**
	 * Tells whether the dockables are resized while the split is
	 * dragged, or not.
	 * @return <code>true</code> if the dockables are resized
	 * @see #setContinousDisplay(boolean)
	 */
	public boolean isContinousDisplay(){
		return continousDisplay;
	}

	/**
	 * Sets whether {@link Dockable Dockables} which are dragged near
	 * the station are captured and added to this station.
	 * @param allowSideSnap <code>true</code> if the station can
	 * snap Dockables which are near.
	 * @see #setBorderSideSnapSize(int)
	 */
	public void setAllowSideSnap( boolean allowSideSnap ){
		this.allowSideSnap = allowSideSnap;
	}

	/**
	 * Tells whether the station can grab Dockables which are dragged
	 * near the station.
	 * @return <code>true</code> if grabbing is allowed
	 * @see #setAllowSideSnap(boolean)
	 */
	public boolean isAllowSideSnap(){
		return allowSideSnap;
	}

	public void requestDockTitle( DockTitleRequest request ){
		// ignore	
	}

	public void changed( Dockable dockable, DockTitle title, boolean active ){
		title.changed(new ActivityDockTitleEvent(this, dockable, active));
	}

	public void requestChildDockTitle( DockTitleRequest request ){
		// ignore	
	}

	public void bind( DockTitle title ){
		if( titles.contains(title) )
			throw new IllegalArgumentException("Title is already bound");
		titles.add(title);
		for( DockableListener listener : dockableListeners.toArray(new DockableListener[dockableListeners.size()]) )
			listener.titleBound(this, title);
	}

	public void unbind( DockTitle title ){
		if( !titles.contains(title) )
			throw new IllegalArgumentException("Title is unknown");
		titles.remove(title);
		for( DockableListener listener : dockableListeners.toArray(new DockableListener[dockableListeners.size()]) )
			listener.titleUnbound(this, title);
	}

	public DockTitle[] listBoundTitles(){
		return titles.toArray(new DockTitle[titles.size()]);
	}

	public DockActionSource getLocalActionOffers(){
		return null;
	}

	public DockActionSource getGlobalActionOffers(){
		return globalSource;
	}

	public void configureDisplayerHints( DockableDisplayerHints hints ){
		this.hints = hints;
		updateConfigurableDisplayerHints();
	}

	/**
	 * Gets the argument that was last used for
	 * {@link #configureDisplayerHints(DockableDisplayerHints)}. 
	 * @return the configurable hints or <code>null</code>
	 */
	protected DockableDisplayerHints getConfigurableDisplayerHints(){
		return hints;
	}

	/**
	 * Updates the {@link #getConfigurableDisplayerHints() current hints}
	 * of this station.
	 */
	protected void updateConfigurableDisplayerHints(){
		if( hints != null ) {
			if( getDockableCount() == 0 )
				hints.setShowBorderHint(Boolean.TRUE);
			else
				hints.setShowBorderHint(Boolean.FALSE);
		}
	}

	public DockStation asDockStation(){
		return this;
	}

	public DefaultDockActionSource getDirectActionOffers( Dockable dockable ){
		if( fullScreenAction == null )
			return null;
		else {
			DefaultDockActionSource source = new DefaultDockActionSource(new LocationHint(LocationHint.DIRECT_ACTION, LocationHint.VERY_RIGHT));
			source.add(fullScreenAction);

			return source;
		}
	}

	public DockActionSource getIndirectActionOffers( Dockable dockable ){
		if( fullScreenAction == null )
			return null;

		DockStation parent = dockable.getDockParent();
		if( parent == null )
			return null;

		if( parent instanceof SplitDockStation )
			return null;

		dockable = parent.asDockable();
		if( dockable == null )
			return null;

		parent = dockable.getDockParent();
		if( parent != this )
			return null;

		DefaultDockActionSource source = new DefaultDockActionSource(fullScreenAction);
		source.setHint(new LocationHint(LocationHint.INDIRECT_ACTION, LocationHint.VERY_RIGHT));
		return source;
	}

	public void addDockStationListener( DockStationListener listener ){
		dockStationListeners.addListener(listener);
	}

	public void removeDockStationListener( DockStationListener listener ){
		dockStationListeners.removeListener(listener);
	}

	public void addDockableStateListener( DockableStateListener listener ){
		dockableStateListeners.addListener( listener );	
	}
	
	public void removeDockableStateListener( DockableStateListener listener ){
		dockableStateListeners.removeListener( listener );
	}
	
	/**
	 * Adds a listener to this station. The listener is informed some 
	 * settings only available to a {@link SplitDockStation} are changed.
	 * @param listener the new listener
	 */
	public void addSplitDockStationListener( SplitDockListener listener ){
		splitListeners.add(listener);
	}

	/**
	 * Removes an earlier added listener.
	 * @param listener The listener to remove
	 */
	public void removeSplitDockStationListener( SplitDockListener listener ){
		splitListeners.remove(listener);
	}

	public boolean isChildShowing( Dockable dockable ){
		return isVisible( dockable );
	}
	
	@Deprecated
	@Todo( compatibility=Compatibility.BREAK_MAJOR, priority=Priority.ENHANCEMENT, target=Version.VERSION_1_1_3, description="remove this method" )
	public boolean isVisible( Dockable dockable ){
		return isStationVisible() && (!isFullScreen() || dockable == getFullScreen());
	}

	public boolean isStationShowing(){
		return isStationVisible();
	}
	
	@Deprecated
	@Todo( compatibility=Compatibility.BREAK_MAJOR, priority=Priority.ENHANCEMENT, target=Version.VERSION_1_1_3, description="remove this method" )
	public boolean isStationVisible(){
		return isDockableVisible();
	}

	public boolean isDockableShowing(){
		return isDockableVisible();
	}
	
	@Deprecated
	@Todo( compatibility=Compatibility.BREAK_MAJOR, priority=Priority.ENHANCEMENT, target=Version.VERSION_1_1_3, description="remove this method" )
	public boolean isDockableVisible(){
    	DockController controller = getController();
    	if( controller == null ){
    		return false;
    	}
    	DockStation parent = getDockParent();
    	if( parent != null ){
    		return parent.isChildShowing( this );
    	}
    	return isShowing();
    }

	public int getDockableCount(){
		return dockables.size();
	}

	public Dockable getDockable( int index ){
		return dockables.get(index).getDockable();
	}

	public DockableProperty getDockableProperty( Dockable child, Dockable target ){
		DockableProperty result = getDockablePlaceholderProperty(child, target);
		if( result == null ) {
			result = getDockablePathProperty(child);
		}
		return result;
	}

	/**
	 * Creates a {@link DockableProperty} for the location of <code>dockable</code>.
	 * The location is encoded as the path through the tree to get to <code>dockable</code>.
	 * @param dockable the element whose location is searched
	 * @return the location
	 */
	public SplitDockPathProperty getDockablePathProperty( final Dockable dockable ){
		final SplitDockPathProperty path = new SplitDockPathProperty();
		root().submit(new SplitTreeFactory<Object>(){
			public Object leaf( Dockable check, long id, Path[] placeholders, PlaceholderMap placeholderMap ){
				if( dockable == check ) {
					path.setLeafId(id);
					return this;
				}
				return null;
			}

			public Object placeholder( long id, Path[] placeholders, PlaceholderMap placeholderMap ){
				return null;
			}

			public Object root( Object root, long id ){
				return root;
			}

			public Object horizontal( Object left, Object right, double divider, long id, Path[] placeholders, PlaceholderMap placeholderMap, boolean visible ){
				if( left != null ) {
					if( visible ) {
						path.insert(SplitDockPathProperty.Location.LEFT, divider, 0, id);
					}
					return left;
				}
				if( right != null ) {
					if( visible ) {
						path.insert(SplitDockPathProperty.Location.RIGHT, 1 - divider, 0, id);
					}
					return right;
				}
				return null;
			}

			public Object vertical( Object top, Object bottom, double divider, long id, Path[] placeholders, PlaceholderMap placeholderMap, boolean visible ){
				if( top != null ) {
					if( visible ) {
						path.insert(SplitDockPathProperty.Location.TOP, divider, 0, id);
					}
					return top;
				}
				if( bottom != null ) {
					if( visible ) {
						path.insert(SplitDockPathProperty.Location.BOTTOM, 1 - divider, 0, id);
					}
					return bottom;
				}
				return null;
			}
		});
		return path;
	}

	/**
	 * Creates a {@link DockableProperty} for the location of <code>dockable</code>.
	 * The location is encoded directly as the coordinates x,y,width and height
	 * of the <code>dockable</code>.
	 * @param dockable the element whose location is searched
	 * @return the location
	 */
	public SplitDockProperty getDockableLocationProperty( Dockable dockable ){
		Leaf leaf = getRoot().getLeaf(dockable);
		return new SplitDockProperty(leaf.getX(), leaf.getY(), leaf.getWidth(), leaf.getHeight());
	}

	/**
	 * Creates a {@link SplitDockPlaceholderProperty} for <code>dockable</code>, may
	 * insert an additional placeholder in the tree.
	 * @param dockable the element whose location is searched
	 * @param target hint required to find the placeholder
	 * @return the placeholder or <code>null</code> if the {@link #getPlaceholderStrategy() strategy}
	 * did not assign a placeholder to <code>dockable</code>
	 */
	public SplitDockPlaceholderProperty getDockablePlaceholderProperty( Dockable dockable, Dockable target ){
		Leaf leaf = getRoot().getLeaf(dockable);
		if( leaf == null ) {
			throw new IllegalArgumentException("dockable not known to this station");
		}

		Path placeholder = getPlaceholderStrategy().getPlaceholderFor(target == null ? dockable : target);
		if( placeholder == null ) {
			return null;
		}

		placeholderSet.set(leaf, placeholder);

		return new SplitDockPlaceholderProperty(placeholder, getDockablePathProperty(dockable));
	}

	public Dockable getFrontDockable(){
		if( isFullScreen() )
			return getFullScreen();

		if( frontDockable == null && dockables.size() > 0 )
			frontDockable = dockables.get(0).getDockable();

		return frontDockable;
	}

	public void setFrontDockable( Dockable dockable ){
		Dockable old = getFrontDockable();

		this.frontDockable = dockable;
		if( isFullScreen() && dockable != null )
			setFullScreen(dockable);

		if( old != dockable ){
			access.dockableSelected( old );
		}
	}

	/**
	 * Tells whether a {@link Dockable} is currently shown in fullscreen-mode
	 * on this station. A <code>true</code> result implies that
	 * {@link #getFullScreen()} returns not <code>null</code>.
	 * @return <code>true</code> if a child is fullscreen. 
	 */
	public boolean isFullScreen(){
		return fullScreenDockable != null;
	}

	/**
	 * Gets the {@link Dockable} which is in fullscreen-mode and covers all
	 * other children of this station.
	 * @return the child or <code>null</code>
	 * @see #setFullScreen(Dockable)
	 * @see #isFullScreen()
	 */
	public Dockable getFullScreen(){
		return fullScreenDockable == null ? null : fullScreenDockable.getDockable();
	}
	
	/**
	 * Tells whether {@link #createFullScreenAction()} was called and returned a value other
	 * than <code>null</code>.
	 * @return <code>true</code> if this station shows a fullscreen-action
	 */
	public boolean hasFullScreenAction(){
		return fullScreenAction != null;
	}

	/**
	 * Sets one of the children of this station as the one child which covers
	 * all other children. This child is in "fullscreen"-mode.
	 * @param dockable a child of this station or <code>null</code> if
	 * all children should be visible.
	 * @see #isFullScreen()
	 */
	public void setFullScreen( Dockable dockable ){
		try{
			access.arm();
			dockable = layoutManager.getValue().willMakeFullscreen(this, dockable);
			Dockable oldFullScreen = getFullScreen();
			if( oldFullScreen != dockable ) {
				if( dockable != null ) {
					access.repositioned.add( dockable );
					Leaf leaf = getRoot().getLeaf(dockable);
					if( leaf == null )
						throw new IllegalArgumentException("Dockable not child of this station");
	
					fullScreenDockable = leaf.getDockableHandle();
	
					for( StationChildHandle handle : dockables ) {
						handle.getDisplayer().getComponent().setVisible(handle == fullScreenDockable);
					}
				}
				else {
					fullScreenDockable = null;
					for( StationChildHandle handle : dockables ) {
						handle.getDisplayer().getComponent().setVisible(true);
					}
				}
	
				if( oldFullScreen != null ){
					access.repositioned.add( oldFullScreen );
				}
				
				doLayout();
				fireFullScreenChanged(oldFullScreen, getFullScreen());
				visibility.fire();
			}
		}
		finally{
			access.fire();
		}
	}

	/**
	 * Switches the child which is in fullscreen-mode. If there is no child,
	 * nothing will happen. If there is only one child, it will be set to
	 * fullscreen (if it is not already fullscreen).
	 */
	public void setNextFullScreen(){
		if( dockables.size() > 0 ) {
			if( fullScreenDockable == null )
				setFullScreen(getDockable(0));
			else {
				int index = indexOfDockable(fullScreenDockable.getDockable());
				index++;
				index %= getDockableCount();
				setFullScreen(getDockable(index));
			}
		}
	}

	public boolean accept( Dockable child ){
		return true;
	}

	public PlaceholderMap getPlaceholders(){
		return createPlaceholderConverter().getPlaceholders();
	}
	
	public void setPlaceholders( PlaceholderMap placeholders ){
		createPlaceholderConverter().setPlaceholders( placeholders );
	}
	
	/**
	 * Creates the algorithm that is used by {@link #getPlaceholders()} and {@link #setPlaceholders(PlaceholderMap)}.
	 * @return the algorithm to handle {@link PlaceholderMap}s, not <code>null</code>
	 */
	protected SplitPlaceholderConverter createPlaceholderConverter(){
		return new SplitPlaceholderConverter( this );
	}

	public StationDropOperation prepareDrop( int x, int y, int titleX, int titleY, boolean checkOverrideZone, Dockable dockable ){
		PutInfo putInfo = null;
		boolean move = dockable.getDockParent() == this;
		
		if( move ){
			putInfo = layoutManager.getValue().prepareMove(this, x, y, titleX, titleY, checkOverrideZone, dockable);
			if( putInfo != null ){
				prepareCombine( putInfo, x, y, move );
			}
		}
		else{
			if( SwingUtilities.isDescendingFrom(getComponent(), dockable.getComponent()) ){
				putInfo = null;
			}
			else{
				putInfo = layoutManager.getValue().prepareDrop(this, x, y, titleX, titleY, checkOverrideZone, dockable);
			}
			
			if( putInfo != null ){
				prepareCombine( putInfo, x, y, move );
			}
		}
		if( putInfo == null ){
			return null;
		}
		return new SplitDropOperation( putInfo, move );
	}
	
	/**
	 * Gets the location where the currently dragged {@link Dockable} would be dropped.
	 * @return a possible location, may be <code>null</code>
	 */
	public PutInfo getDropInfo(){
		return putInfo;
	}
	
	private void prepareCombine( PutInfo putInfo, int x, int y, boolean move ){
		if( putInfo.getCombinerSource() == null && putInfo.getCombinerTarget() == null ){
			if( putInfo.getNode() instanceof Leaf ){
				Point mouseOnStation = new Point( x, y );
				SwingUtilities.convertPointFromScreen( mouseOnStation, getComponent() );
				
				SplitDockCombinerSource source = new SplitDockCombinerSource( putInfo, this, mouseOnStation );
					
				CombinerTarget target = getCombiner().prepare( source, putInfo.getPut() == PutInfo.Put.CENTER || putInfo.getPut() == PutInfo.Put.TITLE );
				if( target == null && putInfo.isCombining() && putInfo.getDockable().asDockStation() != null ){
					DockController controller = getController();
					if( controller != null ){
						Merger merger = controller.getRelocator().getMerger();
						
						target = getCombiner().prepare( source, true );
						putInfo.setCombination( source, target );

						if( !merger.canMerge( new SplitDropOperation( putInfo, move ), this, putInfo.getDockable().asDockStation() ) ){
							putInfo.setCombination( null, null );
						}
					}
				}
				
				putInfo.setCombination( source, target );
			}
		}
	}
	

	public void drop( Dockable dockable ){
		addDockable( dockable, null );
	}

	public boolean drop( Dockable dockable, DockableProperty property ){
		if( property instanceof SplitDockProperty ) {
			return drop(dockable, (SplitDockProperty) property);
		}
		else if( property instanceof SplitDockPathProperty ) {
			return drop(dockable, (SplitDockPathProperty) property);
		}
		else if( property instanceof SplitDockPlaceholderProperty ) {
			return drop(dockable, (SplitDockPlaceholderProperty) property);
		}
		else if( property instanceof SplitDockFullScreenProperty ) {
			return drop(dockable, (SplitDockFullScreenProperty) property);
		}
		else {
			return false;
		}
	}

	/**
	 * Tries to add <code>Dockable</code> such that the boundaries given
	 * by <code>property</code> are full filled.
	 * @param dockable a new child of this station
	 * @param property the preferred location of the child
	 * @return <code>true</code> if the child could be added, <code>false</code>
	 * if no location could be found
	 */
	public boolean drop( Dockable dockable, SplitDockProperty property ){
		return drop(dockable, property, root());
	}

	/**
	 * Tries to add <code>Dockable</code> such that the boundaries given
	 * by <code>property</code> are full filled.
	 * @param dockable a new child of this station
	 * @param property the preferred location of the child
	 * @param root the root of all possible parents where the child could be inserted
	 * @return <code>true</code> if the child could be added, <code>false</code>
	 * if no location could be found
	 */
	private boolean drop( final Dockable dockable, final SplitDockProperty property, SplitNode root ){
		try{
			access.arm();
			DockUtilities.checkLayoutLocked();
			if( getDockableCount() == 0 ) {
				if( !acceptable( dockable )){
					return false;
				}
				
				drop(dockable);
				return true;
			}
	
			updateBounds();
	
			class DropInfo {
				public Leaf bestLeaf;
				public double bestLeafIntersection;
	
				public SplitNode bestNode;
				public double bestNodeIntersection = Double.POSITIVE_INFINITY;
				public PutInfo.Put bestNodePut;
			}
	
			final DropInfo info = new DropInfo();
	
			root.visit(new SplitNodeVisitor(){
				public void handleLeaf( Leaf leaf ){
					double intersection = leaf.intersection(property);
					if( intersection > info.bestLeafIntersection ) {
						info.bestLeafIntersection = intersection;
						info.bestLeaf = leaf;
					}
	
					handleNeighbour(leaf);
				}
	
				public void handleNode( Node node ){
					if( node.isVisible() ) {
						handleNeighbour(node);
					}
				}
	
				public void handleRoot( Root root ){
					// do nothing
				}
	
				public void handlePlaceholder( Placeholder placeholder ){
					// ignore	
				}
	
				private void handleNeighbour( SplitNode node ){
					if( acceptable( dockable )){
						double x = node.getX();
						double y = node.getY();
						double width = node.getWidth();
						double height = node.getHeight();
		
						double left = Math.abs(x - property.getX());
						double right = Math.abs(x + width - property.getX() - property.getWidth());
						double top = Math.abs(y - property.getY());
						double bottom = Math.abs(y + height - property.getY() - property.getHeight());
		
						double value = left + right + top + bottom;
						value -= Math.max(Math.max(left, right), Math.max(top, bottom));
		
						double kx = property.getX() + property.getWidth() / 2;
						double ky = property.getY() + property.getHeight() / 2;
		
						PutInfo.Put put = node.relativeSidePut(kx, ky);
		
						double px, py;
		
						if( put == PutInfo.Put.TOP ) {
							px = x + 0.5 * width;
							py = y + 0.25 * height;
						}
						else if( put == PutInfo.Put.BOTTOM ) {
							px = x + 0.5 * width;
							py = y + 0.75 * height;
						}
						else if( put == PutInfo.Put.LEFT ) {
							px = x + 0.25 * width;
							py = y + 0.5 * height;
						}
						else {
							px = x + 0.5 * width;
							py = y + 0.75 * height;
						}
		
						double distance = Math.pow((kx - px) * (kx - px) + (ky - py) * (ky - py), 0.25);
		
						value *= distance;
		
						if( value < info.bestNodeIntersection ) {
							info.bestNodeIntersection = value;
							info.bestNode = node;
							info.bestNodePut = put;
						}
					}
				}
			});
	
			if( info.bestLeaf != null ) {
				DockStation station = info.bestLeaf.getDockable().asDockStation();
				DockableProperty successor = property.getSuccessor();
				if( station != null && successor != null ) {
					if( station.drop(dockable, successor) ) {
						validate();
						return true;
					}
				}
	
				if( info.bestLeafIntersection > 0.75 ) {
					if( station != null && station.accept(dockable) && dockable.accept(station) ) {
						station.drop(dockable);
						validate();
						return true;
					}
					else {
						boolean result = dropOver(info.bestLeaf, dockable, property.getSuccessor(), null, null);
						validate();
						return result;
					}
				}
			}
	
			if( info.bestNode != null ) {
				if( !acceptable( dockable )){
					return false;
				}
	
				double divider = 0.5;
				if( info.bestNodePut == PutInfo.Put.LEFT ) {
					divider = property.getWidth() / info.bestNode.getWidth();
				}
				else if( info.bestNodePut == PutInfo.Put.RIGHT ) {
					divider = 1 - property.getWidth() / info.bestNode.getWidth();
				}
				else if( info.bestNodePut == PutInfo.Put.TOP ) {
					divider = property.getHeight() / info.bestNode.getHeight();
				}
				else if( info.bestNodePut == PutInfo.Put.BOTTOM ) {
					divider = 1 - property.getHeight() / info.bestNode.getHeight();
				}
	
				divider = Math.max(0, Math.min(1, divider));
				return dropAside( info.bestNode, info.bestNodePut, dockable, null, divider, null );
			}
	
			repaint();
			return false;
		}
		finally{
			access.fire();
		}
	}

	/**
	 * Tries to insert <code>dockable</code> at a location such that the path
	 * to that location is the same as described in <code>property</code>.
	 * @param dockable the element to insert
	 * @param property the preferred path to the element
	 * @return <code>true</code> if the element was successfully inserted
	 */
	public boolean drop( Dockable dockable, SplitDockPathProperty property ){
		try{
			access.arm();
			DockUtilities.checkLayoutLocked();
	
			// use the ids of the topmost nodes in the path to find a node of this station
			int index = 0;
			SplitNode start = null;
	
			long leafId = property.getLeafId();
			if( leafId != -1 ) {
				start = getNode(leafId);
				if( start != null ) {
					index = property.size();
				}
			}
			if( start == null ) {
				for( index = property.size() - 1; index >= 0; index-- ) {
					SplitDockPathProperty.Node node = property.getNode(index);
					long id = node.getId();
					if( id != -1 ) {
						start = getNode(id);
						if( start != null ){
							break;
						}
					}
				}
			}
	
			if( start == null || index < 0 ) {
				start = root();
				index = 0;
			}
	
			updateBounds();
			boolean done = start.insert(property, index, dockable);
			if( done )
				revalidate();
			return done;
		}
		finally{
			access.fire();
		}
	}

	/**
	 * Drops <code>dockable</code> at the placeholder that is referenced by <code>property</code>. This
	 * action removes the placeholder from the tree.
	 * @param dockable the element to add
	 * @param property the location of <code>dockable</code>
	 * @return <code>true</code> if the the operation was a success, <code>false</code> if not
	 */
	public boolean drop( Dockable dockable, SplitDockPlaceholderProperty property ){
		try{
			access.arm();
			DockUtilities.checkLayoutLocked();
			validate();
			return root().insert(property, dockable);
		}
		finally{
			access.fire();
		}
	}

	/**
	 * Drops <code>dockable</code> on this station, may exchange the full screen element to ensure that
	 * <code>dockable</code> is displayed.
	 * @param dockable the element to drop
	 * @param property the location of <code>dockable</code>
	 * @return <code>true</code> if the operation was a success, <code>false</code> if not
	 */
	public boolean drop( Dockable dockable, SplitDockFullScreenProperty property ){
		try{
			access.arm();
			DockUtilities.checkLayoutLocked();
	
			DockableProperty successor = property.getSuccessor();
			if( dockable.getDockParent() == this ) {
				setFullScreen(dockable);
				return true;
			}
			
			Dockable currentFullScreen = getFullScreen();
			if( currentFullScreen == null ) {
				return false;
			}
			
			DockStation currentFullScreenStation = currentFullScreen.asDockStation();
			if( currentFullScreenStation != null ){
				if( successor != null ){
					if( currentFullScreenStation.drop( dockable, successor )){
						return true;
					}
				}
				return false;
			}
			else{
				Leaf leaf = getRoot().getLeaf(currentFullScreen);
				setFullScreen(null);
				if( !dropOver(leaf, dockable, successor, null, null) ){
					return false;
				}
				
				Dockable last = dockable;
				while( dockable != null && dockable != this ){
					last = dockable;
					DockStation station = dockable.getDockParent();
					dockable = station == null ? null : station.asDockable();
				}
				
				if( last != null ){
					setFullScreen(last);
				}
				return true;
			}
		}
		finally{
			access.fire();
		}
	}

	/**
	 * Combines the {@link Dockable} of <code>leaf</code> and <code>dockable</code>
	 * to a new child of this station. No checks whether the two elements accepts
	 * each other nor if the station accepts the new child <code>dockable</code>
	 * are performed.
	 * @param leaf the leaf which will be combined with <code>dockable</code>
	 * @param dockable a {@link Dockable} which is dropped over <code>leaf</code>
	 * @param source information about the combination, may be <code>null</code>
	 * @param target information about the combination, may be <code>null</code>
	 * @return <code>true</code> if the operation was successful, <code>false</code>
	 * otherwise
	 */
	protected boolean dropOver( Leaf leaf, Dockable dockable, CombinerSource source, CombinerTarget target ){
		return dropOver(leaf, dockable, null, source, target );
	}

	/**
	 * Combines the {@link Dockable} of <code>leaf</code> and <code>dockable</code>
	 * to a new child of this station. No checks whether the two elements accepts
	 * each other nor if the station accepts the new child <code>dockable</code>
	 * are performed.
	 * @param leaf the leaf which will be combined with <code>dockable</code>
	 * @param dockable a {@link Dockable} which is dropped over <code>leaf</code>
	 * @param property a hint at which position <code>dockable</code> should be
	 * in the combination.
	 * @param source information about the combination, may be <code>null</code>
	 * @param target information about the combination, may be <code>null</code>
	 * @return <code>true</code> if the operation was successful, <code>false</code>
	 * otherwise
	 */
	protected boolean dropOver( Leaf leaf, Dockable dockable, DockableProperty property, CombinerSource source, CombinerTarget target ){
		if( !acceptable( leaf.getDockable(), dockable )){
			return false;
		}
		
		try{
			access.arm();
			DockUtilities.checkLayoutLocked();
			DockUtilities.ensureTreeValidity(this, dockable);
	
			if( source == null || target == null ){
				PutInfo info = new PutInfo( leaf, Put.TITLE, dockable, true );
				source = new SplitDockCombinerSource( info, this, null );
				target = combiner.prepare( source, true );
			}
			
			if( leaf.getDockable() != null ){
				DockHierarchyLock.Token token = DockHierarchyLock.acquireUnlinking( this, leaf.getDockable() );
				try{
					leaf.setDockable( null, token );
				}
				finally{
					token.release();
				}
			}
			
			Dockable combination = combiner.combine( source, target );
			leaf.setPlaceholderMap(null);
	
			if( property != null ) {
				DockStation combinedStation = combination.asDockStation();
				if( combinedStation != null && dockable.getDockParent() == combinedStation ) {
					combinedStation.move(dockable, property);
				}
			}
	
			DockHierarchyLock.Token token = DockHierarchyLock.acquireLinking( this, combination );
			try{
				dockStationListeners.fireDockableAdding(combination);
				leaf.setDockable( combination, token );
		
				dockStationListeners.fireDockableAdded(combination);
			}
			finally{
				token.release();
			}
			revalidate();
			repaint();
			return true;
		}
		finally{
			access.fire();
		}
	}

	/**
	 * Adds <code>dockable</code> at the side <code>put</code> of 
	 * <code>neighbor</code>. The divider is set to the value of <code>divider</code>,
	 * and if <code>fire</code> is activated, some events are fired. There are
	 * no checks whether <code>dockable</code> accepts this station or anything
	 * else.
	 * @param neighbor The node which will be the neighbor of <code>dockable</code>
	 * @param put The side on which <code>dockable</code> should be added in 
	 * respect to <code>neighbor</code>.
	 * @param dockable the new child of this station
	 * @param leaf the leaf which contains <code>dockable</code>, can be <code>null</code>
	 * @param divider the divider-location, a value between 0 and 1
     * @param token if <code>null</code>, then a token will be acquired by this method
     * and this method will fire events, otherwise this methods is executed silently
     * @return <code>true</code> if the operation was a success, <code>false</code> otherwise
	 */
	protected boolean dropAside( SplitNode neighbor, PutInfo.Put put, Dockable dockable, Leaf leaf, double divider, DockHierarchyLock.Token token ){
		if( !acceptable( dockable )){
			return false;
		}
		
		try{
			boolean fire = token == null;
			access.arm();
			DockUtilities.checkLayoutLocked();
			if( fire ) {
				DockUtilities.ensureTreeValidity(this, dockable);
				token = DockHierarchyLock.acquireLinking( this, dockable );
			}
			try{
				if( fire ){
					dockStationListeners.fireDockableAdding(dockable);
				}
		
				boolean leafSet = false;
		
				if( leaf == null ) {
					leaf = new Leaf(access);
					leafSet = true;
				}
		
				SplitNode parent = neighbor.getParent();
		
				// Node herstellen
				Node node = null;
				updateBounds();
				int location = parent.getChildLocation(neighbor);
		
				if( put == PutInfo.Put.TOP ) {
					node = new Node(access, leaf, neighbor, Orientation.VERTICAL);
				}
				else if( put == PutInfo.Put.BOTTOM ) {
					node = new Node(access, neighbor, leaf, Orientation.VERTICAL);
				}
				else if( put == PutInfo.Put.LEFT ) {
					node = new Node(access, leaf, neighbor, Orientation.HORIZONTAL);
				}
				else {
					node = new Node(access, neighbor, leaf, Orientation.HORIZONTAL);
				}
		
				node.setDivider(divider);
				parent.setChild(node, location);
		
				if( leafSet ) {
					leaf.setDockable( dockable, token );
				}
		
				if( fire ) {
					dockStationListeners.fireDockableAdded(dockable);
				}
				revalidate();
				repaint();
			}
			finally{
				if( fire ){
					token.release();
				}
			}
		}
		finally{
			access.fire();
		}
		return true;
	}

	public void move( Dockable dockable, DockableProperty property ){
		// do nothing
	}

	/**
	 * Copies the layout of <code>grid</code>. Any changes to <code>grid</code>
	 * afterwards will not affect this station, nor will changes on this station
	 * affect <code>grid</code>. 
	 * @param grid the layout to drop, not <code>null</code>
	 * @see #dropTree(SplitDockTree)
	 */
	public void dropGrid( SplitDockGrid grid ){
		dropTree( grid.toTree() );
	}
	
	/**
	 * Removes all children from this station and then adds the contents
	 * that are stored in <code>tree</code>. Calling this method is equivalent
	 * to <code>dropTree( tree, true );</code>
	 * @param tree the new set of children
	 * @throws SplitDropTreeException If the tree is not acceptable.
	 */
	public void dropTree( SplitDockTree<Dockable> tree ){
		dropTree(tree, true);
	}

	/**
	 * Removes all children from this station and then adds the contents
	 * that are stored in <code>tree</code>.
	 * @param tree the new set of children
	 * @param checkValidity whether to ensure that the new elements are
	 * accepted or not.
	 * @throws SplitDropTreeException if <code>checkValidity</code> is
	 * set to <code>true</code> and the tree is not acceptable
	 */
	public void dropTree( SplitDockTree<Dockable> tree, boolean checkValidity ){
		if( tree == null )
			throw new IllegalArgumentException("Tree must not be null");
		DockUtilities.checkLayoutLocked();
		DockController controller = getController();
		try {
			access.arm();
			
			treeLock++;
			if( controller != null ){
				controller.freezeLayout();
			}

			setFullScreen(null);
			removeAllDockables();

			// ensure valid tree
			for( Dockable dockable : tree.getDockables() ) {
				DockUtilities.ensureTreeValidity(this, dockable);
			}

			SplitDockTree<Dockable>.Key rootKey = tree.getRoot();
			if( rootKey != null ) {
				Map<Leaf, Dockable> linksToSet = new HashMap<Leaf, Dockable>();
				root().evolve(rootKey, checkValidity, linksToSet);
				for( Map.Entry<Leaf, Dockable> entry : linksToSet.entrySet() ) {
					entry.getKey().setDockable( entry.getValue(), null );
				}
				updateBounds();
			}
		}
		finally {
			treeLock--;
			if( controller != null ){
				controller.meltLayout();
			}
			access.fire();
		}
	}

	/**
	 * Gets the contents of this station as a {@link SplitDockTree}.
	 * @return the tree
	 */
	public DockableSplitDockTree createTree(){
		DockableSplitDockTree tree = new DockableSplitDockTree();
		createTree(new SplitDockTreeFactory(tree));
		return tree;
	}

	/**
	 * Writes the contents of this station into <code>factory</code>.
	 * @param factory the factory to write into
	 */
	public void createTree( SplitDockTreeFactory factory ){
		root().submit(factory);
	}

	/**
	 * Visits the internal structure of this station.
	 * @param <N> the type of result this method produces
	 * @param factory a factory that will collect information
	 * @return the result of <code>factory</code>
	 */
	public <N> N visit( SplitTreeFactory<N> factory ){
		return root().submit(factory);
	}

	public <D extends Dockable & DockStation> boolean isInOverrideZone( int x, int y, D invoker, Dockable drop ){
		if( isFullScreen() )
			return false;

		if( getDockParent() != null && getDockParent().isInOverrideZone(x, y, invoker, drop) )
			return true;

		Point point = new Point(x, y);
		SwingUtilities.convertPointFromScreen(point, this);

		return root().isInOverrideZone(point.x, point.y);
	}

	public boolean canDrag( Dockable dockable ){
		return true;
	}

	public void drag( Dockable dockable ){
		if( dockable.getDockParent() != this )
			throw new IllegalArgumentException("The dockable cannot be dragged, it is not child of this station.");

		removeDockable(dockable);
	}

	/**
	 * Sends a message to all registered instances of {@link SplitDockListener},
	 * that the {@link Dockable} in fullscreen-mode has changed.
	 * @param oldDockable the old fullscreen-Dockable, can be <code>null</code>
	 * @param newDockable the new fullscreen-Dockable, can be <code>null</code>
	 */
	protected void fireFullScreenChanged( Dockable oldDockable, Dockable newDockable ){
		for( SplitDockListener listener : splitListeners.toArray(new SplitDockListener[splitListeners.size()]) )
			listener.fullScreenDockableChanged(this, oldDockable, newDockable);
	}

	/**
	 * Informs all {@link DockableListener}s that <code>title</code> is no longer
	 * considered to be a good title and should be exchanged.
	 * @param title a title, can be <code>null</code>
	 */
	protected void fireTitleExchanged( DockTitle title ){
		for( DockableListener listener : dockableListeners.toArray(new DockableListener[dockableListeners.size()]) )
			listener.titleExchanged(this, title);
	}

	/**
	 * Informs all {@link DockableListener}s that all bound titles and the
	 * <code>null</code> title are no longer considered good titles and
	 * should be replaced
	 */
	protected void fireTitleExchanged(){
		DockTitle[] bound = listBoundTitles();
		for( DockTitle title : bound )
			fireTitleExchanged(title);

		fireTitleExchanged(null);
	}

	public Rectangle getStationBounds(){
		Point location = new Point(0, 0);
		SwingUtilities.convertPointToScreen(location, this);
		if( isAllowSideSnap() )
			return new Rectangle(location.x - borderSideSnapSize, location.y - borderSideSnapSize, getWidth() + 2 * borderSideSnapSize, getHeight() + 2 * borderSideSnapSize);
		else
			return new Rectangle(location.x, location.y, getWidth(), getHeight());
	}

	public boolean canCompare( DockStation station ){
		if( !isAllowSideSnap() )
			return false;

		if( station.asDockable() != null ) {
			Component component = station.asDockable().getComponent();
			Component root = SwingUtilities.getRoot(getComponent());
			if( root != null && root == SwingUtilities.getRoot(component) ) {
				return true;
			}
		}
		return false;
	}

	public int compare( DockStation station ){
		if( !isAllowSideSnap() )
			return 0;

		if( station.asDockable() != null ) {
			Component component = station.asDockable().getComponent();
			Component root = SwingUtilities.getRoot(getComponent());
			if( root != null && root == SwingUtilities.getRoot(component) ) {
				Rectangle sizeThis = getStationBounds();
				Rectangle sizeOther = station.getStationBounds();

				if( sizeThis == null && sizeOther == null )
					return 0;

				if( sizeThis == null )
					return -1;

				if( sizeOther == null )
					return 1;

				if( sizeThis.width * sizeThis.height > sizeOther.width * sizeOther.height )
					return -1;
				if( sizeThis.width * sizeThis.height < sizeOther.width * sizeOther.height )
					return 1;
			}
		}

		return 0;
	}

	public Dockable asDockable(){
		return this;
	}

	/**
	 * Gets a {@link StationPaint} to paint markings on this station.
	 * @return the paint
	 */
	public DefaultStationPaintValue getPaint(){
		return paint;
	}

	/**
	 * Gets a {@link DisplayerFactory} to create new {@link DockableDisplayer}
	 * for this station.
	 * @return the factory
	 */
	public DefaultDisplayerFactoryValue getDisplayerFactory(){
		return displayerFactory;
	}

	/**
	 * Gets the set of {@link DockableDisplayer displayers} that are currently
	 * used by this station.
	 * @return the set of displayers
	 */
	public DisplayerCollection getDisplayers(){
		return displayers;
	}

	/**
	 * Gets a {@link Combiner} to combine {@link Dockable Dockables} on
	 * this station.
	 * @return the combiner
	 */
	public StationCombinerValue getCombiner(){
		return combiner;
	}

	@Override
	protected void paintOverlay( Graphics g ){
		if( putInfo != null ) {
			DefaultStationPaintValue paint = getPaint();
			if( putInfo.getNode() == null ) {
				Rectangle bounds = new Rectangle(0, 0, getWidth(), getHeight());
				paint.drawInsertion(g, bounds, bounds);
			}
			else {
				CombinerTarget target = putInfo.getCombinerTarget();
				if( target == null ){
					Rectangle bounds = putInfo.getNode().getBounds();
	
					if( putInfo.getPut() == PutInfo.Put.LEFT ) {
						bounds.width = (int) (bounds.width * putInfo.getDivider() + 0.5);
					}
					else if( putInfo.getPut() == PutInfo.Put.RIGHT ) {
						int width = bounds.width;
						bounds.width = (int) (bounds.width * (1 - putInfo.getDivider()) + 0.5);
						bounds.x += width - bounds.width;
					}
					else if( putInfo.getPut() == PutInfo.Put.TOP ) {
						bounds.height = (int) (bounds.height * putInfo.getDivider() + 0.5);
					}
					else if( putInfo.getPut() == PutInfo.Put.BOTTOM ) {
						int height = bounds.height;
						bounds.height = (int) (bounds.height * (1 - putInfo.getDivider()) + 0.5);
						bounds.y += height - bounds.height;
					}
	
					paint.drawInsertion(g, putInfo.getNode().getBounds(), bounds);
				}
				else{
					Rectangle bounds = putInfo.getNode().getBounds();
					StationPaint stationPaint = paint.get();
					if( stationPaint != null ){
						target.paint( g, getComponent(), stationPaint, bounds, bounds );
					}
				}
			}
		}

		dividerStrategy.getValue().paint( this, g );
	}

	/**
	 * Adds <code>dockable</code> to this station.
	 * @param dockable A {@link Dockable} which must not be a child
	 * of this station.
	 */
	public void addDockable( Dockable dockable ){
		addDockable( dockable, null );
	}

	/**
	 * Adds <code>dockable</code> to this station and fires events
	 * only if <code>fire</code> is <code>true</code>.
	 * @param dockable the new child of this station
     * @param token if <code>null</code>, then a token will be acquired by this method
     * and this method will fire events, otherwise this methods is executed silently
	 */
	private void addDockable( Dockable dockable, DockHierarchyLock.Token token ){
		try{
			boolean fire = token == null;
			access.arm();
			DockUtilities.checkLayoutLocked();
			
			if( fire ){
				DockUtilities.ensureTreeValidity(this, dockable);
				token = DockHierarchyLock.acquireLinking( this, dockable );
			}
			try{
				if( fire ){
					dockStationListeners.fireDockableAdding(dockable);
				}
				Leaf leaf = new Leaf(access);
		
				Root root = root();
				if( root.getChild() == null ) {
					root.setChild(leaf);
				}
				else {
					SplitNode child = root.getChild();
					root.setChild(null);
					Node node = new Node(access, leaf, child);
					root.setChild(node);
				}
		
				leaf.setDockable( dockable, token );
		
				if( fire ) {
					dockStationListeners.fireDockableAdded(dockable);
				}
				revalidate();
			}
			finally{
				if( fire ){
					token.release();
				}
			}
		}
		finally{
			access.fire();
		}
	}

	public boolean canReplace( Dockable old, Dockable next ){
		return true;
	}

	public void replace( DockStation old, Dockable next ){
		replace(old.asDockable(), next, true);
	}

	public void replace( Dockable previous, Dockable next ){
		replace(previous, next, false);
	}

	private void replace( Dockable previous, Dockable next, boolean station ){
		try{
			access.arm();
			DockUtilities.checkLayoutLocked();
			if( previous == null )
				throw new NullPointerException("previous must not be null");
			if( next == null )
				throw new NullPointerException("next must not be null");
			if( previous != next ) {
				Leaf leaf = root().getLeaf(previous);
	
				if( leaf == null )
					throw new IllegalArgumentException("Previous is not child of this station");
	
				DockUtilities.ensureTreeValidity(this, next);
	
				boolean wasFullScreen = isFullScreen() && getFullScreen() == previous;
	
				leaf.setDockable(next, null, true, station);
	
				if( wasFullScreen )
					setFullScreen(next);
	
				revalidate();
				repaint();
			}
		}
		finally{
			access.fire();
		}
	}

	/**
	 * Adds <code>handle</code> to the list of dockables,also adds the {@link DockableDisplayer} of the new handle to this station. 
	 * @param handle the element to add
	 * @param token tells whether the operation is monitored or not. If the value is <code>null</code> then this method
	 * will acquire a token itself and fire events, otherwise this method will silently be executed.
	 */
	private void addHandle( StationChildHandle handle, DockHierarchyLock.Token token ){
		Dockable dockable = handle.getDockable();
		DockUtilities.ensureTreeValidity(this, dockable);
		
		boolean fire = token == null;
		
		if( fire ){
			token = DockHierarchyLock.acquireLinking( this, dockable );
		}
		
		try{
			if( fire ){
				dockStationListeners.fireDockableAdding(dockable);
			}
	
			dockables.add(handle);
			dockable.setDockParent(this);
	
			handle.updateDisplayer();
	
			DockableDisplayer displayer = handle.getDisplayer();
			getContentPane().add(displayer.getComponent());
			displayer.getComponent().setVisible(!isFullScreen());
	
			if( fire ){
				dockStationListeners.fireDockableAdded(dockable);
			}
		}
		finally{
			if( fire ){
				token.release();
			}
		}
	}

	/**
	 * Removes <code>displayer</code> and creates a replacement.
	 * @param displayer the displayer to replaces
	 */
	protected void discard( DockableDisplayer displayer ){
		int index = indexOfDockable(displayer.getDockable());
		if( index < 0 )
			throw new IllegalArgumentException("displayer unknown to this station: " + displayer);

		Dockable dockable = displayer.getDockable();
		boolean visible = displayer.getComponent().isVisible();

		Leaf leaf = root().getLeaf(dockable);
		getContentPane().remove(displayer.getComponent());

		StationChildHandle handle = leaf.getDockableHandle();
		handle.updateDisplayer();
		displayer = handle.getDisplayer();

		getContentPane().add(displayer.getComponent());
		displayer.getComponent().setVisible(visible);

		revalidate();
	}

	/**
	 * Gets the index of a child of this station.
	 * @param dockable the child which is searched
	 * @return the index or -1 if the child was not found
	 */
	public int indexOfDockable( Dockable dockable ){
		for( int i = 0, n = dockables.size(); i < n; i++ )
			if( dockables.get(i).getDockable() == dockable )
				return i;

		return -1;
	}

	/**
	 * Removes all children from this station.<br>
	 * Note: clients may need to invoke {@link DockController#freezeLayout()}
	 * and {@link DockController#meltLayout()} to ensure noone else adds or
	 * removes <code>Dockable</code>s.
	 */
	public void removeAllDockables(){
		DockController controller = getController();
		try {
			access.arm();
			DockUtilities.checkLayoutLocked();
			if( controller != null )
				controller.freezeLayout();

			for( int i = getDockableCount() - 1; i >= 0; i-- )
				removeDisplayer(i, null);

			root().setChild(null);
		}
		finally {
			if( controller != null )
				controller.meltLayout();
			
			access.fire();
		}
	}

	/**
	 * Removes <code>dockable</code> from this station. If 
	 * <code>dockable</code> is not a child of this station, nothing happens.<br>
	 * Note: clients may need to invoke {@link DockController#freezeLayout()}
	 * and {@link DockController#meltLayout()} to ensure noone else adds or
	 * removes <code>Dockable</code>s.
	 * @param dockable the child to remove
	 */
	public void removeDockable( Dockable dockable ){
		try{
			access.arm();
			DockUtilities.checkLayoutLocked();
			Leaf leaf = root().getLeaf(dockable);
			if( leaf != null ) {
				leaf.setDockable(null, null, true, dockable.asDockStation() != null);
				leaf.placehold(true);
			}
		}
		finally{
			access.fire();
		}
	}

	/**
	 * Searches the entire tree for any occurence of <code>placeholder</code> and
	 * removes <code>placeholder</code>. Also shrinks the tree if some nodes or leafs
	 * are no longer required due to the removed placeholder
	 * @param placeholder the placeholder to remove
	 */
	public void removePlaceholder( Path placeholder ){
		Set<Path> placeholders = new HashSet<Path>();
		placeholders.add(placeholder);
		removePlaceholders(placeholders);
	}

	/**
	 * Searches the entire tree for all occurences of all placeholders in <code>placeholders</code>.
	 * All placeholders are removed and the tree shrinks where possible.
	 * @param placeholders the placeholders to remove
	 */
	public void removePlaceholders( final Set<Path> placeholders ){
		if( placeholders.isEmpty() )
			return;

		final List<SplitNode> nodesToDelete = new ArrayList<SplitNode>();
		root().visit(new SplitNodeVisitor(){
			public void handleRoot( Root root ){
				handle(root);
			}

			public void handlePlaceholder( Placeholder placeholder ){
				handle(root);
			}

			public void handleNode( Node node ){
				handle(root);
			}

			public void handleLeaf( Leaf leaf ){
				handle(root);
			}

			private void handle( SplitNode node ){
				node.removePlaceholders(placeholders);
				if( !node.isOfUse() ) {
					nodesToDelete.add(node);
				}
			}
		});

		for( SplitNode node : nodesToDelete ) {
			node.delete(true);
		}
	}

	/**
	 * Removes <code>handle</code> from this station. Unbinds its
	 * {@link Dockable}.
	 * @param handle the handle to remove
     * @param token if <code>null</code>, then a token will be acquired by this method
     * and this method will fire events, otherwise this methods is executed silently
	 */
	private void removeHandle( StationChildHandle handle, DockHierarchyLock.Token token ){
		int index = dockables.indexOf( handle );
		if( index >= 0 ) {
			removeDisplayer( index, token );
		}
	}

	/**
	 * Removes the index'th handle from this station
	 * @param index the index of the handle to remove
     * @param token if <code>null</code>, then a token will be acquired by this method
     * and this method will fire events, otherwise this methods is executed silently
	 */
	private void removeDisplayer( int index, DockHierarchyLock.Token token ){
		StationChildHandle handle = dockables.get(index);

		if( handle == fullScreenDockable ) {
			setNextFullScreen();

			if( handle == fullScreenDockable )
				setFullScreen(null);
		}

		Dockable dockable = handle.getDockable();
		
		boolean fire = token == null;
		if( fire ){
			token = DockHierarchyLock.acquireUnlinking( this, dockable );
		}
		try{
			if( fire )
				dockStationListeners.fireDockableRemoving(dockable);
	
			dockables.remove(index);
	
			DockableDisplayer displayer = handle.getDisplayer();
	
			displayer.getComponent().setVisible(true);
			getContentPane().remove(displayer.getComponent());
	
			handle.destroy();
	
			if( dockable == frontDockable ) {
				setFrontDockable(null);
			}
	
			dockable.setDockParent(null);
			if( fire )
				dockStationListeners.fireDockableRemoved(dockable);
		}
		finally{
			if( fire ){
				token.release();
			}
		}
	}

	/**
	 * Gets the {@link Root} of the tree which stores all locations and sizes
	 * of the children of this station. Clients can modify the contents of this
	 * station directly by accessing this tree.<br>
	 * <b>Note</b>
	 * <ul><li>that removing or adding children to the tree does not automatically
	 * remove or add new {@link Dockable}s, that has to be explicitly done through
	 * {@link Leaf#setDockable(Dockable, bibliothek.gui.dock.DockHierarchyLock.Token)}.</li>
	 * <li>The tree should never be invalid. That means that each {@link Node}
	 * should have two children, and each {@link Leaf} should have 
	 * a {@link Dockable}.</li>
	 * </ul>
	 * @return the root
	 * @see #root()
	 */
	public Root getRoot(){
		return root();
	}

	/**
	 * Searches the node whose {@link SplitNode#getId() id} equals <code>id</code>.
	 * @param id the id to search
	 * @return the node with the id <code>id</code>
	 */
	public SplitNode getNode( final long id ){
		class Visitor implements SplitNodeVisitor {
			private SplitNode result;

			public void handleRoot( Root root ){
				if( root.getId() == id ) {
					result = root;
				}
			}

			public void handleLeaf( Leaf leaf ){
				if( leaf.getId() == id ) {
					result = leaf;
				}
			}

			public void handlePlaceholder( Placeholder placeholder ){
				if( placeholder.getId() == id ) {
					result = placeholder;
				}
			}

			public void handleNode( Node node ){
				if( node.getId() == id ) {
					result = node;
				}
			}
		}
		;

		if( root == null )
			return null;

		Visitor visitor = new Visitor();
		getRoot().visit(visitor);
		return visitor.result;
	}

	public String getFactoryID(){
		return SplitDockStationFactory.ID;
	}

	/**
	 * Updates all locations and sizes of the {@link Component Components}
	 * which are in the structure of this tree.
	 */
	public void updateBounds(){
		Insets insets = getBasePane().getInsets();
		double factorW = getWidth() - insets.left - insets.right;
		double factorH = getHeight() - insets.top - insets.bottom;

		SplitLayoutManager manager = layoutManager.getValue();

		if( factorW <= 0 || factorH <= 0 ) {
			manager.updateBounds(root(), 0, 0, 0, 00);
		}
		else {
			manager.updateBounds(root(), insets.left / factorW, insets.top / factorH, factorW, factorH);
		}
	}

	/**
	 * The background algorithm of this {@link SplitDockStation}.
	 * @author Benjamin Sigg
	 */
	private class Background extends BackgroundAlgorithm implements StationBackgroundComponent{
		public Background(){
			super( StationBackgroundComponent.KIND, ThemeManager.BACKGROUND_PAINT + ".station.split" );
		}
		
		public Component getComponent(){
			return SplitDockStation.this.getComponent();
		}
		
		public DockStation getStation(){
			return SplitDockStation.this;
		}
	}
	
	/**
	 * The panel which will be the parent of all {@link DockableDisplayer displayers}
	 * @author Benjamin Sigg
	 */
	private class Content extends ConfiguredBackgroundPanel {
		public Content(){
			super( true, false );
		}
		
		@Override
		public void doLayout(){
			updateBounds();

			Insets insets = getInsets();

			if( fullScreenDockable != null ) {
				fullScreenDockable.getDisplayer().getComponent().setBounds(insets.left, insets.top, getWidth() - insets.left - insets.right,
						getHeight() - insets.bottom - insets.top);
			}
		}
		
		@Override
		public void setSolid( boolean solid ){
			super.setSolid( solid );
			SplitDockStation.this.setSolid( solid );
		}
	}

	/**
	 * Orientation how two {@link Dockable Dockables} are aligned.
	 */
	public enum Orientation {
		/** One {@link Dockable} is at the left, the other at the right */
		HORIZONTAL,
		/** One {@link Dockable} is at the top, the other at the bottom */
		VERTICAL
	};

	/**
	 * This listener is added to the parent of this station, and ensures
	 * that the visibility-state of the children of this station is always
	 * correct.
	 * @author Benjamin Sigg
	 */
	private class VisibleListener extends DockStationAdapter {
		@Override
		public void dockableShowingChanged( DockStation station, Dockable dockable, boolean visible ){
			visibility.fire();
		}
	}

	/**
	 * A listener that reacts on double clicks and can expand a child of
	 * this station to fullscreen-mode.
	 * @author Benjamin Sigg
	 */
	private class FullScreenListener implements DoubleClickListener {
		public DockElement getTreeLocation(){
			return SplitDockStation.this;
		}

		public boolean process( Dockable dockable, MouseEvent event ){
			if( event.isConsumed() || !isExpandOnDoubleclick() )
				return false;
			else {
				if( dockable == SplitDockStation.this )
					return false;

				dockable = unwrap(dockable);
				if( dockable != null ) {
					if( isFullScreen() ) {
						if( getFullScreen() == dockable ) {
							setFullScreen(null);
							event.consume();
						}
					}
					else {
						setFullScreen(dockable);
						event.consume();
					}

					return true;
				}

				return false;
			}
		}

		/**
		 * Searches a parent of <code>dockable</code> which has the 
		 * enclosing {@link SplitDockStation} as its direct parent.
		 * @param dockable the root of the search
		 * @return <code>dockable</code>, a parent of <code>dockable</code>
		 * or <code>null</code>
		 */
		private Dockable unwrap( Dockable dockable ){
			while( dockable.getDockParent() != SplitDockStation.this ) {
				DockStation parent = dockable.getDockParent();
				if( parent == null )
					return null;

				dockable = parent.asDockable();
				if( dockable == null )
					return null;
			}
			return dockable;
		}
	}
	
	/**
	 * Implementation of {@link StationDropOperation}.
	 * @author Benjamin Sigg
	 */
	protected class SplitDropOperation implements StationDropOperation{
		private PutInfo putInfo;
		private boolean move;
		
		/**
		 * Creates a new operation.
		 * @param putInfo the desired location of the dropped {@link Dockable}.
		 * @param move whether this operation is a move operation or not 
		 */
		public SplitDropOperation( PutInfo putInfo, boolean move ){
			this.putInfo = putInfo;
			this.move = move;
		}
		
		public boolean isMove(){
			return move;
		}
		
		public void draw(){
			SplitDockStation.this.putInfo = putInfo;
			repaint();
		}

		public void destroy(){
			if( SplitDockStation.this.putInfo == putInfo ){
				SplitDockStation.this.putInfo = null;
				repaint();
			}
		}
		
		public DockStation getTarget(){
			return SplitDockStation.this;
		}
		
		public Dockable getItem(){
			return putInfo.getDockable();
		}
		
		public CombinerTarget getCombination(){
			return putInfo.getCombinerTarget();
		}
		
		public DisplayerCombinerTarget getDisplayerCombination(){
			CombinerTarget target = getCombination();
			if( target == null ){
				return null;
			}
			return target.getDisplayerCombination();
		}
		
		public void execute(){
			if( isMove() ){
				move();
			}
			else{
				drop( null );
			}
		}
		
		public void move(){
			try{
				access.arm();
				DockUtilities.checkLayoutLocked();
				Root root = root();
				Leaf leaf = root.getLeaf(putInfo.getDockable());
		
				SplitNode parent = putInfo.getNode();
				
				if( leaf.getParent() == parent ) {
					while( parent != null ){
						if( parent == root ) {
							// no movement possible
							return;
						}
						else {
							Node node = (Node)parent;
							SplitNode next;
							
							if( node.getLeft() == leaf ){
								next = node.getRight();
							}
							else{
								next = node.getLeft();
							}
							if( next.isVisible() ){
								putInfo.setNode( next );
								break;
							}
							parent = parent.getParent();
						}
					}
				}
		
				putInfo.setLeaf(leaf);
				if( putInfo.getPut() == Put.CENTER ) {
					leaf.placehold(false);
				}
				else {
					leaf.delete(true);
				}
				drop( DockHierarchyLock.acquireFake() );
			}
			finally{
				access.fire();
			}
		}
		
		/**
		 * Adds the {@link Dockable} given by {@link #putInfo} to this
		 * station.
	     * @param token if <code>null</code>, then a token will be acquired by this method
	     * and this method will fire events, otherwise this methods is executed silently
		 */
		private void drop( DockHierarchyLock.Token token ){
			try{
				boolean fire = token == null;
				access.arm();
				DockUtilities.checkLayoutLocked();
				if( putInfo.getNode() == null ) {
					if( fire ) {
						DockUtilities.ensureTreeValidity(SplitDockStation.this, putInfo.getDockable());
						token = DockHierarchyLock.acquireLinking( SplitDockStation.this, putInfo.getDockable() );
					}
					try{
						if( fire ){
							dockStationListeners.fireDockableAdding(putInfo.getDockable());
						}
						addDockable( putInfo.getDockable(), token );
						if( fire ) {
							dockStationListeners.fireDockableAdded(putInfo.getDockable());
						}
					}
					finally{
						if( fire ){
							token.release();
						}
					}
				}
				else {
					boolean finish = false;
		
					if( putInfo.getCombinerTarget() != null ) {
						if( putInfo.getNode() instanceof Leaf ) {
							if( putInfo.getLeaf() != null ) {
								if( fire ){
									token = DockHierarchyLock.acquireUnlinking( SplitDockStation.this, putInfo.getLeaf().getDockable() );
								}
								try{
									putInfo.getLeaf().setDockable( null, token );
									putInfo.setLeaf(null);
								}
								finally{
									if( fire ){
										token.release();
									}
								}
							}
		
							if( dropOver((Leaf) putInfo.getNode(), putInfo.getDockable(), putInfo.getCombinerSource(), putInfo.getCombinerTarget() ) ) {
								finish = true;
							}
						}
						else {
							putInfo.setPut(PutInfo.Put.TOP);
						}
					}
		
					if( !finish ) {
						updateBounds();
						layoutManager.getValue().calculateDivider(SplitDockStation.this, putInfo, root().getLeaf(putInfo.getDockable()));
						dropAside( putInfo.getNode(), putInfo.getPut(), putInfo.getDockable(), putInfo.getLeaf(), putInfo.getDivider(), token );
					}
				}
		
				revalidate();
			}
			finally{
				access.fire();
			}
		}
	}
	
	/**
	 * Access to this {@link SplitDockStation}.
	 * @author Benjamin Sigg
	 */
	private class Access implements SplitDockAccess{
		private long lastUniqueId = -1;
		private int repositionedArm = 0;
		private Set<Dockable> repositioned = new HashSet<Dockable>();
		private Dockable dockableSelected = null;
		
		public StationChildHandle getFullScreenDockable(){
			return fullScreenDockable;
		}

		public DockTitleVersion getTitleVersion(){
			return title;
		}

		public SplitDockStation getOwner(){
			return SplitDockStation.this;
		}

		public double validateDivider( double divider, Node node ){
			return layoutManager.getValue().validateDivider(SplitDockStation.this, divider, node);
		}

		public StationChildHandle newHandle( Dockable dockable ){
			return new StationChildHandle(SplitDockStation.this, getDisplayers(), dockable, title);
		}

		public void addHandle( StationChildHandle dockable, DockHierarchyLock.Token token ){
			SplitDockStation.this.addHandle( dockable,  token );
		}

		public void removeHandle( StationChildHandle handle, DockHierarchyLock.Token token ){
			SplitDockStation.this.removeHandle( handle, token );
		}

		public boolean drop( Dockable dockable, SplitDockProperty property, SplitNode root ){
			return SplitDockStation.this.drop(dockable, property, root);
		}

		public PutInfo validatePutInfo( PutInfo putInfo ){
			return layoutManager.getValue().validatePutInfo(SplitDockStation.this, putInfo);
		}
		
		public void repositioned( SplitNode node ){
			arm();
			try{
				node.visit( new SplitNodeVisitor(){
					public void handleRoot( Root root ){
						// ignore	
					}
					
					public void handlePlaceholder( Placeholder placeholder ){
						// ignore					
					}
					
					public void handleNode( Node node ){
						// ignore
					}
					
					public void handleLeaf( Leaf leaf ){
						Dockable dockable = leaf.getDockable();
						if( dockable != null ){
							repositioned.add( dockable );
						}
					}
				});
			}
			finally{
				fire();
			}
		}
		
		public void dockableSelected( Dockable dockable ){
			arm();
			if( dockableSelected == null ){
				dockableSelected = dockable;
			}
			fire();
		}
		
		/**
		 * Prepares <code>this</code> to fire an event to 
		 * {@link DockStationListener#dockablesRepositioned(DockStation, Dockable[])}.
		 */
		public void arm(){
			repositionedArm++;
		}

		/**
		 * Fires an event to {@link DockStationListener#dockablesRepositioned(DockStation, Dockable[])}.
		 */
		public void fire(){
			repositionedArm--;
			if( repositionedArm == 0 ){
				List<Dockable> dockables = new ArrayList<Dockable>();
				for( Dockable dockable : repositioned ){
					if( dockable.getDockParent() == SplitDockStation.this ){
						dockables.add( dockable );
					}
				}
				repositioned.clear();
				
				if( dockables.size() > 0 ){
					dockStationListeners.fireDockablesRepositioned( dockables.toArray( new Dockable[ dockables.size() ] ) );
				}
				
				if( dockableSelected != null ){
					Dockable newDockable = getFrontDockable();
					if( dockableSelected != newDockable ){
						dockStationListeners.fireDockableSelected( dockableSelected, newDockable );
					}
					dockableSelected = null;
				}
			}
		}

		public long uniqueID(){
			long id = System.currentTimeMillis();
			if( id <= lastUniqueId ) {
				lastUniqueId++;
				id = lastUniqueId+1;
			}
			while( getNode(id) != null ) {
				id++;
			}
			lastUniqueId = id;
			return id;
		}

		public boolean isTreeAutoCleanupEnabled(){
			return treeLock == 0;
		}

		public SplitPlaceholderSet getPlaceholderSet(){
			return placeholderSet;
		}
	}
}

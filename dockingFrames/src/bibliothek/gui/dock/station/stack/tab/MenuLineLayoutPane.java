/*
 * Bibliothek - DockingFrames
 * Library built on Java/Swing, allows the user to "drag and drop"
 * panels containing any Swing-Component the developer likes to add.
 * 
 * Copyright (C) 2011 Benjamin Sigg
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
package bibliothek.gui.dock.station.stack.tab;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import bibliothek.gui.dock.station.stack.tab.layouting.LayoutBlock;
import bibliothek.gui.dock.station.stack.tab.layouting.MenuLayoutBlock;
import bibliothek.gui.dock.station.stack.tab.layouting.Size;
import bibliothek.gui.dock.station.stack.tab.layouting.TabPlacement;
import bibliothek.gui.dock.station.stack.tab.layouting.TabsLayoutBlock;

/**
 * Represents one of the {@link TabPane}s that are managed by a {@link MenuLineLayout}.
 * @author Benjamin Sigg
 */
public class MenuLineLayoutPane extends AbstractTabLayoutManagerPane{
	private MenuLayoutBlock menu;
	private LayoutBlock info;
	private TabsLayoutBlock tabs;
	private MenuLineLayout layout;
	
	/**
	 * Creates new layout information for <code>pane</code>.
	 * @param layout the layout using this pane
	 * @param pane the owner of this information
	 */
	public MenuLineLayoutPane( MenuLineLayout layout, TabPane pane ){
		super( pane );
		this.layout = layout;
		menu = layout.getFactory().createMenu( layout, pane );
		info = layout.getFactory().createInfo( layout, pane );
		tabs = layout.getFactory().createTabs( layout, pane );
	}
	
	/**
	 * Gets the tabs that are shown on this pane.
	 * @return the tabs
	 */
	public TabsLayoutBlock getTabs(){
		return tabs;
	}
	
	/**
	 * Gets the info {@link Component} that is shown on this pane.
	 * @return the info component
	 */
	public LayoutBlock getInfo(){
		return info;
	}
	
	/**
	 * Gets the menu that is shown on this pane.
	 * @return the menu
	 */
	public MenuLayoutBlock getMenu(){
		return menu;
	}

	/**
	 * Calculates the preferred size to show all elements.
	 * @return the preferred size
	 */
	public Dimension getPreferredSize(){
		List<MenuLineLayoutPossibility> layouts = listLayouts();
		Dimension bestSize = new Dimension( 0, 0 );
		
		if( getPane().getTabPlacement().isHorizontal() ){
			for( MenuLineLayoutPossibility layout : layouts ){
				if( layout.isPreferred() ){
					Dimension size = layout.getSize();
					if( size.width > bestSize.width ){
						bestSize = size;
					}
				}
			}
		}
		else{
			for( MenuLineLayoutPossibility layout : layouts ){
				if( layout.isPreferred() ){
					Dimension size = layout.getSize();
					if( size.height > bestSize.height ){
						bestSize = size;
					}
				}
			}
		}
		
		return bestSize;
	}
	
	/**
	 * Calculates the minimal size required.
	 * @return the minimal size
	 */
	public Dimension getMinimumSize(){
		List<MenuLineLayoutPossibility> layouts = listLayouts();
		Dimension bestSize = null;
		
		if( getPane().getTabPlacement().isHorizontal() ){
			for( MenuLineLayoutPossibility layout : layouts ){
				Dimension size = layout.getSize();
				if( bestSize == null || size.width < bestSize.width ){
					bestSize = size;
				}
			}
		}
		else{
			for( MenuLineLayoutPossibility layout : layouts ){
				Dimension size = layout.getSize();
				if( bestSize == null || size.height < bestSize.height ){
					bestSize = size;
				}
			}
		}
		
		return bestSize;
	}
	
	/**
	 * Informs this layout that it is no longer used and can release any
	 * resource.
	 */
	public void destroy(){
		getPane().destroyMenu( menu.getMenu() );
	}
	
	/**
	 * Gets the {@link MenuLineLayout} that is using this pane.
	 * @return the layout, not <code>null</code>
	 */
	public MenuLineLayout getLayout(){
		return layout;
	}
	
	/**
	 * Updates the number of shown tabs and the boundaries of tabs, menu
	 * and info.
	 */
	public void layout(){
		AxisConversion conversion = getLayout().getConversion( getPane() );
		
		List<MenuLineLayoutPossibility> layouts = listLayouts();
		
		// search the layout that fits into the available space
		Rectangle available = conversion.viewToModel( getPane().getAvailableArea() );
		
		int space = available.width;
		
		MenuLineLayoutPossibility best = null;
		int bestSize = -1;
		
		MenuLineLayoutPossibility smallest = null;
		int smallestSize = -1;
		
		for( MenuLineLayoutPossibility layout : layouts ){
			Dimension size = conversion.viewToModel( layout.getSize() );
			if( size.width <= space ){
				if( layout.isPreferred() ){
					if( (best == null || !best.isPreferred()) || bestSize < size.width ){
						bestSize = size.width;
						best = layout;
					}
				}
				else{
					if( (best == null || (!best.isPreferred()) && bestSize < size.width )){
						bestSize = size.width;
						best = layout;
					}
				}
			}
			
			if( smallest == null || size.width < smallestSize ){
				smallest = layout;
				smallestSize = size.width;
			}
		}
		
		if( best != null ){
			best.apply();
		}
		else if( smallest != null ){
			smallest.apply();
		}
	}
	
	/**
	 * Creates a list of all available layouts.
	 * @return the list of all available layouts
	 */
	private List<MenuLineLayoutPossibility> listLayouts(){
		List<MenuLineLayoutPossibility> results = new ArrayList<MenuLineLayoutPossibility>();
		TabPlacement orientation = getPane().getTabPlacement();
		
		tabs.setOrientation( orientation );
		Size[] sizesTabs = tabs.getSizes();
		
		menu.setOrientation( orientation );
		Size[] sizesMenu = menu.getSizes();
		
		if( info != null ){
			info.setOrientation( orientation );
			Size[] sizesInfo = info.getSizes();
			for( Size size : sizesInfo ){
				listLayouts( results, size, sizesMenu, sizesTabs );
			}
		}
		else{
			listLayouts( results, null, sizesMenu, sizesTabs );
		}	
		return results;
	}
	
	private void listLayouts( List<MenuLineLayoutPossibility> list, Size infoSize, Size[] menuSizes, Size[] tabSizes ){
		for( Size tab : tabSizes ){
			if( tabs.isAllTabs( tab ) ){
				listLayouts( list, infoSize, (Size)null, tab );
			}
			else{
				for( Size menu : menuSizes ){
					listLayouts( list, infoSize, menu, tab );
				}
			}
		}
	}
	
	private void listLayouts( List<MenuLineLayoutPossibility> list, Size infoSize, Size menuSize, Size tabSize ){
		boolean tabMustBeMinimum = (infoSize != null && infoSize.isMinimum()) || (menuSize != null);
		boolean tabMustBeSingle = menuSize != null && menuSize.isMinimum();
		boolean infoMustBeMinimum = menuSize != null && menuSize.isMinimum();
		
		if( tabMustBeMinimum && !tabSize.isMinimum() )
			return;
		
		if( tabMustBeSingle && tabs.getTabsCount( tabSize ) > 1 )
			return;
		
		if( infoMustBeMinimum && (infoSize != null && !infoSize.isMinimum()))
			return;
		
		list.add( getLayout( tabSize, menuSize, infoSize ) );			
	}
	
	/**
	 * Creates a possible layout for the given sizes.
	 * @param tabSize the sizes of the tabs
	 * @param menuSize the size of the menu
	 * @param infoSize the size of the info component
	 * @return a possible layout
	 */
	protected MenuLineLayoutPossibility getLayout( Size tabSize, Size menuSize, Size infoSize ){
		return new MenuLineLayoutPossibility( this, tabSize, menuSize, infoSize );
	}
	
	@Override
	public void infoComponentChanged( TabPane pane, LonelyTabPaneComponent oldInfo, LonelyTabPaneComponent newInfo ){
		super.infoComponentChanged( pane, oldInfo, newInfo );
		if( newInfo == null )
			info = null;
		else
			info = newInfo.toLayoutBlock();
	}
}
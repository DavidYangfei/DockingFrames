package bibliothek.gui.dock.station.toolbar;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import bibliothek.gui.DockStation;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.station.DisplayerFactory;
import bibliothek.gui.dock.station.DockableDisplayer;
import bibliothek.gui.dock.station.Orientation;
import bibliothek.gui.dock.station.OrientingDockStation;
import bibliothek.gui.dock.station.OrientingDockStationEvent;
import bibliothek.gui.dock.station.OrientingDockStationListener;
import bibliothek.gui.dock.themes.basic.BasicDockableDisplayer;
import bibliothek.gui.dock.title.DockTitle;

/**
 * A simple implementation of a {@link DockableDisplayer} that can be used by toolbar-{@link DockStation}s. This displayer
 * is aware of the fact, that some {@link DockStation}s have an orientation and may update its own orientation automatically. 
 * @author Benjamin Sigg
 */
public class ToolbarDockableDisplayer extends BasicDockableDisplayer{
	/**
	 * Creates a new {@link DisplayerFactory} for creating new {@link ToolbarDockableDisplayer}s with a {@link LineBorder}
	 * using the color <code>color</code>.
	 * @param color the color of the {@link LineBorder}
	 * @return the new factory
	 */
	public static final DisplayerFactory createColorBorderFactory( final Color color ){
		return new DisplayerFactory(){
			@Override
			public DockableDisplayer create( DockStation station, Dockable dockable, DockTitle title ){
				ToolbarDockableDisplayer displayer = new ToolbarDockableDisplayer( station, dockable, title );
				displayer.setDefaultBorder( BorderFactory.createLineBorder( color ) );
				displayer.setDefaultBorderHint( true );
				displayer.setRespectBorderHint( false );
				return displayer;
			}
		};
	}
	
	/**
	 * A listener added to the {@link #getStation() station} of this displayer.
	 */
	private OrientingDockStationListener listener = new OrientingDockStationListener(){
		@Override
		public void changed( OrientingDockStationEvent event ){
			Dockable dockable = getDockable();
			if( dockable != null && event.isAffected( dockable )){
				updateOrientation();
			}
		}
	};
	
	private Border defaultBorder;
	
	/**
	 * Creates a new displayer.
	 * @param station the owner of this displayer
	 * @param dockable the element shown on this displayer, can be <code>null</code>
	 * @param title the title shown on this displayer, can be <code>null</code>
	 */
	public ToolbarDockableDisplayer( DockStation station, Dockable dockable, DockTitle title ){
		super( station, dockable, title );
	}
	
	public void setDefaultBorder( Border defaultBorder ){
		this.defaultBorder = defaultBorder;
	}
	
	@Override
	protected Border getDefaultBorder(){
		return defaultBorder;
	}

	@Override
	public void setStation( DockStation station ){
		DockStation old = getStation();
		if( old != null && old instanceof OrientingDockStation ){
			((OrientingDockStation)old).removeOrientingDockStationListener( listener );
		}
		
		super.setStation( station );
		
		if( station != null && station instanceof OrientingDockStation ){
			((OrientingDockStation)station).addOrientingDockStationListener( listener );
		}
		
		updateOrientation();
	}
	
	@Override
	public void setDockable( Dockable dockable ){
		super.setDockable( dockable );
		updateOrientation();
	}
	
	protected void updateOrientation(){
		DockStation station = getStation();
		
		if( station instanceof OrientingDockStation && getDockable() != null ){
			Orientation orientation = ((OrientingDockStation)station).getOrientationOf( getDockable() );
			switch( orientation ){
				case HORIZONTAL:
					setTitleLocation( Location.LEFT );
					break;
				case VERTICAL:
					setTitleLocation( Location.TOP );
					break;
				default:
					throw new IllegalStateException( "unknown orientation: " + orientation );
			}
		}
	}
}

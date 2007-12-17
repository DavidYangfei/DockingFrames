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
package bibliothek.gui.dock.facile.intern;

import java.awt.Component;
import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JFrame;

import bibliothek.gui.DockController;
import bibliothek.gui.dock.facile.FControl;
import bibliothek.gui.dock.station.FlapDockStation;
import bibliothek.gui.dock.station.ScreenDockStation;

/**
 * A factory that uses the most efficient elements, can only be used in
 * environments where global events can be observed.
 * @author Benjamin Sigg
 */
public class EfficientControlFactory implements FControlFactory {
    public DockController createController() {
        DockController controller = new DockController();
        controller.setSingleParentRemove( true );
        return controller;
    }

    public FlapDockStation createFlapDockStation() {
        return new FlapDockStation();
    }

    public ScreenDockStation createScreenDockStation( JFrame owner ) {
        return new ScreenDockStation( owner );
    }

    public Component monitor( Component component, FControl control ) {
        return component;
    }
    
    public Container monitor( Container component, FControl control ) {
        return component;
    }
    
    public JComponent monitor( JComponent component, FControl control ) {
        return component;
    }
}

package de.codesourcery.engine.render;

import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;

public abstract class Panel3D extends JPanel {
	
	private SimpleRenderer renderer;
	
	private ComponentListener sizeListener = new ComponentAdapter() 
	{
		@Override
		public void componentResized(ComponentEvent e) {
			renderer.setHeight( getHeight() );
			renderer.setWidth( getWidth() );
			panelResized( getWidth() , getHeight() );
			repaint();
		}
	};
	
	public Panel3D(SimpleRenderer renderer) {
		this.renderer = renderer;
		addComponentListener( sizeListener );
	}
	
	@Override
	public void paint(Graphics g) {
		renderer.paint( g );
	}
	
	protected abstract void panelResized(int newWidth, int newHeight);
}
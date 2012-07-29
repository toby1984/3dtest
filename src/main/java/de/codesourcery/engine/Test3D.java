package de.codesourcery.engine;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.JFrame;

import de.codesourcery.engine.linalg.Vector4;
import de.codesourcery.engine.render.Panel3D;
import de.codesourcery.engine.render.SoftwareRenderer;

public class Test3D extends AbstractTest {

	private final SoftwareRenderer renderer = new SoftwareRenderer();	
	
	private final Panel3D canvas = new Panel3D( renderer ) {

		@Override
		protected void panelResized(int newWidth, int newHeight) {
			aspectRatio = newWidth / (float) newHeight;
			world.setupPerspectiveProjection( fov.get() ,  aspectRatio  ,  Z_NEAR , Z_FAR );
		}
	};
	
	public static void main(String[] args) throws Exception {
		new Test3D().run();
	}
	
	public void run() throws Exception {

		final SoftwareRenderer renderer = new SoftwareRenderer();
		renderer.setAmbientLightFactor( 0.25f );
		renderer.setLightPosition( new Vector4( 0 , 100 , 100 ) );

		renderer.setWorld( world );
		
		final Panel3D canvas = new Panel3D( renderer ) {

			@Override
			protected void panelResized(int newWidth, int newHeight) {
				aspectRatio = newWidth / (float) newHeight;
				world.setupPerspectiveProjection( fov.get() ,  aspectRatio  ,  Z_NEAR , Z_FAR );
			}
		};
		
		final JFrame frame = new JFrame("test");
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		canvas.setPreferredSize( new Dimension(INITIAL_CANVAS_WIDTH,INITIAL_CANVAS_HEIGHT ) );
		canvas.setMinimumSize( new Dimension(INITIAL_CANVAS_WIDTH,INITIAL_CANVAS_HEIGHT ) );

		frame.getContentPane().setLayout( new BorderLayout() );
		frame.getContentPane().add( canvas , BorderLayout.CENTER );

		frame.pack();
		frame.setVisible(true);
		
		super.setupWorld();
		super.registerInputListeners( frame );
		
		while ( true ) {
			super.animateWorld();
			canvas.repaint();
			Thread.sleep( 20 );
		}
	}
	
	@Override
	protected void forceRepaint() {
		canvas.repaint();
	}

	@Override
	protected void setMouseCursor(Cursor cursor) {
		
	}

}

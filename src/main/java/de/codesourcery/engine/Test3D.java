package de.codesourcery.engine;

import static de.codesourcery.engine.linalg.LinAlgUtils.vector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;

import de.codesourcery.engine.linalg.LinAlgUtils;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;
import de.codesourcery.engine.render.Camera;
import de.codesourcery.engine.render.MouseMotionTracker;
import de.codesourcery.engine.render.Object3D;
import de.codesourcery.engine.render.Panel3D;
import de.codesourcery.engine.render.SoftwareRenderer;
import de.codesourcery.engine.render.World;

public class Test3D
{
	private final Random rnd = new Random(System.currentTimeMillis());

	private static final int INITIAL_CANVAS_WIDTH = 800;
	private static final int INITIAL_CANVAS_HEIGHT = 600;
	
	public static final int NUM_CUBES = 5;
	
	private static final double INC_X = .04;	
	private static final double INC_Y = .1;
	private static final double INC_Z = .4;
	
	public static void main(String[] args) throws InterruptedException
	{
		new Test3D().run();
	}
	
	private volatile double aspectRatio = INITIAL_CANVAS_WIDTH / INITIAL_CANVAS_HEIGHT;

	public void run() throws InterruptedException 
	{
		final Camera camera = new Camera();
		
		// Create some objects...
		final Object3D sphere = new Object3D();
//	      obj.setPrimitives( createPyramid( 10 , 10 , 10 ) );
//		sphere.setPrimitives( LinAlgUtils.createCube( 0.5 ,0.5,0.5 ) );
//		obj.setScaling( 1/10, 1/10, 1/10 );
		
		final Matrix sphereTranslationMatrix = LinAlgUtils.translationMatrix( 0 , 0.3 , -1 );
		sphere.setModelMatrix( sphereTranslationMatrix );
		
		sphere.setPrimitives( LinAlgUtils.createSphere( 0.5 , 60 , 60 ) , true );
		sphere.setIdentifier("sphere");
		sphere.setForegroundColor( Color.BLUE ); // needs to be called AFTER setTriangles() !! 

		final World world = new World();
		
		final Object3D mesh = new Object3D();
		mesh.setPrimitives( LinAlgUtils.createXZMesh( 30 , 30 , 50 , 50 ) , false );
		mesh.setForegroundColor( Color.WHITE ); // needs to be called AFTER setTriangles() !! 
		mesh.setIdentifier( "XZ mesh");
		mesh.setRenderOutline( true );
		
		world.addObject( mesh );
		world.addObject( sphere );
		
//		sphere.addChild( sphere.getBoundingBox() );

//		 for ( int i = 0 ; i < NUM_CUBES-1 ; i++ ) {
//		    Object3D tmp = makeRandomizedCopy( obj );
//		    world.addObject( tmp );
//		 }

		// Setup camera and perspective projection
		
		final AtomicReference<Double> fov = new AtomicReference<>(14.0d);
		
		final int Z_NEAR = 1;
		final int Z_FAR = 500;
		
		world.setupPerspectiveProjection(fov.get(), aspectRatio , Z_NEAR, Z_FAR );
		
		final Vector4 defaultEyePosition = vector( 00.177,5.634,26.718 );
		camera.setEyePosition( defaultEyePosition , vector( 0.003 , -0.1966 , -0.9999 ) );		
		camera.updateViewMatrix();
		world.setCamera( camera );
		
		// display frame
		final SoftwareRenderer renderer = new SoftwareRenderer();
		renderer.setAmbientLightFactor( 0.25f );
		
		renderer.setWorld( world );
		
		final Panel3D canvas = new Panel3D( renderer ) {

			@Override
			protected void panelResized(int newWidth, int newHeight) {
				aspectRatio = newWidth / (double) newHeight;
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

		final MouseMotionTracker tracker = new MouseMotionTracker() {

			private Cursor blankCursor;
			
			{
				final BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
				blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
			}
			
			@Override
			protected void updateEyeTarget(double x, double y, double z) {
				camera.setViewOrientation( new Vector4( x,y,z ) );
				camera.updateViewMatrix();
				canvas.repaint();
			}
			
			@Override
			public void setTrackingEnabled(boolean trackingEnabled) {
				super.setTrackingEnabled(trackingEnabled);
				hideMouseCursor( trackingEnabled );
			}
			
			private void hideMouseCursor(boolean hide) 
			{
				if (hide) {
					frame.getContentPane().setCursor(blankCursor);
				} else {
					frame.getContentPane().setCursor( Cursor.getDefaultCursor() );
				}
			}
		};
		
		tracker.setViewOrientation( camera.getViewOrientation() );
		tracker.setTrackingEnabled( true );
		
		frame.addMouseMotionListener( new MouseMotionAdapter() 
		{
			@Override
			public void mouseMoved(MouseEvent e) {
				tracker.mouseMoved( e.getX() , e.getY() );
			}
		});
		
		frame.addMouseListener( new MouseAdapter() 
		{
			public void mouseClicked(MouseEvent e) {
				if ( e.getButton() == MouseEvent.BUTTON1 ) {
					tracker.setTrackingEnabled( true );
				}
			}
		});
		
		frame.addFocusListener( new FocusAdapter() {
			
			@Override
			public void focusLost(FocusEvent e) {
				tracker.setTrackingEnabled(false);
			}
			
		});
		
		frame.addKeyListener( new KeyAdapter() {

			public void keyPressed(java.awt.event.KeyEvent e) {

				int keyCode = e.getKeyCode();
				switch( keyCode ) 
				{
					case KeyEvent.VK_ESCAPE:
						tracker.setTrackingEnabled( false );
						break;
					case KeyEvent.VK_PLUS:
						fov.set( fov.get() + 1 );
						world.setupPerspectiveProjection(fov.get(),  aspectRatio  , Z_NEAR, Z_FAR );
						System.out.println("FoV: "+fov.get()+" degrees");
						break;
					case KeyEvent.VK_MINUS:
						fov.set( fov.get() - 1 );
						System.out.println("FoV: "+fov.get()+" degrees");
						world.setupPerspectiveProjection(fov.get(),  aspectRatio , Z_NEAR , Z_FAR );
						break;
					case KeyEvent.VK_ENTER:
						tracker.reset();
						camera.reset();
						break;
					case KeyEvent.VK_W:
						camera.moveForward( INC_Z );
						break;
					case KeyEvent.VK_S:
						camera.moveBackward( INC_Z );
						break;                        
					case KeyEvent.VK_A:
						camera.strafeLeft( INC_X );
						break;
					case KeyEvent.VK_D:
						camera.strafeRight( INC_X );
						break;
					case KeyEvent.VK_Q:
						camera.moveUp( INC_Y );
						break;       
					case KeyEvent.VK_E:
						camera.moveDown( INC_Y );
						break;  						
					default:
						return;
				}
				camera.updateViewMatrix();		
			};
		});        

		canvas.repaint();
		
		// rotate eye position around Y axis
		double x1 = 10;
		double y1 = 20;
		double z1 = 30;
		while( true ) 
		{
			// rotate eye position around Y axis
			Matrix rot1 = LinAlgUtils.rotY( y1 );
//			rot1 = rot1.multiply( LinAlgUtils.rotX(x1) );
//			rot1 = rot1.multiply( LinAlgUtils.rotZ(z1) );
			for ( Object3D tmp : world.getObjects() ) {
				if ( tmp == sphere ) {
					tmp.setModelMatrix( rot1.multiply( sphereTranslationMatrix ) );
				}
			}
			
			canvas.repaint();
			x1+=0.5;
			y1+=1;
			z1+=1.5;
			Thread.sleep(20);
		}
	}	

	public Object3D makeRandomizedCopy(Object3D prototype) 
	{
		final Object3D obj2 = prototype.createCopy();

		int transX = rnd.nextInt( 40 );
		int transY = rnd.nextInt( 40 );
		int transZ = -5-rnd.nextInt( 80 );

		obj2.setModelMatrix( LinAlgUtils.translationMatrix( transX ,transY , transZ ) );
		return obj2;
	}
}
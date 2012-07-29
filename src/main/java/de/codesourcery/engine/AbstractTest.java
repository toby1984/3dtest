package de.codesourcery.engine;

import static de.codesourcery.engine.linalg.LinAlgUtils.vector;

import java.awt.Color;
import java.awt.Cursor;
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
import de.codesourcery.engine.render.SoftwareRenderer;
import de.codesourcery.engine.render.World;

public abstract class AbstractTest
{
	private final Random rnd = new Random(System.currentTimeMillis());

	protected static final int INITIAL_CANVAS_WIDTH = 800;
	protected static final int INITIAL_CANVAS_HEIGHT = 800;

	protected static final int Z_NEAR = 1;
	protected static final int Z_FAR = 66000;	

	protected volatile float aspectRatio = INITIAL_CANVAS_WIDTH / (float) INITIAL_CANVAS_HEIGHT;

	protected static final int NUM_CUBES = 55;

	protected static final float INC_X = 1;
	protected static final float INC_Y = 1;
	protected static final float INC_Z = 1;
	
	private float x1 = 10;
	private float y1 = 20;
	private float z1 = 30;	

	protected final World world = new World();

	protected final AtomicReference<Float> fov = new AtomicReference<>(90.0f);	

	private final MouseMotionTracker tracker = new MouseMotionTracker() {

		private Cursor blankCursor;

		{
			final BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		}

		@Override
		protected void updateEyeTarget(float x, float y, float z) 
		{
			Camera camera = world.getCamera();
			camera.setViewOrientation( new Vector4( x,y,z ) );
			camera.updateViewMatrix();
			forceRepaint();
		}

		@Override
		public void setTrackingEnabled(boolean trackingEnabled) {
			super.setTrackingEnabled(trackingEnabled);
			showOrHideMouseCursor( trackingEnabled );
		}

		private void showOrHideMouseCursor(boolean hide) 
		{
			if (hide) {
				setMouseCursor(blankCursor);
			} else {
				setMouseCursor( Cursor.getDefaultCursor() );
			}
		}
	};	

	protected abstract void forceRepaint();

	protected abstract void setMouseCursor(Cursor cursor);

	private final KeyAdapter keyAdapter = new KeyAdapter() {

		public void keyPressed(java.awt.event.KeyEvent e) {

			final Camera camera = world.getCamera();

			int keyCode = e.getKeyCode();
			switch( keyCode ) 
			{
				case KeyEvent.VK_ESCAPE:
					tracker.setTrackingEnabled( false );
					break;
				case KeyEvent.VK_PLUS:
					fov.set( fov.get() - 1 );
					world.setupPerspectiveProjection(fov.get(),  aspectRatio  , Z_NEAR, Z_FAR );
					System.out.println("FoV: "+fov.get()+" degrees");
					break;
				case KeyEvent.VK_MINUS:
					fov.set( fov.get() + 1 );
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
	};

	protected void registerInputListeners(JFrame frame) 
	{
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

		frame.addKeyListener( keyAdapter );       		
	}

	protected void setupWorld() 
	{
		final Object3D sphere = new Object3D();

		sphere.setPrimitives( LinAlgUtils.createSphere( 10f , 7 , 7 ) );
		sphere.setIdentifier("sphere");
		sphere.setModelMatrix( LinAlgUtils.translationMatrix( 0 , 7 , 0 ) );
		sphere.setForegroundColor( Color.BLUE ); // needs to be called AFTER setPrimitives() !! 

		for ( int i = 0 ; i < NUM_CUBES ; i++ ) {
			Object3D tmp = makeRandomizedCopy( i+1, sphere );
			world.addObject( tmp );
		}

		// Setup camera and perspective projection
		System.out.println("*** setting perspective ***");

		world.setupPerspectiveProjection( 90 , 1.0f , Z_NEAR , Z_FAR );

		world.getFrustum().forceRecalculatePlaneDefinitions();

		System.out.println("Frustum is now: "+world.getFrustum() );

		System.out.println("*** setting eye position and view vector ***");
		final Vector4 defaultEyePosition = vector( 0,0,0 );

		final Camera camera = world.getCamera();
		camera.setEyePosition( defaultEyePosition , vector( 0 , 0, -1 ) );		
		camera.updateViewMatrix();

		world.getFrustum().forceRecalculatePlaneDefinitions();

		// display frame
		final SoftwareRenderer renderer = new SoftwareRenderer();
		renderer.setAmbientLightFactor( 0.25f );
		renderer.setLightPosition( new Vector4( 0 , 100 , 100 ) );

		renderer.setWorld( world );

		tracker.setViewOrientation( camera.getViewOrientation() );
		tracker.setTrackingEnabled( true );
	}

	protected final void animateWorld() 
	{
		// rotate eye position around Y axis
		Matrix rot1 = LinAlgUtils.rotY( y1 );
		rot1 = rot1.multiply( LinAlgUtils.rotX(x1) );

		for ( Object3D tmp : world.getObjects() ) 
		{
			Matrix translation = (Matrix) tmp .getMetaData( Object3D.METADATA_TRANSLATION_MATRIX );
			if ( translation != null ) {
				rot1 = translation.multiply( rot1 );
			} 
			tmp.setModelMatrix( rot1.multiply( rot1 ) );
		}

		x1+=0.5;
		y1+=1;
		z1+=1.5;
	}

	protected Object3D makeRandomizedCopy(int count , Object3D prototype) 
	{
		final Object3D obj2 = prototype.createCopy("Copy_"+count+"_of_"+prototype.getIdentifier());

		int transX = -25+rnd.nextInt( 50 );
		int transY = -25+rnd.nextInt( 50 );
		int transZ = -5+rnd.nextInt( 10 );

		System.out.println("Object at "+transX+" / "+transY+" / "+transZ);
		final Matrix translationMatrix = LinAlgUtils.translationMatrix( transX ,transY , transZ );
		obj2.setMetaData( Object3D.METADATA_TRANSLATION_MATRIX , translationMatrix );
		obj2.setModelMatrix( translationMatrix );
		return obj2;
	}
}
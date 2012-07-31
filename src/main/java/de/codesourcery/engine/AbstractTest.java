package de.codesourcery.engine;

import static de.codesourcery.engine.linalg.LinAlgUtils.vector;

import java.awt.Color;
import java.awt.Component;
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
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import de.codesourcery.engine.geom.Triangle;
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
	protected static final int Z_FAR = 65535;	

	protected volatile float aspectRatio = INITIAL_CANVAS_WIDTH / (float) INITIAL_CANVAS_HEIGHT;

	protected static final int NUM_CUBES = 55;

	protected static final float INC_X = 0.1f;
	protected static final float INC_Y = 0.1f;
	protected static final float INC_Z = 1;
	
	private float x1 = 10;
	private float y1 = 20;
	private float z1 = 30;	

	protected final World world = new World();

	protected final AtomicReference<Float> fov = new AtomicReference<>(90.0f);	

	protected final MouseMotionTracker tracker = new MouseMotionTracker() {

		private Cursor blankCursor;

		{
			final BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
		}

		@Override
		protected void updateEyeTarget(float x, float y, float z) 
		{
			Camera camera = world.getCamera();
			Vector4 target = new Vector4( x,y,z );
			camera.setViewOrientation( target );
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
					world.setupPerspectiveProjection(fov.get(),  aspectRatio , Z_NEAR , Z_FAR );
					break;
				case KeyEvent.VK_T:
					for ( Object3D obj : world.getObjects() ) {
						obj.setTexturesDisabled(  ! obj.isTexturesDisabled() );
					}
					return;					
				case KeyEvent.VK_M:
					for ( Object3D obj : world.getObjects() ) {
						obj.setRenderWireframe( ! obj.isRenderWireframe() );
					}
					return;
				case KeyEvent.VK_ENTER:
					tracker.reset();
					camera.reset();
					System.out.println("*** reset ***");
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

	protected void registerInputListeners(Component frame) 
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
		final Vector4 defaultEyePosition = vector( 0,0, 1 );	
		
		final Object3D sphere = new Object3D();

//		sphere.setPrimitives( LinAlgUtils.createSphere( 2f , 25 , 25 ) , false );
		sphere.setPrimitives( LinAlgUtils.createCube( 4 , 4 , 4 ) , false );
		
		sphere.setIdentifier("sphere");
//		sphere.setModelMatrix( LinAlgUtils.translationMatrix( 0 , 1 , 0 ) );

		for ( int i = 0 ; i < NUM_CUBES ; i++ ) {
			Object3D tmp = makeRandomizedCopy( i+1, sphere );
			world.addObject( tmp );
		}
	
		
//		final Object3D cube = new Object3D();
//
//		final float size = 2f;
////		List<Triangle> primitives = LinAlgUtils.createCube( size,size,size );
//		List<Triangle> primitives = LinAlgUtils.createSphere( size , 100 , 100 );
//		cube.setPrimitives( primitives , false );
//		cube.setIdentifier("cube");
//		cube.setForegroundColor( Color.BLUE ); // needs to be called AFTER setPrimitives() !! 
//
//		world.addObject( cube );

		// Setup camera and perspective projection
		System.out.println("*** setting perspective ***");
		world.setupPerspectiveProjection( 90 , 1.0f , Z_NEAR , Z_FAR );

		System.out.println("*** setting eye position and view vector ***");
		
		final Camera camera = world.getCamera();
		camera.setEyePosition( defaultEyePosition , vector( 0 , 0, -1 ) );		
		camera.updateViewMatrix();
		
		world.getFrustum().forceRecalculatePlaneDefinitions();
	}

	protected final void animateWorld() 
	{
		// rotate eye position around Y axis
		Matrix rot1 = LinAlgUtils.rotZ( y1 );
		rot1 = rot1.multiply( LinAlgUtils.rotX(x1) );

		for ( Object3D tmp : world.getObjects() ) 
		{
			Matrix translation = (Matrix) tmp .getMetaData( Object3D.METADATA_TRANSLATION_MATRIX );
			if ( translation != null ) {
				rot1 = translation.multiply( rot1 );
			} 
			tmp.setModelMatrix( rot1  );
		}

		x1+=0.5;
		y1+=1;
		z1+=1.5;
	}

	protected Object3D makeRandomizedCopy(int count , Object3D prototype) 
	{
		final Object3D obj2 = prototype.copyInstance("Copy_"+count+"_of_"+prototype.getIdentifier());

		int transX = -25+rnd.nextInt( 50 );
		int transY = -25+rnd.nextInt( 50 );
		int transZ = -5+rnd.nextInt( 10 );

		final Matrix translationMatrix = LinAlgUtils.translationMatrix( transX ,transY , transZ );
		obj2.setMetaData( Object3D.METADATA_TRANSLATION_MATRIX , translationMatrix );
		obj2.setModelMatrix( translationMatrix );
		return obj2;
	}
}
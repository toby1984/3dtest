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
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;

import de.codesourcery.engine.geom.Quad;
import de.codesourcery.engine.geom.Triangle;
import de.codesourcery.engine.linalg.LinAlgUtils;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;
import de.codesourcery.engine.math.Function2D;
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
	private static final int INITIAL_CANVAS_HEIGHT = 800;

	private static final int Z_NEAR = 1;
	private static final int Z_FAR = 66000;	

	private volatile float aspectRatio = INITIAL_CANVAS_WIDTH / (float) INITIAL_CANVAS_HEIGHT;

	public static final int NUM_CUBES = 55;

	private static final float INC_X = 1;
	private static final float INC_Y = 1;
	private static final float INC_Z = 1;

	public static void main(String[] args) throws InterruptedException
	{
		new Test3D().run();
	}

	public void run() throws InterruptedException 
	{
		final World world = new World();

		/*
		 * Add a cube.
		 */
		final Object3D cube = new Object3D();

//		cube.setPrimitives( LinAlgUtils.createCube( 10 , 10, 10) );
		cube.setPrimitives( LinAlgUtils.createSphere( 10f , 7 , 7 ) );
		cube.setIdentifier("sphere");
		cube.setModelMatrix( LinAlgUtils.translationMatrix( 0 , 7 , 0 ) );
		cube.setForegroundColor( Color.BLUE ); // needs to be called AFTER setPrimitives() !! 
		//		sphere.addChild( sphere.getOrientedBoundingBox().toObject3D() );

//		world.addObject( sphere );

		//		BoundingBox bb = BoundingBoxGenerator.calculateOrientedBoundingBox( sphere );
		//		sphere.addChild( bb.toObject3D() );		

		for ( int i = 0 ; i < NUM_CUBES ; i++ ) {
			Object3D tmp = makeRandomizedCopy( i+1, cube );
			world.addObject( tmp );
		}

		/*
		 * Add mesh.
		 */
//		Object3D mesh = addMesh(world);

		// Setup camera and perspective projection

		final AtomicReference<Float> fov = new AtomicReference<>(90.0f);

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

		final MouseMotionTracker tracker = new MouseMotionTracker() {

			private Cursor blankCursor;

			{
				final BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
				blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
			}

			@Override
			protected void updateEyeTarget(float x, float y, float z) {
				camera.setViewOrientation( new Vector4( x,y,z ) );
				camera.updateViewMatrix();
				canvas.repaint();
			}

			@Override
			public void setTrackingEnabled(boolean trackingEnabled) {
				super.setTrackingEnabled(trackingEnabled);
				showOrHideMouseCursor( trackingEnabled );
			}

			private void showOrHideMouseCursor(boolean hide) 
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
		});        

		canvas.repaint();

		// rotate eye position around Y axis
		float x1 = 10;
		float y1 = 20;
		float z1 = 30;
		while( true ) 
		{
			// rotate eye position around Y axis
			Matrix rot1 = LinAlgUtils.rotY( y1 );
			rot1 = rot1.multiply( LinAlgUtils.rotX(x1) );
			//			rot1 = rot1.multiply( LinAlgUtils.rotZ(z1) );
			for ( Object3D tmp : world.getObjects() ) 
			{
				Matrix translation = (Matrix) tmp .getMetaData( Object3D.METADATA_TRANSLATION_MATRIX );
				if ( translation != null ) {
					rot1 = translation.multiply( rot1 );
				} 
				tmp.setModelMatrix( rot1.multiply( rot1 ) );
			}

			canvas.repaint();
			x1+=0.5;
			y1+=1;
			z1+=1.5;
			Thread.sleep(20);
		}
	}

	private Object3D addMesh(final World world) {
		final Object3D mesh = new Object3D();
		final Function2D function = new Function2D() {

			@Override
			public float apply(float x, float z) 
			{
				final float distance = (float) Math.sqrt( x*x + z*z );
				final float factor = distance != 0 ? 1-(1/(distance*0.5f) ) : 1;
				float result = (float) Math.sin( distance*5 )*0.5f*factor;
				return result >= 0 ? result : -result;
			}
		};

		final int MESH_WIDTH = 5; // X
		final int MESH_DEPTH = 5; // Z

		final List<Triangle> meshQuads = LinAlgUtils.createXZMesh( function, MESH_WIDTH , MESH_DEPTH , 50 , 50 );

		mesh.setPrimitives( meshQuads );
		mesh.setForegroundColor( Color.WHITE ); // needs to be called AFTER setTriangles() !! 
		mesh.setIdentifier( "XZ mesh");
		mesh.setRenderOutline( true );

		world.addObject( mesh );

		/*
		 * Create cube below mesh
		 */
		float minY = Integer.MAX_VALUE;
		for( Vector4 point : mesh.getOrientedBoundingBox().getPoints() ) {
			if ( point.y() < minY ) {
				minY = point.y();
			}
		}

		final float thickness = 0.5f;
		final List<Triangle> cube = LinAlgUtils.createCube( MESH_WIDTH , thickness , MESH_DEPTH );
		final float translateY = minY != 0 ? minY+(thickness/2.0f) : thickness/2.0f;
		Object3D bottomCube = new Object3D();
		bottomCube.setPrimitives( cube );
		bottomCube.setIdentifier("bottomCube");
		bottomCube.setModelMatrix( LinAlgUtils.translationMatrix( 0 , -translateY , 0) );
		mesh.addChild( bottomCube );
		return mesh;
	}	

	public Object3D makeRandomizedCopy(int count , Object3D prototype) 
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
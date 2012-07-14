package de.codesourcery.engine;

import static de.codesourcery.engine.linalg.LinAlgUtils.rotY;
import static de.codesourcery.engine.linalg.LinAlgUtils.vector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;

import sun.font.CreatedFontTracker;

import de.codesourcery.engine.linalg.LinAlgUtils;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;
import de.codesourcery.engine.render.Camera;
import de.codesourcery.engine.render.Object3D;
import de.codesourcery.engine.render.Panel3D;
import de.codesourcery.engine.render.SimpleRenderer;
import de.codesourcery.engine.render.World;

public class Test3D
{
	private final Random rnd = new Random(System.currentTimeMillis());

	public static final int NUM_CUBES = 5;
	
	public static void main(String[] args) throws InterruptedException
	{
		new Test3D().run();
	}

	public void run() throws InterruptedException 
	{
		final Camera camera = new Camera();
		
		// Create some objects...
		final Object3D sphere = new Object3D();
//	      obj.setTriangles( createPyramid( 10 , 10 , 10 ) );
//		obj.setTriangles( createCube( 0.5 ,0.5,0.5 ) );
//		obj.setScaling( 1/10, 1/10, 1/10 );
		sphere.setTranslation( 0 , 0.6 , -10 );
		sphere.setTriangles( LinAlgUtils.createSphere( 0.5 , 10 , 10 ) );
		sphere.updateModelMatrix();

		final World world = new World();
		
		final Object3D mesh = new Object3D();
		mesh.setTriangles( LinAlgUtils.createXZMesh( 1 , 10 , 10 ,10 ) );
		mesh.updateModelMatrix();
		mesh.setRenderWireframe( true );
		
		world.addObject( mesh );
		world.addObject( sphere );

//		 for ( int i = 0 ; i < NUM_CUBES-1 ; i++ ) {
//		    Object3D tmp = makeRandomizedCopy( obj );
//		    world.addObject( tmp );
//		 }

		// Setup camera and perspective projection
		
		final AtomicReference<Double> fov = new AtomicReference<>(10.0d);
		world.setupPerspectiveProjection(fov.get(), 0.75, 1, 500);
		
		final Vector4 defaultEyePosition = vector(0,0,0);
		camera.setEyePosition( defaultEyePosition , vector(0,0, -1 ) );		
		camera.updateViewMatrix();
		world.setCamera( camera );
		
		// display frame
		SimpleRenderer renderer = new SimpleRenderer();
		renderer.setWorld( world );
		
		final Panel3D canvas = new Panel3D( renderer );

		final JFrame frame = new JFrame("test");
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		canvas.setPreferredSize( new Dimension(800,600 ) );
		canvas.setMinimumSize( new Dimension(800,600 ) );

		frame.getContentPane().setLayout( new BorderLayout() );
		frame.getContentPane().add( canvas , BorderLayout.CENTER );

		frame.pack();
		frame.setVisible(true);

		final double INC_XY = .1;
		final double INC_Z = .1;
		final double ROT_INC = 0.1;		
		
		frame.addKeyListener( new KeyAdapter() {

			public void keyPressed(java.awt.event.KeyEvent e) {

				int keyCode = e.getKeyCode();
				switch( keyCode ) 
				{
					case KeyEvent.VK_PLUS:
						fov.set( fov.get() + 1 );
						world.setupPerspectiveProjection(fov.get(), 0.75, 1, 500);
						System.out.println("FoV: "+fov.get()+" degrees");
						break;
					case KeyEvent.VK_MINUS:
						fov.set( fov.get() - 1 );
						System.out.println("FoV: "+fov.get()+" degrees");
						world.setupPerspectiveProjection(fov.get(), 0.75, 1, 500);
						break;
					case KeyEvent.VK_ENTER:
						camera.reset();
						return;
					case KeyEvent.VK_W:
						camera.moveForward( INC_Z );
						break;
					case KeyEvent.VK_S:
						camera.moveBackward( INC_Z );
						break;                        
					case KeyEvent.VK_A:
						camera.rotateLeft( ROT_INC );
						break;
					case KeyEvent.VK_D:
						camera.rotateRight( ROT_INC );
						break;
					case KeyEvent.VK_Q:
						camera.strafeLeft( INC_XY );
						break;       
					case KeyEvent.VK_E:
						camera.strafeRight( INC_XY );
						break;  
					case KeyEvent.VK_UP:
						camera.moveUp( INC_XY );
						break;       
					case KeyEvent.VK_DOWN:
						camera.moveDown( INC_XY );
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
			Matrix rot1 = rotY( y1 );
			rot1 = rot1.multiply( LinAlgUtils.rotX(x1) );
			rot1 = rot1.multiply( LinAlgUtils.rotZ(z1) );
			for ( Object3D tmp : world.getObjects() ) {
				if ( tmp != mesh ) {
					tmp.setRotation( rot1 );
					tmp.updateModelMatrix();
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

		obj2.setTranslation( transX,transY,transZ );
		obj2.updateModelMatrix();
		return obj2;
	}

}

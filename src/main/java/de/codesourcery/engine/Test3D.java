package de.codesourcery.engine;

import static de.codesourcery.engine.LinAlgUtils.*;

import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;

import de.codesourcery.engine.geom.Vector4;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.render.Object3D;
import de.codesourcery.engine.render.Panel3D;
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
		// Create some objects...
		final Object3D obj = new Object3D();
	      obj.setTriangles( createPyramid( 10 , 10 , 10 ) );
//		obj.setTriangles( createCube( 10 , 10 , 10 ) );
//		obj.setTriangles( createSphere( 10 , 10 , 10  ) );
		obj.updateModelMatrix();

		// debug
//		double[] vertices = obj.getVertices();
//		int point = 1;
//		for ( int i = 0 ; i < vertices.length ; i+=4 ) {
//			System.out.println("Point "+point+" : "+new Vector4( vertices , i ) );
//			point++;
//		}

		final World world = new World();
		world.addObject( obj );

//		 for ( int i = 0 ; i < NUM_CUBES-1 ; i++ ) {
//		    Object3D tmp = makeRandomizedCopy( obj );
//		    world.addObject( tmp );
//		 }

		// Setup camera and perspective projection
		final Vector4 defaultEyePosition = vector(0,0,15);
		final Vector4 ePosition = new Vector4( defaultEyePosition );
		world.setEyeTarget( vector( 0, 0, 100 ) );
		world.setEyePosition( ePosition );

//		final Matrix projMatrix = createPerspectiveProjectionMatrix2( 90 , 100 , -100 );
//		final Matrix projMatrix = createPerspectiveProjectionMatrix1( 90 , 1.0 , 100 , -100 );
		
		final Matrix projMatrix = createPerspectiveProjectionMatrix4( 90 , 1.0 , 100 , -100 ); // GOOD
//		final Matrix projMatrix = createOrthoProjection( 90 , 1.0 , 10, -10 );
		world.setProjectionMatrix( projMatrix );

		world.updateLookAtMatrix();

		// display frame
		final Panel3D canvas = new Panel3D();
		canvas.setWorld( world );

		final JFrame frame = new JFrame("test");
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		canvas.setPreferredSize( new Dimension(800,600 ) );
		canvas.setMinimumSize( new Dimension(800,600 ) );

		frame.getContentPane().add( canvas );

		frame.pack();
		frame.setVisible(true);

		final AtomicReference<Vector4> eyePosition = new AtomicReference<>( ePosition );

		final double INC_XY = 1;
		final double INC_Z = 5;

		frame.addKeyListener( new KeyAdapter() {

			public void keyPressed(java.awt.event.KeyEvent e) {

				int keyCode = e.getKeyCode();
				switch( keyCode ) 
				{
					case KeyEvent.VK_ENTER:
						eyePosition.set( new Vector4( defaultEyePosition ) );
						break;
					case KeyEvent.VK_W:
						eyePosition.get().z( eyePosition.get().z() - INC_Z );
						break;
					case KeyEvent.VK_S:
						eyePosition.get().z( eyePosition.get().z() + INC_Z  );
						break;                        
					case KeyEvent.VK_A:
						eyePosition.get().x( eyePosition.get().x() + INC_XY );
						break;                         
					case KeyEvent.VK_D:
						eyePosition.get().x( eyePosition.get().x() - INC_XY );
						break;    
					case KeyEvent.VK_Q:
						eyePosition.get().y( eyePosition.get().y() + INC_XY );
						break;       
					case KeyEvent.VK_E:
						eyePosition.get().y( eyePosition.get().y() - INC_XY );
						break;     
					default:
						return;
				}
				world.setEyePosition( eyePosition.get() );
				world.updateLookAtMatrix();		
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
//			rot1 = rot1.multiply( rotX(x1) );
//			rot1 = rot1.multiply( rotZ(z1) );
			for ( Object3D tmp : world.getObjects() ) {
				tmp.setRotation( rot1 );
				tmp.updateModelMatrix();
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

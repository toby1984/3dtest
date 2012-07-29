package de.codesourcery.engine;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

import javax.media.opengl.DebugGL2;
import javax.media.opengl.DebugGL3;
import javax.media.opengl.DebugGL3bc;
import javax.media.opengl.DebugGL4;
import javax.media.opengl.DebugGL4bc;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GL3bc;
import javax.media.opengl.GL4;
import javax.media.opengl.GL4bc;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.JFrame;

import com.jogamp.opengl.util.FPSAnimator;

import de.codesourcery.engine.geom.Triangle;
import de.codesourcery.engine.linalg.Vector4;
import de.codesourcery.engine.opengl.OpenGLRenderer;
import de.codesourcery.engine.render.Object3D;
import de.codesourcery.engine.render.World;

/**
 * A minimal program that draws with JOGL in a Swing JFrame using the AWT GLCanvas.
 *
 * @author Wade Walker
 */
public class JOGLTest 
{
	public static final float FIELD_OF_VIEW = 90.0f; // degrees

	public static final int FRAME_WIDTH = 640;
	public static final int FRAME_HEIGHT = 480;
	
    private static final World world = new World();
    private static final OpenGLRenderer renderer = new OpenGLRenderer( world );	
	
    static {
        // setting this true causes window events not to get sent on Linux if you run from inside Eclipse
        GLProfile.initSingleton( false );
    }
    
    public void run() {

    	/*
    	 * World setup
    	 */

    	final Object3D test = new Object3D();
    	test.setIdentifier("test triangle");
    	
    	final Vector4 p1=new Vector4( 0.4f,0.2f,0 );
    	final Vector4 p2=new Vector4( 0.9f,0.2f,0 );
    	final Vector4 p3=new Vector4( 0,0.5f,0);
    	
		final Triangle t = new Triangle(p1,p2,p3);
    	test.setPrimitives( Arrays.asList( t ) );
    	
    	world.addObject( test );
    	
    	/*
    	 * OpenGL setup.
    	 */
        final GLProfile glprofile = GLProfile.getDefault();
        final GLCapabilities glcapabilities = new GLCapabilities( glprofile );
        glcapabilities.setRedBits( 8 );
        glcapabilities.setGreenBits( 8 );
        glcapabilities.setBlueBits( 8 );
        glcapabilities.setAlphaBits( 8 );
        
        final GLCanvas glcanvas = new GLCanvas( glcapabilities );
    	
        glcanvas.addGLEventListener( new GLEventListener() {
            
            @Override
            public void reshape( GLAutoDrawable drawable, int x, int y, int width, int height ) 
            {
            	System.out.println("*** Reshape "+width+" x "+height);
            	world.setupPerspectiveProjection( 90 , width / (float) height, 1 , 1024 );
            	drawable.getGL().glViewport( 0 , 0 , width , height );
            }
            
            @Override
            public void init( GLAutoDrawable drawable ) 
            {
            	if (drawable.getGL().isGL4bc()) {
            	    final GL4bc gl4bc = drawable.getGL().getGL4bc();
            	    drawable.setGL(new DebugGL4bc(gl4bc));
            	} 
            	else {
            	    if (drawable.getGL().isGL4()) {
            	        final GL4 gl4 = drawable.getGL().getGL4();
            	        drawable.setGL(new DebugGL4(gl4));
            	    }
            	    else {
            	          if (drawable.getGL().isGL3bc()) {
            	              final GL3bc gl3bc = drawable.getGL().getGL3bc();
            	              drawable.setGL(new DebugGL3bc(gl3bc));
            	          }
            	          else {
            	              if (drawable.getGL().isGL3()) {
            	                  final GL3 gl3 = drawable.getGL().getGL3();
            	                  drawable.setGL(new DebugGL3(gl3));
            	              }
            	              else {
            	                  if (drawable.getGL().isGL2()) {
            	                      final GL2 gl2 = drawable.getGL().getGL2();
            	                      drawable.setGL(new DebugGL2(gl2));
            	                  }
            	              }
            	          }
            	     }
            	}            
            	renderer.setup( drawable.getGL().getGL3() );
            }
            
            @Override
            public void dispose( GLAutoDrawable glautodrawable ) {
            	renderer.cleanUp( glautodrawable.getGL().getGL3() );
            }
            
            @Override
            public void display( GLAutoDrawable glautodrawable ) 
            {
            	renderer.render( glautodrawable );
            }
        });

        /*
         * Setup JFrame.
         */
        final JFrame jframe = new JFrame( "One Triangle Swing GLCanvas" ); 
        jframe.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent windowevent ) 
            {
                jframe.dispose();
                System.exit( 0 );
            }
        });

        jframe.getContentPane().add( glcanvas, BorderLayout.CENTER );
        jframe.setSize( FRAME_WIDTH, FRAME_HEIGHT);
        jframe.setVisible( true );
        
    	final FPSAnimator animator = new FPSAnimator( glcanvas , 60);
    	animator.start();
    }
    
    public static void main( String [] args ) 
    {
    	new JOGLTest().run();
    }    
    
}
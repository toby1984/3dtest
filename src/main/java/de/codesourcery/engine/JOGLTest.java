package de.codesourcery.engine;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

import de.codesourcery.engine.opengl.OpenGLRenderer;

/**
 * A minimal program that draws with JOGL in a Swing JFrame using the AWT GLCanvas.
 *
 * @author Wade Walker
 */
public class JOGLTest extends AbstractTest
{
    private final OpenGLRenderer renderer = new OpenGLRenderer( world );	
	
    private GLCanvas glcanvas; 
    private JFrame jframe; 
    private FPSAnimator animator; 
    
    static {
        // setting this true causes window events not to get sent on Linux if you run from inside Eclipse
        GLProfile.initSingleton( false );
    }
    
    public void run() 
    {
    	setupJOGL();
    	
    	setupWorld();
    	
    	animator = new FPSAnimator( glcanvas , 60);
    	animator.start();    	
    }
    
    public void setupJOGL() 
    {
        final GLProfile glprofile = GLProfile.getDefault();
        final GLCapabilities glcapabilities = new GLCapabilities( glprofile );
        glcapabilities.setRedBits( 8 );
        glcapabilities.setGreenBits( 8 );
        glcapabilities.setBlueBits( 8 );
        glcapabilities.setAlphaBits( 8 );
        
        glcanvas = new GLCanvas( glcapabilities );
    	
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
            	if ( animator != null ) {
            		animator.stop();
            	}
            }
            
            @Override
            public void display( GLAutoDrawable glautodrawable ) 
            {
            	animateWorld();
            	renderer.render( glautodrawable );
            }
        });

        //  Setup JFrame.
        jframe = new JFrame( "One Triangle Swing GLCanvas" ); 
        jframe.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        jframe.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent windowevent ) 
            {
                jframe.dispose();
            }
        });

        jframe.getContentPane().add( glcanvas, BorderLayout.CENTER );
        jframe.setSize( INITIAL_CANVAS_WIDTH, INITIAL_CANVAS_HEIGHT);
        jframe.setVisible( true );
        
        registerInputListeners( jframe );
    }
    
    public static void main( String [] args ) 
    {
    	new JOGLTest().run();
    }

	@Override
	protected void forceRepaint() {
		glcanvas.repaint();
	}

	@Override
	protected void setMouseCursor(Cursor cursor) {
		jframe.setCursor( cursor );
	}    
    
}
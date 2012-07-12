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
    
    public static void main(String[] args) throws InterruptedException
    {
        new Test3D().run();
    }

    public void run() throws InterruptedException 
    {
        /*
         * Create some objects...
         */
        final Object3D obj = new Object3D();
        obj.setTriangles( createCube( 1 , 1 , 1 ) );
        obj.setScaling( 50 , 50 , 50 );
        obj.updateModelMatrix();

        final World world = new World();
        world.addObject( obj );
        
        for ( int i = 0 ; i < 10 ; i++ ) {
            Object3D tmp = makeRandomizedCopy( obj );
            world.addObject( tmp );
        }

        /*
         * Setup camera and perspective projection
         */
//        final Matrix projMatrix = createPerspectiveProjectionMatrix3( 5 , -1000 );
        final Matrix projMatrix = createPerspectiveProjectionMatrix2( 90 , 5 , -1000 );
//        final Matrix projMatrix = createPerspectiveProjectionMatrix1( 90 , 1.0 , 5 , -1000 );
        
        world.setProjectionMatrix( projMatrix );
        
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
        
        final AtomicReference<Vector4> eyePosition = new AtomicReference<>( vector( 0,0,100 ) );
        
        frame.addKeyListener( new KeyAdapter() {

            public void keyPressed(java.awt.event.KeyEvent e) {
                
                int keyCode = e.getKeyCode();
                switch( keyCode ) {
                    case KeyEvent.VK_UP:
                        eyePosition.get().z( eyePosition.get().z() - 10 );
                        break;
                    case KeyEvent.VK_DOWN:
                        eyePosition.get().z( eyePosition.get().z() + 10 );
                        break;                        
                    case KeyEvent.VK_LEFT:
                        eyePosition.get().x( eyePosition.get().x() + 10 );
                        break;                         
                    case KeyEvent.VK_RIGHT:
                        eyePosition.get().x( eyePosition.get().x() - 10 );
                        break;    
                    case KeyEvent.VK_PLUS:
                        eyePosition.get().y( eyePosition.get().y() + 10 );
                        break;       
                    case KeyEvent.VK_MINUS:
                        eyePosition.get().y( eyePosition.get().y() - 10 );
                        break;                          
                }
            };
        });        
        
        // rotate eye position around Y axis
        double x1 = 0;
        while( true ) 
        {
            // rotate eye position around Y axis
            Matrix rot1 = rotY( x1 );
//            rot1 = rot1.mult( rotX(x1) );
            
            world.setEyePosition( eyePosition.get() );
//            world.setRotation( rot1 );
            world.updateLookAtMatrix2();

            canvas.repaint();
            x1+=1;
            Thread.sleep(20);
        }
        
    }	
    
    public Object3D makeRandomizedCopy(Object3D prototype) 
    {
        final Object3D obj2 = prototype.createCopy();
        
        int transX = rnd.nextInt( 5 );
        int transY = rnd.nextInt( 5 );
        int transZ = -100-rnd.nextInt( 5 );

        obj2.setTranslation( transX,transY,transZ );
        obj2.updateModelMatrix();
        return obj2;
    }

}

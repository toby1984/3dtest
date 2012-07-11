package de.codesourcery.engine;

import static de.codesourcery.engine.LinAlgUtils.*;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;

import de.codesourcery.engine.geom.ITriangle;
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
        final JFrame frame = new JFrame("test");
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        final Matrix projMatrix = createPerspectiveProjectionMatrix( 60.0f , 0 , -1000 );
        
        final World world = new World();
        world.setScaling( scalingMatrix( 0.5 , 0.5 , 0.5 ) );
        
        final Panel3D canvas = new Panel3D( world , projMatrix );

        canvas.setPreferredSize( new Dimension(600,600 ) );
        canvas.setMinimumSize( new Dimension(600,600 ) );

        frame.getContentPane().add( canvas );

        // add object 1
        final Object3D obj = new Object3D();
        obj.setTriangles( createCube( 50 , 50 , 50 ) );
        obj.updateModelMatrix();

        for ( ITriangle t : obj ) {
            System.out.println("Got triangle "+t);
        }
        
        world.addObject( obj );
        
        for ( int i = 0 ; i < 200 ; i++ ) {
            Object3D tmp = makeRandomizedCopy( obj );
            world.addObject( tmp );
        }

        // show frame
        frame.pack();
        frame.setVisible(true);

        int x1 = 0;
        while( true ) 
        {
            Matrix rot1 = rotX( x1 );
//            rot1 = mult( rot1 , rotZ(x1+2) );
//            rot1 = mult( rot1 , rotY(x1+4) );
            world.setRotation( rot1 );
            world.updateViewMatrix();

            canvas.repaint();
            x1+=2;
            Thread.sleep(20);
        }
    }	
    
    public Object3D makeRandomizedCopy(Object3D prototype) 
    {
        final Object3D obj2 = prototype.createCopy();
        
        int transX = rnd.nextInt( 200 );
        int transY = rnd.nextInt( 200 );
        int transZ = -100-rnd.nextInt( 200 );

        obj2.setTranslation( transX,transY,transZ );
        obj2.updateModelMatrix();
        return obj2;
    }

}

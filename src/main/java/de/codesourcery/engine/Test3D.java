package de.codesourcery.engine;

import static de.codesourcery.engine.LinAlgUtils.*;

import java.awt.Dimension;

import javax.swing.JFrame;

import de.codesourcery.engine.geom.ITriangle;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.render.Object3D;
import de.codesourcery.engine.render.Panel3D;

public class Test3D
{
    public static void main(String[] args) throws InterruptedException
    {
        new Test3D().run();
    }

    public void run() throws InterruptedException 
    {
        final JFrame frame = new JFrame("test");
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        final Matrix projMatrix = createPerspectiveProjectionMatrix( 60.0f , 0 , -100 );        
        final Panel3D canvas = new Panel3D( projMatrix );

        canvas.setPreferredSize( new Dimension(400,600 ) );
        canvas.setMinimumSize( new Dimension(400,600 ) );

        frame.getContentPane().add( canvas );

        // add object 1
        final Object3D obj = new Object3D();
        obj.setScaling( 5 , 5 , 5 );
        obj.setTriangles( createCube( 10 , 10 , 10 ) );

        for ( ITriangle t : obj ) {
            System.out.println("Got triangle "+t);
        }
        
        canvas.add( obj );

        // add object #2
        final Object3D obj2 = obj.createCopy();
        obj2.setTranslation( 80 , 0 , 0 );
        canvas.add( obj2 );        

        // show frame
        frame.pack();
        frame.setVisible(true);

        int x1 = 0;
        int x2 = 0;
        while( true ) 
        {
            // rotate object #1
            Matrix rot1 = rotX( x1 );
            rot1 = mult( rot1 , rotZ(x1+2) );
            rot1 = mult( rot1 , rotY(x1+4) );
            obj.setRotation( rot1 );
            obj.recalculateModelMatrix();

            // rotate object #2
            Matrix rot2 = rotX( x2 );
            rot2 = mult( rot2 , rotZ(x2+5) );
            rot2 = mult( rot2 , rotY(x1+2) );
            obj2.setRotation( rot2 );
            obj2.recalculateModelMatrix();

            canvas.repaint();
            x1+=2;
            x2+=3;
            Thread.sleep(20);
        }
    }	

}

package de.codesourcery.engine.render;

import static de.codesourcery.engine.LinAlgUtils.vector;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.ejml.data.DenseMatrix64F;

import de.codesourcery.engine.geom.ITriangle;
import de.codesourcery.engine.geom.Vector4;

public final class Panel3D extends JPanel {

    private final List<Object3D> objects = new ArrayList<Object3D>();

    @SuppressWarnings("unused")
    private static final boolean DRAW_SURFACE_NORMALS = false;

    private static final double PI = Math.PI;
    private static final double PI_HALF = PI / 2.0d;

    private final Vector4 viewVector = vector( 0 , 0 , -100 );

    private int xOffset = 100;
    private int yOffset = 100;  

    private final DenseMatrix64F projectionMatrix;

    public Panel3D(DenseMatrix64F projectionMatrix) 
    {
        this( new ArrayList<Object3D>() , projectionMatrix );
        setDoubleBuffered( true );
    }

    public Panel3D(List<Object3D> objects , DenseMatrix64F projectionMatrix) 
    {
        this.projectionMatrix = projectionMatrix;
        this.objects.addAll( objects );
    }

    public void add(Object3D obj) {
        this.objects.add( obj );
    }

    @Override
    public void paint(Graphics g)
    {
        super.paint(g);

        final Graphics2D graphics = (Graphics2D) g;

        synchronized( objects ) 
        {
            for( Object3D obj : objects ) 
            {
                render( obj , graphics );
            }
        }
    }

    public void render(Object3D obj , Graphics2D graphics) {

        DenseMatrix64F modelMatrix = obj.getModelMatrix();

        for ( ITriangle t : obj )
        {
            Vector4 p1 = t.p1().multiply( modelMatrix );
            Vector4 p2 = t.p2().multiply( modelMatrix );
            Vector4 p3 = t.p3().multiply( modelMatrix );   

            // calculate angle between surface normal and view vector
            Vector4 vec1 = p2.minus( p1 );
            Vector4 vec2 = p3.minus( p1 );

            Vector4 normal = vec1.crossProduct( vec2 );
            double dotProduct= viewVector.dotProduct( normal );

            if ( dotProduct < 0.0 ) {
                continue;
            }

            // do flat shading using the already calculated angle between the surface
            // normal and the view vector
            final double len = viewVector.length() * normal.length();
            final float factor = (float) ( 1 - Math.acos( dotProduct / len ) / PI_HALF );
            graphics.setColor( new Color( factor , factor , factor ) );                

            //                if ( DRAW_SURFACE_NORMALS ) {
            //                  normal = p2.plus( normal.normalize().multiply( 30 ) );
            //                }

            // apply perspective projection
            p1 = p1.multiply( projectionMatrix );
            p2 = p2.multiply( projectionMatrix );
            p3 = p3.multiply( projectionMatrix );

            //                if (DRAW_SURFACE_NORMALS) {
            //                  normal = normal.multiply( projectionMatrix );                
            //                  drawLine( p2 , normal , graphics );
            //                }

            drawTriangle( p1 , p2 , p3 , graphics );
        }
    }

    private void drawTriangle(Vector4 p1,Vector4 p2,Vector4 p3,Graphics2D graphics) {

        final int x[] = new int[] { (int) p1.x() + xOffset , (int) p2.x() + xOffset, (int) p3.x() + xOffset};
        final int y[] = new int[] { (int) p1.y() + yOffset, (int) p2.y() + yOffset, (int) p3.y() + yOffset};

        graphics.fillPolygon( x , y , 3 );
    }

    @SuppressWarnings("unused")
    private void drawLine(Vector4 p1 , Vector4 p2,Graphics2D graphics) 
    {
        graphics.drawLine(xOffset+ (int) p1.x() , yOffset+ (int) p1.y() , xOffset+(int) p2.x() , yOffset+(int) p2.y() );
    }

}
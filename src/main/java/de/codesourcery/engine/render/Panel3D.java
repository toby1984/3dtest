package de.codesourcery.engine.render;

import static de.codesourcery.engine.LinAlgUtils.mult;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.JPanel;

import de.codesourcery.engine.geom.ITriangle;
import de.codesourcery.engine.geom.Vector4;
import de.codesourcery.engine.linalg.Matrix;

public final class Panel3D extends JPanel {

    private World world;

    @SuppressWarnings("unused")
    private static final boolean DRAW_SURFACE_NORMALS = false;

    private static final double PI = Math.PI;
    private static final double PI_HALF = PI / 2.0d;

    private static boolean DEBUG = false;
    
    private int xOffset = 400;
    private int yOffset = 300;  

    public Panel3D()
    {
        setDoubleBuffered( true );
    }
    
    public void setWorld(World world)
    {
        this.world = world;
    }
    
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);

        final Graphics2D graphics = (Graphics2D) g;
        
        final List<Object3D> objects = world.getObjects();
        long time = -System.currentTimeMillis();
        for( Object3D obj : objects ) 
        {
            render( obj , graphics );
        }
        time += System.currentTimeMillis();
        
        g.setColor( Color.BLACK );
        g.drawString( objects.size()+" objects in "+time+" millis" , 10 , 20 );
        g.drawString( "Eye position: "+world.getEyePosition() , 10 , 40 );
        
        renderCoordinateSystem(graphics);
    }

    private void renderCoordinateSystem(Graphics2D graphics)
    {
        final int AXIS_LENGTH = 100;
        final int TICK_DISTANCE = 10;
        

        
        final Matrix viewMatrix = world.getViewMatrix();
        final Vector4 viewVector = world.getViewVector();
        final Matrix projectionMatrix = world.getProjectionMatrix();

        Matrix modelView = viewMatrix.mult( projectionMatrix );
        
        // draw x axis
        graphics.setColor( Color.RED );
        
        drawAxis( "X" , new Vector4(0,0,0) , new Vector4(AXIS_LENGTH,0,0) , modelView , graphics );
        
        for ( int x = 0 ; x < AXIS_LENGTH ; x+= TICK_DISTANCE ) 
        {
            final Vector4 p1 = modelView.multiply( new Vector4(x,0,5) );
            final Vector4 p2 = modelView.multiply( new Vector4(x,0,-5) );
            drawLine( p1 , p2 , graphics );
        }
        
        // draw y axis
        graphics.setColor( Color.MAGENTA );
        
        drawAxis( "Y" , new Vector4(0,0,0) , new Vector4(0,AXIS_LENGTH,0) , modelView , graphics );
        
        for ( int y = 0 ; y < AXIS_LENGTH ; y+= TICK_DISTANCE ) 
        {
            final Vector4 p1 = modelView.multiply( new Vector4(-5,y,0) );
            final Vector4 p2 = modelView.multiply( new Vector4(5,y,0) );
            drawLine( p1 , p2 , graphics );
        }        
        
        // draw z axis
        graphics.setColor( Color.WHITE );
        
        drawAxis( "Z" , new Vector4(0,0,0) , new Vector4(0,0,AXIS_LENGTH) , modelView , graphics );
        
        for ( int z = 0 ; z < AXIS_LENGTH ; z+= TICK_DISTANCE ) 
        {
            final Vector4 p1 = modelView.multiply( new Vector4(-5,0,z) );
            final Vector4 p2 = modelView.multiply( new Vector4(5,0,z) );
            drawLine( p1 , p2 , graphics );
        }          
    }
    
    private void drawAxis(String label,Vector4 start,Vector4 end , Matrix modelView , Graphics2D graphics) 
    {
        start = modelView.multiply( start );
        end = modelView.multiply( end );
        
        drawLine( start , end , graphics );
        drawString( label , end , graphics );
    }

    public void render(Object3D obj , Graphics2D graphics) {

        final Matrix modelMatrix = obj.getModelMatrix();
        
        final Matrix viewMatrix = world.getViewMatrix();
        final Vector4 viewVector = world.getViewVector();
        final Matrix projectionMatrix = world.getProjectionMatrix();
        
        for ( ITriangle t : obj )
        {
            // apply model transformation
            Vector4 p1 = modelMatrix.multiply( t.p1() );
            Vector4 p2 = modelMatrix.multiply( t.p2() );
            Vector4 p3 = modelMatrix.multiply( t.p3() );
            
            p1 = viewMatrix.multiply( p1 );
            p2 = viewMatrix.multiply( p2 );
            p3 = viewMatrix.multiply( p3 );
//            
            // now calculate angle between surface normal and view vector
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

            // apply perspective projection
            
            if ( DEBUG )  System.out.print("P1: "+p1);
            p1 = projectionMatrix.multiply( p1 );
//            p1 = p1.multiply( projectionMatrix );
            if ( DEBUG )  System.out.println(" -> "+p1);
            
            if ( DEBUG )  System.out.print("P2: "+p2);
            p2 = projectionMatrix.multiply( p2 );
//            p2 = p2.multiply( projectionMatrix );
            if ( DEBUG )  System.out.println(" -> "+p2);
            
            if ( DEBUG )  System.out.print("P3: "+p3);
            p3 = projectionMatrix.multiply( p3 );
//            p3 = p3.multiply( projectionMatrix );
            if ( DEBUG )  System.out.println(" -> "+p3);

            drawTriangle( p1 , p2 , p3 , graphics );
        }
    }

    private void drawTriangle(Vector4 p1,Vector4 p2,Vector4 p3,Graphics2D graphics) {

        final int x[] = new int[] { (int) p1.x() + xOffset , (int) p2.x() + xOffset, (int) p3.x() + xOffset};
        final int y[] = new int[] { (int) p1.y() + yOffset, (int) p2.y() + yOffset, (int) p3.y() + yOffset};

        graphics.fillPolygon( x , y , 3 );
    }

    private void drawLine(Vector4 p1 , Vector4 p2,Graphics2D graphics) 
    {
        graphics.drawLine(xOffset+ (int) p1.x() , yOffset+ (int) p1.y() , xOffset+(int) p2.x() , yOffset+(int) p2.y() );
    }
    
    private void drawString(String s, Vector4 p1 , Graphics2D graphics) 
    {
        graphics.drawString( s , xOffset+ (int) p1.x() , yOffset+ (int) p1.y() );
    }    

}
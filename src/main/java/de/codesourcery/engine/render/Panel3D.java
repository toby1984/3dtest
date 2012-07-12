package de.codesourcery.engine.render;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;

import de.codesourcery.engine.geom.ITriangle;
import de.codesourcery.engine.geom.Triangle;
import de.codesourcery.engine.geom.Vector4;
import de.codesourcery.engine.linalg.Matrix;

public final class Panel3D extends JPanel {

    private World world;

    @SuppressWarnings("unused")
    private static final boolean DRAW_SURFACE_NORMALS = false;

    private static final double PI = Math.PI;
    private static final double PI_HALF = PI / 2.0d;

    private static boolean SHOW_NORMALS = true;
    private static boolean RENDER_WIREFRAME = false;
    private static boolean Z_SORTING_ENABLED = true;
    private static boolean RENDER_COORDINATE_SYSTEM = true;
    
    private double scaleX = 100;
    private double scaleY = 100;
    
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
    	g.setColor(Color.GRAY);
        g.fillRect( 0 , 0, getWidth() , getHeight() );

        final Graphics2D graphics = (Graphics2D) g;
        
        final List<Object3D> objects = world.getObjects();
        long time = -System.currentTimeMillis();
        for( Object3D obj : objects ) 
        {
            render( obj , graphics );
        }
        
        triangleBatch.render( graphics );
        
        time += System.currentTimeMillis();
        
        g.setColor( Color.BLACK );
        g.drawString( objects.size()+" objects in "+time+" millis" , 10 , 20 );
        g.drawString( "Eye position: "+world.getEyePosition() , 10 , 40 );
        
        if ( RENDER_COORDINATE_SYSTEM ) {
        	renderCoordinateSystem(graphics);
        }
        
        triangleBatch.clear();
    }

    private void renderCoordinateSystem(Graphics2D graphics)
    {
        final int AXIS_LENGTH = 1;
        final double TICK_DISTANCE = 0.1;
        final double TICK_LENGTH = 0.1;
        
        final Matrix viewMatrix = world.getViewMatrix();
        final Matrix projectionMatrix = world.getProjectionMatrix();

        // draw x axis
        graphics.setColor( Color.RED );
        
        drawAxis( "X" , new Vector4(0,0,0) , new Vector4(AXIS_LENGTH,0,0) , viewMatrix , projectionMatrix  , graphics );
        
        for ( double x = 0 ; x < AXIS_LENGTH ; x+= TICK_DISTANCE ) 
        {
            Vector4 p1 = viewMatrix.multiply( new Vector4(x,0,TICK_LENGTH) );
            Vector4 p2 = viewMatrix.multiply( new Vector4(x,0,-TICK_LENGTH) );
            drawLine( project( p1 , projectionMatrix ) , project( p2 , projectionMatrix ) , graphics );
        }
        
        // draw y axis
        graphics.setColor( Color.MAGENTA );
        
        drawAxis( "Y" , new Vector4(0,0,0) , new Vector4(0,AXIS_LENGTH,0) , viewMatrix , projectionMatrix , graphics );
        
        for ( double y = 0 ; y < AXIS_LENGTH ; y+= TICK_DISTANCE ) 
        {
            final Vector4 p1 = viewMatrix.multiply( new Vector4(-TICK_LENGTH,y,0) );
            final Vector4 p2 = viewMatrix.multiply( new Vector4(TICK_LENGTH,y,0) );
            drawLine( project( p1 , projectionMatrix ) , project( p2 , projectionMatrix ) , graphics );
        }        
        
        // draw z axis
        graphics.setColor( Color.WHITE );
        
        drawAxis( "Z" , new Vector4(0,0,0) , new Vector4(0,0,AXIS_LENGTH) , viewMatrix , projectionMatrix  , graphics );
        
        for ( double z = 0 ; z < AXIS_LENGTH ; z+= TICK_DISTANCE ) 
        {
            final Vector4 p1 = viewMatrix.multiply( new Vector4(-TICK_LENGTH,0,z) );
            final Vector4 p2 = viewMatrix.multiply( new Vector4(TICK_LENGTH,0,z) );
            drawLine( project( p1 , projectionMatrix ) , project( p2 , projectionMatrix ) , graphics );
        }          
    }
    
    private final TriangleBatch triangleBatch = new TriangleBatch();
    
    protected class TriangleBatch {
    	
    	private List<Triangle> triangles = new ArrayList<>();
    	
    	public void add(Color color, Vector4 p1,Vector4 p2,Vector4 p3) {
    		triangles.add( new Triangle( color, new Vector4(p1) ,new Vector4(p2),new Vector4(p3) ) );
    	}
    
    	public void clear() {
    		triangles.clear();
    	}
    	
    	public void render(Graphics2D graphics) {
    		
    		for ( Triangle t : getTriangles() ) 
    		{
    			graphics.setColor( t.getColor() );
    			drawTriangle( t.p1(), t.p2(), t.p3(), graphics );
    		}
    	}
    	
    	private List<Triangle> getTriangles() 
    	{
    		if ( ! Z_SORTING_ENABLED ) {
    			return triangles;
    		}
    		
    		Collections.sort( triangles , new Comparator<Triangle>() {

				@Override
				public int compare(Triangle o1, Triangle o2) 
				{
					double z1 = o1.getMaxW();
					double z2 = o2.getMaxW();
					
					if ( z1 < z2 ) {
						return 1;
					} else if ( z1 > z2 ) {
						return -1;
					}
					return 0;
				}
    		});
    		return triangles;
    	}
    }
    
    private void drawAxis(String label,Vector4 start,Vector4 end , Matrix viewMatrix , Matrix projectionMatrix , Graphics2D graphics) 
    {
        Vector4 p1 = viewMatrix.multiply( start );
        Vector4 p2 = viewMatrix.multiply( end );
        
        drawLine( project( p1 , projectionMatrix ) , project( p2 , projectionMatrix ) , graphics );
        drawString( label , project( p2 , projectionMatrix ) , graphics );
    }

    public void render(Object3D obj , Graphics2D graphics) {

        final Matrix modelMatrix = obj.getModelMatrix();
        
        final Matrix viewMatrix = world.getViewMatrix();
        
        Vector4 viewVector = world.getEyeTarget().minus( world.getEyePosition() );
        
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
            
            // now calculate angle between surface normal and view vector
            Vector4 vec1 = p2.minus( p1 );
            Vector4 vec2 = p3.minus( p1 );

            Vector4 normal = vec1.crossProduct( vec2 );
            
            if ( SHOW_NORMALS ) 
            {
	            graphics.setColor( Color.GREEN );
	            
	            drawLine( project( p1 , projectionMatrix ) , 
	            		  project( p1.plus( normal.normalize() ) , projectionMatrix ) , graphics );
            }
            
            double dotProduct= -1 * viewVector.dotProduct( normal );

            if ( ! SHOW_NORMALS && dotProduct < 0.0 ) {
                continue;
            }

            // do flat shading using the already calculated angle between the surface
            // normal and the view vector
            Color color;
            if ( SHOW_NORMALS && dotProduct < 0.0 ) {
            	color = Color.RED;
            } else {
            	final double len = viewVector.length() * normal.length();
            	final float factor = (float) ( 1 - Math.acos( dotProduct / len ) / PI_HALF );
            	color = new Color( factor , factor , factor );
            }
            
            if ( ! Z_SORTING_ENABLED ) {
            	graphics.setColor( color );
            	drawTriangle( project( p1 , projectionMatrix ) , 
                		      project( p2 , projectionMatrix ) , 
                		      project( p3 , projectionMatrix ) , graphics  );
            } else {
            triangleBatch.add( color , 
            		project( p1 , projectionMatrix ) , 
            		project( p2 , projectionMatrix ) , 
            		project( p3 , projectionMatrix ) );
            }
        }
    }
    
    private Vector4 project(Vector4 in, Matrix projectionMatrix) {
    	return projectionMatrix.multiply( in ).normalizeW();
    }
    
    private void drawTriangle(Vector4 p1,Vector4 p2,Vector4 p3,Graphics2D graphics) 
    {
		if ( RENDER_WIREFRAME ) {
            drawWireTriangle( p1, p2, p3 , graphics );    				
		} else {
            drawFilledTriangle( p1, p2, p3 , graphics );    			
		}    	
    }

    
    private void drawFilledTriangle(Vector4 p1,Vector4 p2,Vector4 p3,Graphics2D graphics) {

        final int x[] = new int[] { screenX( p1 ) , screenX( p2 ) ,  screenX( p3 ) }; 
        final int y[] = new int[] { screenY( p1 ) , screenY( p2 ) , screenY( p3 ) }; 

        graphics.fillPolygon( x , y , 3 );
    }
    
    private void drawWireTriangle(Vector4 p1,Vector4 p2,Vector4 p3,Graphics2D graphics) {

        final int x[] = new int[] { screenX( p1 ) , screenX( p2 ) ,  screenX( p3 ) }; 
        final int y[] = new int[] { screenY( p1 ) , screenY( p2 ) , screenY( p3 ) }; 

        graphics.drawPolygon( x , y , 3 );
    }    
    
    private void drawLine(Vector4 p1 , Vector4 p2,Graphics2D graphics) 
    {
        graphics.drawLine( screenX( p1 ), 
        		           screenY( p1 ) , 
        		           screenX( p2 ) , 
        		           screenY( p2 ) );
    }
    
    private void drawString(String s, Vector4 p1 , Graphics2D graphics) 
    {
        graphics.drawString( s , screenX( p1 ) , screenY( p1 ) );
    }  
    
    private int screenX(Vector4 vector) {
    	final double val = xOffset + vector.x() * scaleX;
    	return (int) val;
    }
    
    private int screenY(Vector4 vector) {
    	final double val = yOffset - vector.y() * scaleY;
    	return (int) val;
    }      

}
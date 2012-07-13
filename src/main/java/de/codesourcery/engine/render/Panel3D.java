package de.codesourcery.engine.render;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JPanel;

import de.codesourcery.engine.LinAlgUtils;
import de.codesourcery.engine.geom.ITriangle;
import de.codesourcery.engine.geom.Triangle;
import de.codesourcery.engine.geom.Vector4;
import de.codesourcery.engine.linalg.Matrix;

public final class Panel3D extends JPanel {

    private World world;

    private static final double PI = Math.PI;
    private static final double PI_HALF = PI / 2.0d;

    private static final boolean SHOW_NORMALS = true;
    private static final boolean RENDER_HIDDEN = true;
    private static final boolean RENDER_WIREFRAME = false;
    private static final boolean Z_SORTING_ENABLED = true;
    private static final boolean RENDER_COORDINATE_SYSTEM = true;
    private static final boolean DRAW_VIEW_VECTOR = true;
    
    private double scaleX = 100;
    private double scaleY = 100;
    
    private int xOffset = 400;
    private int yOffset = 300;  
    
    private final TriangleBatch triangleBatch = new TriangleBatch();    

    protected static final class TriangleWithDepth extends Triangle {

        private final double depth;
        
        public TriangleWithDepth(int color, 
                Vector4 p1, 
                Vector4 p2, 
                Vector4 p3, double depth)
        {
            super(color, p1, p2, p3);
            this.depth = depth;
        }
        
        public double getDepth()
        {
            return depth;
        }
        
    }
    
    protected class TriangleBatch {
    	
    	private List<TriangleWithDepth> triangles = new ArrayList<>();
    	
    	public void add(int color, Vector4 p1,Vector4 p2,Vector4 p3 , double depth ) {
    		triangles.add( new TriangleWithDepth( color, new Vector4(p1) ,new Vector4(p2),new Vector4(p3) , depth ) );
    	}
    
    	public void clear() {
    		triangles.clear();
    	}
    	
    	public void renderBatch(Graphics2D graphics) {
    		
    		for ( Triangle t : getTriangles() ) 
    		{
    			graphics.setColor( new Color( t.getColor() ) );
    			drawTriangle( t.p1(), t.p2(), t.p3(), graphics );
    		}
    	}
    	
		private List<TriangleWithDepth> getTriangles() 
    	{
    		if ( ! Z_SORTING_ENABLED ) {
    			return triangles;
    		}
    		
    		Collections.sort( triangles , new Comparator<TriangleWithDepth>() {

				@Override
				public int compare(TriangleWithDepth o1, TriangleWithDepth o2) 
				{
					double z1 = o1.getDepth();
					double z2 = o2.getDepth();
					
					if ( z1 > z2 ) {
						return -1;
					} else if ( z1 < z2 ) {
						return 1;
					}
					return 0;
				}
    		});
    		return triangles;
    	}
    }
    
    public Panel3D()
    {
        setDoubleBuffered( true );
    }
    
    public void setWorld(World world)
    {
        this.world = world;
    }
    
    private static double[] createCircle(double diameter , int segments,Graphics2D g) {
        
        final double inc = (2*Math.PI) / segments;
        final double radius = diameter / 2.0;
        final double[] result = new double[ segments * 2 ];
        
        int count = 0;
        int i = 0;
        for ( double angle = 2*Math.PI ; angle >0  ; angle -= inc ) 
        {
            result[i++] = radius * Math.cos( angle ); // x
            result[i++] = radius * Math.sin( angle ); // z
            g.drawLine( 100,100 , (int) (100+result[i-2]) , (int) (100 + result[ i-1 ] ) );
            switch(count) {
                case 0:
                    g.setColor(Color.RED);
                    break;
                case 1:
                    g.setColor(Color.GREEN);
                    break;               
                case 2:
                    g.setColor(Color.BLUE);
                    break;              
                default:
                    g.setColor(Color.BLACK);
            }
            count++;
        }
        return result;
    }   
    
    @Override
    public void paint(Graphics g)
    {
    	g.setColor(Color.LIGHT_GRAY);
        g.fillRect( 0 , 0, getWidth() , getHeight() );

        final Graphics2D graphics = (Graphics2D) g;
        
//        createCircle( 100 , 5 , graphics  );
//        
//        if ( 1 != 2 ) {
//            return;
//        }
        
        final List<Object3D> objects = world.getObjects();
        long time = -System.currentTimeMillis();
        for( Object3D obj : objects ) 
        {
            renderObject( obj , graphics );
        }
        
        triangleBatch.renderBatch( graphics );
        
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
        final int AXIS_LENGTH = 10;
        final double TICK_DISTANCE = 1;
        final double TICK_LENGTH = 0.1;
        
        final Matrix viewMatrix = world.getViewMatrix();
        final Matrix projectionMatrix = world.getProjectionMatrix();

        // draw x axis
        graphics.setColor( Color.RED );
        
        drawAxis( "X" , new Vector4(0,0,0) , new Vector4(AXIS_LENGTH,0,0) , viewMatrix , projectionMatrix  , graphics );
        
        for ( double x = 0 ; x < AXIS_LENGTH ; x+= TICK_DISTANCE ) 
        {
            Vector4 p1 = viewMatrix.multiply( new Vector4(x,TICK_LENGTH , 0 ) );
            Vector4 p2 = viewMatrix.multiply( new Vector4(x,-TICK_LENGTH , 0 ) );
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
    
    private void drawAxis(String label,Vector4 start,Vector4 end , Matrix viewMatrix , Matrix projectionMatrix , Graphics2D graphics) 
    {
        Vector4 p1 = viewMatrix.multiply( start );
        Vector4 p2 = viewMatrix.multiply( end );
        
        drawLine( project( p1 , projectionMatrix ) , project( p2 , projectionMatrix ) , graphics );
        drawString( label , project( p2 , projectionMatrix ) , graphics );
    }

    public void renderObject(Object3D obj , Graphics2D graphics) {

        final Matrix modelMatrix = obj.getModelMatrix();
        final Matrix projectionMatrix = world.getProjectionMatrix();
        final Matrix viewMatrix = world.getViewMatrix();

        final Matrix modelView = modelMatrix.multiply(viewMatrix);
        
        final Matrix normalMatrix = modelView.invert();

        int count = 0;
        for ( ITriangle t : obj )
        {
            // apply model transformation
            Vector4 p1 = modelMatrix.multiply( t.p1() );
            Vector4 p2 = modelMatrix.multiply( t.p2() );
            Vector4 p3 = modelMatrix.multiply( t.p3() );
            
            // determine surface normal
            final Vector4 viewVector = world.getEyePosition().minus( p1 );
            
            final double depth = LinAlgUtils.findFarestDistance( world.getEyePosition() , p1 , p2 , p3 );
            
            Vector4 vec1 = p2.minus( p1 );
            Vector4 vec2 = p3.minus( p1 );

            Vector4 normal = vec1.crossProduct( vec2 );
            
            // normal vector needs to be transformed using
            // the INVERTED modelView matrix 
//            normal = normalMatrix.multiply( normal );
            
            // calculate angle between surface normal and view vector
            final double dotProduct= viewVector.dotProduct( normal );
            
            p1 = viewMatrix.multiply( p1 );
            p2 = viewMatrix.multiply( p2 );
            p3 = viewMatrix.multiply( p3 );              
            
            if ( DRAW_VIEW_VECTOR ) 
            {
//                System.out.println("Surface #"+count+" , angle = "+angle+" , normal = "+normal.normalize() +" , zAxis = "+zAxis);
            	graphics.setColor(Color.YELLOW );
            	Vector4 start = p1;
            	Vector4 end =  p1.plus( world.getEyeTarget().minus( world.getEyePosition() ).normalize().multiply( 1000 ) );
            	drawLine( project( start , projectionMatrix ) , project( end , projectionMatrix ) , graphics );
            }
            
            if ( SHOW_NORMALS ) 
            {
            	if ( ( count++ % 2 ) == 0 ) {
            		graphics.setColor( Color.GREEN );
            	} else {
            		graphics.setColor( Color.MAGENTA );
            	}
            	
	            drawLine( project( p1 , projectionMatrix ) , 
	            		  project( p1.plus( normalMatrix.multiply( normal ).normalize().multiply(2) ) , 
	            				  projectionMatrix ) , graphics );            	
	            
            }
            
            // do flat shading using the already calculated angle between the surface
            // normal and the view vector
            int color;
            if ( dotProduct < 0 ) 
            {
                if ( RENDER_HIDDEN ) {
                    color = 255 << 16 | 0 << 8 | 0;
                } else {
                    continue;
                }
            } else {
            	final double len = viewVector.length() * normal.length();
            	final float factor = (float) ( 1 - Math.acos( dotProduct / len ) / PI_HALF );
            	color = (int) (255*0.9*factor) << 16 | 0 << 8 | 0; // new Color( 0.9f*factor , 0 , 0 );
            }
            
            if ( ! Z_SORTING_ENABLED ) {
            	graphics.setColor( new Color( color ) );
            	drawTriangle( project( p1 , projectionMatrix ) , 
                		      project( p2 , projectionMatrix ) , 
                		      project( p3 , projectionMatrix ) , graphics  );
            } 
            else 
            {
            	triangleBatch.add( color , 
            		project( p1 , projectionMatrix ) , 
            		project( p2 , projectionMatrix ) , 
            		project( p3 , projectionMatrix ) , depth  );
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
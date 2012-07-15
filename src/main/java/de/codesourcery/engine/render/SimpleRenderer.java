package de.codesourcery.engine.render;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.codesourcery.engine.geom.IConvexPolygon;
import de.codesourcery.engine.linalg.LinAlgUtils;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;

public final class SimpleRenderer 
{
    private static final double PI = Math.PI;
    private static final double PI_HALF = PI / 2.0d;

    private static final boolean RENDER_OUTLINES = false;
    private static final boolean SHOW_NORMALS = false;
    private static final boolean RENDER_HIDDEN = false;
    private static final boolean RENDER_WIREFRAME = false;
    private static final boolean Z_SORTING_ENABLED = true;
    private static final boolean RENDER_COORDINATE_SYSTEM = true;

    private World world;
    
    private long frameCounter = 0;
    private long totalTime = 0;
    private long totalRenderingTime = 0;
    
    private double scaleX = 100;
    private double scaleY = 100;
    
    private int width;
    private int height;
    
    private int xOffset = 400;
    private int yOffset = 300;  
    
    public static enum RenderingMode {
    	DEFAULT,
    	RENDER_OUTLINE,
    	RENDER_WIREFRAME;
    }
    
    public int getHeight() {
		return height;
	}
    
    public int getWidth() {
		return width;
	}
    
    public void setHeight(int height) {
    	// NDC range is [-1,1] so we put the center of
    	// the viewport at ( width /2 , height / 2 ) and
    	// adjust the scaling factors to be width/2 and height/2 so the
    	// viewport is width x height pixels 
    	yOffset = height / 2;
    	scaleY = height / 2;
		this.height = height;
	}
    
    public void setWidth(int width) {
    	// NDC range is [-1,1] so we put the center of
    	// the viewport at ( width /2 , height / 2 ) and
    	// adjust the scaling factors to be width/2 and height/2 so the
    	// viewport is width x height pixels     	
    	xOffset = width / 2;
    	scaleX = width / 2;
		this.width = width;
	}
    
    protected static final class PolygonWithDepth {

    	private final Vector4[] points;
        private final double depth;
        private final int color;
        
        public PolygonWithDepth(int color, 
                Vector4 p1, 
                Vector4 p2, 
                Vector4 p3, double depth)
        {
            this.color = color;
            this.points= new Vector4[] { p1,p2,p3};
            this.depth = depth;
        }
        
        public PolygonWithDepth(int color, 
                Vector4[] points, double depth)
        {
            this.color = color;
            this.points= points;
            this.depth = depth;
        }        
        
        public Vector4[] getPoints() {
			return points;
		}
        
        public int getColor() {
			return color;
		}
        
        public double getDepth()
        {
            return depth;
        }
    }
    
    protected class TriangleBatch {
    	
    	private List<PolygonWithDepth> triangles = new ArrayList<>();
    	
    	private final RenderingMode renderingMode;
    	
    	public TriangleBatch(RenderingMode renderingMode) {
    		this.renderingMode = renderingMode;
    	}
    	
    	public void add(int color, Vector4 p1,Vector4 p2,Vector4 p3 , double depth ) {
    		
    		if ( world.isInClipSpace( p1,p2,p3 ) ) {
    			triangles.add( new PolygonWithDepth( color, new Vector4(p1) ,new Vector4(p2),new Vector4(p3) , depth ) );
    		} 
    	}
    	
    	public void add(int color, Vector4[] points, double depth ) {
    		
    		if ( world.isInClipSpace( points ) ) {
    			triangles.add( new PolygonWithDepth( color, points , depth ) );
    		} 
    	}    	
    
    	public void clear() {
    		triangles.clear();
    	}
    	
    	public void renderBatch(Object3D obj , Graphics2D graphics) {
    		
    		for ( PolygonWithDepth t : getTriangles() ) 
    		{
    			graphics.setColor( new Color( t.getColor() ) );
    			drawPolygon( t.getPoints() , graphics , renderingMode );
    		}
    		triangles.clear();
    	}
    	
		private List<PolygonWithDepth> getTriangles() 
    	{
    		if ( ! Z_SORTING_ENABLED ) {
    			return triangles;
    		}
    		
    		Collections.sort( triangles , new Comparator<PolygonWithDepth>() {

				@Override
				public int compare(PolygonWithDepth o1, PolygonWithDepth o2) 
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
    
    public void setWorld(World world)
    {
        this.world = world;
    }
    
    public void paint(Graphics g)
    {
        final Graphics2D graphics = (Graphics2D) g;
        
    	graphics.setColor(Color.LIGHT_GRAY);
        graphics.fillRect( 0 , 0, getWidth() , getHeight() );

        final List<Object3D> objects = world.getObjects();
        
        final long start = -System.currentTimeMillis();
        
        /** rendering start **/
        final Matrix viewProjectionMatrix =world.getProjectionMatrix().multiply(  world.getViewMatrix() );
        for( Object3D obj : objects ) 
        {
        	final RenderingMode renderMode;
        	if ( obj.isRenderOutline() ) {
        		renderMode = RenderingMode.RENDER_OUTLINE;
        	} else if ( obj.isRenderWireframe() ) {
        		renderMode = RenderingMode.RENDER_WIREFRAME;
        	} else {
        		renderMode = RenderingMode.DEFAULT;
        	}
			
        	final TriangleBatch batch = new TriangleBatch(renderMode);
            
            prepareRendering( obj , viewProjectionMatrix , batch , graphics );
            
            batch.renderBatch( obj, graphics );
        }
        
        /** rendering end**/
        
        final long renderingTime = start + System.currentTimeMillis();
        
        if ( RENDER_COORDINATE_SYSTEM ) {
            renderCoordinateSystem(graphics);
        }
        
        final long totalTime = start + System.currentTimeMillis();
        
        /*
         * Print statistics.
         */
        this.totalTime += totalTime;
        this.totalRenderingTime += renderingTime;

        frameCounter++;
        final double avgTotalTime = this.totalTime / frameCounter;
        
        final double fps = 1000.0 / avgTotalTime;
        final String fpsString = new DecimalFormat("###0.0#").format( fps );
        final String drawingTimeString = new DecimalFormat("##0.0#").format( 100.0*(this.totalRenderingTime / (double) this.totalTime));
        
        g.setColor( Color.BLACK );
        g.drawString( objects.size()+" objects in "+totalTime+" millis ( rendering time: "+drawingTimeString+"% , "+fpsString+" fps)" , 10 , 20 );
        g.drawString( "Eye position: "+world.getCamera().getEyePosition() , 10 , 40 );
        g.drawString( "Eye target: "+world.getCamera().getEyeTarget() , 10 , 60 );
    }
    
    private void renderCoordinateSystem(Graphics2D graphics)
    {
        final int AXIS_LENGTH = 2;
        final double TICK_DISTANCE = 0.5;
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

    private void prepareRendering(Object3D obj , Matrix viewProjectionMatrix , TriangleBatch batch , Graphics2D graphics) {

        final Matrix modelMatrix = obj.getModelMatrix();
        final Matrix projectionMatrix = world.getProjectionMatrix();
        final Matrix viewMatrix = world.getViewMatrix();

        Matrix normalMatrix = null;
        if ( SHOW_NORMALS ) {
            final Matrix modelView = modelMatrix.multiply(viewMatrix); 
            // normal/directional vectors need to be multiplied with
            // the inverted+transposed modelView matrix because we must not
            // apply translation to them
        	normalMatrix = modelView.invert().transpose();
        }

        final Vector4 eyePosition = world.getCamera().getEyePosition();
        
        int count = 0;
        for ( IConvexPolygon t : obj )
        {
        	final Vector4[] originalPoints = t.getAllPoints();
        	final Vector4[] points = new Vector4[ originalPoints.length ];
        	
        	for ( int i = 0 ; i < points.length ; i++ ) {
        		// multiply() returns NEW vector instances so it's safe to use multiplyInPlace() afterwards
        		points[i] = modelMatrix.multiply( originalPoints[i] );
        	}
        	
            // apply model transformation
            Vector4 p1 = points[0];
            Vector4 p2 = points[1];
            Vector4 p3 = points[2];
            
            // determine surface normal
            final Vector4 viewVector = eyePosition.minus( p1 );
            
            final double depth = LinAlgUtils.findFarestDistance( eyePosition , p1 , p2 , p3 );
            
            Vector4 vec1 = p2.minus( p1 );
            Vector4 vec2 = p3.minus( p1 );

            Vector4 normal = vec1.crossProduct( vec2 );
            
            // calculate angle between surface normal and view vector
            final double dotProduct= viewVector.dotProduct( normal );
            
            if ( SHOW_NORMALS ) 
            {
                // transform points using view matrix
                viewMatrix.multiplyInPlace( points );    
                
            	if ( ( count++ % 2 ) == 0 ) {
            		graphics.setColor( Color.GREEN );
            	} else {
            		graphics.setColor( Color.MAGENTA );
            	}
            	
	            final Vector4 end = p1.plus( normalMatrix.multiply( normal ).normalize().multiply(0.1) );
				drawLine( project( p1 , projectionMatrix ) , project( end , projectionMatrix ) , graphics );
				
	            // do perspective projection by multiplying with projectionMatrix and
	            // dividing coordinates by W
				projectionMatrix.multiplyInPlaceAndNormalizeW( points );
            } 
            else 
            {
	            // do perspective projection by multiplying with projectionMatrix and
	            // dividing coordinates by W            	
            	viewProjectionMatrix.multiplyInPlaceAndNormalizeW( points );
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
            } 
            else 
            {
            	final double len = viewVector.length() * normal.length();
            	final float factor = (float) ( 1 - Math.acos( dotProduct / len ) / PI_HALF );
            	
            	int r = (int) (factor * ((t.getColor() >> 16 ) & 0xff));
            	int g = (int) (factor * ((t.getColor() >> 8 )  & 0xff));
            	int b = (int) (factor * (t.getColor()          & 0xff));
            	color = (int) (r << 16 | g << 8 | b); 
            }
            
            if ( ! Z_SORTING_ENABLED ) {
            	graphics.setColor( new Color( color ) );
            	drawPolygon( points , graphics , batch.renderingMode );
            } 
            else 
            {
            	batch.add( color , points , depth );
            }
        }
    }
    
    private Vector4 project(Vector4 in, Matrix projectionMatrix) {
    	return projectionMatrix.multiply( in ).normalizeW();
    }
    
    private void drawPolygon(Vector4[] points,Graphics2D graphics,RenderingMode mode) 
    {
		if ( mode == RenderingMode.RENDER_WIREFRAME|| RENDER_WIREFRAME ) {
            drawWirePolygon( points , graphics );    				
		} else {
            drawFilledPolygon( points , graphics );
            if ( mode == RenderingMode.RENDER_OUTLINE|| RENDER_OUTLINES ) {
            	Color c = graphics.getColor();
            	graphics.setColor( Color.BLACK );
            	drawWirePolygon(points, graphics);
            	graphics.setColor( c );
            }
		}    	
    }
    
    private void drawFilledPolygon(Vector4[] points,Graphics2D graphics) {

    	final int len = points.length;
    	
        final int x[] = new int[len];
        final int y[] = new int[len];
        
        for ( int i = 0 ; i < len ; i++ ) {
        	x[i] = screenX( points[i] );
        	y[i] = screenY( points[i] );
        }

        graphics.fillPolygon( x , y , 3 );
        if ( RENDER_OUTLINES ) {
        	graphics.getColor();
        }
    }    
    
    private void drawWirePolygon(Vector4[] points,Graphics2D graphics) 
    {
    	final int len = points.length;
    	
        final int x[] = new int[len];
        final int y[] = new int[len];
        
        for ( int i = 0 ; i < len ; i++ ) {
        	x[i] = screenX( points[i] );
        	y[i] = screenY( points[i] );
        }    	
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
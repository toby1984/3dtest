package de.codesourcery.engine.render;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import de.codesourcery.engine.geom.IConvexPolygon;
import de.codesourcery.engine.linalg.Frustum;
import de.codesourcery.engine.linalg.LinAlgUtils;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;
import de.codesourcery.engine.math.Constants;

public final class SoftwareRenderer 
{
	private static final boolean RENDER_OUTLINES = false;
	private static final boolean SHOW_NORMALS = false;
	private static final boolean RENDER_WIREFRAME = false;
	private static final boolean RENDER_COORDINATE_SYSTEM = false;
	private static final boolean DISABLE_LIGHTING = false;
	private static final boolean SKIP_RENDERING = false;
	private static final boolean USE_FRUSTUM_CULLING = true;

	private World world;

	private long frameCounter = 0;
	private long totalTime = 0;
	private long totalRenderingTime = 0;

	private float scaleX = 100;
	private float scaleY = 100;

	private int width;
	private int height;

	private int xOffset = 400;
	private int yOffset = 300;  
	
	private Vector4 lightPosition = new Vector4(0f,0.1f,0f);	
	private float ambientLightFactor =0.1f;

	private final List<PrimitiveBatch> batches = new ArrayList<PrimitiveBatch>();
	
	private ExecutorService calculationThreadPool;

	public static enum RenderingMode {
		DEFAULT,
		RENDER_OUTLINE,
		RENDER_WIREFRAME;
	}

	public SoftwareRenderer() 
	{
		// setup calculation thread-pool
		int threadCount = Runtime.getRuntime().availableProcessors();
		if ( threadCount > 1 ) {
			threadCount--; // reserve one CPU core for the rendering thread
		}
		setupThreadPool( threadCount );
	}

	protected void setupThreadPool(int threads) 
	{
		final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(500);
		final ThreadFactory threadFactory = new ThreadFactory() {

			@Override
			public Thread newThread(final Runnable r) 
			{
				final Thread result = new Thread(r , "calculation-thread");
				result.setDaemon( true );
				return result;
			}
		};
		System.out.println("Using one thread for rendering and "+threads+" threads for calculations");
		calculationThreadPool = new ThreadPoolExecutor(threads, 
				threads, 
				24, 
				TimeUnit.HOURS , queue, threadFactory, new ThreadPoolExecutor.CallerRunsPolicy() );
	}
	
	public void setAmbientLightFactor(float ambientLightFactor) {
		this.ambientLightFactor = ambientLightFactor;
	}

	public void setLightPosition(Vector4 lightPosition) {
		this.lightPosition = lightPosition;
	}
	
	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void setCpuCount(int cpuCount) 
	{
		if (cpuCount < 1 ) {
			throw new IllegalArgumentException("cpuCount must be >= 1");
		}
		// reserves one CPU core for the rendering thread
		setupThreadPool( cpuCount <= 1 ? 1 : cpuCount -1 );
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

	protected static final class PrimitiveWithDepth {

		private final Vector4[] points;
		private final int color;

		public PrimitiveWithDepth(int color, Vector4[] points)
		{
			this.color = color;
			this.points= points;
		}        

		public Vector4[] getPoints() {
			return points;
		}

		public int getColor() {
			return color;
		}

		public float getDepth()
		{
			float result = points[0].w();
			final int len = points.length;
			for ( int i = 1 ; i < len ; i++ ) 
			{
				if ( points[i].w() > result ) {
					result = points[i].w();
				}
			}
			return result;
		}
	}

	protected final class PrimitiveBatch {

		private float distanceToViewer;
		private final Object3D object;
		private List<PrimitiveWithDepth> primitives = new ArrayList<>();

		private final RenderingMode renderingMode;

		public PrimitiveBatch(RenderingMode renderingMode,Object3D object) {
			this.renderingMode = renderingMode;
			this.object = object;
		}

		public void setDistanceToViewer(float distanceToViewer) {
			this.distanceToViewer = distanceToViewer;
		}
		
		public float getDistanceToViewer() {
			return distanceToViewer;
		}
		
		public int getPrimitiveCount() {
			return primitives.size();
		}
		
		public RenderingMode getRenderingMode() {
			return renderingMode;
		}
		
		public boolean isEmpty() {
			return primitives.isEmpty();
		}
		
		public void add(int color, Vector4[] points) {

			if ( world.isInClipSpace( points ) ) {
				primitives.add( new PrimitiveWithDepth( color, points) );
			} 
		}    	

		public void clear() {
			primitives.clear();
		}

		public void renderBatch(Graphics2D graphics) {

		    if ( ! SKIP_RENDERING ) 
		    {
    			for ( PrimitiveWithDepth t : getTriangles() ) 
    			{
    				graphics.setColor( new Color( t.getColor() ) );
    				drawPolygon( t.getPoints() , graphics , renderingMode );
    			}
		    }
		    // TODO: Performance - Maybe clearing is not helping GC that much and should better be omitted ?
			primitives.clear();
		}

		private List<PrimitiveWithDepth> getTriangles() 
		{
			Collections.sort( primitives , new Comparator<PrimitiveWithDepth>() {

				@Override
				public int compare(PrimitiveWithDepth o1, PrimitiveWithDepth o2) 
				{
					float z1 = o1.getDepth();
					float z2 = o2.getDepth();

					if ( z1 > z2 ) {
						return -1;
					} else if ( z1 < z2 ) {
						return 1;
					}
					return 0;
				}
			});
			return primitives;
		}
	}

	public void setWorld(World world)
	{
		this.world = world;
	}

	protected final class RenderThread implements Runnable {

		private final Graphics2D graphics;
		private final Object3D object;
		private final Matrix viewProjectionMatrix;

		public RenderThread(Object3D object,Matrix viewProjectionMatrix,Graphics2D graphics) {
			this.graphics = graphics;
			this.object = object;
			this.viewProjectionMatrix = viewProjectionMatrix;
		}

		@Override
		public void run() 
		{
			final RenderingMode renderMode;
			if ( object.isRenderOutline() ) {
				renderMode = RenderingMode.RENDER_OUTLINE;
			} else if ( object.isRenderWireframe() ) {
				renderMode = RenderingMode.RENDER_WIREFRAME;
			} else {
				renderMode = RenderingMode.DEFAULT;
			}

			final PrimitiveBatch batch = new PrimitiveBatch(renderMode,object);

			prepareRendering( object , viewProjectionMatrix , batch , graphics );
			
			if ( ! batch.isEmpty() ) {
				batch.renderBatch( graphics );
				
			}
		}
	}

	public void paint(final Graphics g)
	{
		final Graphics2D graphics = (Graphics2D) g;

		// clear canvas
		graphics.setColor(Color.LIGHT_GRAY);
		graphics.fillRect( 0 , 0, getWidth() , getHeight() );

		final List<Object3D> objects = world.getObjects();

		final long start = -System.currentTimeMillis();

		// ** rendering start **
		
		final Matrix viewProjectionMatrix = world.getProjectionMatrix().multiply(  world.getViewMatrix() );

		// latch used to wait until all objects have been calculated
		final CountDownLatch latch = new CountDownLatch( objects.size() );

		final AtomicLong renderingTime = new AtomicLong(0); // updated by rendering thread
		
		synchronized( batches ) {
			batches.clear();
		}
		
		for( final Object3D obj : objects ) 
		{
			renderObject(graphics, viewProjectionMatrix, latch, renderingTime, obj , ! obj.hasChildren() );
		}

		// wait until primitives for all visible objects have been queued
		while( true ) 
		{
			try {
				if ( latch.await(5,TimeUnit.SECONDS) ) 
				{
					break;
				} else {
					System.err.println("Rendering didn't return after 5 seconds?");
				}
			} catch(InterruptedException e) {
			}
		}
		
		// render objects using painter's algorithm
		
		final long renderStart = System.currentTimeMillis();
		
		synchronized( batches ) 
		{
			// sort objects ascending by distance to viewer...	
			Collections.sort( batches , new Comparator<PrimitiveBatch>() {
	
				@Override
				public int compare(PrimitiveBatch o1, PrimitiveBatch o2) 
				{
					final float d1 = o1.getDistanceToViewer();
					final float d2 = o2.getDistanceToViewer();
					if ( d1 > d2 ) {
						return -1; // farther away , render first
					} else if ( d1 < d2 ) {
						return 1;
					}
					return 0;
				}
			});
			
			for ( PrimitiveBatch batch : batches ) {
				batch.renderBatch( graphics );
			}
		}
		
		if ( RENDER_COORDINATE_SYSTEM ) {
			renderCoordinateSystem(graphics);
		}
		
		renderingTime.addAndGet( System.currentTimeMillis() - renderStart );
		
		// ** rendering end **

		final long totalTime = start + System.currentTimeMillis();

		//  Render statistics
		this.totalTime += totalTime;
		this.totalRenderingTime += renderingTime.get();

		frameCounter++;
		
		final float avgTotalTime = this.totalTime / frameCounter;
		final float fps = 1000.0f / avgTotalTime;
		
		final String fpsString = new DecimalFormat("###0.0#").format( fps );
		final String drawingTimeString = new DecimalFormat("##0.0#").format( 100.0*(this.totalRenderingTime / (float) this.totalTime));

		g.setColor( Color.WHITE );
		g.drawString( objects.size()+" objects in "+totalTime+" millis ( rendering time: "+drawingTimeString+"% , "+fpsString+" fps)" , 10 , 20 );
//		g.drawString( "Use A,D,W,S,Q,E to move , + and - minus to zoom , ESC to exit mouse look, ENTER to reset view position", 10 , 40 );
		g.drawString( "Eye position: "+world.getCamera().getEyePosition() , 10 , 40 );
		g.drawString( "Eye target: "+world.getCamera().getEyeTarget() , 10 , 60 );
		g.drawString( "View vector: "+world.getCamera().getViewOrientation() , 10 , 80 );		
	}

	private void renderObject(final Graphics2D graphics,
			final Matrix viewProjectionMatrix, 
			final CountDownLatch latch,
			final AtomicLong renderingTime, 
			final Object3D obj,final boolean isLastChild) 
	{
		calculationThreadPool.submit( new Runnable() {

			@Override
			public void run() 
			{
				try 
				{
					final RenderingMode renderMode;
					if ( obj.isRenderOutline() ) {
						renderMode = RenderingMode.RENDER_OUTLINE;
					} else if ( obj.isRenderWireframe() ) {
						renderMode = RenderingMode.RENDER_WIREFRAME;
					} else {
						renderMode = RenderingMode.DEFAULT;
					}

					final PrimitiveBatch batch = new PrimitiveBatch(renderMode,obj);

					prepareRendering( obj , viewProjectionMatrix , batch , graphics );

					if ( ! batch.isEmpty() ) 
					{
						synchronized( batches ) {
							batches.add( batch );
						}
						
						if ( isLastChild && ! obj.hasChildren() ) { // only count down after a root object has been queued for rendering
							latch.countDown();
						}						
						
						// recursively render children only if parent was rendered
						for ( Iterator<Object3D> it = obj.getChildren().iterator() ; it.hasNext(); ) 
						{
							renderObject( graphics , viewProjectionMatrix , latch , renderingTime , it.next() , ! it.hasNext() );
						}
					} 
					else 
					{
						latch.countDown();
					}
				}
				catch(Exception e) 
				{
					e.printStackTrace();
				    System.err.println("Failed to render "+obj);
					latch.countDown();
				}
			}
		} );
	}

	private void renderCoordinateSystem(Graphics2D graphics)
	{
		final int AXIS_LENGTH = 2;
		final float TICK_DISTANCE = 0.5f;
		final float TICK_LENGTH = 0.1f;

		final Matrix viewMatrix = world.getViewMatrix();
		final Matrix projectionMatrix = world.getProjectionMatrix();

		// draw x axis
		graphics.setColor( Color.RED );

		drawAxis( "X" , new Vector4(0,0,0) , new Vector4(AXIS_LENGTH,0,0) , viewMatrix , projectionMatrix  , graphics );

		for ( float x = 0 ; x < AXIS_LENGTH ; x+= TICK_DISTANCE ) 
		{
			Vector4 p1 = viewMatrix.multiply( new Vector4(x,TICK_LENGTH , 0 ) );
			Vector4 p2 = viewMatrix.multiply( new Vector4(x,-TICK_LENGTH , 0 ) );
			drawLine( project( p1 , projectionMatrix ) , project( p2 , projectionMatrix ) , graphics );
		}

		// draw y axis
		graphics.setColor( Color.MAGENTA );

		drawAxis( "Y" , new Vector4(0,0,0) , new Vector4(0,AXIS_LENGTH,0) , viewMatrix , projectionMatrix , graphics );

		for ( float y = 0 ; y < AXIS_LENGTH ; y+= TICK_DISTANCE ) 
		{
			final Vector4 p1 = viewMatrix.multiply( new Vector4(-TICK_LENGTH,y,0) );
			final Vector4 p2 = viewMatrix.multiply( new Vector4(TICK_LENGTH,y,0) );
			drawLine( project( p1 , projectionMatrix ) , project( p2 , projectionMatrix ) , graphics );
		}        

		// draw z axis
		graphics.setColor( Color.WHITE );

		drawAxis( "Z" , new Vector4(0,0,0) , new Vector4(0,0,AXIS_LENGTH) , viewMatrix , projectionMatrix  , graphics );

		for ( float z = 0 ; z < AXIS_LENGTH ; z+= TICK_DISTANCE ) 
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

	private void prepareRendering(Object3D obj , Matrix viewProjectionMatrix , PrimitiveBatch batch , Graphics2D graphics) {

		final Matrix modelMatrix = obj.getModelMatrix();
		final Matrix viewMatrix = world.getViewMatrix();
		final Matrix projectionMatrix = world.getProjectionMatrix();
		
		final Matrix modelView = modelMatrix.multiply(viewMatrix);
		
		// Frustum culling
		if ( USE_FRUSTUM_CULLING && world.getFrustum().testContains( modelView , obj ) == Frustum.TestResult.OUTSIDE ) 
		{
			return;
		}		

		Matrix normalMatrix = null;
		if ( SHOW_NORMALS ) {
			// normal/directional vectors need to be multiplied with
			// the inverted+transposed modelView matrix because we must not
			// apply translation to them
			normalMatrix = modelView.invert().transpose();
		}

		final boolean renderWireframe = RENDER_WIREFRAME || batch.getRenderingMode() == RenderingMode.RENDER_WIREFRAME;
		
		final Vector4 eyePosition = world.getCamera().getEyePosition();

		// calculate distance of object to viewer based on bounding box
		final Vector4[] bbPoints = obj.getOrientedBoundingBox().getPoints();
		modelMatrix.multiplyInPlace( bbPoints );
		float maxDistance = 0;
		for ( Vector4 vertex : bbPoints ) 
		{
			float distance = vertex.distanceTo( eyePosition );
			if ( distance > maxDistance ) {
				maxDistance = distance;
			}
		}
		batch.setDistanceToViewer( maxDistance );
		
		int count = 0;
		for ( IConvexPolygon t : obj )
		{
			final Vector4[] originalPoints = t.getAllPoints();
			final Vector4[] points = new Vector4[ originalPoints.length ];

            // apply model transformation			
			for ( int i = 0 ; i < points.length ; i++ ) {
				// multiply() returns NEW vector instances so it's safe to use multiplyInPlace() afterwards
				points[i] = modelMatrix.multiply( originalPoints[i] );
			}

			Vector4 p1 = points[0];
			Vector4 p2 = points[1];
			Vector4 p3 = points[2];
			
			/* Calculate surface normal.
			 * 
			 * For this to work, the vertices p1,p2,p3 need to be in COUNTER clock-wise orientation, 
			 * otherwise the normal vector will point inside the object.
			 */
			final Vector4 vec1 = p2.minus( p1 );
			final Vector4 vec2 = p3.minus( p1 );

			final Vector4 normal = vec1.crossProduct( vec2 );			

			// calculate angle between surface normal and view vector
			final Vector4 viewVector = eyePosition.minus( p1 );			
			final float viewDotProduct= viewVector.dotProduct( normal );

			if ( viewDotProduct < 0 && ! renderWireframe ) {
				continue;
			}
			
			final Vector4 lightVector = lightPosition.minus( p1 );			
			
			if ( SHOW_NORMALS ) 
			{
				// transform points using view matrix
				viewMatrix.multiplyInPlace( points );    

				if ( ( count++ % 2 ) == 0 ) {
					graphics.setColor( Color.GREEN );
				} else {
					graphics.setColor( Color.MAGENTA );
				}

				final Vector4 end = p1.plus( normalMatrix.multiply( normal ).normalize().multiply(2f) );
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
			
			// ============= all vertices are in NDC (normalized device coordinates ) from here on ============

			// do flat shading using the already calculated angle between the surface
			// normal and the light vector
			int color;
			if ( renderWireframe || DISABLE_LIGHTING ) 
			{
				color = t.getColor();
			} 
			else 
			{
		        final float lightDotProduct= lightVector.dotProduct( normal ); 
				float factor;
				if ( lightDotProduct < 0 ) { // surface does not point towards the light source
					factor = ambientLightFactor;
				} else 
				{
					float len = lightVector.length() * normal.length();
					factor = (float) ( 1 - Math.acos( lightDotProduct / len ) / Constants.PI_HALF );
					if ( len < 1 ) {
						len = 1;
					}
//					factor = Math.min( 1.0f , (float) ( factor * 1/(len*len) ) );
					factor = Math.min( 1.0f , factor );
				}
				if ( factor < ambientLightFactor ) {
					factor = ambientLightFactor;
				} 
				int r = (int) (factor * ((t.getColor() >> 16 ) & 0xff));
				int g = (int) (factor * ((t.getColor() >> 8 )  & 0xff));
				int b = (int) (factor * (t.getColor()          & 0xff));
				color = (r << 16 | g << 8 | b); 					
			}
			
			// queue primitives for rendering
			batch.add( color , points );
		}
	}

	private Vector4 project(Vector4 in, Matrix projectionMatrix) {
		return projectionMatrix.multiply( in ).normalizeW();
	}

	protected void drawPolygon(Vector4[] points,Graphics2D graphics,RenderingMode mode) 
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

	protected void drawFilledPolygon(Vector4[] points,Graphics2D graphics) {

		final int len = points.length;

		final int x[] = new int[len];
		final int y[] = new int[len];

		for ( int i = 0 ; i < len ; i++ ) {
			x[i] = screenX( points[i] );
			y[i] = screenY( points[i] );
		}

		graphics.fillPolygon( x , y , points.length );
		if ( RENDER_OUTLINES ) {
			graphics.getColor();
		}
	}    

	protected void drawWirePolygon(Vector4[] points,Graphics2D graphics) 
	{
		final int len = points.length;

		final int x[] = new int[len];
		final int y[] = new int[len];

		for ( int i = 0 ; i < len ; i++ ) {
			x[i] = screenX( points[i] );
			y[i] = screenY( points[i] );
		}    	
		graphics.drawPolygon( x , y , points.length );        
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
		final float val = xOffset + vector.x() * scaleX;
		return (int) val;
	}

	private int screenY(Vector4 vector) {
		final float val = yOffset - vector.y() * scaleY;
		return (int) val;
	}      

}
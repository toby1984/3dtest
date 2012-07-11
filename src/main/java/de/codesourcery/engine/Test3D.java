package de.codesourcery.engine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.ejml.alg.dense.mult.MatrixMatrixMult;
import org.ejml.alg.dense.mult.MatrixVectorMult;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.RowD1Matrix64F;
import org.ejml.ops.CommonOps;

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

        final RowD1Matrix64F projMatrix = createPerspectiveProjectionMatrix( 60.0f , 0 , -100 );        
        final MyCanvas canvas = new MyCanvas( projMatrix );
        
        canvas.setPreferredSize( new Dimension(400,600 ) );
        canvas.setMinimumSize( new Dimension(400,600 ) );
        
        frame.getContentPane().add( canvas );
        
        // add object 1
        final Object3D obj = new Object3D();
        obj.setScaling( 5 , 5 , 5 );
        obj.setTranslation( 0 , 0 , 0 );
        obj.add( createCube( 10 , 10 , 10 ) );
        
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
        		DenseMatrix64F rot1 = rotX( x1 );
        	    rot1 = mult( rot1 , rotZ(x1+2) );
        	    rot1 = mult( rot1 , rotY(x1+4) );
	        	obj.setRotation( rot1 );
	        	obj.updateViewMatrix();
	        	
        	    // rotate object #2
        		DenseMatrix64F rot2 = rotX( x2 );
        	    rot2 = mult( rot2 , rotZ(x2+5) );
        	    rot2 = mult( rot2 , rotY(x1+2) );
	        	obj2.setRotation( rot2 );
	        	obj2.updateViewMatrix();
	        	
	        	canvas.repaint();
	        	x1+=2;
	        	x2+=3;
	        	Thread.sleep(20);
        }
    }	

    private static final class Triangle {
        
        private Vector3D p1;
        private Vector3D p2;
        private Vector3D p3;
        
        public Triangle(Vector3D p1,Vector3D p2,Vector3D p3) {
            this.p1 = p1;
            this.p2 = p2;
            this.p3 = p3;
        }
    }
    
    private static final class Quad {

        private Triangle t1;
        private Triangle t2;
        
        public Quad(Triangle t1,Triangle t2)
        {
        	this.t1 = t1;
        	this.t2 = t2;
        }
        
        public Quad(Vector3D p1 , Vector3D p2,Vector3D p3) 
        {
            Vector3D v1 = vector( p1.x , p1.y , p1.z );
            Vector3D v2 = vector( p2.x , p2.y , p2.z );
            Vector3D v3 = vector( p3.x , p3.y , p3.z );
            
            t1 = new Triangle( v1 , v2 , v3 );
            
            Vector3D v4 = vector( p1.x , p1.y , p1.z );
            Vector3D v5 = vector( p3.x , p3.y , p3.z );
            Vector3D v6 = vector( p3.x , p1.y , p3.z );
            
            t2 = new Triangle( v4 , v5 , v6 );
        }
    }
    
    public static final class Vector3D 
    {
        private double x;
        private double y;
        private double z;
        private double w;
        
        public Vector3D(double[] data) 
        {
            if ( data.length < 3 || data.length > 4 ) {
                throw new IllegalArgumentException("Invalid vector component count: "+data);
            }
            this.x = data[0];
            this.y = data[1];
            this.z = data[2];
            if ( data.length == 4 ) {
                this.w = data[3];
            } else {
                this.w = 1.0f;
            }
        }
        
        public Vector3D minus(Vector3D other) {
        	return vector( this.x - other.x , this.y - other.y , this.z - other.z , w );
        }
        
        public Vector3D plus(Vector3D other) {
        	return vector( this.x + other.x , this.y + other.y , this.z + other.z , w );
        }        
        
        public Vector3D(double x,double y,double z) {
            this(x,y,z,1);
        }
        
        public Vector3D(double x,double y,double z,double w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }
        
        public Vector3D(DenseMatrix64F result)
        {
            if ( result.numRows != 4 ) {
                throw new IllegalArgumentException("Invalid row count "+result.numRows+" for "+result);
            }
            if ( result.numCols != 1 ) {
                throw new IllegalArgumentException("Invalid column count "+result.numCols+" for "+result);
            }            
            this.x = (double) result.get( 0 , 0 );
            this.y = (double) result.get( 1 , 0 );
            this.z = (double) result.get( 2 , 0 );
            this.w = (double) result.get( 3 , 0 );
        }

        private DenseMatrix64F toRowMatrix() 
        {
            final DenseMatrix64F result = new DenseMatrix64F( 4 , 1 );
            result.set( 0 , 0 , x );
            result.set( 1 , 0 , y );
            result.set( 2 , 0 , z );
            result.set( 3 , 0 , w );
            return result;
        }

        public Vector3D multiply( RowD1Matrix64F matrix ) {
            
            final DenseMatrix64F result = new DenseMatrix64F(4 , 1 );
            MatrixVectorMult.mult( matrix , toRowMatrix()  , result );
            Vector3D resultVec = new Vector3D( result );
            return resultVec;
        }
        
        public double length() {
            return Math.sqrt( x*x + y*y + z*z );   
        }
        
        public Vector3D multiply(double value) {
            return new Vector3D( x*value , y*value , z*value , w );
        }
        
        public Vector3D normalize() 
        {
            final double len = length();
            return new Vector3D( x / len , y / len , z / len  , w );
        }
        
        // scalar / dot product
        public double dotProduct(Vector3D o) {
            return (x*o.x + y*o.y + z*o.z );
        }
        
        public double angleInRadians(Vector3D o) {
            // => cos
            final double cosine = dotProduct( o ) / ( length() * o.length() );
            return (double) Math.acos( cosine );
        }
        
        public double angleInDegrees(Vector3D o) {
            final double factor = (double) (180.0f / Math.PI);
            return angleInRadians(o)*factor;
        }        
        
        public Vector3D crossProduct(Vector3D other) 
        {
        	/*
	         * ox = (y1 * z2) - (y2 * z1)
	         * oy = (z1 * x2) - (z2 * x1)
	         * oz = (x1 * y2) - (x2 * y1)        	 
        	 */
            return new Vector3D( y*other.z - other.y * z , 
            		             (z*other.x - other.z * x) , 
            		             x*other.y - other.x * y );
        }
        
        @Override
        public String toString()
        {
            return "("+x+","+y+","+z+","+w+")";
        }
    }
    
    public static final class Object3D {
        
        private DenseMatrix64F translation = identity();
        private DenseMatrix64F scaling = identity();
        private DenseMatrix64F rotation = identity();
        
        private DenseMatrix64F viewMatrix = identity();
        
        private final List<Triangle> triangles = new ArrayList<>();
        
        public Object3D() {
        }
        
        public Object3D createCopy() 
        {
        	Object3D result = new Object3D();
        	result.translation = translation;
        	result.scaling = scaling;
        	result.rotation = rotation;
        	result.triangles.addAll( this.triangles );
        	result.updateViewMatrix();
        	return result;
        }
        
        public void updateViewMatrix() {
        	DenseMatrix64F transform = mult( translation , scaling );
        	viewMatrix = mult(  transform , rotation );
        }        
        
        public DenseMatrix64F getViewMatrix() {
        	return viewMatrix;
        }
        
        public void setRotation(DenseMatrix64F rotation) {
        	this.rotation = rotation;
        }
        
        public void setTranslation(double x , double y , double z )
        {
            this.translation = translationMatrix(x , y , z );
        }
        
        public void setScaling(double x , double y , double z)
        {
            this.scaling = scalingMatrix(x,y,z);
        }
        
        public void add(Triangle t) {
            this.triangles.add( t );
        }
        
        public void add(Quad quad) 
        {
            this.triangles.add( quad.t1 );
            this.triangles.add( quad.t2 );
        }     
        
        public void add(List<Quad> quads) 
        {
        	for ( Quad q : quads ) {
        		add( q );
        	}
        }          
        
        public Object3D(DenseMatrix64F translation ) {
            this.translation = translation;
        }        
        
        public Object3D(DenseMatrix64F translation , DenseMatrix64F scaling ) {
            this.translation = translation;
            this.scaling = scaling;
        }          
        
        public List<Triangle> getTriangles() {
            return triangles;
        }
    }
    
    public static DenseMatrix64F createMatrix(Vector3D v1,Vector3D v2,Vector3D v3,Vector3D v4) 
    {
        DenseMatrix64F result = new DenseMatrix64F(4,4);
        
        result.set( 0 , 0 , v1.x );
        result.set( 1 , 0 , v1.y );
        result.set( 2 , 0 , v1.z );
        result.set( 3 , 0 , v1.w );
        
        result.set( 0 , 1 , v2.x );
        result.set( 1 , 1 , v2.y );
        result.set( 2 , 1 , v2.z );
        result.set( 3 , 1 , v2.w );   
        
        result.set( 0 , 2 , v3.x );
        result.set( 1 , 2 , v3.y );
        result.set( 2 , 2 , v3.z );
        result.set( 3 , 2 , v3.w );          
        
        result.set( 0 , 3 , v4.x );
        result.set( 1 , 3 , v4.y );
        result.set( 2 , 3 , v4.z );
        result.set( 3 , 3 , v4.w );        
        return result;
    }
    
    private RowD1Matrix64F createPerspectiveProjectionMatrix(double fovInDegrees , double zNearClippingPlane , double zFarClippingPlane) {
        
        final double S = (double) ( 1.0d / ( Math.tan( fovInDegrees * 0.5f * (Math.PI/180.0f) ) ) );
        
        Vector3D vec1 = vector(S,0,0,0);
        Vector3D vec2 = vector(0,S,0,0);
        
        final double f = zFarClippingPlane;
        final double n = zNearClippingPlane;
        
        final double f1 = -( f / ( f - n ) );
        final double f2 = -( (f*n) / ( f - n ) );
        
        Vector3D vec3 = vector(0,0,f1,f2);
        Vector3D vec4 = vector(0,0,-1,0);
        
        return createMatrix( vec1 , vec2 , vec3 , vec4 );
    }
    
    public static DenseMatrix64F translationMatrix(double x , double y , double z ) {
        /*
         *  1 0 0 x
         *  0 1 0 y
         *  0 0 1 z
         *  0 0 0 1
         */    	
        return createMatrix( vector( 1 , 0 , 0 , 0 ) , vector( 0, 1 , 0 , 0 ) , vector( 0 , 0, 1 , 0 ) , vector( x,y,z,1 ) );
    }
    
    public static DenseMatrix64F rotX(double angleInDegrees) 
    {
    	final double angleInRad = (double) ( angleInDegrees * 0.5f * ( Math.PI / 180.0f ) );
    	
    	final double cos = (double) Math.cos( angleInRad );
    	final double sin = (double) Math.sin( angleInRad );
    	
        /*
         *  0   0    0 0
         *  0 cos -sin 0
         *  0 sin  cos 0
         *  0   0    0 0
         */    	
    	DenseMatrix64F result =
    			createMatrix( vector( 1, 0 , 0 , 0 ) , vector( 0, cos , sin , 0 ) , vector( 0 , -sin, cos , 0 ) , vector( 0,0,0,1 ) );
    	
    	return result;
    }    
    
    public static DenseMatrix64F rotY(double angleInDegrees) 
    {
    	final double angleInRad = (double) ( angleInDegrees * 0.5f * ( Math.PI / 180.0f ) );
    	
    	final double cos = (double) Math.cos( angleInRad );
    	final double sin = (double) Math.sin( angleInRad );
    	
        /*
         *  cos 0 sin 0
         *    0 1   0 0
         * -sin 0 cos 0
         *    0 0   0 1
         */    	
    	DenseMatrix64F result =
    			createMatrix( vector( cos, 0 , -sin , 0 ) ,
    					      vector( 0, 1 , 0 , 0 ) , 
    					      vector( sin , 0 , cos , 0 ) , 
    					      vector( 0,0,0,1 ) );
    	return result;
    }  
    
    public static DenseMatrix64F rotZ(double angleInDegrees) 
    {
    	final double angleInRad = (double) ( angleInDegrees * 0.5f * ( Math.PI / 180.0f ) );
    	
    	final double cos = (double) Math.cos( angleInRad );
    	final double sin = (double) Math.sin( angleInRad );
    	
        /*
         *  cos -sin   0 0
         *  sin  cos   0 0
         *    0    0   1 0
         *    0    0   0 1
         */    	
    	DenseMatrix64F result =
    			createMatrix( vector( cos, sin , 0 , 0 ) ,
    					      vector( -sin, cos , 0 , 0 ) , 
    					      vector( 0 , 0 , 1 , 0 ) , 
    					      vector( 0,0,0,1 ) );
    	return result;
    }     
    
    public static DenseMatrix64F transpose(DenseMatrix64F input) {
        return CommonOps.transpose( input , null );
    }
    
    public static DenseMatrix64F scalingMatrix(double x , double y , double z ) {
        /*
         *  x 0 0 0
         *  0 y 0 0
         *  0 0 z 0
         *  0 0 0 1
         */
        return createMatrix( vector( x , 0 , 0 , 0 ) , vector( 0, y , 0 , 0 ) , vector( 0 , 0, z , 0 ) , vector( 0,0,0, 1 ) );
    }    
    
    public static DenseMatrix64F identity() 
    {
        return CommonOps.identity( 4 );
    }    
    
    private static Vector3D vector(double x,double y , double z ,double w) {
        return new Vector3D(x,y,z,w);
    }
    
    private static Vector3D vector(double x,double y , double z ) {
        return new Vector3D(x,y,z);
    }
    
    public List<Quad> createCube(double width, double height , double depth) {
        
    	final Vector3D p = vector( -(width/2.0) , (height/2.0) , depth/2.0 );
    	
        Vector3D p1;
        Vector3D p2;
        Vector3D p3;
        Vector3D p4;
        
        final double x = p.x;
        final double y = p.y;
        final double z = p.z;
        
        // front plane
        p1 = vector( x , y , z  );
        p2 = vector( x , y - height , z );
        p3 = vector( x+width  , y - height , z );
        p4 = vector( x+width , y , z );
        
        Quad front = new Quad( new Triangle( p1 ,p2 , p3 ) , new Triangle( p1 , p3 , p4 ) ); 
        
        // back plane
        p1 = vector( x + width , y , z - depth );
        p2 = vector( x + width , y - height , z - depth  );
        p3 = vector( x  , y - height , z - depth );
        p4 = vector( x , y , z-depth );
        
        Quad back = new Quad( new Triangle( p1 ,p2 , p3 ) , new Triangle( p1 , p3 , p4 ) ); 
        
        // left
        Quad left = new Quad( 
        			vector( x , y , z - depth ) , 
        			vector ( x , y - height , z -depth ) , 
        			vector( x , y - height , z ) 
        	);
        
        // right
        Quad right = new Quad( 
        				vector( x+width , y , z ) , 
        				vector ( x+width , y - height , z ) , 
        				vector( x+width , y - height , z-depth ) 
        		); 
        
        // top
        p1 = vector( x , y , z - depth );
        p2 = vector( x , y , z );
        p3 = vector( x + width , y , z );
        p4 = vector( x + width, y , z-depth );
        
        Quad top = new Quad( new Triangle( p1 ,p2 , p3 ) , new Triangle( p1 , p3 , p4 ) ); 

        // bottom
        p1 = vector( x + width, y-height , z-depth );
        p2 = vector( x + width , y-height , z );
        p3 = vector( x , y-height , z );
        p4 = vector( x , y-height , z - depth );
        
        Quad bottom =  new Quad( new Triangle( p1 ,p2 , p3 ) , new Triangle( p1 , p3 , p4 ) ); 

        final List<Quad> result = new ArrayList<>();
        result.add( front );
        result.add( back );
        result.add( top  );
        result.add( bottom );
        result.add( left );
        result.add( right  );
        return result;
    }
    
    public static DenseMatrix64F mult(RowD1Matrix64F m1 , RowD1Matrix64F m2) {
        final DenseMatrix64F result = new DenseMatrix64F( 4 ,4 );
        MatrixMatrixMult.mult_small( m1 , m2 , result );
        return result;
    }    
    
    public static final class MyCanvas extends JPanel {
        
        private final List<Object3D> objects = new ArrayList<Object3D>();
        
        private static final boolean DRAW_SURFACE_NORMALS = false;

        private static final double PI = Math.PI;
        private static final double PI_HALF = PI / 2.0d;
        
    	private final Vector3D viewVector = vector( 0 , 0 , -100 );
    	
        private int xOffset = 100;
        private int yOffset = 100;  
        
        private final RowD1Matrix64F projectionMatrix;
        
        public MyCanvas(RowD1Matrix64F projectionMatrix) 
        {
            this( new ArrayList<Object3D>() , projectionMatrix );
            setDoubleBuffered( true );
        }
        
        public MyCanvas(List<Object3D> objects , RowD1Matrix64F projectionMatrix) 
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

        	DenseMatrix64F viewMatrix = obj.getViewMatrix();
        	
            for ( Triangle t : obj.getTriangles() )
            {
                Vector3D p1 = t.p1.multiply( viewMatrix );
                Vector3D p2 = t.p2.multiply( viewMatrix );
                Vector3D p3 = t.p3.multiply( viewMatrix );   
                
                // calculate angle between surface normal and view vector
                Vector3D vec1 = p2.minus( p1 );
                Vector3D vec2 = p3.minus( p1 );
                
                Vector3D normal = vec1.crossProduct( vec2 );
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
//                	normal = p2.plus( normal.normalize().multiply( 30 ) );
//                }
                
           		// apply perspective projection
                p1 = p1.multiply( projectionMatrix );
                p2 = p2.multiply( projectionMatrix );
                p3 = p3.multiply( projectionMatrix );
                
//                if (DRAW_SURFACE_NORMALS) {
//                	normal = normal.multiply( projectionMatrix );                
//                	drawLine( p2 , normal , graphics );
//                }
              
               	drawPoly( p1 , p2 , p3 , graphics );
            }
        }
        
        private void drawPoly(Vector3D p1,Vector3D p2,Vector3D p3,Graphics2D graphics) {
        	
        	final int x[] = new int[] { (int) p1.x + xOffset , (int) p2.x + xOffset, (int) p3.x + xOffset};
        	final int y[] = new int[] { (int) p1.y + yOffset, (int) p2.y + yOffset, (int) p3.y + yOffset};
        	
        	graphics.fillPolygon( x , y , 3 );
        }
        
        private void drawLine(Vector3D p1 , Vector3D p2,Graphics2D graphics) 
        {
            graphics.drawLine(xOffset+ (int) p1.x , yOffset+ (int) p1.y , xOffset+(int) p2.x , yOffset+(int) p2.y );
        }
    
    }
    
}

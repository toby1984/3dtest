package de.codesourcery.engine.linalg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.codesourcery.engine.geom.IConvexPolygon;
import de.codesourcery.engine.geom.Quad;
import de.codesourcery.engine.geom.Triangle;
import de.codesourcery.engine.math.Constants;
import de.codesourcery.engine.render.BoundingBoxGenerator;
import de.codesourcery.engine.render.Object3D;

public class LinAlgUtils
{

    public static Matrix createMatrix(Vector4 v1,Vector4 v2,Vector4 v3,Vector4 v4) 
    {
        return new Matrix(v1,v2,v3,v4);
    }

    public static Matrix identity() 
    {
        return Matrix.identity();
    }

    public static Matrix mult(Matrix m1 , Matrix m2) 
    {
        return m1.multiply( m2 );
    }

    public static Matrix rotX(float angleInDegrees) 
    {
        final float angleInRad = (float) ( angleInDegrees * 0.5f * ( Math.PI / 180.0f ) );

        final float cos = (float) Math.cos( angleInRad );
        final float sin = (float) Math.sin( angleInRad );

        /*
         *  0   0    0 0
         *  0 cos -sin 0
         *  0 sin  cos 0
         *  0   0    0 0
         */    	
        Matrix result =
                createMatrix( vector( 1, 0 , 0 , 0 ) , 
                        vector( 0, cos , sin , 0 ) , 
                        vector( 0 , -sin, cos , 0 ) , 
                        vector( 0,0,0,1 ) );

        return result;
    }
    
    /**
     * Transforms vertices from another coordinate system to model coordinates.
     * 
     * <p>Note that this method will update the input vertices.</p>
     * <p>Coordinate system axis are expected to be columns 0 (x axis),1 (y axis),2 (z axis) in the
     * input matrices.</p> 
     * 
     * @param vertices vertices to convert
     * @param srcSystem the orthonormal basis of the source coordinate system 
     * @param dstSystem the orthonormal basis of the target coordinate system
     */
    public static void convertToCoordinateSystem(List<Vector4> vertices,Matrix dstSystem, Vector4 dstCenter) 
    {
        // 
        final Vector4 xAxis2 = dstSystem.getColumn( 0 ).normalize();
        final Vector4 yAxis2 = dstSystem.getColumn( 1 ).normalize();
        final Vector4 zAxis2 = dstSystem.getColumn( 2 ).normalize();

        Matrix result2 = new Matrix();
        result2.set( 0 , 0 , xAxis2.x() );
        result2.set( 1 , 0 , xAxis2.y() );
        result2.set( 2 , 0 , xAxis2.z() );

        result2.set( 0 , 1 , yAxis2.x() );
        result2.set( 1 , 1 , yAxis2.y() );
        result2.set( 2 , 1 , yAxis2.z() );
        
        result2.set( 0 , 2 , -zAxis2.x() );
        result2.set( 1 , 2 , -zAxis2.y() );
        result2.set( 2 , 2 , -zAxis2.z() );
        
        result2.set( 3 , 0 , -1 * xAxis2.dotProduct( dstCenter ) );
        result2.set( 3 , 1 , -1 * yAxis2.dotProduct( dstCenter ) );
        result2.set( 3 , 2 , zAxis2.dotProduct( dstCenter ) );
        result2.set( 3 , 3 , 1 );  
        
        result2 = result2.invert();
        
        for ( Vector4 v : vertices ) {
            result2.multiplyInPlace( v );
        }
    }
    
    public static void main(String[] args)
    {
        Object3D sphere = new Object3D();
        List<? extends IConvexPolygon> quads = createSphere( 1 , 25 , 25 );
        sphere.setPrimitives( quads , false );
        final BoundingBox box = new BoundingBoxGenerator().calculateOrientedBoundingBox( sphere );
        
        System.out.println("CENTER = "+box.getCenter());
        System.out.println("X axis = "+box.getXAxis() );
        System.out.println("Y axis = "+box.getYAxis() );
        System.out.println("Z axis = "+box.getZAxis() );
        
    }
    
    public static List<IConvexPolygon> createPyramid(float height,float width,float length) {
        
        float x1 = -(width/2);
        float x2 = width/2;
        
        float y1 = -(height/2);
        float y2 = height/2;
        
        float z1 = length/2;        
        float z2 = -(length/2);
        
        Vector4 top = new Vector4(0,y2,0);
        
        Vector4 frontLeft = new Vector4(x1,y1,z1);
        Vector4 frontRight = new Vector4(x2,y1,z1);
        
        Vector4 backLeft = new Vector4(x1,y1,z2);
        Vector4 backRight = new Vector4(x2,y1,z2);        
        
        final List<IConvexPolygon> result = new ArrayList<IConvexPolygon>();
        
        result.add( new Triangle( top , frontLeft , frontRight ) );
        
        result.add( new Triangle( top , backLeft , frontLeft) );
        result.add( new Triangle( top , frontRight , backRight) );
        
        result.add( new Triangle( top , backRight , backLeft ) );
        
        result.add( new Triangle( frontLeft , backLeft , backRight ) );
        result.add( new Triangle( backRight , frontRight , frontLeft ) );
        
        return result;
    }

    public static Matrix rotY(float angleInDegrees) 
    {
        final float angleInRad = (float) ( angleInDegrees * 0.5f * ( Math.PI / 180.0f ) );

        final float cos = (float) Math.cos( angleInRad );
        final float sin = (float) Math.sin( angleInRad );

        /*
         *  cos 0 sin 0
         *    0 1   0 0
         * -sin 0 cos 0
         *    0 0   0 1
         */    	
        Matrix result =
                createMatrix( vector( cos, 0 , -sin , 0 ) ,
                        vector( 0, 1 , 0 , 0 ) , 
                        vector( sin , 0 , cos , 0 ) , 
                        vector( 0,0,0,1 ) );
        return result;
    }

    public static Matrix rotZ(float angleInDegrees) 
    {
        final float angleInRad =  (float)( angleInDegrees * 0.5f * ( Math.PI / 180.0f ) );

        final float cos = (float) Math.cos( angleInRad );
        final float sin = (float) Math.sin( angleInRad );

        /*
         *  cos -sin   0 0
         *  sin  cos   0 0
         *    0    0   1 0
         *    0    0   0 1
         */    	
        Matrix result =
                createMatrix( vector( cos, sin , 0 , 0 ) ,
                        vector( -sin, cos , 0 , 0 ) , 
                        vector( 0 , 0 , 1 , 0 ) , 
                        vector( 0,0,0,1 ) );
        return result;
    }

    public static Matrix scalingMatrix(float x , float y , float z ) {
        /*
         *  x 0 0 0
         *  0 y 0 0
         *  0 0 z 0
         *  0 0 0 1
         */
        return createMatrix( vector( x , 0 , 0 , 0 ) , vector( 0, y , 0 , 0 ) , vector( 0 , 0, z , 0 ) , vector( 0,0,0, 1 ) );
    }

    public static Matrix translationMatrix(float x , float y , float z ) {
        /*
         *  1 0 0 x
         *  0 1 0 y
         *  0 0 1 z
         *  0 0 0 1
         */    	
        return createMatrix( vector( 1 , 0 , 0 , 0 ) , vector( 0, 1 , 0 , 0 ) , vector( 0 , 0, 1 , 0 ) , vector( x,y,z,1 ) );
    }

    public static Vector4 vector(float x,float y , float z ) {
        return new Vector4(x,y,z);
    }

    public static Vector4 vector(float x,float y , float z ,float w) {
        return new Vector4(x,y,z,w);
    }
    
    public static List<? extends IConvexPolygon> createSphere(float diameter,int strips,int tiles) {
    	
    	final float yInc = Constants.PI_HALF / strips;
    	
    	final float radius = diameter / 2.0f;
    	
        final List<Quad> result = new ArrayList<>();    	

    	for ( float currentAngle = Constants.PI_HALF ; currentAngle > 0 ; currentAngle -= yInc ) 
    	{
    	    final float angle1 = currentAngle;
    	    final float angle2 = angle1 - yInc;
    	    
			final float diameter1 = (float) Math.cos( angle1 )*diameter;
			final float[] tiles1 = createCircle( diameter1 , tiles );
			
			final float diameter2 = (float) Math.cos( angle2 )*diameter;
			final float[] tiles2 = createCircle( diameter2 , tiles );
			
			for ( int i=0 ; i < ( tiles1.length - 2 ) ; i+= 2) 
			{
				float x1 = tiles1[i];
				float y1 = radius * (float) Math.sin( angle1 );
				float z1 = tiles1[i+1];
				
				final Vector4 p1 = new Vector4(x1,y1,z1);
				
				float x2 = tiles2[i];
				float y2 = radius * (float) Math.sin( angle2 );
				float z2 = tiles2[i+1];
				
				final Vector4 p2 = new Vector4(x2,y2,z2);
				
				float x3 = tiles1[i+2];
				float y3 = radius * (float) Math.sin( angle1 );
				float z3 = tiles1[i+3];
				
				final Vector4 p3 = new Vector4(x3,y3,z3);
				
				float x4 = tiles2[i+2];
				float y4 = radius * (float) Math.sin( angle2 );
				float z4 = tiles2[i+3];	
				
				final Vector4 p4 = new Vector4(x4,y4,z4);
				
				result.add( new Quad( p1,p2,p4,p3) );
//				result.add( new Triangle( p2,p4,p3) ); 
//              result.add( new Triangle( p1,p2,p3) ); 
			}
    	}
    	
    	// mirror sphere along the the X/Z plane
    	// and reverse vertex order so the surface normal
    	// still points to the outside of the sphere
    	Matrix m = scalingMatrix( 1 , -1 , 1 );
    	final List<Quad> bottomHalf = transform( result , m );
    	
    	for ( Quad q : bottomHalf ) 
    	{
    		q.reverseVertices();
    	    result.add( q );
    	}
        return result;    	
    }
    
    public static List<Quad> transform(List<Quad> triangles,Matrix m) 
    {
        List<Quad> result = new ArrayList<>();
        for ( Quad t : triangles ) 
        {
        	Vector4[] transformed = m.multiply( t.getAllPoints() );
            result.add( new Quad( transformed ) );
        }
        return result;
    }
    
    public static List<IConvexPolygon> transformPolygons(List<? extends IConvexPolygon> triangles,Matrix m) 
    {
        final List<IConvexPolygon> result = new ArrayList<>();
        for ( IConvexPolygon t : triangles ) 
        {
            Vector4[] transformed = m.multiply( t.getAllPoints() );
            switch( t.getVertexCount() ) {
                case 3:
                    result.add( new Triangle( transformed ) );
                    break;
                case 4:
                    result.add( new Quad( transformed ) );
                    break;
                    default:
                        throw new RuntimeException("Unsupported vertex count: "+t.getVertexCount());
            }
        }
        return result;
    }    

    private static float[] createCircle(float diameter , int segments) {
        
        final float inc = (2*Constants.PI) / segments;
        final float radius = diameter / 2.0f;
        final float[] result = new float[ (segments+1) * 2 ];
        
        int i = 0;
        for ( float angle = 2*Constants.PI ; angle >=0  ; angle -= inc ) 
        {
            result[i++] = radius * (float) Math.cos( angle ); // x
            result[i++] = radius * (float) Math.sin( angle ); // z
        }
        return result;
    }    

    public static List<Quad> createCube(float width, float height , float depth) {

        final Vector4 p = vector( -(width/2.0f) , (height/2.0f) , depth/2.0f );

        Vector4 p1;
        Vector4 p2;
        Vector4 p3;
        Vector4 p4;

        final float x = p.x();
        final float y = p.y();
        final float z = p.z();

        // front plane
        p1 = vector( x , y , z  );
        p2 = vector( x , y - height , z );
        p3 = vector( x+width  , y - height , z );
        p4 = vector( x+width , y , z );

        Quad front = new Quad( p1 ,p2 , p3 , p4 ); 

        // back plane
        p1 = vector( x + width , y , z - depth );
        p2 = vector( x + width , y - height , z - depth  );
        p3 = vector( x  , y - height , z - depth );
        p4 = vector( x , y , z-depth );

        Quad back = new Quad( p1 ,p2 , p3 , p4 ); 

        // left
        p1 = vector( x , y , z - depth );
        p2 = vector ( x , y - height , z -depth );
        p3 = vector( x , y - height , z );
        p4 = vector( x , y , z );
        // p4 = vector( p3.x() , p1.y() , p3.z() );
        
        Quad left = new Quad( p1,p2,p3,p4 );

        // right
        p1 = vector( x+width , y , z );
        p2 = vector ( x+width , y - height , z );
        p3 = vector( x+width , y - height , z-depth );
        p4 = vector( x+width , y , z-depth );
//        p4 = vector( p3.x() , p1.y() , p3.z() );
        
        Quad right = new Quad( p1 , p2 , p3 , p4 ); 

        // top
        p1 = vector( x , y , z - depth );
        p2 = vector( x , y , z );
        p3 = vector( x + width , y , z );
        p4 = vector( x + width, y , z-depth );

        Quad top = new Quad( p1 ,p2 , p3 , p4 ); 

        // bottom
        p1 = vector( x + width, y-height , z-depth );
        p2 = vector( x + width , y-height , z );
        p3 = vector( x , y-height , z );
        p4 = vector( x , y-height , z - depth );

        Quad bottom =  new Quad( p1 ,p2 , p3 , p4 ); 

        final List<Quad> result = new ArrayList<>();
        result.add( front );
        result.add( back );
        result.add( top  );
        result.add( bottom );
        result.add( left );
        result.add( right  );

        return result;
    }

    public static List<? extends IConvexPolygon> createXZMesh(float width,float depth , float stripsX,float stripsY) 
    {
    	final float incX = width / stripsX;
    	final float incZ = depth /stripsY;
    	
    	final float zEnd = -(depth/2);
    	final float xEnd = width/2;
    	
    	List<Quad> result = new ArrayList<>();
    	
    	for ( float z = depth / 2 ; z >= (zEnd - incZ ) ; z-= incZ ) 
    	{
        	for ( float x = -(width/2) ; x <= (xEnd - incX ) ; x+= incX ) 
        	{
        		final float x1=x;
        		final float z1=z;
        		
        		final float x2=x+incX;
        		final float z2=z;
        		        		
        		final float x3=x+incX;
        		final float z3=z-incZ;        		
        		
        		final float x4=x;
        		final float z4=z-incZ;
        		
        		result.add( new Quad( vector( x1 , 0 , z1 ) , 
        				              vector( x2 , 0 , z2 ) , 
        				              vector( x3 , 0 , z3 ) ,
        				              vector( x4 , 0 , z4 ) ) );
        	}
    	}
    	return result;
    }

    public static Matrix makeFrustum(float left, float right, float bottom, float top, float near,float far) 
    {
        final float[] data = new float[16];

        data[0] = 2.0f * near / (right - left);  
        data[1] = 0.0f; 
        data[2] = 0.0f; 
        data[3] = 0.0f;

        data[4] = 0.0f; 
        data[5] = 2.0f * near / (top - bottom); 
        data[6] = 0.0f; 
        data[7] =  0.0f;

        data[8] = (right + left) / (right - left);
        data[9] = (top + bottom) / (top - bottom); 
        data[10] = -( (far + near) / (far - near) ); 
        data[11] = -1.0f;

        data[12] = 0.0f; 
        data[13] = 0.0f; 
        data[14] = -(( 2.0f * far * near) / (far - near) ); 
        data[15] = 0.0f;

        return new Matrix(data);
    }    
    
    public static Matrix createOrthoProjection(float field_of_view , float aspect_ratio , float near,float far) 
    {
        final float rad = field_of_view * Constants.DEG_TO_RAD;
        float size = near * (float) Math.tan( rad / 2.0f); 

        float left = -size; // left X
        float right = size;  // right X
        float bottom = -size / aspect_ratio; // bottom Y
        float top = size / aspect_ratio; // top Y

        
        Matrix result = new Matrix(
                vector(2.0f / (right - left), 0, 0, 0 ) ,
                vector(0, 2.0f / (top - bottom), 0, 0),
                vector(0, 0, -2.0f / (far - near), 0),
                vector(-(right + left) / (right - left), -(top + bottom) / (top - bottom), -(far + near) / (far - near), 1 ) );
        
        return result;
    }
    
    public static Matrix createPerspectiveProjection(float field_of_view, float aspect_ratio ,float zNear, float zFar) 
    {
        final float rad = field_of_view * Constants.DEG_TO_RAD;

        float size = zNear * (float) Math.tan( rad / 2.0f); 

        float xLeft = -size;
		float xRight = size;
		float yBottom = -size / aspect_ratio;
		float yTop = size / aspect_ratio;

		return makeFrustum(xLeft, xRight, yBottom,yTop, zNear, zFar);
    }
    
    public static Vector4 findFarestVertex(Vector4 referencePoint,Vector4 p1,Vector4 p2,Vector4 p3) 
    {
        float dist1 = p1.minus( referencePoint ).length();
        float dist2 = p2.minus( referencePoint ).length();
        float dist3 = p3.minus( referencePoint ).length();
        
        Vector4 result = p1;
        float dist = dist1;
        
        if ( dist2 > dist ) {
            result = p2;
            dist = dist2;
        }
        if ( dist3 > dist ) {
            return p3;
        }
        return result;
    }   
    
    public static float findFarestDistance(Vector4 referencePoint,Vector4 p1,Vector4 p2,Vector4 p3) 
    {
        float dist1 = p1.minus( referencePoint ).length();
        float dist2 = p2.minus( referencePoint ).length();
        float dist3 = p3.minus( referencePoint ).length();
        
        float dist = dist1;
        
        if ( dist2 > dist ) {
            dist = dist2;
        }
        if ( dist3 > dist ) {
            return dist3;
        }
        return dist;
    }     
}

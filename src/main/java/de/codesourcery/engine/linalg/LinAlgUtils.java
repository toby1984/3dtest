package de.codesourcery.engine.linalg;

import java.util.ArrayList;
import java.util.List;

import de.codesourcery.engine.geom.IConvexPolygon;
import de.codesourcery.engine.geom.Quad;
import de.codesourcery.engine.geom.Triangle;

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

    public static Matrix rotX(double angleInDegrees) 
    {
        final double angleInRad = ( angleInDegrees * 0.5f * ( Math.PI / 180.0f ) );

        final double cos = Math.cos( angleInRad );
        final double sin = Math.sin( angleInRad );

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
    
    public static List<IConvexPolygon> createPyramid(double height,double width,double length) {
        
        double x1 = -(width/2.0);
        double x2 = width/2.0;
        
        double y1 = -(height/2.0);
        double y2 = height/2.0;
        
        double z1 = length/2.0;        
        double z2 = -(length/2.0);
        
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

    public static Matrix rotY(double angleInDegrees) 
    {
        final double angleInRad = ( angleInDegrees * 0.5f * ( Math.PI / 180.0f ) );

        final double cos = Math.cos( angleInRad );
        final double sin = Math.sin( angleInRad );

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

    public static Matrix rotZ(double angleInDegrees) 
    {
        final double angleInRad = ( angleInDegrees * 0.5f * ( Math.PI / 180.0f ) );

        final double cos = Math.cos( angleInRad );
        final double sin = Math.sin( angleInRad );

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

    public static Matrix scalingMatrix(double x , double y , double z ) {
        /*
         *  x 0 0 0
         *  0 y 0 0
         *  0 0 z 0
         *  0 0 0 1
         */
        return createMatrix( vector( x , 0 , 0 , 0 ) , vector( 0, y , 0 , 0 ) , vector( 0 , 0, z , 0 ) , vector( 0,0,0, 1 ) );
    }

    public static Matrix translationMatrix(double x , double y , double z ) {
        /*
         *  1 0 0 x
         *  0 1 0 y
         *  0 0 1 z
         *  0 0 0 1
         */    	
        return createMatrix( vector( 1 , 0 , 0 , 0 ) , vector( 0, 1 , 0 , 0 ) , vector( 0 , 0, 1 , 0 ) , vector( x,y,z,1 ) );
    }

    public static Vector4 vector(double x,double y , double z ) {
        return new Vector4(x,y,z);
    }

    public static Vector4 vector(double x,double y , double z ,double w) {
        return new Vector4(x,y,z,w);
    }
    
    public static List<IConvexPolygon> createSphere(double diameter,int strips,int tiles) {
    	
    	final double yInc = Math.PI / 2 / strips;
    	
    	final double radius = diameter / 2.0d;
    	
        final List<IConvexPolygon> result = new ArrayList<>();    	

    	for ( double currentAngle = Math.PI / 2 ; currentAngle > 0 ; currentAngle -= yInc ) 
    	{
    	    final double angle1 = currentAngle;
    	    final double angle2 = angle1 - yInc;
    	    
			final double diameter1 = Math.cos( angle1 )*diameter;
			final double[] tiles1 = createCircle( diameter1 , tiles );
			
			final double diameter2 = Math.cos( angle2 )*diameter;
			final double[] tiles2 = createCircle( diameter2 , tiles );
			
			for ( int i=0 ; i < ( tiles1.length - 2 ) ; i+= 2) 
			{
				double x1 = tiles1[i];
				double y1 = radius * Math.sin( angle1 );
				double z1 = tiles1[i+1];
				
				final Vector4 p1 = new Vector4(x1,y1,z1);
				
				double x2 = tiles2[i];
				double y2 = radius * Math.sin( angle2 );
				double z2 = tiles2[i+1];
				
				final Vector4 p2 = new Vector4(x2,y2,z2);
				
				double x3 = tiles1[i+2];
				double y3 = radius * Math.sin( angle1 );
				double z3 = tiles1[i+3];
				
				final Vector4 p3 = new Vector4(x3,y3,z3);
				
				double x4 = tiles2[i+2];
				double y4 = radius * Math.sin( angle2 );
				double z4 = tiles2[i+3];	
				
				final Vector4 p4 = new Vector4(x4,y4,z4);
				
				result.add( new Triangle( p2,p4,p3) ); 
                result.add( new Triangle( p1,p2,p3) ); 
			}
    	}
    	
    	// mirror sphere along the the X/Z plane
    	// and swap first and third vertex so the surface normal
    	// still points to the outside of the sphere
    	Matrix m = scalingMatrix( 1 , -1 , 1 );
    	for ( IConvexPolygon t : transform( result , m ) ) {
    	    result.add( new Triangle( t.p3() , t.p2() , t.p1() ) );
    	}
        return result;    	
    }
    
    public static List<IConvexPolygon> transform(List<IConvexPolygon> triangles,Matrix m) 
    {
        List<IConvexPolygon> result = new ArrayList<>();
        for ( IConvexPolygon t : triangles ) 
        {
            Vector4 vec1 = t.p1();
            Vector4 vec2 = t.p2();
            Vector4 vec3 = t.p3();
            vec1 = m.multiply( vec1 );
            vec2 = m.multiply( vec2 );
            vec3 = m.multiply( vec3 );
            result.add( new Triangle( vec1,vec2,vec3 ) );
        }
        return result;
    }

    private static double[] createCircle(double diameter , int segments) {
        
        final double inc = (2*Math.PI) / segments;
        final double radius = diameter / 2.0;
        final double[] result = new double[ (segments+1) * 2 ];
        
        int i = 0;
        for ( double angle = 2*Math.PI ; angle >=0  ; angle -= inc ) 
        {
            result[i++] = radius * Math.cos( angle ); // x
            result[i++] = radius * Math.sin( angle ); // z
        }
        return result;
    }    

    public static List<? extends IConvexPolygon> createCube(double width, double height , double depth) {

        final Vector4 p = vector( -(width/2.0) , (height/2.0) , depth/2.0 );

        Vector4 p1;
        Vector4 p2;
        Vector4 p3;
        Vector4 p4;

        final double x = p.x();
        final double y = p.y();
        final double z = p.z();

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

    public static List<Triangle> createXZMesh(double width,double depth , double stripsX,double stripsY) 
    {
    	final double incX = width / stripsX;
    	final double incZ = depth /stripsY;
    	
    	final double zEnd = -(depth/2);
    	final double xEnd = width/2;
    	
    	List<Triangle> result = new ArrayList<>();
    	
    	for ( double z = depth / 2 ; z >= (zEnd - incZ ) ; z-= incZ ) 
    	{
        	for ( double x = -(width/2) ; x <= (xEnd - incX ) ; x+= incX ) 
        	{
        		final double x1=x;
        		final double z1=z;
        		
        		final double x2=x+incX;
        		final double z2=z;
        		        		
        		final double x3=x+incX;
        		final double z3=z-incZ;        		
        		
        		final double x4=x;
        		final double z4=z-incZ;
        		
        		result.add( new Triangle( vector( x1 , 0 , z1 ) , vector( x2 , 0 , z2 ) , vector( x3 , 0 , z3 ) ) );
        		result.add( new Triangle( vector( x3 , 0 , z3 ) , vector( x4 , 0 , z4 ) , vector( x1 , 0 , z1 ) ) );
        	}
    	}
    	return result;
    }

    public static Matrix makeFrustum(double left, double right, double bottom, double top, double near,double far) 
    {
        final double[] data = new double[16];

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
        data[10] = (-1*(far + near)) / (far - near) ; 
        data[11] = -1.0f;

        data[12] = 0.0f; 
        data[13] = 0.0f; 
        data[14] = (-2.0f * far * near) / (far - near); 
        data[15] = 0.0f;

        return new Matrix(data);
    }    
    
    public static Matrix createOrthoProjection(double field_of_view , double aspect_ratio , double near,double far) 
    {
        final double rad = field_of_view * 0.5 * (Math.PI/180.0d);
        double size = near * Math.tan( rad / 2.0f); 

        double left = -size; // left X
        double right = size;  // right X
        double bottom = -size / aspect_ratio; // bottom Y
        double top = size / aspect_ratio; // top Y

        
        Matrix result = new Matrix(
                vector(2.0 / (right - left), 0, 0, 0 ) ,
                vector(0, 2.0 / (top - bottom), 0, 0),
                vector(0, 0, -2.0 / (far - near), 0),
                vector(-(right + left) / (right - left), -(top + bottom) / (top - bottom), -(far + near) / (far - near), 1 ) );
        
        return result;
    }
    
    public static Matrix createPerspectiveProjection(double field_of_view, double aspect_ratio ,double zNear, double zFar) 
    {
        final double rad = field_of_view * 0.5 * (Math.PI/180.0d);

        double size = zNear * Math.tan( rad / 2.0f); 

        double xLeft = -size;
		double xRight = size;
		double yBottom = -size / aspect_ratio;
		double yTop = size / aspect_ratio;

		return makeFrustum(xLeft, xRight, yBottom,yTop, zNear, zFar);
    }
    
    public static Vector4 findFarestVertex(Vector4 referencePoint,Vector4 p1,Vector4 p2,Vector4 p3) 
    {
        double dist1 = p1.minus( referencePoint ).length();
        double dist2 = p2.minus( referencePoint ).length();
        double dist3 = p3.minus( referencePoint ).length();
        
        Vector4 result = p1;
        double dist = dist1;
        
        if ( dist2 > dist ) {
            result = p2;
            dist = dist2;
        }
        if ( dist3 > dist ) {
            return p3;
        }
        return result;
    }   
    
    public static double findFarestDistance(Vector4 referencePoint,Vector4 p1,Vector4 p2,Vector4 p3) 
    {
        double dist1 = p1.minus( referencePoint ).length();
        double dist2 = p2.minus( referencePoint ).length();
        double dist3 = p3.minus( referencePoint ).length();
        
        double dist = dist1;
        
        if ( dist2 > dist ) {
            dist = dist2;
        }
        if ( dist3 > dist ) {
            return dist3;
        }
        return dist;
    }     
}

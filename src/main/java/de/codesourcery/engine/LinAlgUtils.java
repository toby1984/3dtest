package de.codesourcery.engine;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import de.codesourcery.engine.geom.ITriangle;
import de.codesourcery.engine.geom.Quad;
import de.codesourcery.engine.geom.Triangle;
import de.codesourcery.engine.geom.Vector4;
import de.codesourcery.engine.linalg.Matrix;

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
    
    public static List<ITriangle> createPyramid(double height,double width,double length) {
        
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
        
        final List<ITriangle> result = new ArrayList<ITriangle>();
        
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
    
    /*
6 down vote accepted
    

If there are M lines of latitude (horizontal) and N lines of longitude (vertical), then put dots at

(x, y, z) = (sin(Pi * m/M) cos(2Pi * n/N), sin(Pi * m/M) sin(2Pi * n/N), cos(Pi * m/M))

for each m in { 0, ..., M } and n in { 0, ..., N-1 } and draw the line segments between the dots, accordingly.

edit: maybe adjust M by 1 or 2 as required, because you should decide whether or not to count "latitude lines" at the poles
     
     */
    public static List<ITriangle> createSphere2(double diameter,int strips,int tiles) {

        final double M = strips;
        final double N = tiles;
        final double Pi = Math.PI;
        final double radius = diameter / 2;
        
        final List<ITriangle> triangles = new ArrayList<>();
        for ( int m = 0 ; m < M ; m++ ) 
        {        
            final int m1 = m;
            final int m2 = m+1;
            
            for ( int n = 0 ; n < (tiles-1) ; n++ ) 
            {
                final int n1 = n;
                final int n2 = n+1;
                
                final Vector4 p1 = new Vector4(  radius*Math.sin(Pi * m1/M) * Math.cos(2*Pi * n1/N), 
                                                  radius*Math.sin(Pi * m1/M) * Math.sin(2*Pi * n1/N), 
                                                  radius*Math.cos(Pi * m1/M) );
                
                System.out.println("p1 = "+p1);
                final Vector4 p2 = new Vector4(  radius*Math.sin(Pi * m2/M) * Math.cos(2*Pi * n1/N), 
                                                  radius*Math.sin(Pi * m2/M) * Math.sin(2*Pi * n1/N), 
                                                  radius*Math.cos(Pi * m2/M) );                
                
                System.out.println("p2 = "+p2);
                
                final Vector4 p3 = new Vector4(  radius*Math.sin(Pi * m1/M) * Math.cos(2*Pi * n2/N), 
                                                  radius*Math.sin(Pi * m1/M) * Math.sin(2*Pi * n2/N), 
                                                  radius*Math.cos(Pi * m1/M) );
                
                System.out.println("p3 = "+p3);
                
                final Vector4 p4 = new Vector4(  radius*Math.sin(Pi * m2/M) * Math.cos(2*Pi * n2/N), 
                                                  radius*Math.sin(Pi * m2/M) * Math.sin(2*Pi * n2/N), 
                                                  radius*Math.cos(Pi * m2/M) );
                
                System.out.println("p4 = "+p4);                
                
                triangles.add( new Triangle(p2,p1,p3) );
//                triangles.add( new Triangle(p1,p3,p4 ) );
            }
        }
        return triangles;
    }
    
    public static List<ITriangle> createSphere(double diameter,int strips,int tiles) {
    	
    	final double yInc = Math.PI / 2 / strips;
    	
    	final double radius = diameter / 2.0d;
    	
        final List<ITriangle> triangles = new ArrayList<>();    	

    	for ( double currentAngle = Math.PI / 2 ; currentAngle > 0 ; currentAngle -= yInc ) 
    	{
    	    final double angle1 = currentAngle;
    	    final double angle2 = angle1 - yInc;
    	    
			final double diameter1 = Math.cos( angle1 )*diameter;
			final double[] tiles1 = createCircle( diameter1 , tiles );
			
			final double diameter2 = Math.cos( angle2 )*diameter;
			final double[] tiles2 = createCircle( diameter2 , tiles );
			
//			System.out.println("Diameter #1: "+diameter1+" / diameter 2 : "+diameter2 );
			
			for ( int i=0 ; i < ( tiles1.length - 2 ) ; i+= 2) 
			{
				double x1 = tiles1[i];
				double y1 = radius * Math.cos( angle1 );
				double z1 = tiles1[i+1];
				
				double x2 = tiles2[i];
				double y2 = radius * Math.cos( angle2 );
				double z2 = tiles2[i+1];
				
				double x3 = tiles1[i+2];
				double y3 = radius * Math.cos( angle1 );
				double z3 = tiles1[i+3];
				
				double x4 = tiles2[i+2];
				double y4 = radius * Math.cos( angle2 );
				double z4 = tiles2[i+3];	
				
				System.out.println("X1 : "+x1+" / X2 : "+x2 );
				
				triangles.add( new Triangle( vector( x3 , y3, z3 ) ,
						                     vector( x2 , y2 , z2 ) ,
						                     vector( x1 , y1 , z1) 
						        ));
				
				triangles.add( new Triangle( vector( x2 , y2 , z2 ) ,
						                     vector( x4 , y4 , z4 ) ,
						                     vector( x1 , y1 , z1 ) 
	                     ) );				
			}
    	}
        return triangles;    	
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

    public static List<ITriangle> createCube(double width, double height , double depth) {

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

        List<ITriangle> triangles = new ArrayList<>();
        for ( Quad q : result ) {
            triangles.add( q.t1 );
            triangles.add( q.t2 );
        }
        return triangles;
    }

    public static Matrix createPerspectiveProjectionMatrix2(double fovInDegrees , double nearPlane , double farPlane) {

        final double S = ( 1.0d / ( Math.tan( fovInDegrees * 0.5f * (Math.PI/180.0f) ) ) );

        Vector4 vec1 = vector(S,0,0,0);
        Vector4 vec2 = vector(0,S,0,0);

        final double f1 = -( farPlane / ( farPlane - nearPlane ) );
        final double f2 = -( (farPlane*nearPlane) / ( farPlane - nearPlane ) );

        Vector4 vec3 = vector(0,0,f1,f2);
        Vector4 vec4 = vector(0,0,-1,0);

        return createMatrix( vec1 , vec2 , vec3 , vec4 );
    }    

    public static Matrix createPerspectiveProjectionMatrix4(double field_of_view, double aspect_ratio ,double near, double far) 
    {
        final double rad = field_of_view * 0.5 * (Math.PI/180.0d);

        double size = near * Math.tan( rad / 2.0f); 

        return makeFrustum(-size, size, -size / aspect_ratio,size / aspect_ratio, near, far);
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

    private static Matrix makeFrustum(double left, double right, double bottom, double top, double near,double far) 
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
        data[10] = - (far + near) / (far - near); 
        data[11] = -1.0f;

        data[12] = 0.0f; 
        data[13] = 0.0f; 
        data[14] = -2.0f * far * near / (far - near); 
        data[15] = 0.0f;

        return new Matrix(data);
    }

    public static Matrix createPerspectiveProjectionMatrix1(double fov, double aspect, double znear, double zfar)
    {
        final double[] m = new double[16];

        final double angleInRad = 0.5d*(Math.PI/180.0d);

        final double xymax = znear * Math.tan(fov * angleInRad );
        final double ymin = -xymax;
        final double xmin = -xymax;

        final double width = xymax - xmin;
        final double height = xymax - ymin;

        final double depth = zfar - znear;
        final double q = -(zfar + znear) / depth;
        final double qn = -2.0d * (zfar * znear) / depth;

        double w = 2.0d * znear / width;
        w = w / aspect;
        final double h = 2.0d * znear / height;

        m[0]  = w;
        m[1]  = 0;
        m[2]  = 0;
        m[3]  = 0;

        m[4]  = 0;
        m[5]  = h;
        m[6]  = 0;
        m[7]  = 0;

        m[8]  = 0;
        m[9]  = 0;
        m[10] = q;
        m[11] = -1; 

        m[12] = 0;
        m[13] = 0;
        m[14] = qn;
        m[15] = 0;
        return new Matrix( m );
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

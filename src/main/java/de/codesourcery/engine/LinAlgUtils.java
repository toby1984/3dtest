package de.codesourcery.engine;

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
        return m1.mult( m2 );
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

    public static Matrix createPerspectiveProjectionMatrix(double fovInDegrees , double zNearClippingPlane , double zFarClippingPlane) {
        
        final double S = ( 1.0d / ( Math.tan( fovInDegrees * 0.5f * (Math.PI/180.0f) ) ) );
        
        Vector4 vec1 = vector(S,0,0,0);
        Vector4 vec2 = vector(0,S,0,0);
        
        final double f = zFarClippingPlane;
        final double n = zNearClippingPlane;
        
        final double f1 = -( f / ( f - n ) );
        final double f2 = -( (f*n) / ( f - n ) );
        
        Vector4 vec3 = vector(0,0,f1,f2);
        Vector4 vec4 = vector(0,0,-1,0);
        
        return createMatrix( vec1 , vec2 , vec3 , vec4 );
    }

}

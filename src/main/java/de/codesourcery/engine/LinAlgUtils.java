package de.codesourcery.engine;

import java.util.ArrayList;
import java.util.List;

import org.ejml.alg.dense.mult.MatrixMatrixMult;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.RowD1Matrix64F;
import org.ejml.ops.CommonOps;

import de.codesourcery.engine.geom.ITriangle;
import de.codesourcery.engine.geom.Quad;
import de.codesourcery.engine.geom.Triangle;
import de.codesourcery.engine.geom.Vector4;

public class LinAlgUtils
{

    public static DenseMatrix64F createMatrix(Vector4 v1,Vector4 v2,Vector4 v3,Vector4 v4) 
    {
        DenseMatrix64F result = new DenseMatrix64F(4,4);
        
        result.set( 0 , 0 , v1.x() );
        result.set( 1 , 0 , v1.y() );
        result.set( 2 , 0 , v1.z() );
        result.set( 3 , 0 , v1.w() );
        
        result.set( 0 , 1 , v2.x() );
        result.set( 1 , 1 , v2.y() );
        result.set( 2 , 1 , v2.z() );
        result.set( 3 , 1 , v2.w() );   
        
        result.set( 0 , 2 , v3.x() );
        result.set( 1 , 2 , v3.y() );
        result.set( 2 , 2 , v3.z() );
        result.set( 3 , 2 , v3.w() );          
        
        result.set( 0 , 3 , v4.x() );
        result.set( 1 , 3 , v4.y() );
        result.set( 2 , 3 , v4.z() );
        result.set( 3 , 3 , v4.w() );        
        return result;
    }

    public static DenseMatrix64F identity() 
    {
        return CommonOps.identity( 4 );
    }

    public static DenseMatrix64F mult(RowD1Matrix64F m1 , RowD1Matrix64F m2) {
        final DenseMatrix64F result = new DenseMatrix64F( 4 ,4 );
        MatrixMatrixMult.mult_small( m1 , m2 , result );
        return result;
    }

    public static DenseMatrix64F rotX(double angleInDegrees) 
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
    	DenseMatrix64F result =
    			createMatrix( vector( 1, 0 , 0 , 0 ) , 
    			              vector( 0, cos , sin , 0 ) , 
    			              vector( 0 , -sin, cos , 0 ) , 
    			              vector( 0,0,0,1 ) );
    	
    	return result;
    }

    public static DenseMatrix64F rotY(double angleInDegrees) 
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
    	DenseMatrix64F result =
    			createMatrix( vector( cos, 0 , -sin , 0 ) ,
    					      vector( 0, 1 , 0 , 0 ) , 
    					      vector( sin , 0 , cos , 0 ) , 
    					      vector( 0,0,0,1 ) );
    	return result;
    }

    public static DenseMatrix64F rotZ(double angleInDegrees) 
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
    	DenseMatrix64F result =
    			createMatrix( vector( cos, sin , 0 , 0 ) ,
    					      vector( -sin, cos , 0 , 0 ) , 
    					      vector( 0 , 0 , 1 , 0 ) , 
    					      vector( 0,0,0,1 ) );
    	return result;
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

    public static DenseMatrix64F translationMatrix(double x , double y , double z ) {
        /*
         *  1 0 0 x
         *  0 1 0 y
         *  0 0 1 z
         *  0 0 0 1
         */    	
        return createMatrix( vector( 1 , 0 , 0 , 0 ) , vector( 0, 1 , 0 , 0 ) , vector( 0 , 0, 1 , 0 ) , vector( x,y,z,1 ) );
    }

    public static DenseMatrix64F transpose(DenseMatrix64F input) {
        return CommonOps.transpose( input , null );
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

    public static DenseMatrix64F createPerspectiveProjectionMatrix(double fovInDegrees , double zNearClippingPlane , double zFarClippingPlane) {
        
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

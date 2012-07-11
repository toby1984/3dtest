package de.codesourcery.engine.geom;

import org.ejml.alg.dense.mult.MatrixVectorMult;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.RowD1Matrix64F;

public final class Vector4 
{
    private final double x;
    private final double y;
    private final double z;
    private final double w;
    
    public Vector4(double[] data) 
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
    
    public boolean isEquals(Vector4 other) {
        return this.x() == other.x() &&
                this.y() == other.y() &&
                this.z() == other.z() &&
                this.w() == other.w();
    }
    
    public double x() {
        return x;
    }
    
    public double y() {
        return y;
    }
    
    public double z() {
        return z;
    }
    
    public double w() {
        return w;
    }
    
    public Vector4 minus(Vector4 other) {
        return new Vector4( this.x - other.x , this.y - other.y , this.z - other.z , w );
    }
    
    public Vector4 plus(Vector4 other) {
        return new Vector4( this.x + other.x , this.y + other.y , this.z + other.z , w );
    }        
    
    public Vector4(double x,double y,double z) {
        this(x,y,z,1);
    }
    
    public Vector4(double x,double y,double z,double w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    
    public Vector4(DenseMatrix64F result)
    {
        if ( result.numRows != 4 ) {
            throw new IllegalArgumentException("Invalid row count "+result.numRows+" for "+result);
        }
        if ( result.numCols != 1 ) {
            throw new IllegalArgumentException("Invalid column count "+result.numCols+" for "+result);
        }            
        this.x = result.get( 0 , 0 );
        this.y = result.get( 1 , 0 );
        this.z = result.get( 2 , 0 );
        this.w = result.get( 3 , 0 );
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

    public Vector4 multiply( RowD1Matrix64F matrix ) {
        
        final DenseMatrix64F result = new DenseMatrix64F(4 , 1 );
        MatrixVectorMult.mult( matrix , toRowMatrix()  , result );
        Vector4 resultVec = new Vector4( result );
        return resultVec;
    }
    
    public double length() {
        return Math.sqrt( x*x + y*y + z*z );   
    }
    
    public Vector4 multiply(double value) {
        return new Vector4( x*value , y*value , z*value , w );
    }
    
    public Vector4 normalize() 
    {
        final double len = length();
        return new Vector4( x / len , y / len , z / len  , w );
    }
    
    // scalar / dot product
    public double dotProduct(Vector4 o) {
        return (x*o.x + y*o.y + z*o.z );
    }
    
    public double angleInRadians(Vector4 o) {
        // => cos
        final double cosine = dotProduct( o ) / ( length() * o.length() );
        return Math.acos( cosine );
    }
    
    public double angleInDegrees(Vector4 o) {
        final double factor = (180.0f / Math.PI);
        return angleInRadians(o)*factor;
    }        
    
    public Vector4 crossProduct(Vector4 other) 
    {
        /*
         * ox = (y1 * z2) - (y2 * z1)
         * oy = (z1 * x2) - (z2 * x1)
         * oz = (x1 * y2) - (x2 * y1)            
         */
        return new Vector4( y*other.z - other.y * z , 
                             (z*other.x - other.z * x) , 
                             x*other.y - other.x * y );
    }
    
    @Override
    public String toString()
    {
        return "("+x+","+y+","+z+","+w+")";
    }
}
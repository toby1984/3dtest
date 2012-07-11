package de.codesourcery.engine.geom;

import org.apache.commons.lang.ArrayUtils;
import org.ejml.alg.dense.mult.MatrixVectorMult;
import org.ejml.data.DenseMatrix64F;
import org.ejml.data.RowD1Matrix64F;

public final class Vector4 
{
    private double[] data;
    private int offset=0;
    
    public Vector4() {
        data = new double[4];
    }
    
    public void setData(double[] data,int offset) {
        this.data = data;
        this.offset = offset;
    }
    
    public double[] getData() {
        if ( data.length == 4 ) {
            return data;
        }
        return ArrayUtils.subarray( this.data , offset , offset + 4 );
    }
    
    public Vector4(double[] data,int offset) 
    {
        this.data = data;
        this.offset = offset;
    }
    
    public boolean isEquals(Vector4 other) {
        return this.x() == other.x() &&
                this.y() == other.y() &&
                this.z() == other.z() &&
                this.w() == other.w();
    }
    
    public void x(double value) {
        this.data[ offset ] = value;
    }
    
    public void y(double value) {
        this.data[ offset +1 ] = value;        
    }
    
    public void z(double value) {
        this.data[ offset +2 ] = value;           
    }
    
    public void w(double value) {
        this.data[ offset + 3 ] = value;  
    }
    
    public double x() {
        return this.data[ offset ];
    }
    
    public double y() {
        return this.data[ offset + 1 ];
    }
    
    public double z() {
        return this.data[ offset + 2 ];
    }
    
    public double w() {
        return this.data[ offset + 3];
    }
    
    public Vector4 minus(Vector4 other) 
    {
        // TODO: Maybe it's faster to use a loop here ? Needs benchmarking
        return new Vector4( this.x() - other.x() , this.y() - other.y() , this.z() - other.z() , w() );
    }
    
    public Vector4 plus(Vector4 other) {
     // TODO: Maybe it's faster to use a loop here ? Needs benchmarking
        return new Vector4( this.x() + other.x() , this.y() + other.y() , this.z() + other.z() , w() );
    }        
    
    public Vector4(double x,double y,double z) {
        this(x,y,z,1);
    }
    
    public Vector4(double x,double y,double z,double w) 
    {
        this.data = new double[] { x , y , z , w };
    }
    
    public Vector4(DenseMatrix64F result)
    {
        if ( result.numRows != 4 ) {
            throw new IllegalArgumentException("Invalid row count "+result.numRows+" for "+result);
        }
        if ( result.numCols != 1 ) {
            throw new IllegalArgumentException("Invalid column count "+result.numCols+" for "+result);
        }        
        
        // TODO: Performance - use DenseMatrix64F#getData()
        this.data = new double[] {  result.get( 0 , 0 ) , result.get( 1 , 0 ) , result.get( 2 , 0 ), result.get( 3 , 0 ) };
    }

    private DenseMatrix64F toRowMatrix() 
    {
        final DenseMatrix64F result = new DenseMatrix64F( 4 , 1 );
        // TODO: Performance - use DenseMatrix64F#setData()
        result.set( 0 , 0 , x() );
        result.set( 1 , 0 , y() );
        result.set( 2 , 0 , z() );
        result.set( 3 , 0 , w() );
        return result;
    }

    public Vector4 multiply( RowD1Matrix64F matrix ) 
    {
        final DenseMatrix64F result = new DenseMatrix64F(4 , 1 );
        MatrixVectorMult.mult( matrix , toRowMatrix()  , result );
        Vector4 resultVec = new Vector4( result );
        return resultVec;
    }
    
    public double length() 
    {
        return Math.sqrt( x()*x() + y()*y() + z()*z() );   
    }
    
    public Vector4 multiply(double value) {
        return new Vector4( x()*value , y()*value , z()*value , w() );
    }
    
    public Vector4 normalize() 
    {
        final double len = length();
        return new Vector4( x() / len , y() / len , z() / len  , w() );
    }
    
    // scalar / dot product
    public double dotProduct(Vector4 o) {
        return (x()*o.x() + y()*o.y() + z()*o.z() );
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
        return new Vector4( y()*other.z() - other.y() * z() , 
                             (z()*other.x() - other.z() * x()) , 
                             x()*other.y() - other.x() * y() );
    }
    
    @Override
    public String toString()
    {
        return "("+x()+","+y()+","+z()+","+w()+")";
    }
}
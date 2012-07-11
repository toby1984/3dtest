package de.codesourcery.engine.geom;

import de.codesourcery.engine.linalg.Matrix;

public final class Vector4 
{
    private double[] data;
    private int offset=0;
    
    public Vector4() {
        data = new double[4];
    }
    
    public Vector4(double[] data) {
        this.data = data;
    }    
    
    public void setData(double[] data,int offset) {
        this.data = data;
        this.offset = offset;
    }
    
    public void copyInto(double[] array,int startingOffset) 
    {
        array[startingOffset] = this.data[offset];
        array[startingOffset+1] = this.data[offset+1];
        array[startingOffset+2] = this.data[offset+2];
        array[startingOffset+3] = this.data[offset+3];
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
    
    public Vector4 multiply( Matrix matrix) {
        return matrix.multiply( this );
    }
    
    public double[] getDataArray()
    {
        return data;
    }
    
    public int getDataOffset()
    {
        return offset;
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
package de.codesourcery.engine.linalg;


public final class Vector4 
{
    private double[] data;
    private int offset=0;
    
    public Vector4(Vector4 input) 
    {
        data = new double[4];
        input.copyInto( data , 0 );
    }
    
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
    
    public Vector4 multiply( Matrix matrix) 
    {
        final double[] result = new double[4];
        
        final double[] thisData = this.data;
        final double[] matrixData = matrix.getData();

        final int offset = this.offset;
        
        result[0] = thisData[ offset ] * matrixData[0] + thisData[offset+1] * matrixData[1]+
                    thisData[offset+2] * matrixData[2]+ thisData[offset+3] * matrixData[3];
        
        result[1] = thisData[ offset ] * matrixData[4] + thisData[offset+1] * matrixData[5] +
                    thisData[offset+2] * matrixData[6] + thisData[offset+3] * matrixData[7];
        
        result[2] = thisData[ offset ] * matrixData[8] + thisData[offset+1] * matrixData[9] +
                    thisData[offset+2] * matrixData[10] + thisData[offset+3] * matrixData[11];
        
        result[3] = thisData[ offset ] * matrixData[12] + thisData[offset+1] * matrixData[13] +
                    thisData[offset+2] * matrixData[14] + thisData[offset+3] * matrixData[15];
        
        return new Vector4( result );
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
    
    public Vector4 multiply(double value) 
    {
        return new Vector4( x()*value , y()*value , z()*value , w() );
    }
    
    public Vector4 normalize() 
    {
        final double len = length();
        if ( len  == 0 ) {
        	return new Vector4(0,0,0); 
        }
        return new Vector4( x() / len , y() / len , z() / len  , w() );
    }
    
    public Vector4 normalizeW() 
    {
        double w = w();
        if ( w != 1.0 ) 
        {
            return new Vector4( x() / w, y() / w , z() / w , w );
        }
        return this;
    }    
    
    public void normalizeWInPlace() 
    {
        double w = w();
        if ( w != 1.0 ) 
        {
        	x( x() / w );
        	y( y() / w );
        	z( z() / w );
        }
    }      
    
    // scalar / dot product
    public double dotProduct(Vector4 o) 
    {
        return data[offset]*o.data[o.offset] + data[offset+1]*o.data[o.offset+1]+data[offset+2]*o.data[o.offset+2];
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
        final double[] thisData = this.data;
        final int thisOffset = this.offset;
        
        final double[] o = other.data;
        final int oOffset = other.offset;
        
        final double x1 = thisData[thisOffset];
        final double y1 = thisData[thisOffset+1];
        final double z1 = thisData[thisOffset+2];
        
        final double x2 = o[ oOffset ];
        final double y2 = o[ oOffset+1 ];
        final double z2 = o[ oOffset+2 ];
        
        double newX = y1 * z2 - y2 * z1;
        double newY = z1 * x2 - z2 * x1;
        double newZ = x1 * y2 - x2 * y1;
        
        return new Vector4( newX ,newY,newZ );
    }
    
    @Override
    public String toString()
    {
        return "("+x()+","+y()+","+z()+","+w()+")";
    }
}
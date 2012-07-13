package de.codesourcery.engine.linalg;

import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;


public final class Matrix
{
    public static final int SIZE = 4; // 4x4 matrix
    /*
     * Matrix coefficients are stored in column-major order:
     * 
     * 0 4  8 12
     * 1 5  9 13
     * 2 6 10 14
     * 3 7 11 15
     * 
     */
    private final double[] data;


    /**
     * Creates an empty 4x4 matrix.
     */
    public Matrix() {
        this.data = new double[SIZE*SIZE];
    }

    public Matrix(double[] data) 
    {
        if ( data.length != SIZE*SIZE ) {
            throw new IllegalArgumentException("Invalid array length: "+data.length);
        }
        this.data = data;
    }

    /**
     * Creates a 4x4 matrix from COLUMN vectors.
     * 
     * @param v1
     * @param v2
     * @param v3
     * @param v4
     */
    public Matrix(Vector4 v1,Vector4 v2,Vector4 v3,Vector4 v4) 
    {
        this.data = new double[ SIZE*SIZE ];
        v1.copyInto( this.data , 0 );
        v2.copyInto( this.data , 4 );
        v3.copyInto( this.data , 8 );
        v4.copyInto( this.data , 12 );
    }

    public void set(int column,int row,double value) {
        this.data[ column*SIZE + row ] = value;
    }

    public double get(int column,int row) {
        return this.data[ column*SIZE + row ];
    }

    /**
     * Multiply by another 4x4 matrix.
     * 
     * @param other
     * @return
     */
    public Matrix multiply(Matrix other) {
        return new Matrix( multiply( other , new double[ SIZE*SIZE ] ) );
    }

    /**
     * Multiply by another 4x4 matrix.
     * 
     * @param other
     * @param target target array where results should be stored (in column-major order)
     * @return
     */
    public double[] multiply(Matrix other,double[] target) {

        target[ 0 ] = multRow( 0 , 0 , this.data , other.data );
        target[ 1 ] = multRow( 1 , 0 , this.data , other.data );  
        target[ 2 ] = multRow( 2 , 0 , this.data , other.data );    
        target[ 3 ] = multRow( 3 , 0 , this.data , other.data );  

        target[ 4 ] = multRow( 0 , 1 , this.data , other.data );
        target[ 5 ] = multRow( 1 , 1 , this.data , other.data );  
        target[ 6 ] = multRow( 2 , 1 , this.data , other.data );    
        target[ 7 ] = multRow( 3 , 1 , this.data , other.data );          

        target[ 8 ] = multRow( 0 , 2 , this.data , other.data );
        target[ 9 ] = multRow( 1 , 2 , this.data , other.data );  
        target[ 10 ] = multRow( 2 , 2 , this.data , other.data );    
        target[ 11 ] = multRow( 3 , 2 , this.data , other.data );            

        target[ 12 ] = multRow( 0 , 3 , this.data , other.data );
        target[ 13 ] = multRow( 1 , 3 , this.data , other.data );  
        target[ 14 ] = multRow( 2 , 3 , this.data , other.data );    
        target[ 15 ] = multRow( 3 , 3 , this.data , other.data );          
        return target;
    }

    private double multRow(int srcRow , int dstCol , double[] thisMatrix,double[] otherMatrix) 
    {
        int thisOffset = srcRow; // currentCol*size + srcRow
        int otherOffset = dstCol*SIZE;

        double result = thisMatrix[ thisOffset ] * otherMatrix[ otherOffset++ ];
        thisOffset+=SIZE;

        result += thisMatrix[ thisOffset ] * otherMatrix[ otherOffset++ ];
        thisOffset+=SIZE;

        result += thisMatrix[ thisOffset ] * otherMatrix[ otherOffset++ ];
        thisOffset+=SIZE;

        result += thisMatrix[ thisOffset ] * otherMatrix[ otherOffset++ ];
        thisOffset+=SIZE;

        return result;
    }

    public static Matrix identity() 
    {
        double[] data = new double[] {
                1 , 0 , 0 , 0 ,
                0 , 1 , 0 , 0 ,
                0 , 0 , 1 , 0 ,
                0 , 0 , 0 , 1 ,
        };
        return new Matrix( data );
    }

    public static Matrix scale(double factor) 
    {
        double[] data = new double[] {
                factor , 0 , 0 , 0 ,
                0 , factor , 0 , 0 ,
                0 , 0 , factor , 0 ,
                0 , 0 , 0 , 1 ,
        };
        return new Matrix( data );
    }   

    public static Matrix scale(double scaleX,double scaleY,double scaleZ) 
    {
        double[] data = new double[] {
                scaleX , 0 , 0 , 0 ,
                0 , scaleY , 0 , 0 ,
                0 , 0 , scaleZ , 0 ,
                0 , 0 , 0 , 1 ,
        };
        return new Matrix( data );
    }     

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for ( int y = 0 ; y < SIZE ; y++ ) 
        {
            for ( int x = 0 ; x < SIZE ; x++ ) {
                builder.append( format( get( x , y ) ) );
            }
            if ( (y+1) < SIZE ) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    private static String format(double v) {
        final DecimalFormat df = new DecimalFormat("##0.0#");
        return StringUtils.leftPad( df.format( v ) , 6 );
    }

    public Matrix transpose() {
    
        Matrix result = new Matrix();
        
        for ( int row = 0 ; row < SIZE ; row++ ) {
            for ( int col = 0 ; col < SIZE ; col++ ) {
                result.set( row , col , get( col , row ) );
            }
        }
        return result;
    }
    
    public Vector4 multiply(Vector4 vector4)
    {
        final double[] result = new double[4];
        final double[] thisData = this.data;

        final int offset = vector4.getDataOffset(); 
        final double[] data = vector4.getDataArray();

        double value = this.data[ 0 ] * data[ offset ];
        value+= thisData[ 0 + SIZE ] * data[ offset+1 ];
        value+= thisData[ 0 + SIZE*2 ] * data[ offset+2 ];
        value+= thisData[ 0 + SIZE*3 ] * data[ offset+3 ];
        result[ 0 ] = value;

        value = thisData[ 1 ] * data[ offset ];
        value+= thisData[ 1 + SIZE ] * data[ offset+1 ];
        value+= thisData[ 1 + SIZE*2 ] * data[ offset+2 ];
        value+= thisData[ 1 + SIZE*3 ] * data[ offset+3 ];
        result[ 1 ] = value;

        value = thisData[ 2 ] * data[ offset ];
        value+= thisData[ 2 + SIZE ] * data[ offset+1 ];
        value+= thisData[ 2 + SIZE*2 ] * data[ offset+2 ];
        value+= thisData[ 2 + SIZE*3 ] * data[ offset+3 ];
        result[ 2 ] = value;

        value = thisData[ 3 ] * data[ offset+0 ];
        value+= thisData[ 3 + SIZE ] * data[ offset+1 ];
        value+= thisData[ 3 + SIZE*2 ] * data[ offset+2 ];
        value+= thisData[ 3 + SIZE*3 ] * data[ offset+3 ];
        result[ 3 ] = value;        

        return new Vector4( result );
    }

    public Matrix invert() {

        final double[] m = this.data;
        final double[] invOut = new double[16];

        double[] inv = new double[16];
        double det=0.0;
        int i = 0;

        inv[0] = m[5]  * m[10] * m[15] - 
                m[5]  * m[11] * m[14] - 
                m[9]  * m[6]  * m[15] + 
                m[9]  * m[7]  * m[14] +
                m[13] * m[6]  * m[11] - 
                m[13] * m[7]  * m[10];

        inv[4] = -m[4]  * m[10] * m[15] + 
                m[4]  * m[11] * m[14] + 
                m[8]  * m[6]  * m[15] - 
                m[8]  * m[7]  * m[14] - 
                m[12] * m[6]  * m[11] + 
                m[12] * m[7]  * m[10];

        inv[8] = m[4]  * m[9] * m[15] - 
                m[4]  * m[11] * m[13] - 
                m[8]  * m[5] * m[15] + 
                m[8]  * m[7] * m[13] + 
                m[12] * m[5] * m[11] - 
                m[12] * m[7] * m[9];

        inv[12] = -m[4]  * m[9] * m[14] + 
                m[4]  * m[10] * m[13] +
                m[8]  * m[5] * m[14] - 
                m[8]  * m[6] * m[13] - 
                m[12] * m[5] * m[10] + 
                m[12] * m[6] * m[9];

        inv[1] = -m[1]  * m[10] * m[15] + 
                m[1]  * m[11] * m[14] + 
                m[9]  * m[2] * m[15] - 
                m[9]  * m[3] * m[14] - 
                m[13] * m[2] * m[11] + 
                m[13] * m[3] * m[10];

        inv[5] = m[0]  * m[10] * m[15] - 
                m[0]  * m[11] * m[14] - 
                m[8]  * m[2] * m[15] + 
                m[8]  * m[3] * m[14] + 
                m[12] * m[2] * m[11] - 
                m[12] * m[3] * m[10];

        inv[9] = -m[0]  * m[9] * m[15] + 
                m[0]  * m[11] * m[13] + 
                m[8]  * m[1] * m[15] - 
                m[8]  * m[3] * m[13] - 
                m[12] * m[1] * m[11] + 
                m[12] * m[3] * m[9];

        inv[13] = m[0]  * m[9] * m[14] - 
                m[0]  * m[10] * m[13] - 
                m[8]  * m[1] * m[14] + 
                m[8]  * m[2] * m[13] + 
                m[12] * m[1] * m[10] - 
                m[12] * m[2] * m[9];

        inv[2] = m[1]  * m[6] * m[15] - 
                m[1]  * m[7] * m[14] - 
                m[5]  * m[2] * m[15] + 
                m[5]  * m[3] * m[14] + 
                m[13] * m[2] * m[7] - 
                m[13] * m[3] * m[6];

        inv[6] = -m[0]  * m[6] * m[15] + 
                m[0]  * m[7] * m[14] + 
                m[4]  * m[2] * m[15] - 
                m[4]  * m[3] * m[14] - 
                m[12] * m[2] * m[7] + 
                m[12] * m[3] * m[6];

        inv[10] = m[0]  * m[5] * m[15] - 
                m[0]  * m[7] * m[13] - 
                m[4]  * m[1] * m[15] + 
                m[4]  * m[3] * m[13] + 
                m[12] * m[1] * m[7] - 
                m[12] * m[3] * m[5];

        inv[14] = -m[0]  * m[5] * m[14] + 
                m[0]  * m[6] * m[13] + 
                m[4]  * m[1] * m[14] - 
                m[4]  * m[2] * m[13] - 
                m[12] * m[1] * m[6] + 
                m[12] * m[2] * m[5];

        inv[3] = -m[1] * m[6] * m[11] + 
                m[1] * m[7] * m[10] + 
                m[5] * m[2] * m[11] - 
                m[5] * m[3] * m[10] - 
                m[9] * m[2] * m[7] + 
                m[9] * m[3] * m[6];

        inv[7] = m[0] * m[6] * m[11] - 
                m[0] * m[7] * m[10] - 
                m[4] * m[2] * m[11] + 
                m[4] * m[3] * m[10] + 
                m[8] * m[2] * m[7] - 
                m[8] * m[3] * m[6];

        inv[11] = -m[0] * m[5] * m[11] + 
                m[0] * m[7] * m[9] + 
                m[4] * m[1] * m[11] - 
                m[4] * m[3] * m[9] - 
                m[8] * m[1] * m[7] + 
                m[8] * m[3] * m[5];

        inv[15] = m[0] * m[5] * m[10] - 
                m[0] * m[6] * m[9] - 
                m[4] * m[1] * m[10] + 
                m[4] * m[2] * m[9] + 
                m[8] * m[1] * m[6] - 
                m[8] * m[2] * m[5];

        det = m[0] * inv[0] + m[1] * inv[4] + m[2] * inv[8] + m[3] * inv[12];

        if (det == 0) {
            return null;
        }

        det = 1.0 / det;

        for (i = 0; i < 16; i++) {
            invOut[i] = inv[i] * det;
        }

        return new Matrix( invOut );
    }

    public double[] getData()
    {
        return this.data;
    }

}

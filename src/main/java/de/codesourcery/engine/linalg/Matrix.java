package de.codesourcery.engine.linalg;

import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;
import org.ejml.alg.dense.mult.MatrixMatrixMult;
import org.ejml.data.DenseMatrix64F;

import de.codesourcery.engine.geom.Vector4;

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
    public Matrix mult(Matrix other) {

        /*
         * 
         * a c     e g       a*e+c*f  a*g+c*h
         *      x        =>  
         * b d     f h
         */
        final double[] target = new double[ SIZE * SIZE ];

        int ptr=0;

        target[ ptr++ ] = multRow( 0 , 0 , this.data , other.data );
        target[ ptr++ ] = multRow( 1 , 0 , this.data , other.data );  
        target[ ptr++ ] = multRow( 2 , 0 , this.data , other.data );    
        target[ ptr++ ] = multRow( 3 , 0 , this.data , other.data );  

        target[ ptr++ ] = multRow( 0 , 1 , this.data , other.data );
        target[ ptr++ ] = multRow( 1 , 1 , this.data , other.data );  
        target[ ptr++ ] = multRow( 2 , 1 , this.data , other.data );    
        target[ ptr++ ] = multRow( 3 , 1 , this.data , other.data );          

        target[ ptr++ ] = multRow( 0 , 2 , this.data , other.data );
        target[ ptr++ ] = multRow( 1 , 2 , this.data , other.data );  
        target[ ptr++ ] = multRow( 2 , 2 , this.data , other.data );    
        target[ ptr++ ] = multRow( 3 , 2 , this.data , other.data );            

        target[ ptr++ ] = multRow( 0 , 3 , this.data , other.data );
        target[ ptr++ ] = multRow( 1 , 3 , this.data , other.data );  
        target[ ptr++ ] = multRow( 2 , 3 , this.data , other.data );    
        target[ ptr++ ] = multRow( 3 , 3 , this.data , other.data );          
        return new Matrix( target );
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

    public void benchmarkMultiplication() {

        Matrix m1 = new Matrix( new double[] {
                1,3,8 , 2, // col 0
                2,4,9,3, // col 1
                2,-3,1,4,
                9.5,15,3,2
        } ); // 2x2 matrix

        Matrix m2 = new Matrix( new double[] {
                5,7,11 , 12, // col 0
                6,8,13,14, // col 1
                -17,18,2,16,
                2,23,16,3  }); 

        // warm-up
        Matrix tmp = null;
        for ( int i = 0 ; i < 100000000 ; i++ ) {
            tmp = m1.mult( m2 );
        }        

        // run test
        long delta = -System.currentTimeMillis();

        for ( int i = 0 ; i < 100000000 ; i++ ) {
            tmp = m1.mult( m2 );
        }
        delta += System.currentTimeMillis();
        System.out.println("\n\nTime = "+delta+" ms, \n\nresult = "+tmp);
    }

    public void benchmarkMultiplication2() 
    {
        DenseMatrix64F m1 = new DenseMatrix64F( 4,4 );
        m1.set( 4 , 4 , false , new double[] {
                1,3,8 , 2, // col 0
                2,4,9,3, // col 1
                2,-3,1,4,
                9.5,15,3,2
        } ); // 2x2 matrix

        DenseMatrix64F m2 = new DenseMatrix64F( 4,4 );

        m2.set( 4 ,4 , false , new double[] {
                5,7,11 , 12, // col 0
                6,8,13,14, // col 1
                -17,18,2,16,
                2,23,16,3  }); 

        // warm-up
        DenseMatrix64F target = null;
        for ( int i = 0 ; i < 100000000 ; i++ ) 
        {
            target = new DenseMatrix64F(4,4);
            MatrixMatrixMult.mult_small( m1 , m2 , target );
        }        

        // run test
        long delta = -System.currentTimeMillis();

        for ( int i = 0 ; i < 100000000 ; i++ ) {
            target = new DenseMatrix64F(4,4);
            MatrixMatrixMult.mult_small( m1 , m2 , target );            
        }
        delta += System.currentTimeMillis();
        System.out.println("\n\nTime = "+delta+" ms, \n\nresult = "+target);
    }

    public static void main(String[] args)
    {
        Matrix m1 = new Matrix( new double[] {
                1,2,3,4,
                5,6,7,8,
                9,10,11,12,
                13,14,15,16
        } ); 

        Vector4 vec = new Vector4(1,2,3,4);

        System.out.println("vec="+vec);
        System.out.println("M="+m1);
        System.out.println("result="+m1.multiply( vec ) );

        DenseMatrix64F m2 = new DenseMatrix64F(4,4);
        m2.set( 4 , 4 , false , m1.data );

        System.out.println("result 2 = "+vec.multiply( m2 ) );

        //        new Matrix().benchmarkMultiplication();
        //        new Matrix().benchmarkMultiplication2();
    }

    public Vector4 multiply(Vector4 vector4)
    {
        final double[] result = new double[4];
        
        final double[] thisData = this.data;
        final double[] data = vector4.getData();

        double value = this.data[ 0 ] * data[ 0 ];
        value+= thisData[ 0 + SIZE ] * data[ 1 ];
        value+= thisData[ 0 + SIZE*2 ] * data[ 2 ];
        value+= thisData[ 0 + SIZE*3 ] * data[ 3 ];
        result[ 0 ] = value;
        
        value = thisData[ 1 ] * data[ 0 ];
        value+= thisData[ 1 + SIZE ] * data[ 1 ];
        value+= thisData[ 1 + SIZE*2 ] * data[ 2 ];
        value+= thisData[ 1 + SIZE*3 ] * data[ 3 ];
        result[ 1 ] = value;
        
        value = thisData[ 2 ] * data[ 0 ];
        value+= thisData[ 2 + SIZE ] * data[ 1 ];
        value+= thisData[ 2 + SIZE*2 ] * data[ 2 ];
        value+= thisData[ 2 + SIZE*3 ] * data[ 3 ];
        result[ 2 ] = value;
        
        value = thisData[ 3 ] * data[ 0 ];
        value+= thisData[ 3 + SIZE ] * data[ 1 ];
        value+= thisData[ 3 + SIZE*2 ] * data[ 2 ];
        value+= thisData[ 3 + SIZE*3 ] * data[ 3 ];
        result[ 3 ] = value;        

        return new Vector4( result );
    }
}

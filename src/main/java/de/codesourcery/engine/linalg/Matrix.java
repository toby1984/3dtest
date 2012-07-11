package de.codesourcery.engine.linalg;

import de.codesourcery.engine.geom.Vector4;

public class Matrix
{
    /*
     * Matrix coefficients are stored in column-major order:
     * 
     * 0 4  8 12
     * 1 5  9 13
     * 2 6 10 14
     * 3 7 11 15
     * 
     */
    private double[] data=new double[ 16 ];
    
    public Matrix(Vector4 v1,Vector4 v2,Vector4 v3,Vector4 v4) {
        
    }
}

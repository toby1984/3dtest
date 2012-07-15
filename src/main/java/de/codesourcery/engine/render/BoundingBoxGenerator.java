package de.codesourcery.engine.render;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

import java.util.Iterator;

import de.codesourcery.engine.linalg.BoundingBox;
import de.codesourcery.engine.linalg.LinAlgUtils;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;

public class BoundingBoxGenerator {

	public static void main(String[] args) 
	{
		Object3D object = new Object3D();
		object.setPrimitives( LinAlgUtils.createCube( 2 , 3 , 4 ) , false );
		new BoundingBoxGenerator().calculateBoundingBox( object );
		
//		Vector4 v1 = new Vector4(-1,-2,1);
//		Vector4 v2 = new Vector4(1,0,2);
//		Vector4 v3 = new Vector4(2,-1,3);
//		Vector4 v4 = new Vector4(2,-1,2);
//		
//		Object3D obj = new Object3D();
//		obj.setPrimitives( Arrays.asList( new Quad( v1 , v2 , v3 , v4 ) ) );
//		calculateBoundingBox( obj );
	}
	
	public static BoundingBox calculateBoundingBox(Object3D object) 
	{
		// calculate mean (average) position
		double avgX = 0;
		double avgY = 0;
		double avgZ = 0;
		
		final double[] vertices = object.getVertices();
		final int N = (int) (vertices.length / 4);
		for ( int i = 0 ; i < vertices.length ; ) {
			avgX += vertices[i++];
			avgY += vertices[i++];
			avgZ += vertices[i++];
			i++; // ignore W
		}
		
		avgX /= N;
		avgY /= N;
		avgZ /= N;

		// construct 3x3 covariance matrix
		double c11 = 0;
		double c12 = 0;
		double c22 = 0;
		double c13 = 0;
		double c33 = 0;
		double c23 = 0;
		for ( int i = 0 ; i < vertices.length ; i+=4 ) 
		{
			final double x = vertices[i];
			final double y = vertices[i+1];
			final double z = vertices[i+2];
			
			c11 += ( x - avgX ) * ( x - avgX );
			c12 += ( x - avgX ) * ( y - avgY );
			c22 += ( y - avgY ) * ( y - avgY );
			c13 += ( x - avgX ) * ( z - avgZ );
			c33 += ( z - avgZ ) * ( z - avgZ );
			c23 += ( y - avgY ) * ( z - avgZ );
		}
		c11 /= N;
		c12 /= N;
		final double c21 = c12;
		c22 /= N;
		c13 /= N;
		final double c31 = c13;
		c33 /= N;
		c23 /= N;
		final double c32 = c23;
		
		final Matrix c = new Matrix( new Vector4(c11,c12,c13),
				new Vector4(c21,c22,c23),
				new Vector4(c31,c32,c33),
				new Vector4(0,0,0,0)
		);
		
		/* The eigenvalues (c11, c22 , c33) are the roots of the characteristic polynomial:
		 *  
		 *                           | c11 - lambda  c21         c31         |                  
		 * det( C - lambda * I ) =   | c12           c22-lambda  c32         |
		 *                           | c13           c23         c32-lambda  |
		 * 
		 */
		
		// calculate eigenvectors
		
		final Matrix eigenVectors = new Matrix();
		calculateEigenSystem( c , eigenVectors );
		
		System.out.println("Matrix:\n"+eigenVectors);
		
		Vector4 r = eigenVectors.getRow(0).multiply( -1 );
		Vector4 s = eigenVectors.getRow(1);
		Vector4 t = eigenVectors.getRow(2).multiply( -1 );

		System.out.println("R = "+r);
		System.out.println("S = "+s);
		System.out.println("T = "+t);
		
		double minPR = 0;
		double maxPR = 0;
		
		double minPS = 0;
		double maxPS = 0;
		
		double minPT = 0;
		double maxPT = 0;
		
		for ( Iterator<Vector4> iterator = object.getVertexIterator(); iterator.hasNext() ; ) {
			
			final Vector4 v = iterator.next();
			
			double dot1 = v.dotProduct( r );
			double dot2 = v.dotProduct( s );
			double dot3 = v.dotProduct( t );
			
			if ( dot1 < minPR ) {
				minPR = dot1;
			}
			if ( dot2 < minPS ) {
				minPS = dot2;
			}
			if ( dot3 < minPT ) {
				minPT = dot3;
			}		
			
			if ( dot1 > maxPR ) {
				maxPR = dot1;
			}	
			if ( dot2 > maxPS ) {
				maxPS = dot2;
			}	
			if ( dot3 > maxPT ) {
				maxPT = dot3;
			}				
		}
		
		// calculate center of bounding box
		final double extendR = (minPR + maxPR) / 2;
		final double extendS = (minPS + maxPS) / 2;
		final double extendT = (minPT + maxPT) / 2;
		
		final Vector4 center = r.multiply( extendR ).plus( s.multiply( extendS ) ).plus( t.multiply( extendT ) );

		System.out.println("Center: "+center);
		System.out.println("Extent R: "+minPR+" , "+maxPR);
		System.out.println("Extent S: "+minPS+" , "+maxPS);
		System.out.println("Extent T: "+minPT+" , "+maxPT);
		
		final double width = maxPR - minPR;
		final double height = maxPS - minPS;
		final double depth = maxPT - minPT;
		return new BoundingBox( center , r , s , t , width , height , depth*2 );
	}
	
	/**
	 * Calculates eigenvectors and eigenvalues for a given 3x3 matrix.
	 * 
	 * @param m input matrix
	 * @param eigenVectors matrix whose first 3 columns will be set to the calculated eigenvectors
	 * @return calculated eigenvalues
	 */
	private static double[] calculateEigenSystem(Matrix m , Matrix eigenVectors) {
		
		final int maxSweeps = 32;
		final double epsilon = 1.0e-10;
		final double[] lambda = new double[3];
		
		/*
		 * m11 m21 m31 
		 * m12 m22 m32
		 * m13 m23 m33
		 */
		double m11 = m.get( 0 , 0 );
		double m12 = m.get( 0 , 1 );
		double m13 = m.get( 0 , 2 );
		double m22 = m.get( 1 , 1 );
		double m23 = m.get( 1 , 2 );
		double m33 = m.get( 2 , 2 );
			
		eigenVectors.setIdentity();
		
		for ( int a = 0 ; a < maxSweeps ; a++ ) {
			
			// exit if off-diagonal entries are small enough
			if ( abs( m12 ) < epsilon && abs( m13 ) < epsilon && abs( m23 ) < epsilon ) {
				break;
			}
			
			// annihilate (1,2) entry
			if ( m12 != 0.0 ) {
				double u = (m22 - m11 ) * 0.5 / m12;
				double u2 = u*u;
				double u2p1 = u2 + 1.0;
				double t = (u2p1 != u2 ) ? ((u < 0.0 ) ? -1.0 : 1.0 ) * ( sqrt( u2p1 ) - abs( u ) ): 0.5/u;
				double c = 1.0 / sqrt( t*t + 1.0 );
				double s = c*t;
				m11 -= t * m12;
				m22 += t * m12;
				m12 = 0.0f;
				
				double temp = c * m13 - s * m23;
				m23 = s * m13 + c * m23;
				m13 = temp;
				
				for ( int i = 0 ; i < 3 ; i++ ) {
					double tmp = c * eigenVectors.get( i, 0 ) - s * eigenVectors.get( i , 1 );
					eigenVectors.set( i,1 ,  s * eigenVectors.get(i,0) + c * eigenVectors.get(i,1) );
					eigenVectors.set( i, 0 , tmp );
				}
			}
			
			// annihilate (1,3) entry
			if ( m13 != 0.0 ) 
			{
				double u = (m33 - m11 ) * 0.5 / m13;
				double u2 = u * u;
				double u2p1 = u2 + 1;
				double t = (u2p1 != u2 ) ? ((u < 0.0 ) ? -1.0 : 1.0 ) * ( Math.sqrt( u2p1 ) - Math.abs( u ) ): 0.5/u;	
				double c = 1.0 / Math.sqrt( t*t + 1.0 );
				double s = c*t;
				
				m11 -= t * m13;
				m33 += t * m13;
				m13 = 0.0f;
				
				double temp = c * m12 - s * m23;
				m23 = s * m12 + c * m23;
				m12 = temp;
				
				for ( int i = 0 ; i < 3 ; i++ ) {
					double tmp = c * eigenVectors.get( i, 0 ) - s * eigenVectors.get( i , 2 );
					eigenVectors.set( i, 2 ,  s * eigenVectors.get(i,0) + c * eigenVectors.get(i,2) );
					eigenVectors.set( i, 0 , tmp );
				}				
			}
			
			// annihilate (2,3) entry
			if ( m23 != 0.0 ) 
			{
				double u = (m33 - m22 ) * 0.5 / m23;
				double u2 = u * u;
				double u2p1 = u2 + 1;
				double t = (u2p1 != u2 ) ? ((u < 0.0 ) ? -1.0 : 1.0 ) * ( Math.sqrt( u2p1 ) - Math.abs( u ) ): 0.5/u;	
				double c = 1.0 / Math.sqrt( t*t + 1.0 );
				double s = c*t;
				
				m22 -= t * m23;
				m33 += t * m23;
				m23 = 0.0f;
				
				double temp = c * m12 - s * m13;
				m13 = s * m12 + c * m13;
				m12 = temp;
				
				for ( int i = 0 ; i < 3 ; i++ ) {
					double tmp = c * eigenVectors.get( i, 1 ) - s * eigenVectors.get( i , 2 );
					eigenVectors.set( i, 2 ,  s * eigenVectors.get(i,1) + c * eigenVectors.get(i,2) );
					eigenVectors.set( i, 1 , tmp );
				}				
			}			
		}
		
		lambda[0] = m11;
		lambda[1] = m22;
		lambda[2] = m33;
		
		System.out.println("Lambda #1 = "+lambda[0]);
		System.out.println("Lambda #2 = "+lambda[1]);
		System.out.println("Lambda #3 = "+lambda[2]);
		return lambda;
	}
}

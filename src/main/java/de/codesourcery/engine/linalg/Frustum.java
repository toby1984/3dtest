package de.codesourcery.engine.linalg;


public final class Frustum  
{  
    public static enum CullResult  
    {  
        TOTALLY_INSIDE,  
        PARTIALLY_INSIDE,  
        TOTALLY_OUTSIDE  
    };  
 
    private final Vector4[] planes = new Vector4[6];
 
    public Frustum() {
    	for ( int i =0 ; i < planes.length ; i++ ) {
    		planes[i] = new Vector4();
    	}
    }
    public void update(Matrix viewProjectionMatrix)  
    {  
        extractPlanes(viewProjectionMatrix, true);  
    } 
    
    @Override
    public String toString() 
    {
    	return "plane 0 = "+planes[0]+"\n"+
    		   "plane 1 = "+planes[1]+"\n"+
    		   "plane 2 = "+planes[2]+"\n"+
    		   "plane 3 = "+planes[3]+"\n"+
    		   "plane 4 = "+planes[4]+"\n"+
    		   "plane 5 = "+planes[5];
    }
 
    /**
     * Test a sphere against the frustum.   
     */
    public CullResult cullTest(Vector4 center, float radius)  
    {  
        CullResult result = CullResult.TOTALLY_INSIDE;  
 
        float dist;  
        for (int i = 0; i < 6; ++i)  
        {  
            // Calc the distance to the plane.   
            dist = planes[i].x() * center.x() + planes[i].y() * center.y() + planes[i].z() * center.z() + planes[i].w();  
 
            if (dist < -radius) // If sphere is outside and we can exit.   
            {  
                result = CullResult.TOTALLY_OUTSIDE;  
                break;  
            }  
            else if (dist < radius) // If sphere intersects plane, change result to   
            { // partial but keep looking for full exclusion.   
                result = CullResult.PARTIALLY_INSIDE;  
            }  
        }  
 
        return result;  
    } 
 
    /**
     * Test an axis aligned bounding box against the frustum.   
     */
    public CullResult cullTest(BoundingBox box)  
    {  
    	if ( ! box.isAxisAligned() ) {
    		throw new IllegalArgumentException("Only supports axis-aligned BBs , offending: "+box);
    	}
    	final Vector4[] minMax = box.getMinMax();
        return CullTest(minMax[0], minMax[1] );  
    } 
 
    /// Test an axis aligned bounding box against the frustum.   
    private CullResult CullTest(Vector4 min, Vector4 max)  
    {  
        CullResult result = CullResult.TOTALLY_INSIDE;  
 
        for (int i = 0; i < 6; i++)  
        {  
            // Calc the two vertices we need to test against.   
            Vector4 pVertex = new Vector4();
            Vector4 nVertex = new Vector4();
            
            if (planes[i].x() < 0)  
            {  
                pVertex.x( min.x() );  
                nVertex.x( max.x() );  
            }  
            else 
            {  
                pVertex.x( max.x() );  
                nVertex.x( min.x() );  
            }  
            if (planes[i].y() < 0)  
            {  
                pVertex.y( min.y() );  
                nVertex.y( max.y() );  
            }  
            else 
            {  
                pVertex.y( max.y() );  
                nVertex.y( min.y() );  
            }  
            if (planes[i].z() < 0)  
            {  
                pVertex.z( min.z() );  
                nVertex.z( max.z() );  
            }  
            else 
            {  
                pVertex.z( max.z() );  
                nVertex.z( min.z() );  
            }  
 
            // Check for totally outside case.   
            float dist = pVertex.x() * planes[i].x() + pVertex.y() * planes[i].y() + pVertex.z() * planes[i].z() + planes[i].w();  
            if (dist < 0)  
            {  
                result = CullResult.TOTALLY_OUTSIDE;  
                break;  
            }  
 
            // Check for box intersecting plane case.   
            dist = nVertex.x() * planes[i].x() + nVertex.y() * planes[i].y() + nVertex.z() * planes[i].z() + planes[i].w();  
            if (dist < 0)  
            {  
                result = CullResult.PARTIALLY_INSIDE;  
            }  
 
        } // end of loop over frustum planes.   
 
        return result;  
    } 
 
    private void normalizePlane(Vector4 plane)  
    {  
        float invMag = 1.0f / (float)Math.sqrt(plane.x() * plane.x() + plane.y() * plane.y() + plane.z() * plane.z());  
 
        plane.x( plane.x() * invMag );  
        plane.y( plane.y() * invMag );  
        plane.z( plane.z() * invMag );  
        plane.w( plane.w() * invMag );  
    } 
 
    private void extractPlanes(Matrix viewProjectionMatrix, boolean normalize)  
    {  
    	/*
	void Frustum3<Scalar>::set( const Transformation4<Scalar>&amp; mvp )
	{
		// Right clipping plane.
		mPlanes[0].set( mvp[3]-mvp[0], mvp[7]-mvp[4], mvp[11]-mvp[8], mvp[15]-mvp[12] );
		// Left clipping plane.
		mPlanes[1].set( mvp[3]+mvp[0], mvp[7]+mvp[4], mvp[11]+mvp[8], mvp[15]+mvp[12] );
		// Bottom clipping plane.
		mPlanes[2].set( mvp[3]+mvp[1], mvp[7]+mvp[5], mvp[11]+mvp[9], mvp[15]+mvp[13] );
		// Top clipping plane.
		mPlanes[3].set( mvp[3]-mvp[1], mvp[7]-mvp[5], mvp[11]-mvp[9], mvp[15]-mvp[13] );
		// Far clipping plane.
		mPlanes[4].set( mvp[3]-mvp[2], mvp[7]-mvp[6], mvp[11]-mvp[10], mvp[15]-mvp[14] );
		// Near clipping plane.
		mPlanes[5].set( mvp[3]+mvp[2], mvp[7]+mvp[6], mvp[11]+mvp[10], mvp[15]+mvp[14] );

		// Normalize, this is not always necessary...
		for( unsigned int i = 0; i < 6; i++ )
		{
			mPlanes[i].normalize();
		}
	}    	 
    	 */
    	
        // Left clipping plane   
        planes[0].x( viewProjectionMatrix.get(0,3) + viewProjectionMatrix.get(0,0));  
        planes[0].y( viewProjectionMatrix.get(1,3) + viewProjectionMatrix.get(1,0));  
        planes[0].z( viewProjectionMatrix.get(2,3) + viewProjectionMatrix.get(2,0));  
        planes[0].w( viewProjectionMatrix.get(3,3) + viewProjectionMatrix.get(3,0));  
 
        // Right clipping plane   
        planes[1].x( viewProjectionMatrix.get(0,3) - viewProjectionMatrix.get(0,0));  
        planes[1].y( viewProjectionMatrix.get(1,3) - viewProjectionMatrix.get(1,0));  
        planes[1].z( viewProjectionMatrix.get(2,3) - viewProjectionMatrix.get(2,0));  
        planes[1].w( viewProjectionMatrix.get(3,3) - viewProjectionMatrix.get(3,0));  
 
        // Top clipping plane   
        planes[2].x( viewProjectionMatrix.get(0,3) - viewProjectionMatrix.get(0,1));  
        planes[2].y( viewProjectionMatrix.get(1,3) - viewProjectionMatrix.get(1,1));  
        planes[2].z( viewProjectionMatrix.get(2,3) - viewProjectionMatrix.get(2,1));  
        planes[2].w( viewProjectionMatrix.get(3,3) - viewProjectionMatrix.get(3,1));  
 
        // Bottom clipping plane   
        planes[3].x( viewProjectionMatrix.get(0,3) + viewProjectionMatrix.get(0,1));  
        planes[3].y( viewProjectionMatrix.get(1,3) + viewProjectionMatrix.get(1,1));  
        planes[3].z( viewProjectionMatrix.get(2,3) + viewProjectionMatrix.get(2,1));  
        planes[3].w( viewProjectionMatrix.get(3,3) + viewProjectionMatrix.get(3,1));  
 
        // Near clipping plane   
        planes[4].x( viewProjectionMatrix.get(0,2));  
        planes[4].y( viewProjectionMatrix.get(1,2));  
        planes[4].z( viewProjectionMatrix.get(2,2));  
        planes[4].w( viewProjectionMatrix.get(3,2));  
 
        // Far clipping plane   
        planes[5].x( viewProjectionMatrix.get(0,3) - viewProjectionMatrix.get(0,2));  
        planes[5].y( viewProjectionMatrix.get(1,3) - viewProjectionMatrix.get(1,2));  
        planes[5].z( viewProjectionMatrix.get(2,3) - viewProjectionMatrix.get(2,2));  
        planes[5].w( viewProjectionMatrix.get(3,3) - viewProjectionMatrix.get(3,2));  
 
        // Normalize the plane equations, if requested.   
        if (normalize == true)  
        {  
            normalizePlane(planes[0]);  
            normalizePlane(planes[1]);  
            normalizePlane(planes[2]);  
            normalizePlane(planes[3]);  
            normalizePlane(planes[4]);  
            normalizePlane(planes[5]);  
        }  
    } 
}

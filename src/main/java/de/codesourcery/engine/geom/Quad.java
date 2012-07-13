package de.codesourcery.engine.geom;

import de.codesourcery.engine.linalg.Vector4;

public final class Quad {

    public ITriangle t1;
    public ITriangle t2;
    
    public Quad(ITriangle t1,ITriangle t2)
    {
        this.t1 = t1;
        this.t2 = t2;
    }
    
    public Quad(Vector4 v1 , Vector4 v2,Vector4 v3) 
    {
        final Vector4 v4 = new Vector4( v3.x() , v1.y() , v3.z() );
        t1 = new Triangle( v1 , v2 , v3 );            
        t2 = new Triangle( v1 , v3 , v4 );
    }
}

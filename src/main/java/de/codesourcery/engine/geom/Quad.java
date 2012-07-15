package de.codesourcery.engine.geom;

import de.codesourcery.engine.linalg.Vector4;

public final class Quad {

    public IConvexPolygon t1;
    public IConvexPolygon t2;
    
    public Quad(IConvexPolygon t1,IConvexPolygon t2)
    {
        this.t1 = t1;
        this.t2 = t2;
    }
    
    public static Quad makeQuad(Vector4 v1 , Vector4 v2,Vector4 v3) 
    {
        final Triangle t1 = new Triangle( v1 , v2 , v3 );
        
        final Vector4 v4 = new Vector4( v3.x() , v1.y() , v3.z() );
        Triangle t2 = new Triangle( v1 , v3 , v4 );
        return new Quad( t1 , t2 );
    }
}

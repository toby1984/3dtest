package de.codesourcery.engine.geom;

import de.codesourcery.engine.linalg.Vector4;

public interface IConvexPolygon {
    
    public Vector4 p1();
    
    public Vector4 p2();
    
    public Vector4 p3();
    
    public Vector4[] getAllPoints();
}
package de.codesourcery.engine.geom;

public interface ITriangle {
    
    public Vector4 p1();
    
    public Vector4 p2();
    
    public Vector4 p3();
    
    public Vector4 findFarestVertex(Vector4 reference);
}
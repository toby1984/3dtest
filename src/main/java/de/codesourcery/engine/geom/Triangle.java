package de.codesourcery.engine.geom;

public final class Triangle {
    
    private final Vector4 p1;
    private final Vector4 p2;
    private final Vector4 p3;
    
    public Triangle(Vector4 p1,Vector4 p2,Vector4 p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }
    
    public Vector4 p1() {
        return p1;
    }
    
    public Vector4 p2() {
        return p2;
    }
    
    public Vector4 p3() {
        return p3;
    }    
}
package de.codesourcery.engine.geom;

public final class OcTree<T> {
    
    private OcTreeNode root;
    
    protected static final class OcTreeNode<T> {
        
        protected final Box box;
        protected final T value;
        
        protected OcTreeNode<T> parent;
        protected final OcTreeNode<T>[] children = new OcTreeNode[8];

        public OcTreeNode(Box box, T value)
        {
            this.box = box;
            this.value = value;
        }
        
        public void setChild(int index,OcTreeNode child) {
            this.children[index]=child;
            child.parent = this;
        }
    }
    
    public OcTree() {
        
    }
    
    protected static final class Box 
    {
        protected final float x;
        protected final float y;
        protected final float z;
        protected final float extendX;
        protected final float extendY;
        protected final float extendZ;
        
        public Box(float x, float y, float z, float extendX, float extendY, float extendZ)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            this.extendX = extendX;
            this.extendY = extendY;
            this.extendZ = extendZ;
        }

        public boolean intersects(Box other) {
            // TODO: Implement me
            return false;
        }
        
    }
    
}

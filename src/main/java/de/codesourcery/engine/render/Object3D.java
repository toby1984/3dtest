package de.codesourcery.engine.render;

import static de.codesourcery.engine.linalg.LinAlgUtils.identity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import de.codesourcery.engine.geom.IConvexPolygon;
import de.codesourcery.engine.geom.Triangle;
import de.codesourcery.engine.linalg.BoundingBox;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;
import de.codesourcery.engine.objectmodifiers.IObjectModifier;

public final class Object3D implements Iterable<IConvexPolygon> {
    
    private Matrix modelMatrix = identity();
    
    public static final String METADATA_IDENTIFIER = "_identifier";
    public static final String METADATA_TRANSLATION_MATRIX = "translation_matrix";
    
    /* Vertices of all primitives , vector components are stored in x,y,z,w order. */
    private float[] vertices; //
    
    private float[] textureST;
    
    /* Edges - pointers into the vertices array , each element pair
     * edge[i]/edge[i+1] describes one edge.
     * 
     * Since all polygons need to have a closed shape, the edge from the 
     * polygon's last vertex to the first is NOT stored here and implicitly assumed
     * during rendering.  
     */
    private int[] edges;
    
    private float[] normalVectors;
    
    private final ModifierContainer modifierContainer = new ModifierContainer();
    
    private byte flags;
    
    private BoundingBox boundingBox;
    
    private String textureName;
    
    private final Map<String,Object> metadata = new HashMap<>();
    
    private Object3D parent;
    private final List<Object3D> children = new ArrayList<Object3D>();
    
    protected static final class ModifierNode 
    {
    	protected ModifierNode next;
    	protected final IObjectModifier modifier;
    	
    	public ModifierNode(IObjectModifier m) {
    		this.modifier = m;
    	}
    }
    
    protected final class ModifierContainer {
    	
    	private ModifierNode modifiers = null;
    	private ModifierNode lastModifier = null;
    
        public void addObjectModifier(IObjectModifier modifier) 
        {
        	final ModifierNode newNode = new ModifierNode( modifier );
        	
        	if ( this.modifiers == null ) {
        		this.modifiers = newNode;
        		this.lastModifier = newNode;
        	} else {
        		this.lastModifier.next = newNode;
        		this.lastModifier = newNode;
        	}
        	
        	applyAllModifiers();
        }    	
    	
        public void removeObjectModifier(IObjectModifier modifier) {
        
        	ModifierNode previous = null;
        	ModifierNode current = this.modifiers;
        	while ( current != null ) 
        	{
        		if ( current.modifier == modifier ) {
        			if ( previous == null ) { // removed first node
        				this.modifiers = current.next;
        				if ( this.lastModifier == current ) {
        					this.lastModifier = current.next;
        				}
        			} else { // removed a node inbetween / the last node 
        				previous.next = current.next;
        				if ( this.lastModifier == current ) {
        					this.lastModifier = previous;
        				}
        			}
        			applyAllModifiers();
        			return;
        		}
        		current = current.next;
        	}
        }
        
    	public void tick() 
    	{
    		boolean requiresUpdate = false;
        	ModifierNode m = this.modifiers;
        	while ( m != null ) 
        	{
        		if ( ! m.modifier.isStatic() ) {
        			requiresUpdate |= m.modifier.tick();
        		}
        		m = m.next;
        	}  
        	
        	if ( requiresUpdate ) {
        		applyAllModifiers();
        	}
        	
        	// broadcast tick to child objects
        	for ( Object3D child : children ) {
        		child.tick();
        	}
    	}
    	
    	public void applyAllModifiers() 
    	{
    		if ( hasParent() ) {
    			modelMatrix = getParent().getModelMatrix();
    		} else {
    			modelMatrix = identity();
    		}
    		
    		// apply modifiers
        	ModifierNode m = this.modifiers;
        	while ( m != null ) {
        		m.modifier.apply( Object3D.this ); 
        		m = m.next;
        	}      		
    	}
    }    
    
    public boolean visit(IObject3DVisitor visitor) 
    {
    	if ( ! visitor.visit( this ) ) {
    		return false;
    	}
    	
    	for ( Object3D child : children ) {
    		if ( ! child.visit( visitor ) ) {
    			return false;
    		}
    	}
    	return true;
    }
    
    public static enum RenderingFlag 
    {
    	RENDER_OUTLINE(0),
    	RENDER_WIREFRAME(1),
    	TEXTURES_DISABLED(2),
    	DISABLE_FACE_CULLING(4);
    	
    	private final byte mask;
    	
    	private RenderingFlag(int value) {
    		this.mask= (byte) (1<<value);
    	}
    	
    	public static boolean isFlagSet(RenderingFlag f, byte someValue) {
    		return (someValue & f.mask)!=0;
    	}
    	
       	public boolean isFlagSet(byte someValue) {
    		return (someValue & mask)!=0;
    	}    	

    	public byte setFlag(boolean enable , byte currentValue) {
    		if ( enable ) {
    			return setFlag( currentValue );
    		}
    		return clearFlag( currentValue );
    	}
    	
    	public byte setFlag(byte currentValue) {
    		return (byte) (currentValue | mask);
    	}
    	
    	public byte clearFlag(byte currentValue) {
    		return (byte) (currentValue & ~mask);
    	}    	
    }
    
    public void tick() {
    	this.modifierContainer.tick();
    }
    
    public Object getMetaData(String key) {
    	return metadata.get( key );
    }
    
    public Object setMetaData(String key,Object value) {
    	return metadata.put( key , value );
    }    
    
    public void addObjectModifier(IObjectModifier modifier) {
    	this.modifierContainer.addObjectModifier( modifier );
    }
    
    public Object3D copyInstance(String identifier) 
    {
        final Object3D result = new Object3D();

        for ( Map.Entry<String,Object> entry : this.metadata.entrySet() ) {
        	result.metadata.put( entry.getKey() , entry.getValue() );
        }
        
        result.modelMatrix = new Matrix( this.modelMatrix );

        result.vertices = vertices;
        result.textureST = textureST;
        
        result.edges = edges;
        result.normalVectors = normalVectors;
        result.textureName = textureName;
        
        result.flags = flags;

        result.boundingBox = this.boundingBox != null ? this.boundingBox.createCopy() : null;
        return result;
    }
    
    public Object3D() {
    }
    
    public float[] getNormalVectors() {
		return normalVectors;
	}
    
    public BoundingBox getOrientedBoundingBox() {
		return boundingBox;
	}
    
    public void setIdentifier(String identifier) {
		setMetaData( METADATA_IDENTIFIER , identifier );
	}
    
    public String getIdentifier() {
		return (String) getMetaData( METADATA_IDENTIFIER );
	}
    
    public void setRenderOutline(boolean isRenderOutline) {
		this.flags = RenderingFlag.RENDER_OUTLINE.setFlag( isRenderOutline , flags );
	}
    
    public boolean isRenderOutline() {
		return RenderingFlag.RENDER_OUTLINE.isFlagSet( this.flags );
	}
    
    public void setTexturesDisabled(boolean disabled) {
		this.flags = RenderingFlag.TEXTURES_DISABLED.setFlag( disabled , flags );
	}  
    
    public boolean isTexturesDisabled() {
		return RenderingFlag.TEXTURES_DISABLED.isFlagSet( this.flags );
	}    
    
    public void setFaceCullingDisabled(boolean disabled) {
        this.flags = RenderingFlag.DISABLE_FACE_CULLING.setFlag( disabled , flags );
    }  
    
    public boolean isFaceCullingDisabled() {
        return RenderingFlag.DISABLE_FACE_CULLING.isFlagSet( this.flags );
    }       
    
    public void setRenderWireframe(boolean isRenderWireframe) {
		this.flags = RenderingFlag.RENDER_WIREFRAME.setFlag( isRenderWireframe, flags );
	}
    
    public boolean isRenderWireframe() {
		return RenderingFlag.RENDER_WIREFRAME.isFlagSet( this.flags );
	}
    
    public void setPrimitives(List<Triangle> primitives) { 
    	setPrimitives(primitives , true );
	}

    public void setPrimitives(List<Triangle> primitives,boolean useVertexIndex) 
    {
        System.out.println("Adding "+primitives.size()+" primitives...");

        final int totalVertexCount = primitives.size() * 3 ;
        
        final float[] tmpVertices = new float[ totalVertexCount * 4 ]; // 3 vertices per triangle with 4 components each
        final int[] tmpEdges = new int[ totalVertexCount ]; // 3 edges per triangle with 2 vertices each
        final float[] tmpNormals = new float[ totalVertexCount*4 ]; 
        
        int currentVertex = 0;
        int currentEdge = 0;
        int duplicateVertices = 0;
        int currentNormal = 0;
        
        for ( Triangle t : primitives ) 
        {
        	
        	// calculate normal vector
        	final Vector4 v1 = t.p1().minus( t.p2() );
        	final Vector4 v2 = t.p3().minus( t.p2() );
        	
        	final Vector4 normal = v2.crossProduct( v1 );
        	normal.normalizeInPlace();
        	
        	for ( Vector4 p : t.getAllPoints() ) 
        	{
        		// TODO: PERFORMANCE - findVertex() uses a linear / O(n) search 
        		int vertex1 = useVertexIndex ? findVertex( p , tmpVertices , currentVertex ) : -1;
        		if ( vertex1 == -1 ) 
        		{ // store new vertex
        			vertex1 = currentVertex;
        			tmpVertices[ currentVertex++ ] = p.x();
        			tmpVertices[ currentVertex++ ] = p.y();
        			tmpVertices[ currentVertex++ ] = p.z();
        			tmpVertices[ currentVertex++ ] = p.w();

        			tmpNormals[ currentNormal++ ] = normal.x();
        			tmpNormals[ currentNormal++ ] = normal.y();
        			tmpNormals[ currentNormal++ ] = normal.z();
        			tmpNormals[ currentNormal++ ] = normal.w();        			
        		} else {
        			duplicateVertices++;
        		}
            
        		// store edge
        		tmpEdges[ currentEdge++ ] = vertex1/4;
        	}
        }
        
        this.vertices = ArrayUtils.subarray( tmpVertices , 0 , currentVertex );
        this.edges = tmpEdges;
        this.normalVectors = ArrayUtils.subarray( tmpNormals, 0 , currentNormal );
        
        System.out.println("Vertex array size: "+this.vertices.length );
        System.out.println("Normals array size: "+this.normalVectors.length );
        System.out.println("Primitives: "+primitives.size());
        System.out.println("Vertices: "+totalVertexCount+" (removed duplicates: "+duplicateVertices+")");
        
        calculateBoundingBox();
    }
    
    private void calculateBoundingBox() {
       	this.boundingBox = BoundingBoxGenerator.calculateOrientedBoundingBox( this );
    }
    
    public int getPointCount() {
        return vertices != null ? vertices.length / 4 : 0;
    }

    public float[] getVertices()
    {
        return vertices;
    }
    
    public Iterator<Vector4> getVertexIterator() {
    	
    	return new Iterator<Vector4>() {
			
    		private final Vector4 result = new Vector4();
    		
    		private int currentIndex = 0;
    		
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public Vector4 next() 
			{
				result.setData( vertices , currentIndex );
				currentIndex+=4;
				return result;
			}
			
			@Override
			public boolean hasNext() {
				return currentIndex < vertices.length;
			}
		};
    }
    
    public int[] getEdges()
    {
        return edges;
    }
    
    private int findVertex(Vector4 p,float[] array,int maxIndex) 
    {
        final float x = p.x();
        final float y = p.y();
        final float z = p.z();
        final float w = p.w();
        
        for ( int i = 0 ; i < maxIndex ; i +=4 ) 
        {
            if ( array[i] == x &&
                 array[i+1] == y &&
                 array[i+2] == z &&
                 array[i+3] == w ) 
            {
                return i;
            }
        }
        return -1;
    }
    
    public boolean hasParent() {
    	return parent != null;
    }
    
    public Matrix getModelMatrix() 
    {
        return this.modelMatrix;
    }

    private final class MyTriangle implements IConvexPolygon 
    {
    	private final Vector4[] threePoints = new Vector4[]{new Vector4(0,0,0), new Vector4(0,0,0),new Vector4(0,0,0) };
        
    	private Vector4[] points = threePoints;
    	
        @Override
        public Vector4 p1()
        {
            return points[0];
        }

        @Override
        public Vector4 p2()
        {
            return points[1];
        }

        @Override
        public Vector4 p3()
        {
            return points[2];
        }
        
        public void setVertices(int firstVerticeIndex) 
        {
			points = threePoints;
			
            points[0].setData( vertices , edges[ firstVerticeIndex ] * 4 );
            points[1].setData( vertices , edges[ firstVerticeIndex + 1 ] * 4 );
            points[2].setData( vertices , edges[ firstVerticeIndex + 2 ] * 4 );
        }
        
        @Override
        public String toString()
        {
            return p1()+" -> "+p2()+" -> "+p3()+" -> "+p1();
        }

		@Override
		public Vector4[] getAllPoints() {
			return points;
		}

    };    
    
    @Override
    public Iterator<IConvexPolygon> iterator()
    {
        return new Iterator<IConvexPolygon>() {

            private int currentVertexIndex = 0;
            private final int edgeCount = edges.length;
            
            private final MyTriangle t = new MyTriangle();
            
            @Override
            public boolean hasNext()
            {
                return currentVertexIndex < edgeCount;
            }

            @Override
            public IConvexPolygon next()
            {
                t.setVertices( currentVertexIndex );
                currentVertexIndex+=3;
                return t;
            }

            @Override
            public void remove()
            {
                throw new UnsupportedOperationException("remove() not supported");
            }
         };
    }
    
    @Override
    public String toString() {
    	return getIdentifier() != null ? getIdentifier() : "<not object identifier set>";
    }
    
    public List<Object3D> getChildren() {
		return children;
	}
 
    public boolean hasChildren() {
    	return ! children.isEmpty();
    }
    
    private void setParent(Object3D parent) {
    	this.parent = parent;
    	modifierContainer.applyAllModifiers();    	
    }
    
    public Object3D getParent() {
    	return this.parent;
    }
    
    public void addChild(Object3D child) 
    {
    	this.children.add( child );
    	child.setParent( this );
    }

	public void setPrimitives(float[] vertexData, int[] edges2, float[] normalsData,float[] textureST) 
	{
		this.vertices = vertexData;
		this.edges = edges2;
		this.normalVectors = normalsData;
		this.textureST = textureST;
		calculateBoundingBox();
	}

	public String getTextureName() {
		return textureName;
	}
	
	public void setTextureName(String textureName) {
		this.textureName = textureName;
	}

	public float[] getTexture2DCoords() {
		return textureST;
	}
	
	public void setModelMatrix(Matrix modelMatrix) {
		this.modelMatrix = modelMatrix;
	}
}
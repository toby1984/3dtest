package de.codesourcery.engine.render;

import static de.codesourcery.engine.linalg.LinAlgUtils.identity;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import de.codesourcery.engine.geom.IConvexPolygon;
import de.codesourcery.engine.linalg.BoundingBox;
import de.codesourcery.engine.linalg.Matrix;
import de.codesourcery.engine.linalg.Vector4;

public final class Object3D implements Iterable<IConvexPolygon> {
    
    private boolean recalculateModelMatrix = false;
    
    private Matrix thisModelMatrix = identity();
    private Matrix cachedModelMatrix = identity();
    
    public static final String METADATA_IDENTIFIER = "_identifier";
    public static final String METADATA_TRANSLATION_MATRIX = "translation_matrix";
    
    /* Vertices of all primitives , vector components are stored in x,y,z,w order. */
    private float[] vertices; //
    
    /* Edges - pointers into the vertices array , each element pair
     * edge[i]/edge[i+1] describes one edge.
     * 
     * Since all polygons need to have a closed shape, the edge from the 
     * polygon's last vertex to the first is NOT stored here and implicitly assumed
     * during rendering.  
     */
    private int[] edges;
    
    /* colors of each primitive */
    private int[] colors;  
    
    /* number of vertices each primitive uses  */ 
    private byte[] vertexCounts; 
    
    private byte flags;
    
    private BoundingBox boundingBox;
    
    private final Map<String,Object> metadata = new HashMap<>();
    
    private Object3D parent;
    private final List<Object3D> children = new ArrayList<Object3D>();
    
    public static enum RenderingFlag 
    {
    	RENDER_OUTLINE(0),
    	RENDER_WIREFRAME(1);
    	
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
    
    public Object getMetaData(String key) {
    	return metadata.get( key );
    }
    
    public Object setMetaData(String key,Object value) {
    	return metadata.put( key , value );
    }    
    
    public Object3D createCopy(String identifier) 
    {
        final Object3D result = new Object3D();

        for ( Map.Entry<String,Object> entry : this.metadata.entrySet() ) {
        	result.metadata.put( entry.getKey() , entry.getValue() );
        }
        
        result.recalculateModelMatrix = this.recalculateModelMatrix;
        
        result.thisModelMatrix = new Matrix( this.thisModelMatrix );
        result.cachedModelMatrix = this.cachedModelMatrix != null ? new Matrix( this.cachedModelMatrix ) : null;

        result.vertices = vertices;
        result.edges = edges;
        
        result.colors = colors;
        
        result.vertexCounts = vertexCounts;
        result.flags = flags;

        result.boundingBox = this.boundingBox != null ? this.boundingBox.createCopy() : null;
        return result;
    }
    
    public Object3D() {
    }
    
    public BoundingBox getOrientedBoundingBox() {
		return boundingBox;
	}
    
    /**
     * Set foreground color (must be called <b>after</b> {@link #setPrimitives(List)}).
     * 
     */
    public void setForegroundColor(Color c) 
    {
    	int value = c.getRGB();
    	if ( this.colors != null ) {
    		final int len = this.colors.length;
    		for ( int i = 0 ; i < len ; i++ ) {
    			colors[i] = value;
    		}
    	} else {
    		System.err.println("setColor() invoked on "+this+" without calling setTriangles() first ?");
    	}
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
    
    public void setRenderWireframe(boolean isRenderWireframe) {
		this.flags = RenderingFlag.RENDER_WIREFRAME.setFlag( isRenderWireframe, flags );
	}
    
    public boolean isRenderWireframe() {
		return RenderingFlag.RENDER_WIREFRAME.isFlagSet( this.flags );
	}
    
    public void setPrimitives(List<? extends IConvexPolygon> primitives) 
    {
        System.out.println("Adding "+primitives.size()+" primitives...");
        int totalVertexCount = 0;
        for ( IConvexPolygon p : primitives ) {
        	totalVertexCount += p.getVertexCount();
        }
        final float[] tmpVertices = new float[ totalVertexCount * 4 ]; // 3 vertices per triangle with 4 components each
        final int[] tmpEdges = new int[ totalVertexCount ]; // 3 edges per triangle with 2 vertices each
        final int[] tmpColors = new int[ primitives.size() ];
        final byte[] tmpVertexCounts = new byte[ primitives.size() ];
        
        int currentVertex = 0;
        int currentEdge = 0;
        int duplicateVertices = 0;
        int currentPrimitive = 0;
        
        for ( IConvexPolygon t : primitives ) 
        {
        	for ( Vector4 p : t.getAllPoints() ) 
        	{
        		// TODO: PERFORMANCE - findVertex() uses a linear / O(n) search 
        		int vertex1 = findVertex( p , tmpVertices , currentVertex );
        		if ( vertex1 == -1 ) { // store new vertex
        			vertex1 = currentVertex;
        			tmpVertices[ currentVertex++ ] = p.x();
        			tmpVertices[ currentVertex++ ] = p.y();
        			tmpVertices[ currentVertex++ ] = p.z();
        			tmpVertices[ currentVertex++ ] = p.w();
        		} else {
        			duplicateVertices++;
        		}
            
        		// store edge
        		tmpEdges[ currentEdge++ ] = vertex1;
        	}
        	
        	tmpVertexCounts[ currentPrimitive ] = t.getVertexCount();
            tmpColors[ currentPrimitive ] = t.getColor();
            currentPrimitive++;
        }
        
        this.vertices = ArrayUtils.subarray( tmpVertices , 0 , currentVertex );
        this.edges = tmpEdges;
        this.colors = tmpColors;
        this.vertexCounts = tmpVertexCounts;
        System.out.println("Primitives: "+primitives.size());
        System.out.println("Vertices: "+totalVertexCount+" (removed duplicates: "+duplicateVertices+")");
        
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
    
    public void setModelMatrix(Matrix m) 
    {
        this.thisModelMatrix = m;
        this.cachedModelMatrix = m;
        
        if ( hasParent() ) 
        {
        	this.cachedModelMatrix = m.multiply( getParent().getModelMatrix() );
        }
        
        for ( Object3D child : children ) {
        	child.markModelMatrixForRecalculation();
        }
    }      
    
    public void markModelMatrixForRecalculation() {
        recalculateModelMatrix = true;
    }
    
    public boolean hasParent() {
    	return parent != null;
    }
    
    public Matrix getModelMatrix() 
    {
        if ( recalculateModelMatrix || this.cachedModelMatrix == null ) 
        {
            if ( thisModelMatrix == null && ! hasParent( ) ) {
                throw new IllegalStateException("Object "+getIdentifier()+" has no model matrix set and no parent ?");
            }
            this.cachedModelMatrix = thisModelMatrix;
            if ( hasParent() ) {
                this.cachedModelMatrix = this.cachedModelMatrix.multiply( getParent().getModelMatrix() );
            }
        }
        return this.cachedModelMatrix;
    }
    
    private final class MyTriangle implements IConvexPolygon 
    {
    	private final Vector4[] threePoints = new Vector4[]{new Vector4(0,0,0), new Vector4(0,0,0),new Vector4(0,0,0) };
    	private final Vector4[] fourPoints =  new Vector4[]{new Vector4(0,0,0), new Vector4(0,0,0),new Vector4(0,0,0) , new Vector4(0,0,0) };
        
    	private Vector4[] points = threePoints;
    	private byte vertexCount;
    	
        private int color;
        
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
        public int getColor() {
        	return color;
        }
        
        @Override
        public Vector4 p3()
        {
            return points[2];
        }
        
        public void setVerticesAndColor(int firstVerticeIndex,byte vertexCount , int color) 
        {
        	switch( vertexCount ) {
        		case 3:
        			points = threePoints;
                    points[0].setData( vertices , edges[ firstVerticeIndex ] );
                    points[1].setData( vertices , edges[ firstVerticeIndex + 1 ]);
                    points[2].setData( vertices , edges[ firstVerticeIndex + 2 ] );
                    break;
        		case 4:
        			points = fourPoints;
                    points[0].setData( vertices , edges[ firstVerticeIndex ] );
                    points[1].setData( vertices , edges[ firstVerticeIndex + 1 ]);
                    points[2].setData( vertices , edges[ firstVerticeIndex + 2 ] );
                    points[3].setData( vertices , edges[ firstVerticeIndex + 3 ] );
                    break;
        		default:
        			throw new IllegalArgumentException("Unsupported vertex count "+vertexCount);
        	}
        	
        	this.vertexCount = vertexCount;
            this.color = color; 
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

		@Override
		public void setColor(int color) {
		}

		@Override
		public byte getVertexCount() {
			return vertexCount;
		}
    };    
    
    @Override
    public Iterator<IConvexPolygon> iterator()
    {
        return new Iterator<IConvexPolygon>() {

        	private int currentPrimitiveIndex = 0;
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
            	final byte vertices = vertexCounts[ currentPrimitiveIndex ];
            	
                t.setVerticesAndColor( currentVertexIndex , vertices , colors[ currentPrimitiveIndex ] );
                
                currentVertexIndex+=vertices;
                currentPrimitiveIndex+=1;
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
    }
    
    public Object3D getParent() {
    	return this.parent;
    }
    
    public void addChild(Object3D child) 
    {
    	this.children.add( child );
    	child.setParent( this );
    	child.markModelMatrixForRecalculation();
    }
}
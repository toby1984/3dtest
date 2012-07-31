package de.codesourcery.engine.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import de.codesourcery.engine.render.Object3D;

public class PLYReader {

	 public static void main(String[] args) throws IOException, ParseException {
		
		 final String input = "ply\n"+
				 "format ascii 1.0\n"+
				 "comment Created by Blender 2.62 (sub 0) - www.blender.org, source file: ''\n"+
				 "element vertex 8\n"+
				 "property float x\n"+
				 "property float y\n"+
				 "property float z\n"+
				 "property float nx\n"+
				 "property float ny\n"+
				 "property float nz\n"+
				 "element face 6\n"+
				 "property list uchar uint vertex_indices\n"+
				 "end_header\n"+
				 "1.000000 1.000000 -1.000000 0.577349 0.577349 -0.577349\n"+
				 "1.000000 -1.000000 -1.000000 0.577349 -0.577349 -0.577349\n"+
				 "-1.000000 -1.000000 -1.000000 -0.577349 -0.577349 -0.577349\n"+
				 "-1.000000 1.000000 -1.000000 -0.577349 0.577349 -0.577349\n"+
				 "1.000000 0.999999 1.000000 0.577349 0.577349 0.577349\n"+
				 "-1.000000 1.000000 1.000000 -0.577349 0.577349 0.577349\n"+
				 "-1.000000 -1.000000 1.000000 -0.577349 -0.577349 0.577349\n"+
				 "0.999999 -1.000001 1.000000 0.577349 -0.577349 0.577349\n"+
				 "4 0 1 2 3\n"+
				 "4 4 5 6 7\n"+
				 "4 0 4 7 1\n"+
				 "4 1 7 6 2\n"+
				 "4 2 6 5 3\n"+
				 "4 4 0 3 5";
				 		 
		 final Object3D object3d = new PLYReader().read( new ByteArrayInputStream( input.getBytes() ) );
	}

	public Object3D read(File file) throws IOException , ParseException
	{
		try ( InputStream in = new FileInputStream(file); ) {
			return read( in );
		}
	}

	protected static final class Vertex {
		protected float x;
		protected float y;
		protected float z;

		protected float nx;
		protected float ny;
		protected float nz;
		
		protected float s;
		protected float t;
		
		public int writePosition(float[] array,int offset) {
			array[offset ] = x;
			array[offset+1] = y;
			array[offset+2] = z;
			array[offset+3] = 1;
			return 4;
		}
		
		public int writeTextureST(float[] array,int offset) {
			array[offset ] = s;
			array[offset+1] = t;
			return 2;
		}		
		
		public int writeNormal(float[] array,int offset) 
		{
			array[offset ] = nx;
			array[offset+1] = ny;
			array[offset+2] = nz;
			array[offset+3] = 0;
			return 4;
		}		
		
		@Override
		public String toString() {
			return x+" "+y+" "+z+" "+nx+" "+ny+" "+nz+" "+s+" "+t;
		}
	}

	protected static final class Polygon 
	{
		protected int[] edges;

		public Polygon(int[] edges) {
			this.edges = edges;
		}
	}

	protected interface VertexAttributeParser {
		public void parse(String value,Vertex currentVertex);
	}
	
	protected static final class TextureSCoordinateParser implements VertexAttributeParser {
		@Override
		public void parse(String value, Vertex currentVertex) {
			currentVertex.s = Float.parseFloat( value );
		}
	}
	
	protected static final class TextureTCoordinateParser implements VertexAttributeParser {
		@Override
		public void parse(String value, Vertex currentVertex) {
			currentVertex.t = Float.parseFloat( value );
		}
	}	

	protected static final class XCoordinateParser implements VertexAttributeParser {
		@Override
		public void parse(String value, Vertex currentVertex) {
			currentVertex.x = Float.parseFloat( value );
		}
	}

	protected static final class YCoordinateParser implements VertexAttributeParser 
	{
		@Override
		public void parse(String value, Vertex currentVertex) {
			currentVertex.y = Float.parseFloat( value );
		}
	}	

	protected static final class ZCoordinateParser implements VertexAttributeParser 
	{
		@Override
		public void parse(String value, Vertex currentVertex) {
			currentVertex.z = Float.parseFloat( value );
		}
	}	

	protected static final class NXCoordinateParser implements VertexAttributeParser {
		@Override
		public void parse(String value, Vertex currentVertex) {
			currentVertex.nx = Float.parseFloat( value );
		}
	}

	protected static final class NYCoordinateParser implements VertexAttributeParser 
	{
		@Override
		public void parse(String value, Vertex currentVertex) {
			currentVertex.ny = Float.parseFloat( value );
		}
	}	

	protected static final class NZCoordinateParser implements VertexAttributeParser 
	{
		@Override
		public void parse(String value, Vertex currentVertex) {
			currentVertex.nz = Float.parseFloat( value );
		}
	}		

	protected VertexAttributeParser createAttributeParser(String propertyName,int lineNumber) throws ParseException {
		switch( propertyName ) 
		{
			case "x":
				return new XCoordinateParser();
			case "y":
				return new YCoordinateParser();
			case "z":
				return new ZCoordinateParser();		
			case "nx":
				return new NXCoordinateParser();
			case "ny":
				return new NYCoordinateParser();
			case "nz":
				return new NZCoordinateParser();			
			case "t":
				return new TextureTCoordinateParser();
			case "s":
				return new TextureSCoordinateParser();				
			default:
				throw new ParseException("Internal error, no parser for property '"+propertyName+"'",lineNumber);
		}
	}
	
	protected static final class CountingBufferedReader extends BufferedReader {
		
		private int lastLineNumber=0;
		public CountingBufferedReader(Reader reader) {
			super(reader);
		}
		
		@Override
		public String readLine() throws IOException {
			final String result = super.readLine();
			if ( result != null ) {
				lastLineNumber++;
			}
			return result;
		}
		
		public int lastLineNumber() {
			return lastLineNumber;
		}
	}
	
	private static boolean containsParser(List<VertexAttributeParser> parsers , Class<?>... classes) 
	{
		for ( VertexAttributeParser p : parsers ) 
		{
			for ( Class<?> clazz : classes ) {
				if ( clazz.isAssignableFrom( p.getClass() ) ) {
					return true;
				}
			}
		}
		return false;
	}
	
	public Object3D readFromClasspath(String path) throws IOException,ParseException 
	{
    	final InputStream in = getClass().getResourceAsStream("/models/sphere.ply");
    	if ( in == null ) {
    		throw new IOException("Failed to load '"+path+"' from classpath");
    	}
    	return read( in );		
	}

	public Object3D read(InputStream in) throws IOException , ParseException {

		try ( CountingBufferedReader reader = new CountingBufferedReader(new InputStreamReader(in)); ) 
		{
			String line = reader.readLine();
			if ( ! "ply".equals( line ) ) {
				throw new IllegalArgumentException("Input is not a .ply file");
			}
			int vertexCount = -1;
			int surfaceCount = -1;

			final List<VertexAttributeParser> parsers = new ArrayList<VertexAttributeParser>();
			boolean parsingVertexProperties = false;
			while( ( line = reader.readLine() ) != null ) 
			{
				if ( line.startsWith("property" ) && parsingVertexProperties ) 
				{
					final String[] split = line.split(" ");
					if ( split.length != 3 ) {
						throw new ParseException("Failed to parse property line '"+line+"'",reader.lastLineNumber());
					}
					final String unit = split[1];
					final String property = split[2];
					if ( ! "float".equals( unit ) ) {
						throw new ParseException("Parse error, unsupported unit '"+unit+"' in property line '"+line+"'", reader.lastLineNumber());
					}
					parsers.add( createAttributeParser( property , reader.lastLineNumber() ) );
					continue;
				} 

				if ( line.startsWith("element vertex" ) ) {
					vertexCount = Integer.parseInt( line.substring( "element vertex".length() ).trim() );
					parsingVertexProperties = true;
					continue;
				} 

				parsingVertexProperties = false;
				
				if ( line.startsWith("element face" ) ) {
					surfaceCount = Integer.parseInt( line.substring( "element face".length() ).trim() );
				} else if ( line.startsWith("end_header") ) {
					break;
				}
			}

			if ( line == null ) {
				throw new ParseException("Unexpected EOF",reader.lastLineNumber());
			}
			if ( surfaceCount == -1 ) {
				throw new ParseException("Parse error, unable to determine face count",reader.lastLineNumber());
			}
			
			if ( vertexCount == -1 ) {
				throw new ParseException("Parse error, unable to determine face count",reader.lastLineNumber());
			}
			
			System.out.println("Loading data from .ply file ...");
			System.out.println("Vertices: "+vertexCount+" / surfaces: "+surfaceCount );
			
			final boolean hasNormalVectors = containsParser( parsers , NXCoordinateParser.class , NYCoordinateParser.class , NZCoordinateParser.class );

			// parse vertices
			List<Vertex> vertices = new ArrayList<>();
			for ( int i = 0 ; i < vertexCount ; i++ ) 
			{
				line = reader.readLine();
				if ( line == null ) {
					throw new ParseException("Unexpected EOF while reading vertices",reader.lastLineNumber());
				}

				final Vertex currentVertex = new Vertex();
				final String[] split = line.split(" ");
				if ( split.length != parsers.size() ) {
					throw new ParseException("Parse error, line has "+split.length+" tokens but we have "+
				parsers.size()+" parsers? : "+line,reader.lastLineNumber());
				}
				int currentTokenIndex = 0;
				for ( VertexAttributeParser parser : parsers ) {
					parser.parse( split[ currentTokenIndex++ ] , currentVertex);
				}
				vertices.add( currentVertex );
			}

			// parse faces
			int totalEdgeCount = 0;
			final List<Polygon> polygons = new ArrayList<Polygon>();
			for ( int i = 0 ; i < surfaceCount ; i++ ) {
				line = reader.readLine();
				if ( line == null ) {
					throw new ParseException("Unexpected EOF while reading surface definitions",reader.lastLineNumber());
				}
				final String[] split = line.split(" ");
				final int edgeCount = Integer.parseInt( split[0] );
				if ( edgeCount != 3 && edgeCount != 4 ) {
					throw new ParseException("Surface has unsupported edge count "+edgeCount,reader.lastLineNumber());
				}
				final int[] edges = new int[ edgeCount ];
				for ( int counter = 0 ; counter < edgeCount ; counter++) {
					edges[counter] = Integer.parseInt( split[counter+1].trim() );
				}
				
				if ( edgeCount == 3 ) {
					polygons.add( new Polygon( edges ) );
					totalEdgeCount += 3;
				} else if ( edgeCount == 4 ) {
					// split quad into two triangles
					final int[] t1 = new int[] { edges[2] , edges[1] , edges[0] , };   
					final int[] t2 = new int[] { edges[3] , edges[2] , edges[0] , };
					polygons.add( new Polygon( t1 ) );
					polygons.add( new Polygon( t2 ) );
					totalEdgeCount += 6;
				} else {
					throw new RuntimeException("Unreachable code reached");
				}
			}
			
			System.out.println("Triangle count: "+polygons.size());
			System.out.println("Line count: "+polygons.size()*3);
			
			// assembly edge index array
			final int[] edges = new int[ totalEdgeCount ];
			int edgePtr = 0;
			for ( Polygon p : polygons ) 
			{
				for ( int idx : p.edges ) {
					edges[edgePtr++] = idx;
				}
			}
			
			// assemble normal/vertex arrays of Object3D
			final float[] normalsData = new float[ vertexCount * 4 ];
			final float[] textureST = new float[ vertexCount * 2 ];
			final float[] vertexData = new float[ vertexCount * 4 ];
			int vertexPtr = 0;
			int normalPtr = 0;
			int texturePtr = 0;
			for ( Vertex v : vertices ) 
			{
				vertexPtr += v.writePosition( vertexData , vertexPtr  );
				texturePtr += v.writeTextureST( textureST , texturePtr );
				normalPtr += v.writeNormal( normalsData , normalPtr );
			}
			
			final Object3D object = new Object3D();
			object.setPrimitives( vertexData , edges , normalsData , textureST );
			return object;
		}
	}
}

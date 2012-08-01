package de.codesourcery.engine.geom;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;

import de.codesourcery.engine.linalg.Vector4;
import de.codesourcery.engine.opengl.TextureManager;
import de.codesourcery.engine.render.Object3D;

public class TerrainGenerator
{
    private final TextureManager textureManager;

    private final AtomicLong randomTextureId = new AtomicLong(1);
    
    public TerrainGenerator(TextureManager textureManager) {
        this.textureManager = textureManager;
    }
    
    public static void main(String[] args)
    {
        final TextureManager tex = new TextureManager();
        Object3D terrain = new TerrainGenerator(tex).generateTerrain( 3 , 1 , 1 , false , System.currentTimeMillis()  );
    }
    
    private String generateTexture(float[] heightMap,int heightMapSize) 
    {
        final ByteArrayOutputStream out = createTextureAsPNG(heightMap, heightMapSize);
        
        try {
            final String textureIdentifier = "rnd_"+Long.toString( randomTextureId.incrementAndGet() );
            textureManager.createTexture( textureIdentifier , new ByteArrayInputStream( out.toByteArray() ) );
            return textureIdentifier;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ByteArrayOutputStream createTextureAsPNG(float[] heightMap, int heightMapSize)
    {
        // create color gradient
        Vector4 green = new Vector4(0,8f,0);
        Vector4 green2 = new Vector4(0,0.5f,0);        
        Vector4 brown = new Vector4( 158f/255f ,69f/255f ,14f/255f);
        Vector4 grey = new Vector4(0.5f,0.5f,0.5f);        
        Vector4 white = new Vector4(1,1,1);
        
        final int[] colorRange = generateColorGradient( new Vector4[] { green,green2,brown,grey,white } );
        
        final BufferedImage img = new BufferedImage(heightMapSize,heightMapSize,BufferedImage.TYPE_INT_RGB);
        for ( int z1 = 0 ; z1 < heightMapSize ; z1++ ) 
        {        
            for ( int x1 = 0 ; x1 < heightMapSize ; x1++ ) 
            {
                float height = heightMap[ x1 + z1 * heightMapSize ];
                img.setRGB( x1 , z1 , colorRange[ (int) (height * 255) ] );
            }
        }
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write( img , "PNG" , out );
            FileOutputStream tmpOut = new FileOutputStream("/tmp/texture.png");
            tmpOut.write( out.toByteArray() );
            tmpOut.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return out;
    }
    
    private int[] generateColorGradient(Vector4[] colors) 
    {
        final int[][] ranges = new int[colors.length][];
        final int increment = 256/colors.length;
        int totalElements = 0;
        for( int i = 0 ; i < colors.length -1 ; i++) 
        {
            ranges[i] = interpolateColor(colors[i],colors[i+1],increment);
            totalElements+=increment;
        }
        
        // fill-up range with final color of gradient
        if ( totalElements < 256 ) 
        {
            final int delta = 256 -totalElements;
            int[] tmp = new int[ delta ];
            ranges[ranges.length-1] = tmp;
            final Vector4 lastColor = colors[ colors.length - 1 ];
            for ( int i = 0 ; i < delta ; i++ ) {
                tmp[i]=lastColor.toRGB();
            }
        }

        int dstPos = 0;
        final int[] colorRange = new int[256];
        for ( int[] range : ranges ) 
        {
            if ( range != null ) {
                System.arraycopy(range,0,colorRange,dstPos, range.length);
                dstPos+=range.length;
            }
        }      
        return colorRange;
    }
    
    private int[] interpolateColor(Vector4 start,Vector4 end,int elements) {
        
        final float incR = (end.r() - start.r())/elements;
        final float incG = (end.g() - start.g())/elements;
        final float incB = (end.b() - start.b())/elements;
        
        final int[] result = new int[ elements ];
        Vector4 current = new Vector4(start);
        for ( int i = 0 ; i < elements ; i++ ) 
        {
            result[i] = current.toRGB();
            current.r( current.r() + incR );
            current.g( current.g() + incG );
            current.b( current.b() + incB );
        }
        return result;
    }

    public Object3D generateTerrain(int heightMapSize,int maxY,float tileSizeInPixels,boolean generateTexture, long seed) 
    {
        final float[] heightMap = generateHeightMap( heightMapSize , seed );

        final int tiles = heightMapSize*heightMapSize;
        final int vertexCount = (heightMapSize+1)*(heightMapSize+1);
        final int triangleCount = heightMapSize*heightMapSize*2; // 2 triangles per quad

        final int[] edges = new int[triangleCount*3]; // 3 edges per triangle
        final float[] coords = new float[ vertexCount * 4 ];
        final float[] normals = new float[ vertexCount * 4 ];

        System.out.println("Generating terrain....");
        System.out.println("Seed          : "+seed);
        System.out.println("Heightmap size: "+heightMapSize);
        System.out.println("Tiles         : "+tiles);
        System.out.println("Vertices      : "+vertexCount);
        System.out.println("Triangles     : "+triangleCount);

        int vertexPtr = 0;
        for ( int z1 = 0 ; z1 < heightMapSize ; z1++ ) 
        {        
            for ( int x1 = 0 ; x1 < heightMapSize ; x1++ ) 
            {
                final float y1 = heightMap[ z1*heightMapSize + x1 ] * maxY;

                coords[vertexPtr++] = tileSizeInPixels*x1;
                coords[vertexPtr++] = y1;
                coords[vertexPtr++] = tileSizeInPixels*z1;
                coords[vertexPtr++] = 1;
            }
        }

        // generate edges + normals 
        int edgePtr = 0;
        for ( int z1 = 0 ; z1 < heightMapSize-1 ; z1++ ) 
        {        
            for ( int x1 = 0 ; x1 < heightMapSize-1 ; x1++ ) 
            {
                final int x2 = x1+1;
                final int z2 = z1;

                final int x3 = x1+1;
                final int z3 = z1+1;

                final int x4 = x1;
                final int z4 = z1+1;

                int p1Idx = x1+(z1*heightMapSize);
                int p2Idx = x2+(z2*heightMapSize);
                int p3Idx = x3+(z3*heightMapSize);
                int p4Idx = x4+(z4*heightMapSize);

                // triangle p1->p4->p3
                Vector4 p1 = new Vector4(coords,p1Idx*4);
                Vector4 p4 = new Vector4(coords,p4Idx*4);
                Vector4 p3 = new Vector4(coords,p3Idx*4);
                Vector4 normal = calcNormal(p1,p4,p3);

                normals[p1Idx*4]=normal.x();
                normals[p1Idx*4 +1 ]=normal.y();
                normals[p1Idx*4 +2 ]=normal.z();
                normals[p1Idx*4 +3]=0;

                normals[p2Idx*4]=normal.x();
                normals[p2Idx*4 +1 ]=normal.y();
                normals[p2Idx*4 +2 ]=normal.z();
                normals[p2Idx*4 +3]=0;

                normals[p3Idx*4]=normal.x();
                normals[p3Idx*4 +1 ]=normal.y();
                normals[p3Idx*4 +2 ]=normal.z();
                normals[p3Idx*4 +3]=0;

                normals[p4Idx*4]=normal.x();
                normals[p4Idx*4 +1 ]=normal.y();
                normals[p4Idx*4 +2 ]=normal.z();
                normals[p4Idx*4 +3]=0;                

                edges[ edgePtr++ ] = p1Idx;
                edges[ edgePtr++ ] = p4Idx;
                edges[ edgePtr++ ] = p3Idx;

                // triangle p1->p3->p2
                edges[ edgePtr++ ] = p1Idx;
                edges[ edgePtr++ ] = p3Idx;
                edges[ edgePtr++ ] = p2Idx;                
            }
        }

        // translate grid so it's centered around the origin
        final float extend = heightMapSize*tileSizeInPixels;
        final float offset = extend/2f;
        for ( int ptr = 0 ; ptr < coords.length ; ptr+=4) {
            coords[ptr] = coords[ptr] - offset; // x
            coords[ptr+2] = coords[ptr+2] - offset; // z
        }

        // assemble Object3D
        final Object3D result = new Object3D();
        
        float[] textureCoords = null;
        if ( generateTexture ) {
            // create texture coordinates
            textureCoords = new float[ vertexCount*2 ];
            int ptr = 0;
            float inc = 1f/heightMapSize;
            for ( float z1 = 0 ; z1 < 1 ; z1+=inc ) 
            {        
                for ( float x1 = 0 ; x1 < 1 ; x1 += inc ) 
                {
                    textureCoords[ptr++]=x1;
                    textureCoords[ptr++]=z1;
                }
            }
            result.setTextureName( generateTexture( heightMap , heightMapSize  ) );
        }
        
        //
        result.setPrimitives( coords , edges , normals , textureCoords );
        //        result.setRenderWireframe( true );
        //        result.setTexturesDisabled( true );
        return result;
    }

    private Vector4 calcNormal(Vector4 p1,Vector4 p2,Vector4 p3) {
        Vector4 v1 = p1.minus( p2 );
        Vector4 v2 = p3.minus( p2 );
        Vector4 normal = v1.crossProduct( v2 );
        normal.normalizeInPlace();
        return normal;
    }

    private float[] generateHeightMap(int heightMapSize,long seed) 
    {
        // generate some white noise
        final float[][] whiteNoise = FractalNoise.generateWhiteNoise( heightMapSize , heightMapSize , seed);
        final float[][] perlinNoise = FractalNoise.generateFractalNoise( whiteNoise, 6 , 0.6f );

        float[] map = new float[ heightMapSize * heightMapSize ];

        int ptr = 0;
        for ( int x = 0 ; x < heightMapSize ;x++ ) {
            for ( int y = 0 ; y < heightMapSize ; y++ ) 
            {
                map[ ptr++ ] = perlinNoise[x][y];
            }
        }
        return map;
    }
}

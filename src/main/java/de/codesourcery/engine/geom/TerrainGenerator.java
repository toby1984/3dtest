package de.codesourcery.engine.geom;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;

import de.codesourcery.engine.linalg.Vector4;
import de.codesourcery.engine.opengl.TextureManager;
import de.codesourcery.engine.render.Object3D;

public class TerrainGenerator
{
    private static final boolean DEBUG_WRITE_TEXTURE_TO_TMPFILE = false;

    private final TextureManager textureManager;

    private final AtomicLong randomTextureId = new AtomicLong(1);

    public TerrainGenerator(TextureManager textureManager) {
        this.textureManager = textureManager;
    }

    public static void main(String[] args)
    {
//        final TextureManager tex = new TextureManager();
//        Object3D terrain = new TerrainGenerator(tex).generateTerrain( 4 , 1 , 1 , false , System.currentTimeMillis()  );

        float min = Integer.MAX_VALUE;
        float max = Integer.MIN_VALUE;

        for ( int i = 0 ; i < 100 ; i++ ) {
        PerlinNoise test = new PerlinNoise( System.currentTimeMillis() );
            for ( float x = 0.3f ; x < 256 ; x+=0.5f) 
            {
                for ( float y = 0.3f ; y < 256 ; y+=0.5f) {
                    float value = test.noise2( x,y);
                    if ( value < min ) {
                        min = value;
                    }
                    if ( value > max ) {
                        max = value;
                    }
                }
            }
        }
        System.out.println("min/max= "+min+" / "+max+" / delta = "+( max - min ) );
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
        Vector4 blue1=   new Vector4( 0 , 0f  , 0.8f);
        Vector4 blue2=   new Vector4( 0 , 0f  , 0.5f);        
        Vector4 green =  new Vector4( 0 , 0.8f, 0);
        Vector4 green2 = new Vector4( 0 , 0.5f, 0);        
        Vector4 brown =  new Vector4( 224f/255f ,132f/255f ,27f/255f);
        Vector4 brown2 = new Vector4( 171f/255f ,99f/255f ,70f/255f);
        Vector4 grey =   new Vector4( 0.5f , 0.5f , 0.5f);        
        Vector4 white =   new Vector4( 1 , 1 , 1);

        final int[] colorRange = generateColorGradient( 
                new Vector4[] { blue1,blue2,green,green2,brown,brown2,grey,white } ,
                new int[]     {   48 ,48   ,32   ,32    ,32   ,16    ,32   ,16   }
                );

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
            if ( DEBUG_WRITE_TEXTURE_TO_TMPFILE ) {
                FileOutputStream tmpOut = new FileOutputStream("/tmp/texture.png");
                tmpOut.write( out.toByteArray() );
                tmpOut.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return out;
    }

    private int[] generateColorGradient(Vector4[] colors,int[] interpolatedColorCount) 
    {
        if ( colors.length != interpolatedColorCount.length ) {
            throw new IllegalArgumentException("Colors array needs to have same length as gradient position array");
        }

        final int[][] ranges = new int[colors.length][];

        int totalElements = 0;
        for( int i = 1 ; i < colors.length  ; i++) 
        {
            final int elements;
            if ( (i+1) < colors.length ) {
                elements = interpolatedColorCount[i];
            }  else {
                elements = 256-totalElements;
            }
            ranges[i] = interpolateColor(colors[i-1],colors[i],elements);
            totalElements+=elements;
        }

        // fill-up range with final color of gradient
        if ( totalElements < 256 ) 
        {
            final int delta = 256 - totalElements;
            System.out.println("Delta: "+delta);
            int[] tmp = new int[ delta ];
            ranges[ranges.length-1] = tmp;
            final int lastColor = colors[ colors.length - 1 ].toRGB();
            for ( int i = 0 ; i < delta ; i++ ) {
                tmp[i]=lastColor;
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
        //        for ( int i = 0 ; i < colorRange.length ; i++ ) {
        //        	final int r = (colorRange[i] & 0xff0000) >> 16;
        //        	final int g = (colorRange[i] & 0x00ff00) >> 8;
        //        	final int b = (colorRange[i] & 0x0000ff);
        //        	System.out.println("Color "+i+" = ( "+r+" , "+g+" , "+b+" )");
        //        }
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
                Vector4 normal = calcNormal(p3,p4,p1);

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
        final PerlinNoise noise = new PerlinNoise( seed );
        // generate some white noise
        //        final float[][] whiteNoise = FractalNoise.generateWhiteNoise( heightMapSize , heightMapSize , seed);
        //        final float[][] perlinNoise = FractalNoise.generateFractalNoise( whiteNoise, 6 , 0.4f );

        final float[][] map = generateNoise( noise , heightMapSize , 7 , 0.5f  );
        final float[] result = new float[heightMapSize*heightMapSize];
        int ptr = 0;
        for ( int x = 0 ; x < heightMapSize ; x++) {
            for ( int y = 0 ; y < heightMapSize ; y++) {
                float value = map[x][y];
                //                System.out.println("value="+value);
                result[ptr++] = value;
            }
        }
        return result;
    }

    public static float[][] generateNoise(PerlinNoise noise, int heightMapSize,int octaveCount,float persistance)
    {
        int width = heightMapSize;
        int height = heightMapSize;

        float[][][] smoothNoise = new float[octaveCount][][]; //an array of 2D arrays containing

        //generate smooth noise
        for (int i = 0; i < octaveCount; i++)
        {
            smoothNoise[i] = generateSmoothNoise(noise,heightMapSize , i);
        }

        float[][] fractalNoise = FractalNoise.createEmptyArray(width, height);
        float amplitude = 1.0f;
        float totalAmplitude = 0.0f;

        //blend noise together
        for (int octave = octaveCount - 1; octave >= 0; octave--)
        {
            amplitude *= persistance;
            totalAmplitude += amplitude;

            for (int i = 0; i < width; i++)
            {
                for (int j = 0; j < height; j++)
                {
                    float tmp = smoothNoise[octave][i][j];
                    tmp = tmp * amplitude;
                    fractalNoise[i][j] += tmp;
                }
            }
        }

        //normalisation
        float min = Integer.MAX_VALUE;
        float max = Integer.MIN_VALUE;
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                float tmp = fractalNoise[i][j]/octaveCount;
//                if ( tmp <= 0.4f ) {
//                    tmp = 0f;
//                }
//                tmp = tmp / totalAmplitude;
                fractalNoise[i][j]=tmp;
                if ( tmp < min ) {
                    min = tmp;
                }
                if ( tmp > max ) {
                    max = tmp;
                }
            }
        }
        final float actualRange = max-min;
        final float factor = 1/actualRange;
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                float newValue = (fractalNoise[i][j]-min)*factor;
                if ( newValue < 0.2) {
                    newValue = 0.2f;
                }
                fractalNoise[i][j]=newValue;
            }
        }
        return fractalNoise;
    }     

    private static float[][] generateSmoothNoise(PerlinNoise noise, int heightMapSize,int octave)
    {
        int width = heightMapSize;
        int height = heightMapSize;

        float[][] smoothNoise = FractalNoise.createEmptyArray(width, height);

        int samplePeriod = 1 << octave; // calculates 2 ^ k
        float sampleFrequency = 1.0f / samplePeriod;

        for (int i = 0; i < width; i++)
        {
            //calculate the horizontal sampling indices
            int sample_i0 = (i / samplePeriod) * samplePeriod;
            int sample_i1 = (sample_i0 + samplePeriod) % width; //wrap around
            float horizontal_blend = (i - sample_i0) * sampleFrequency;

            for (int j = 0; j < height; j++)
            {
                //calculate the vertical sampling indices
                int sample_j0 = (j / samplePeriod) * samplePeriod;
                int sample_j1 = (sample_j0 + samplePeriod) % height; 
                float vertical_blend = (j - sample_j0) * sampleFrequency;

                //blend the top two corners
                float noise1 = noise2(noise,sample_i0,sample_j0,heightMapSize);
                float noise2 = noise2(noise,sample_i1,sample_j0,heightMapSize);
                float top = interpolateCosine(noise1,noise2, horizontal_blend);

                //blend the bottom two corners
                float noise3 = noise2(noise,sample_i0,sample_j1,heightMapSize);
                float noise4 = noise2(noise,sample_i1,sample_j1,heightMapSize);
                float bottom = interpolateCosine(noise3,noise4, horizontal_blend);

                //final blend
                smoothNoise[i][j] = interpolateCosine(top, bottom, vertical_blend);
            }
        }
        return smoothNoise;
    }     
    
    private static float noise2(PerlinNoise noise,int x,int y,float heightMapSize) 
    {
        final float blockSize1 = 117.3254f;
        final float blockSize2 = 2223.1823f;
//        final float xOffset = rnd.nextFloat()*0.05f;
//        final float yOffset = rnd.nextFloat()*0.05f;
        final float xOffset = 0;
        final float yOffset = 0;
        float realX = xOffset+(x/heightMapSize)*blockSize1;
        float realY = yOffset+(y/heightMapSize)*blockSize2;
//        float realX = (x+0.5f)*117;
//        float realY = (0.75f+y)*14;
        return noise.tileableNoise2( realX , realY , heightMapSize , heightMapSize );
    }

    private static float interpolate(float a,float b,float t) {
        return PerlinNoise.lerp( t , a , b );
    }
    
    private static float interpolateCosine( float y1,float y2, float mu)
    {
       double mu2 = (1-Math.cos(mu*Math.PI))/2;
       return (float) (y1*(1-mu2)+y2*mu2);
    }      
}

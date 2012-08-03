package de.codesourcery.engine.geom;

import java.util.Random;

public class FractalNoise
{
    public static float[][] generateFractalNoise(float[][] baseNoise, int octaveCount,float persistance)
    {
        int width = baseNoise.length;
        int height = baseNoise[0].length;

        float[][][] smoothNoise = new float[octaveCount][][]; //an array of 2D arrays containing

        //generate smooth noise
        for (int i = 0; i < octaveCount; i++)
        {
            smoothNoise[i] = generateSmoothNoise(baseNoise, i);
        }

        float[][] fractalNoise = createEmptyArray(width, height);
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
                    fractalNoise[i][j] += smoothNoise[octave][i][j] * amplitude;
                }
            }
        }

        //normalisation
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                fractalNoise[i][j] /= totalAmplitude;
            }
        }

        return fractalNoise;
    }    

    public static float[][] generateWhiteNoise(int width, int height,long seed)
    {
        final Random rnd = new Random(seed);
        float[][] noise = createEmptyArray(width,height);

        for (int i = 0; i < width ; i++)
        {
            for (int j = 0; j < height ; j++)
            {
                noise[i][j] = rnd.nextFloat();
            }
        }
        return noise;
    }  

    public static float[][] createEmptyArray(int width,int height) {
        float[][] noise = new float[width][];

        for(int i = 0 ; i < width ; i++ ) {
            noise[i] = new float[height];
        }
        return noise;
    }

    private static float[][] generateSmoothNoise(float[][] baseNoise, int octave)
    {
        int width = baseNoise.length;
        int height = baseNoise[0].length;

        float[][] smoothNoise = createEmptyArray(width, height);

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
                int sample_j1 = (sample_j0 + samplePeriod) % height; //wrap around
                float vertical_blend = (j - sample_j0) * sampleFrequency;

                //blend the top two corners
                float top = interpolate(baseNoise[sample_i0][sample_j0],
                        baseNoise[sample_i1][sample_j0], horizontal_blend);

                //blend the bottom two corners
                float bottom = interpolate(baseNoise[sample_i0][sample_j1],
                        baseNoise[sample_i1][sample_j1], horizontal_blend);

                //final blend
                smoothNoise[i][j] = interpolate(top, bottom, vertical_blend);
            }
        }

        return smoothNoise;
    }    

//    private static float interpolate(float x0, float x1, float alpha)
//    {
//        return x0 * (1 - alpha) + alpha * x1;
//    } 
    
    private static float interpolate( double y1,double y2, double mu)
         {
            double mu2 = (1-Math.cos(mu*Math.PI))/2;
            return (float) (y1*(1-mu2)+y2*mu2);
         }    
}

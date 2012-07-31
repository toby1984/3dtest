#version 330

smooth in vec3 vVaryingNormal;
smooth in vec3 vVaryingLightDir;

uniform float hardness;

uniform vec4 ambientColor;
uniform vec4 diffuseColor;
uniform vec4 specularColor;

out vec4 vFragColor;

void main(void)
{
    // Dot product gives us diffuse intensity
    float diff = max(0.0, dot(normalize(vVaryingNormal), normalize(vVaryingLightDir)));

    // Multiply intensity by diffuse color, force alpha to 1.0
    vFragColor = diff * diffuseColor;
    vFragColor.a = 1.0;

    // Add in ambient light
    vFragColor += ambientColor;

    // Specular Light
    vec3 vReflection = normalize(reflect(-normalize(vVaryingLightDir), normalize(vVaryingNormal)));
    float spec = max(0.0, dot(normalize(vVaryingNormal), vReflection));
    if(diff != 0) {
        float fSpec = pow(spec, 128.0)*hardness;
        vFragColor.rgb += vec3(fSpec, fSpec, fSpec);
    }
}
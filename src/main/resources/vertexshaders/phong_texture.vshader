#version 330

in vec4 vVertex;
in vec4 vNormal;
in vec2 vTexCoords;

smooth out vec3 vVaryingNormal;
smooth out vec3 vVaryingLightDir;
smooth out vec2 vVaryingTexCoords;

uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat4 normalMatrix;

uniform vec4 vLightPosition;

void main(void)
{
  vVaryingNormal = normalMatrix * vNormal;
  
  vec4 vPosition4 = mvMatrix * vVertex;
  vec3 vPosition3 = vPosition4.xyz / vPosition4.w;
  
  vVaryingLightDir = normalize( vLightPosition.xyz - vPosition3 );
  
  gl_Position = mvpMatrix * vVertex;
  vVaryingTexCoords = vTexCoords;
}
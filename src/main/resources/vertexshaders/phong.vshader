#version 330

in vec4 vVertex;
in vec4 vNormal;

smooth out vec3 vVaryingNormal;
smooth out vec3 vVaryingLightDir;

uniform mat4 mvpMatrix;
uniform mat4 mvMatrix;
uniform mat4 normalMatrix;

uniform vec4 vLightPosition;

void main(void)
{
  vec4 tmp = normalMatrix * vNormal;
  vVaryingNormal = tmp.xyz;

  vec4 vPosition4 = mvMatrix * vVertex;
  vec3 vPosition3 = vPosition4.xyz / vPosition4.w;

  vVaryingLightDir = normalize( vLightPosition.xyz - vPosition3 );

  gl_Position = mvpMatrix * vVertex;
}
#version 330

in vec4 vVertex;
in vec4 vNormal;

smooth out vec4 color;

uniform mat4 normalMatrix;
uniform mat4 mvMatrix;
uniform mat4 mvpMatrix;

uniform vec4 diffuseColor;
uniform vec4 vLightPosition;

void main(void)
{
  vec4 vEyeNormal = normalMatrix * vNormal;
  
  vec4 vPosition4 = mvMatrix * vVertex;
  vec4 vPosition3 = vPosition4 / vPosition4.w;
  
  vec4 vLightDir = normalize( vLightPosition - vPosition3 );
  
  float diff = max(0.3 , dot( vEyeNormal , vLightDir ) );
  
  color.xyz = diff*diffuseColor;
  color.a = 1.0;
  
  gl_Position = mvpMatrix * vVertex;
}
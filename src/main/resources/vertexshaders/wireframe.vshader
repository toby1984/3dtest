#version 330

in vec4 vVertex;
uniform mat4 mvpMatrix;

smooth out vec4 color;

void main(void)
{
  color = vec4(1.0,0,0,1);
  gl_Position = mvpMatrix * vVertex;
}
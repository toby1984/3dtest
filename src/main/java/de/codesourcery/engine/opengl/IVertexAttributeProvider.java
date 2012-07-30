package de.codesourcery.engine.opengl;

public interface IVertexAttributeProvider {

	public void setVertexAttribute(ShaderProgram program , ProgramAttribute attribute);
	
	public void cleanup();
}

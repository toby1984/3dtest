package de.codesourcery.engine.opengl;

public final class ProgramAttribute 
{
	private final String identifier;
	private final AttributeType type;
	
	public static enum AttributeType 
	{
		/*
		 * per-vertex attributes
		 */
		VERTEX_POSITION(0,false),
		VERTEX_NORMAL(1,false),
		VERTEX_COLOR(2,false),
		VERTEX_TEXTURE2D_COORDS(3,false),
		
		/* 
		 * uniforms
		 */
		MVP_MATRIX(4,true),
		NORMAL_MATRIX(5,true),
		MV_MATRIX(6,true),
		EYE_POSITION(7,true),
		COLORMAP(8,true),
		
		// lighting
		LIGHT_POSITION(9,true),
		AMBIENT_COLOR(10,true),
		DIFFUSE_COLOR(11,true),
		SPECULAR_COLOR(12,true);
		
		private final boolean isUniform;
		private final int id;
		
		private AttributeType(int id,boolean isUniform) {
			this.id = id;
			this.isUniform = isUniform;
		}
		
		public int getId() {
			return id;
		}
		
		public boolean isUniform() {
			return isUniform;
		}
	}
	
	public ProgramAttribute(String identifier,AttributeType type) {
		this.identifier = identifier;
		this.type=type;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public AttributeType getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return "Attribute( "+identifier+" )";
	}
}
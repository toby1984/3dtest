package de.codesourcery.engine.opengl;

public final class ProgramAttribute 
{
	private final String identifier;
	private final AttributeType type;
	
	public static enum AttributeType 
	{
		VERTEX_POSITION(0,false),
		VERTEX_NORMAL(1,false),
		VERTEX_COLOR(2,false),
		MVP_MATRIX(3,true),
		NORMAL_MATRIX(4,true),
		MV_MATRIX(5,true),
		DIFFUSE_COLOR(6,true),
		EYE_POSITION(7,true),
		LIGHT_POSITION(8,true);
		
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
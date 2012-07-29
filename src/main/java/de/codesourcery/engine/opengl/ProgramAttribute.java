package de.codesourcery.engine.opengl;

public final class ProgramAttribute 
{
	private final String identifier;
	private final AttributeType type;
	
	public static enum AttributeType {
		VERTEX_POSITION(0),
		VERTEX_NORMAL(1),
		VERTEX_COLOR(2),
		MVP_MATRIX(3);
		
		private final int id;
		
		private AttributeType(int id) {
			this.id = id;
		}
		
		public int getId() {
			return id;
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
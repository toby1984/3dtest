package de.codesourcery.engine.opengl;

import java.util.Stack;

import de.codesourcery.engine.linalg.Matrix;

public class MatrixStack {

	private final Stack<Matrix> stack = new Stack<>();
	
	public void loadIdentity() {
		push( Matrix.identity() );
	}
	
	public void push(Matrix m) {
		stack.push( m );
	}
	
	public Matrix pop() {
		return stack.pop();
	}
}

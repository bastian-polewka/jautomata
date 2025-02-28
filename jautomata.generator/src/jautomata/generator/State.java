package jautomata.generator;

import java.util.Objects;


/**
 * @author Bastian Polewka
*/
public class State {
	private String id;
	private boolean initialState;
	private boolean finalState;

	public State(String id) {
		this.id = id;
		this.initialState = false;
		this.finalState = false;
	}
	
	public State(String id, boolean initalState, boolean finalState) {
		this.id = id;
		this.initialState = initalState;
		this.finalState = finalState;
	}
	
    public State(State another) {
        this.id = another.id;
        this.initialState = another.initialState;
        this.finalState = another.finalState;
    }
	
	public String getId() {
		return id;
	}
	
	public boolean isInitialState() {
		return initialState;
	}
	
	public void setInitialState(boolean initial) {
		this.initialState = initial;
	}
	
	public boolean isFinalState() {
		return finalState;
	}
	
	public void setFinalState(boolean finalS) {
		this.finalState = finalS;
	}
	
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return Objects.equals(id, state.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
    	String text = "State{" + "id='" + id + "'";
    	
    	if(initialState) {
    		text+= ", initialState: true";
    	}
    	
    	if(finalState) {
    		text+= ", finalState: true";
    	}
    	
    	text += "}";
    	
        return text;
    }
}

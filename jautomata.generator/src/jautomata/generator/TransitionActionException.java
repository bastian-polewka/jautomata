package jautomata.generator;

public class TransitionActionException extends Exception { 
	private static final long serialVersionUID = 1L;

	public TransitionActionException(String errorMessage) {
        super(errorMessage);
    }
}

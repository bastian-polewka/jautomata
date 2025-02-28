package jautomata.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;


/**
 * @author Bastian Polewka
 * @summary Generator for weighted automata based on multiple parameters.
 * Uses class WeightedAutomaton to save the generated weighted automaton.
*/
public class WAGenerator {

	private WeightedAutomaton weightedAutomaton;
	private Random random;
	private int numStates;
	private int numTransitions;
	private int minWeight;
	private int maxWeight;
	private boolean selfLoop;
	private int numActions;
	private int numFeatures;
	private Stack<Action> actionStack;

	
	public WAGenerator(int seed, int numStates, int numTransitions, int minWeight, int maxWeight, int numActions, int numFeatures, boolean selfLoop) throws Exception, TransitionActionException {
		this.random = new Random(seed);
		this.numStates = numStates;
		this.numTransitions = numTransitions;
		this.minWeight = minWeight;
		this.maxWeight = maxWeight;
		this.numActions = numActions;
		this.numFeatures = numFeatures;
		this.selfLoop = selfLoop;
		this.weightedAutomaton = new WeightedAutomaton(random);
		
		if (numTransitions < numActions) {
			throw new TransitionActionException("Number of transitions cannot be smaller than number of actions");
		}
		
		if (numTransitions > numActions * numStates) {			
			throw new TransitionActionException("There are too many transitions and not enough actions to make the weighted automaton.");
		}
				
		this.generateAutomaton();
    }
	
	
	public WeightedAutomaton getWeightedAutomaton() {
		return weightedAutomaton;
	}
	
	
	private void generateAutomaton() throws Exception {
		this.generateStates();
		this.generateActions();
		this.generateTransitions();
	}
	
	
	private void generateStates() {	
        for (int i = 0; i < numStates; i++) {
        	String stateID = Converter.convertBase26(i);

            boolean initialState = false;
            boolean finalState = false;
            
            if (i == 0) {
            	initialState = true;
            }
            
            if (i == numStates-1) {
            	finalState = true;
            }
            
            weightedAutomaton.addState(new State(stateID, initialState, finalState));
        }
    }
	

	private void generateActions() {
		Set<Action> actionSet = new HashSet<>();
		Set<String> featureSet = new HashSet<>();
				
		for (int i = 0; i < numActions; i++) {
			String letter = Converter.convertBase26(i);
			actionSet.add(new Action("action" + letter));
		}
		
		for (int i = 0; i < numFeatures; i++) {
			String letter = Converter.convertBase26(i);
			featureSet.add("feature" + letter);
		}
		
	    List<Action> actionList = new ArrayList<>(actionSet);
	    List<String> featureList = new ArrayList<>(featureSet);
	    
		if (featureSet.size() <= actionSet.size()) {
			for (int i = 0; i < featureSet.size(); i++) {
		    	boolean duplicate = true;
				Action action = null;
				String feature = null;
				
				while (duplicate) {
					action = actionList.get(random.nextInt(actionList.size()));
					feature = featureList.get(random.nextInt(featureList.size()));
					int weight = random.nextInt(maxWeight) + minWeight;
					duplicate = action.addWeight(feature, weight);
				}
				actionList.remove(action);
				featureList.remove(feature);
			}
			
			featureList = new ArrayList<>(featureSet);
			
			int actionSize = actionList.size();
			for (int i = 0; i < actionSize; i++) {
				boolean duplicate = true;
				Action action = null;
				String feature = null;
				
				while (duplicate) {
					action = actionList.get(random.nextInt(actionList.size()));
					feature = featureList.get(random.nextInt(featureList.size()));
					int weight = random.nextInt(maxWeight) + minWeight;
					duplicate = action.addWeight(feature, weight);
				}
				actionList.remove(action);
			}
		}
		else {
			for (int i = 0; i < actionSet.size(); i++) {
		    	boolean duplicate = true;
				Action action = null;
				String feature = null;
				
				while (duplicate) {
					action = actionList.get(random.nextInt(actionList.size()));
					feature = featureList.get(random.nextInt(featureList.size()));
					int weight = random.nextInt(maxWeight) + minWeight;
					duplicate = action.addWeight(feature, weight);
				}
				actionList.remove(action);
				featureList.remove(feature);
			}
	    
			actionList = new ArrayList<>(actionSet);
			int featureSize = featureList.size();
		    for (int i = 0; i < featureSize; i++) {
			    	boolean duplicate = true;
					Action action = null;
					String feature = null;
					
					while (duplicate) {
						action = actionList.get(random.nextInt(actionList.size()));
						feature = featureList.get(random.nextInt(featureList.size()));
						int weight = random.nextInt(maxWeight) + minWeight;
						duplicate = action.addWeight(feature, weight);
					}
					featureList.remove(feature);
		    	}
		}
	    
	    actionStack = new Stack<>();
	    
	    int minNumActions = numTransitions - numActions;
	    
	    
	    for (int i = 0; i < minNumActions; i++) {
	    	Action action = this.getByRandomClass(actionSet);
	    	   	
	    	actionStack.add(action);
	    	
	    	if (Collections.frequency(actionStack, action) >= numStates) {
	    		actionSet.remove(action);
	    		minNumActions++;
	    	}
	    }
	    
	    int actionSetSize = actionSet.size();
	   
	    for (int i = 0; i < actionSetSize; i++) {	
	    	Action action = this.getByRandomClass(actionSet);
	    	actionStack.add(action);
	    	actionSet.remove(action);
	    }	   
    	System.out.println(actionStack.size() +  " " + numTransitions);
	}
	
	
	private Transition createTransition(State fromState, State toState) {
        Action action = actionStack.pop();      
        Transition transition = new Transition(fromState, toState, action.getName(), action.getWeights());
        weightedAutomaton.addTransition(transition);
        return transition;
    }
	
	
	private Transition createTransition(State fromState, State toState, Action action) {
        Transition transition = new Transition(fromState, toState, action.getName(), action.getWeights());
        weightedAutomaton.addTransition(transition);
        return transition;
    }
	
	
	private State getRandomState(Set<State> states) {
		Iterator<State> itr = states.iterator();
		
		int randomIndex = random.nextInt(states.size());
        State randomState = null;

        for (int i = 0; i <= randomIndex; i++) {
        	randomState = itr.next();
        }
        
        return randomState;
	}
	
	
	private void makeRandomTransition() {
		State fromState = weightedAutomaton.getRandomState(true);
		State toState = weightedAutomaton.getRandomState(true);
		
		Set<Transition> transitions = weightedAutomaton.getTransitions();
		Set<State> fromStatesSet = new HashSet<>(weightedAutomaton.getStates());
		
		outerLoop:
			while (!fromStatesSet.isEmpty()) {         	
				for (Transition transition : transitions) {
					if (transition.getFromState().equals(fromState) && transition.getAction().equals(actionStack.peek().getName())) {
						fromStatesSet.remove(fromState);
						fromState = this.getByRandomClass(fromStatesSet);
						continue outerLoop;
					}
				}

				if(!fromStatesSet.isEmpty()) {
					break;
				}         	
			}

		if (!fromStatesSet.isEmpty()) {
			createTransition(fromState, toState);
		}
		else {
			throw new IllegalStateException("Error: No Valid Transition can be generated");
		}
    }
	
	

	private void generateTransitions() throws Exception {
		Set<State> states = weightedAutomaton.getStates();
        State initialState = weightedAutomaton.getInitialState();
        State finalState = weightedAutomaton.getInitialState();

        if (states.size() < 2) {
            throw new IllegalStateException("Not enough states to generate transitions.");
        }
        
        if (initialState == null || finalState == null) {
            throw new IllegalStateException("States must be generated before generating guaranteed path.");
        }
        
        State fromState = initialState;
        State toState;
       

        for (int i = 0; i < numTransitions; i++) {
        	
        	toState = weightedAutomaton.getRandomState(true);

            // Ensure a transition isn't from a state to itself unless multiple transitions are allowed
            if (!selfLoop) {
	            while (fromState.equals(toState)) {
	                toState = weightedAutomaton.getRandomState(true);
	            }
            }
                      
            Set<Transition> transitions = new HashSet<>(weightedAutomaton.getTransitions());
            Set<State> fromStatesSet = new HashSet<>(weightedAutomaton.getStates());
            
            if (transitions.isEmpty()) {
            	createTransition(fromState, toState);
            	continue;
            }
            
            
            outerLoop:
            while (!fromStatesSet.isEmpty()) {         	
            	for (Transition transition : transitions) {
            		if (transition.getFromState().equals(fromState) && transition.getAction().equals(actionStack.peek().getName())) {
            			fromStatesSet.remove(fromState);
                       	fromState = this.getByRandomClass(fromStatesSet);
            			continue outerLoop;
            		}
            	}
           	
            	if(!fromStatesSet.isEmpty()) {
            		break;
            	}         	
            }
                           
            if (!fromStatesSet.isEmpty()) {
            	createTransition(fromState, toState);
            }
            else {
            	throw new IllegalStateException("Error: No Valid Transition can be generated");
            }
                    
	        fromState = toState;            
        }    
       
        
        // Check if Automaton is accepting, if not delete necessary amount of transitions to create a path from initial to final state
        if (!weightedAutomaton.isAccepting()) {      	
        	Set<Transition> transitions = weightedAutomaton.getTransitions();
        	int minNumTransitions = states.size() - 1;
        	
        	// Delete randomly transitions to create new ones
        	for (int i = 0; i < minNumTransitions; i++) {    		
        		Transition transition = weightedAutomaton.getRandomSelfLoopTransition();
        		
        		if (transition == null) {
        			transition = weightedAutomaton.getRandomTransition();
        		} 

        		transitions.remove(transition);

        		Action action = new Action(transition.getAction());
        		action.addWeightMap(transition.getWeight());
        		actionStack.push(action);
        		
        		weightedAutomaton.removeTransition(transition);
        	} 
        	
        	Set<State> visitedStates = new HashSet<>(weightedAutomaton.getStates());
        	List<State> pathStates = new ArrayList<>();
        	visitedStates.remove(weightedAutomaton.getInitialState());
        	     	
        	int addTransitions = 0;	// count transitions we need to add if there is already a transition
        	fromState = weightedAutomaton.getInitialState();
        	pathStates.add(fromState);
        	
        	
        	List<Action> actionList = new ArrayList<>(actionStack);
        	   	
        	outerLoop:
        	while (visitedStates.size() > 0) {        		
            	toState = getRandomState(visitedStates);  
            	                  	
            	for (Transition transition : transitions) {        
                    if (transition.getFromState().equals(fromState) && transition.getToState().equals(toState)) {
                    	visitedStates.remove(toState);
                    	pathStates.add(toState);
                    	fromState = toState;             	
                    	addTransitions++;                  	
                    	continue outerLoop;
                    }
                }      	
            	
            	if(!fromState.equals(toState)) {           		
            		Set<Transition> transitionsFromState = weightedAutomaton.getTransitionsFromState(fromState);    
            		Iterator<Action> itr = actionList.iterator();
           		
            		itrLoop:
            		while(itr.hasNext()) {	
            			Action action = itr.next();
            			for (Transition transition : transitionsFromState) {
            				if (transition.getAction().equals(action.getName())) {
            					continue itrLoop;
            				}
            			}
            			
            			this.createTransition(fromState, toState, action);
            			itr.remove();
            			visitedStates.remove(toState);
            			pathStates.add(toState);
                    	fromState = toState;  
            			break;
            		}
            		
            		Set<State> possibleStates = new HashSet<>(weightedAutomaton.getStates());
            		possibleStates.remove(toState);
            		possibleStates.removeAll(visitedStates);

            		fromState = this.getRandomState(possibleStates);           	
            	} 		                	
            }
        	
        	actionStack.removeAllElements();
        	actionStack.addAll(actionList);
        	
        	for(int i = 0; i < addTransitions; i++) {
        		makeRandomTransition();
        	}    	
        }

        if (!weightedAutomaton.isDeterministic()) {
        	throw new IllegalStateException("Weighted Automaton not determinstic!");
        }
        
        if (!weightedAutomaton.isAccepting()) {
        	throw new IllegalStateException("Weighted Automaton is not accepting!");
        }   
    }
	
	public Random getRandom() {
		return this.random;
	}
	
	private <T> T getByRandomClass(Set<T> set) {
	    if (set == null || set.isEmpty()) {
	        throw new IllegalArgumentException("The Set cannot be empty.");
	    }
	    int randomIndex = random.nextInt(set.size());
	    int i = 0;
	    for (T element : set) {
	        if (i == randomIndex) {
	            return element;
	        }
	        i++;
	    }
	    throw new IllegalStateException("Could not pick random Element");
	}
}

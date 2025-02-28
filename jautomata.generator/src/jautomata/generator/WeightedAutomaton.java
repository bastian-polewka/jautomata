package jautomata.generator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jautomata.extensions.semirings.*;
import net.jhoogland.jautomata.Automaton;
import net.jhoogland.jautomata.EditableAutomaton;


/**
 * @author Bastian Polewka
*/
public class WeightedAutomaton {
	
	private String name;
	private Set<State> states;
	private Set<Transition> transitions;
	private String semiringType;
	private List<ExperimentSettings> experimentSettings;
	private List<TestConfiguration> testConfigurationsList;
	private Random random;
	
	
	public WeightedAutomaton(Random random) {
        this.states = new HashSet<>();
        this.transitions = new HashSet<>();
        this.random = random;
    }
		
	public WeightedAutomaton(WeightedAutomaton another) {
		this.states = new HashSet<>();
	    this.transitions = new HashSet<>();
	    	    
	    Map<State, State> stateMapping = new HashMap<>();
	    for (State s : another.getStates()) {
	        State newState = new State(s);
	        stateMapping.put(s, newState);
	        this.states.add(newState);
	    }

	    for (Transition t : another.getTransitions()) {
	        this.transitions.add(new Transition(t, stateMapping));
	    }
	    
        this.random = another.random;
    }
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	private void updateName() {
		int numStates = states.size();
		int numTransitions = transitions.size();		
		Set<Integer> weightValue = new HashSet<>();		
		for (Transition transition : transitions) {
			weightValue.addAll(transition.getWeight().values());
		}	
		int minWeight = Collections.min(weightValue);
		int maxWeight = Collections.max(weightValue);
		int numActions = this.getActions().size();
		int numFeatures = this.getFeatures().size();
		boolean selfLoop = true;
		
		String newName = numStates + "_" + numTransitions + "_" + minWeight + "_" + maxWeight + "_" + numActions + "_" + numFeatures + "_" + selfLoop;
		
		this.name = newName;	
	}
		
	/**
	 * All state functions:
	*/
	
	public State getState(String id) {
		for (State state : states) {
            if (state.getId().equals(id)) {
                return state;
            }
        }
		throw new NoSuchElementException("No State found with ID: " + id);
	}
	
	public Set<State> getStates() {
        return states;
    }
		
	public State getInitialState() {
		for (State state : states) {
			if (state.isInitialState()) {
				return state;
			}
		}
		throw new NoSuchElementException("No Initial State!");
    }
	
	public boolean changeRandomInitialState() {			
		WeightedAutomaton waTest = new WeightedAutomaton(this);
		Set<State> statesTest = new HashSet<>(waTest.getStates());
				
		statesTest.remove(waTest.getInitialState());
				
		while (!statesTest.isEmpty()) {
			State randomState = waTest.getByRandomClass(statesTest);
			
			waTest.getInitialState().setInitialState(false);
			waTest.getState(randomState.getId()).setInitialState(true);
			
			if (waTest.isAccepting()) {
				this.getInitialState().setInitialState(false);
				this.getState(randomState.getId()).setInitialState(true);
				return true;
			}
			else {
				waTest = new WeightedAutomaton(this);
				statesTest.remove(randomState);
			}		
		}
		return false;
    }
	
	public State getFinalState() {
		for (State state : states) {
			if (state.isFinalState()) {
				return state;
			}
		}
		throw new NoSuchElementException("No FinalState found");
    }
		
	/**
	 * Pick a random state from all states
	 * If special is true, it can also output initial and final states
	 * @param special
	 * @return State
	 */
	public State getRandomState(boolean special) {
		Iterator<State> itr = states.iterator();
		int randomIndex = random.nextInt(states.size());
        State randomState = null;
		
		if (!special) {
			if (states.size() < 3) {
				return null;
			}
			else {
				for (int i = 0; i <= randomIndex; i++) {
		        	randomState = itr.next();
		        }
				if (randomState.isInitialState() || randomState.isFinalState()) {
					itr = states.iterator();
					while (randomState.isInitialState() || randomState.isFinalState()) {
						randomState = itr.next();
					}
				}
			}
		}
		else {
			for (int i = 0; i <= randomIndex; i++) {
	        	randomState = itr.next();
	        }
		}
		    
        return randomState;
	}
		
	public void addState(State state) {
        states.add(state);
    }
	
	public boolean addRandomState() {	
		Set<State> statesSet = new HashSet<>(this.states);
		int numActions = this.getActions().size();

		for (State state : this.states) {
			int numActionForState = 0;
			for (Transition transition : this.transitions) {
				if (transition.getFromState().equals(state)) {
					numActionForState++;
				}
			}

			if (numActionForState >= numActions) {
				statesSet.remove(state);
			}
		}

		if (statesSet.isEmpty()) {
			return false;
		}
		else {
			try {
				Set<String> stateIDs = new HashSet<>();

				for (State state : this.states) {
					stateIDs.add(state.getId());
				}

				Comparator<String> customComparator = Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder());	
				String maxID = Collections.max(stateIDs, customComparator);		
				String newID = Converter.incrementBase26(maxID);	
				State newState = new State(newID);

								
				State fromState = this.getByRandomClass(statesSet);
				
				
				Set<String> actions = new HashSet<>(this.getActions());
				Set<String> freeActions = new HashSet<>(this.getActions());
				
				
				for (String action : actions) {
					for (Transition transition : transitions) {
						if (transition.getFromState().equals(fromState) && transition.getAction().equals(action)) {
							freeActions.remove(action);
						}
					}
				}
								
				String action = getByRandomClass(freeActions);	
				Map<String, Integer> weight = null;	
				for (Transition transition : transitions) {
					if (transition.getAction().equals(action)) {
						weight = new HashMap<>(transition.getWeight());				
						break;
					}
				}				
				
				Transition newToTransition = new Transition(fromState, newState, action, weight);

				action = getByRandomClass(actions);	
				weight = null;	
				for (Transition transition : transitions) {
					if (transition.getAction().equals(action)) {
						weight = new HashMap<>(transition.getWeight());				
						break;
					}
				}		
				State toState = getRandomState(true);	
				Transition newFromTransition = new Transition(newState, toState, action, weight);

				this.addState(newState);
				this.addTransition(newToTransition);
				this.addTransition(newFromTransition);
				
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			return true;	
		}
	}
		
	public void removeState(State state) {
		this.states.remove(state);
		removeTransitionsFromState(state);	
	}
	
	public void removeState(String id) {
		State state = getState(id);
		this.states.remove(state);		
		removeTransitionsFromState(state);
	}
		
	public boolean changeRandomFinalState() {				
		WeightedAutomaton waTest = new WeightedAutomaton(this);
		Set<State> statesTest = new HashSet<>(waTest.getStates());
		
		
		statesTest.remove(waTest.getFinalState());
				
		while (!statesTest.isEmpty()) {
			State randomState = waTest.getByRandomClass(statesTest);
					
			waTest.getFinalState().setFinalState(false);
			randomState.setFinalState(true);
			
			if (waTest.isAccepting()) {
				this.getFinalState().setFinalState(false);
				this.getState(randomState.getId()).setFinalState(true);			
				return true;
			}
			else {
				waTest = new WeightedAutomaton(this);
				statesTest.remove(randomState);
			}		
		}
		return false;
    }
	
	/**
	 * give number of states to delete from the automaton
	 * deletes states until it reached the given number of deleted states or
	 * if no other states can be deleted, because the automata will not accept
	 * have an accepting path
	 * @param num
	 */
	public boolean reduceSafelyStates(int num) {
		Set<State> copyStates = new HashSet<>(states);
		copyStates.remove(getInitialState());
		int total = num;
		boolean removed = false;
		
		int i = copyStates.size();
		while(i > 0 && num > 0) {
			WeightedAutomaton waTest = new WeightedAutomaton(this);
			State randomState = this.getRandomState(false);	
			waTest.removeState(randomState);
			if (waTest.isAccepting()) {
				this.removeState(randomState);
				removed = true;
				i = states.size();
				num--;
			}
			else {
				i--;
			}
		}
		
		System.out.println("Safely deleted " + (total-num) + " states");
		return removed;
	}
	
	/**
	 * Merges second state B into first given state A.
	 * Merged state keeps name of first given state A.
	 * If one state is a initalState or finalState, the merged state will be also initial or final.
	 */
	public boolean mergeRandomStates() {	 
		Set<State> possibleStatesA = new HashSet<>(this.states);
		Set<State> possibleStatesB = new HashSet<>(this.states);
		State stateA = this.getByRandomClass(possibleStatesA);
		State stateB = this.getByRandomClass(possibleStatesB);
		

		outerLoop:
		while (!possibleStatesA.isEmpty()) {	
			if (possibleStatesB.isEmpty()) {
				possibleStatesB = new HashSet<>(this.states);
				possibleStatesA.remove(stateA);
								
				if (possibleStatesA.isEmpty()) {
					return false;
				} else {
					stateA = this.getByRandomClass(possibleStatesA);
				}
			}
						
			stateB = this.getByRandomClass(possibleStatesB);
			
			if (stateA == stateB) {
				possibleStatesB.remove(stateB);	
				continue;
			}
					
			Set<Transition> transitionsA = new HashSet<>(this.getTransitionsFromState(stateA));
			Set<Transition> transitionsB = new HashSet<>(this.getTransitionsFromState(stateB));
			
			
			for (Transition transitionA : transitionsA) {
				for (Transition transitionB : transitionsB) {
					if (!transitionA.getToState().equals(transitionB.getToState())) {
						if (transitionA.getAction().equals(transitionB.getAction())) {
							possibleStatesB.remove(stateB);		
							continue outerLoop;
						}
					}
				}
			}
			
			if (!possibleStatesB.isEmpty()) {
				break;
			}
		}
		
		if (possibleStatesA.isEmpty()) {
			return false;
		}
										
		try {		
			if(stateB.isInitialState()) {
				stateA.setInitialState(true);
			}

			if(stateB.isFinalState()) {
				stateA.setFinalState(true);
			}

			for (Transition transition : transitions) {			
				if (transition.getToState().equals(stateB)) {
					transition.setToState(stateA);
				}

				if (transition.getFromState().equals(stateB)) {
					transition.setFromState(stateA);
				}
			}

			states.remove(stateB);


		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return true;
	}
	
	/**
	 * All transition functions:
	*/
	
	public void addTransition(Transition transition) {		
        transitions.add(transition);
    }
	
	public boolean addRandomTransition() {
		Set<String> actionSet = new HashSet<>(getActions());
		String randomAction = null;
		
		Set<State> sourceStateSet = new HashSet<>(states);
		Set<State> targetStateSet = new HashSet<>(states);
		
		State sourceState = this.getByRandomClass(sourceStateSet);
		State targetState = this.getByRandomClass(targetStateSet);
		
		outerLoop:
		while(!sourceStateSet.isEmpty()) {
			if (actionSet.isEmpty()) {
				actionSet = new HashSet<>(getActions());
				randomAction = this.getByRandomClass(actionSet);
				targetStateSet.remove(targetState);
								
				if (targetStateSet.isEmpty()) {
					targetStateSet = new HashSet<>(states);
					targetState = this.getByRandomClass(targetStateSet);
					sourceStateSet.remove(sourceState);
					
					if (sourceStateSet.isEmpty()) {
						return false;
					}
					else {
						sourceState = this.getByRandomClass(sourceStateSet);
					}												
				}
				else {
					targetState = this.getByRandomClass(targetStateSet);
				}			
			} 	
			else {
				randomAction = this.getByRandomClass(actionSet);
			}
			
			Set<Transition> sourceTransitionsSet = this.getTransitionsFromState(sourceState);
			
			for (Transition transition : sourceTransitionsSet) {
				if (transition.getAction().equals(randomAction)) {
					actionSet.remove(randomAction);	
					continue outerLoop;
				}
			}
				
			for (Transition transitionSameAction : transitions) {
				if (transitionSameAction.getAction().equals(randomAction)) {
					Transition newTransition = new Transition(sourceState, targetState, randomAction, transitionSameAction.getWeight());
					this.transitions.add(newTransition);
					return true;
				}
			}			    			
		}
		return false;
	}
		
	public boolean removeRandomTransition() {			
		Set<Transition> transitionSet = new HashSet<>(this.transitions);
			
		while (!transitionSet.isEmpty()) {
			WeightedAutomaton waTest = new WeightedAutomaton(this);
			Transition randomTransition = this.getByRandomClass(transitionSet);
			
			waTest.removeTransition(randomTransition);
			
			if (waTest.isAccepting()) {
				this.removeTransition(randomTransition);
				return true;
			}
			else {
				transitionSet.remove(randomTransition);
			}	
		}
		
		return false;	
	}
	
	public Transition getTransition(State fromState, State toState) {
        for (Transition transition : transitions) {
            if (transition.getFromState().equals(fromState) && transition.getToState().equals(toState)) {
                return transition;
            }
        }
		return null;
	}
	
	public Transition getTransition(String fromState, String toState) {
        for (Transition transition : transitions) {
            if (transition.getFromState().equals(getState(fromState)) && transition.getToState().equals(getState(toState))) {
                return transition;
            }
        }
		return null;
	}
		
	public Set<Transition> getTransitions() {
        return transitions;
    }
		
	public Transition getRandomTransition() {
		Iterator<Transition> itr = transitions.iterator();
		
		int randomIndex = random.nextInt(transitions.size());
        Transition randomTransition = null;

        for (int i = 0; i <= randomIndex; i++) {
        	randomTransition= itr.next();
        }
        
        return randomTransition;
	}
		
	public Transition getRandomSelfLoopTransition() {
		Set<Transition> loopTransitions = new HashSet<>(transitions);		
		Iterator<Transition> itr = loopTransitions.iterator();
		
		while (itr.hasNext()) {
			Transition transition = itr.next();		
			if (!transition.getFromState().equals(transition.getToState())) {
				itr.remove();
			}
		}
		
		if (loopTransitions.isEmpty()) {
			return null;
		} else {
			return this.getByRandomClass(loopTransitions);
		}
	}
		
	public Set<Transition> getTransitionsFromState(State state) {
        Set<Transition> result = new HashSet<>();
        for (Transition transition : transitions) {
            if (transition.getFromState().equals(state)) {
                result.add(transition);
            }
        }
        return result;
    }
		
	public Set<Transition> getTransitionsToState(State state) {
        Set<Transition> result = new HashSet<>();
        for (Transition transition : transitions) {
            if (transition.getToState().equals(state)) {
                result.add(transition);
            }
        }
        return result;
    }
		
	/**
	 * Deletes every transition that is connected to the state
	 */
	private void removeTransitionsFromState(State state) {
		Set<Transition> transitionSet = getTransitionsFromState(state);	
		transitionSet.addAll(getTransitionsToState(state));
		
		for (Transition transition : transitionSet) {
			transitions.remove(transition);
		}
	}
	
	public void removeTransition(Transition transition) {
		transitions.remove(transition);
	}
		
	public void reverseTransition(Transition transition) {
		transitions.remove(transition);
		
		State fromState = transition.getFromState();
		State toState = transition.getToState();
		
		transition.setFromState(toState);
		transition.setToState(fromState);
		
		transitions.add(transition);	
	}
		
	public void removeSelfLoops() {
		int num = 0;
		Set<Transition> newTransitions = new HashSet<>(transitions);
		
		for (Transition transition : transitions) {
			if (transition.getFromState().equals(transition.getToState())) {
				newTransitions.remove(transition);
				num++;
			}
		}
		transitions = newTransitions;
		System.out.println("Removed " + num + " self-loop transitions");
	}
	
	
	/**
	 * All action and feature functions:
	*/
	
	public Set<String> getActions() {
		Set<String> actions = new HashSet<>();
		for (Transition transition : transitions) {
			actions.add(transition.getAction());
		}	
		return actions;
	}	
	
	/**
	 * Removes all transitions with specified action
	 */
	public void removeAction(String action) {
		for (Transition transition : transitions) {
			if (action.equals(transition.getAction())) {
				removeTransition(transition);
			}
		}
	}
	
	/**
	 * get all features that are currently used in the automaton
	 * @return
	 */
	public Set<String> getFeatures() {
		Set<String> featureSet = new HashSet<>();
		
		for (Transition transition : transitions) {
			Map<String, Integer> weight = transition.getWeight();
			weight.forEach((key, value) -> {
	            featureSet.add(key);
	        });
        }
		
		return featureSet;
	}
	
	public String getRandomFeature() {
		Set<String> featureSet = new HashSet<>(getFeatures());	
		String randomFeature = getByRandomClass(featureSet);
		
		return randomFeature;
	}
	
	public boolean addRandomFeature() {
		try {
			Comparator<String> customComparator = Comparator.comparingInt(String::length).thenComparing(Comparator.naturalOrder());	
			String maxName = Collections.max(this.getFeatures(), customComparator);		
			maxName = maxName.replace("feature", "");
			String newFeatureName = Converter.incrementBase26(maxName);	
			newFeatureName = "feature" + newFeatureName;
			
			Set<Integer> weights = new HashSet<>();		
			for (Transition transition : transitions) {				
				weights.addAll(transition.getWeight().values());
			}
			
			int newWeight = random.nextInt(Collections.min(weights), Collections.max(weights));			
			
			Set<String> actions = this.getActions();	
			String action = getByRandomClass(actions);			
					
			for (Transition transition : transitions) {
				if (transition.getAction().equals(action)) {																
					Map<String, Integer> weight = new HashMap<>(transition.getWeight());				
					weight.put(newFeatureName, newWeight);
					
					transition.setWeight(weight);
				}
			}			
		} catch (Exception e) {
            e.printStackTrace();
            return false;
        }
		return true;		
	}
		
	public boolean removeRandomFeature() {
		Set<String> featureSet = new HashSet<>(this.getFeatures());
		
		while (!featureSet.isEmpty()) {
			WeightedAutomaton waTest = new WeightedAutomaton(this);

			Set<Transition> transitionSet = waTest.getTransitions();
			
			String randomFeature = this.getByRandomClass(featureSet);
			
			Iterator<Transition> iterator = transitionSet.iterator();

			while (iterator.hasNext()) {
			    Transition transition = iterator.next();
			    if (transition.getWeight().containsKey(randomFeature)) {
			        if (transition.getWeight().size() == 1) {
			            iterator.remove();
			        }
			        else {
			        	Map<String, Integer> map = transition.getWeight();		        	
			        	map.remove(randomFeature);
			        	transition.setWeight(map);
			        }
			    }
			}
			
			if (waTest.isAccepting()) {
				iterator = transitions.iterator();

				while (iterator.hasNext()) {
				    Transition transition = iterator.next();
				    if (transition.getWeight().containsKey(randomFeature)) {
				        if (transition.getWeight().size() == 1) {
				            iterator.remove();
				        }
				        else {
				        	Map<String, Integer> map = transition.getWeight();		        	
				        	map.remove(randomFeature);
				        	transition.setWeight(map);
				        }
				    }
				}
				return true;
			}
			else {
				featureSet.remove(randomFeature);
			}
		}
		return false;
	}
	
	public List<ExperimentSettings> getExperimentSettings() {
		return experimentSettings;
	}
	
	public void printAutomaton() {
		for (State state : states) {
		    System.out.println(state);  
		}
		
		for (Transition transition : transitions) {
		    System.out.println(transition);
		}
	}
		
	public void updateWeight(Transition transition, Map<String, Integer> weights) {
		transition.setWeight(weights);
	}
		
	/**
	 * scale to percentage
	 * @param scale
	 */
	public boolean normalizeWeights(int scale) {
	    Map<String, Integer> globalWeightSum = new HashMap<>();
	    
	    for (Transition transition : transitions) {
	        Map<String, Integer> weights = transition.getWeight();
	        for (Map.Entry<String, Integer> entry : weights.entrySet()) {
	            globalWeightSum.put(entry.getKey(),
	                globalWeightSum.getOrDefault(entry.getKey(), 0) + entry.getValue());
	        }
	    }

	    int totalWeight = globalWeightSum.values().stream().mapToInt(Integer::intValue).sum();

	    if (totalWeight == 0) {
	        return false;
	    }

	    for (Transition transition : transitions) {
	        Map<String, Integer> weights = transition.getWeight();
	        Map<String, Integer> normalizedWeights = new HashMap<>();

	        for (Map.Entry<String, Integer> entry : weights.entrySet()) {

	            double normalizedValue = entry.getValue() / (double) totalWeight;
	            normalizedWeights.put(entry.getKey(), (int) (normalizedValue * scale));
	        }

	        transition.setWeight(normalizedWeights);
	    }
	    
	    return true;
	}
		
	public void generateTestConyfigurations(int maxCombinationSize, int[] multiplyWeight) {	
		Map<String, Set<Integer>> weightMap = new HashMap<>();
		
		for (Transition transition : transitions) { 
			
			Map<String, Integer> weight = new HashMap<>(transition.getWeight());
					
			for (String key: weight.keySet()) {
				if (weightMap.get(key) == null) {
					Set<Integer> weightValues = new HashSet<>();
					weightValues.add(weight.get(key));
					weightMap.put(key, weightValues);
				}
				else {
					Set<Integer> weightValues = new HashSet<>(weightMap.get(key));
					weightValues.add(weight.get(key));
					weightMap.put(key, weightValues);
				}		    
			}
		}
				
		Set<TestConfiguration> testConfigurations = new HashSet<>();
		List<String> weightKeyList = new ArrayList<>(weightMap.keySet());
						
		for (int size = 1; size <= maxCombinationSize; size++) {		
			testConfigurations = generateConfigCombination(weightKeyList, new HashMap<>(), weightMap, testConfigurations, 0, size, multiplyWeight);
		}

		this.testConfigurationsList = new ArrayList<>(testConfigurations);
	}
	
	private Set<TestConfiguration> generateConfigCombination(List<String> weightKeyList, Map<String, Integer> config, Map<String, Set<Integer>> weightMap,
			Set<TestConfiguration> configurations, int startKey, int size, int[] multiplyWeight) {
		
		if (config.size() == size) {
			configurations.add(new TestConfiguration(new HashMap<>(config), false, false));
            return configurations;
        }

        for (int i = startKey; i < weightKeyList.size(); i++) {
        	String feature = weightKeyList.get(i);
        	
        	for (int weightValue : weightMap.get(feature)) {
        		
        		for (int multiplyBy : multiplyWeight) {
        			
        			int chance = random.nextInt(5);
        			if (chance == 0) {
        				config.put(feature, (weightValue * multiplyBy) + 1);
        			}
        			else {
        				config.put(feature, weightValue * multiplyBy);
        			}
        						
	            	generateConfigCombination(weightKeyList, config, weightMap, configurations, i + 1, size, multiplyWeight);
	            	config.remove(weightKeyList.get(i));
        		}	
        	}        	
        }
			
		return configurations;
	}
	
	/**
	 * Checks if there is a path from initial to final state and if all states are visited
	 */
	public boolean isAccepting() {
        Set<State> visited = new HashSet<>();
        Stack<State> stack = new Stack<>();
        
               
        State initialState = getInitialState();
        State finalState = getFinalState();
               
        // Start DFS from the initial state
        stack.push(initialState);
        boolean hasPath = false;
        
        while (!stack.isEmpty()) {
            State currentState = stack.pop();
            
            // If we reach the final state, mark path as found
            if (currentState.equals(finalState)) {
                hasPath = true;
            }
            
            // Mark the current state as visited
            if (!visited.contains(currentState)) {
                visited.add(currentState);
                
                // Explore all transitions from the current state
                Set<Transition> currentTransitions = getTransitionsFromState(currentState);
                
                for (Transition transition : currentTransitions) {               	
                	State toState = transition.getToState();
                 
                    // If the transition starts from the current state and the next state has not been visited
                    if (!visited.contains(toState)) {
                        stack.push(toState);
                    }
                }
            }
        }
             
        // Check if all states were visited
        boolean allStatesVisited = visited.size() == states.size();
        
        if (hasPath && allStatesVisited) {
            return true;
        } else {
        	return false;
        }         
       
    }
		
	public boolean isDeterministic() {     
        boolean deterministic = true;
        
        for (Transition transition : transitions) {        	
        	for (Transition transition2 : transitions) {
        		if(!transition.equals(transition2)) {
        			if (transition.getFromState().equals(transition2.getFromState())) {
            			if (transition.getAction().equals(transition2.getAction())) {
            				deterministic = false;
            				System.out.println(transition + " - " + transition2);
            			}
            		}
        		}    		
        	}
        }              
        return deterministic;   
    }
	
	public void createFromJson(String path) throws FileNotFoundException {
		File file = new File(path);
		
		if (!file.exists()) {
            throw new FileNotFoundException("Error: The file " + path + " was not found.");
        }
		else {
			System.out.println("File " + path + " loaded.");
		}
		
    	ObjectMapper mapper = new ObjectMapper();
    	   	
    	try {
        	JsonNode rootNode = mapper.readTree(file);

        	name = rootNode.get("automatonName").asText();
        	JsonNode statesNode = rootNode.get("states");
    		JsonNode transitionsNode = rootNode.get("transitions");
    		JsonNode settingsNode = rootNode.get("settings");
    		JsonNode configNode = rootNode.get("configurations");
    		 		
    		// add states
    		for(JsonNode jsonState : statesNode) {
    			JsonNode name = jsonState.get("name");
        		boolean hasInitial = jsonState.has("initial");
        		boolean hasFinal = jsonState.has("final");
        		
        		State state = new State(name.asText(), hasInitial, hasFinal);
        		
        		this.addState(state);
    		}
    				
    		// add transitions
    		for(JsonNode jsonTransition : transitionsNode) {    
				State fromState = getState(jsonTransition.get("from").asText());
				State toState = getState(jsonTransition.get("to").asText());
				String action = jsonTransition.get("action").asText();
				
    			Map<String, Integer> weights = new HashMap<>();
    			JsonNode jsonWeights = jsonTransition.get("weights");       
                Iterator<String> fieldNames = jsonWeights.fieldNames();
                
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    int value = jsonWeights.get(fieldName).asInt();
                    weights.put(fieldName, value);
                } 
                
                Transition transition = new Transition(fromState, toState, action, weights);
                
                this.addTransition(transition);               
    		}
    		
    		// add settings and configurations if they exist
    		if(settingsNode != null && configNode != null) {
    			
    			semiringType = rootNode.get("semiring").asText();
    			
    			List<TestConfiguration> testConfigurations = new ArrayList<>();
    			
    			
    			JsonNode configurations = rootNode.get("configurations");
    			for(JsonNode configuration : configurations) {    	
    				
    				
    	    		Map<String, Integer> configurationMap = new HashMap<>();
    	            JsonNode config = configuration.get("configuration");
    	            
    	            Iterator<String> fieldNames = config.fieldNames();
    	            while (fieldNames.hasNext()) {
    	                String fieldName = fieldNames.next();
    	                int value = config.get(fieldName).asInt();
    	                configurationMap.put(fieldName, value);
    	            }      
    	            
    	            TestConfiguration t = new TestConfiguration(configurationMap, configuration.get("nonEmptiness").asBoolean(), configuration.get("universality").asBoolean());
    	    				
    	            
    	            testConfigurations.add(t);
    			} 			
    			
    			JsonNode pathsNode = settingsNode.path("pathsCombinations");
    			int[] pathsCombinations = new int[pathsNode.size()];

    	        for (int i = 0; i < pathsNode.size(); i++) {   	   
    	            pathsCombinations[i] = pathsNode.get(i).asInt();
    	        }
    			
    			experimentSettings = List.of(
    				new ExperimentSettings(
    					createAutomaton(false),
    					name,
    					settingsNode.get("warmupRuns").asInt(),
    					settingsNode.get("realRuns").asInt(),
    					pathsCombinations,
    					15,
    					100,
    					testConfigurations,
    					settingsNode.get("lowerBoundedness").asBoolean(), // lower boundedness ground truth
    					settingsNode.get("upperBoundedness").asBoolean() // upper boundedness ground truth
    				)
				); 
    			
    			System.out.println("test:" + experimentSettings);
    		}
        			     
        } catch (IOException e) {
            e.printStackTrace();
        }
    	
	}
	
	public void saveJson(String path) throws StreamWriteException, DatabindException, IOException {
		Map<String, Object> data = new HashMap<>();
		
		data.put("automatonName", name);
		data.put("semiring", semiringType);
        data.put("states", states);
        data.put("transitions", transitions);           
        
        if (experimentSettings != null) {
        	ExperimentSettings settings = experimentSettings.get(0);
	        data.put("configurations", settings.testConfigurations());
	     
	        Map<String, Object> settingsMap = new HashMap<>();        
	        settingsMap.put("warmupRuns", settings.numWarmupRuns());
	        settingsMap.put("realRuns", settings.numRealRuns());
	        settingsMap.put("pathsCombinations", settings.numPathsCombinations());
	        settingsMap.put("lowerBoundedness", settings.lowerBoundednessGroundTruth());
	        settingsMap.put("upperBoundedness", settings.upperBoundednessGroundTruth());      
	        data.put("settings", settingsMap);   
        }
        
        
        ObjectMapper mapper = new ObjectMapper();

        // Custom pretty printer
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter(" ", DefaultIndenter.SYS_LF);
        prettyPrinter.indentObjectsWith(indenter);
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

        mapper.writer(prettyPrinter).writeValue(new File(path), data);
        
        System.out.println("File " + path + " saved.");
	}
		
	public void loadExperimentSettings(String path) throws FileNotFoundException {
		File file = new File(path);
		
		if (!file.exists()) {
            throw new FileNotFoundException("Error: The file " + path + " was not found.");
        }
		else {
			System.out.println("File " + path + " loaded.");
		}
		
    	ObjectMapper mapper = new ObjectMapper();
    	   	
    	try {
        	JsonNode rootNode = mapper.readTree(file);
    		JsonNode settingsNode = rootNode.get("settings");
    		
    		// add settings and configurations if they exist
    		if(settingsNode != null) {
    			
    			semiringType = rootNode.get("semiring").asText();		
    			
    			JsonNode pathsNode = settingsNode.path("pathsCombinations");
    			int[] pathsCombinations = new int[pathsNode.size()];

    	        for (int i = 0; i < pathsNode.size(); i++) {   	   
    	            pathsCombinations[i] = pathsNode.get(i).asInt();
    	        }
    	         	        			
    			experimentSettings = List.of(
    				new ExperimentSettings(
    					createAutomaton(false),
    					name,
    					settingsNode.get("warmupRuns").asInt(),
    					settingsNode.get("realRuns").asInt(),
    					pathsCombinations,
    					settingsNode.get("convergenceThreshold").asInt(),
    					settingsNode.get("convergenceStepSize").asInt(),
    					testConfigurationsList,
    					settingsNode.get("lowerBoundedness").asBoolean(), // lower boundedness ground truth
    					settingsNode.get("upperBoundedness").asBoolean() // upper boundedness ground truth
    				)
				);   			
    		}
        			     
        } catch (IOException e) {
            e.printStackTrace();
        }
    	
	}
	
	/**
	 * if automaton is changed, use method to updated experimentSettings
	 */
	public void updateSettings(boolean reversePathOrder) {
		ExperimentSettings settings = experimentSettings.get(0);
		
		List<TestConfiguration> configs = settings.testConfigurations();
		List<TestConfiguration> newConfigs = new ArrayList<>();
		
		Set<String> featureSet = getFeatures();
		
		for (TestConfiguration config : configs) {		
			boolean found = config.configuration().keySet().stream().anyMatch(featureSet::contains);
			
			if (found) {
				newConfigs.add(config);
			}						
		}
		
		experimentSettings = List.of(
				new ExperimentSettings(
					createAutomaton(reversePathOrder),
					name,
					settings.numWarmupRuns(),
					settings.numRealRuns(),
					settings.numPathsCombinations(),
					settings.convergenceThreshold(),
					settings.convergenceStepSize(),
					newConfigs,
					settings.lowerBoundednessGroundTruth(),
					settings.upperBoundednessGroundTruth()
				)
			); 
	}
	
	public void updateGroundTruths(List<TestConfiguration> newConfigs, boolean lowerBoundednessGroundTruth, boolean upperBoundednessGroundTruth) {
		ExperimentSettings settings = experimentSettings.get(0);
		
		experimentSettings = List.of(
				new ExperimentSettings(
					createAutomaton(false),
					name,
					settings.numWarmupRuns(),
					settings.numRealRuns(),
					settings.numPathsCombinations(),
					settings.convergenceThreshold(),
					settings.convergenceStepSize(),
					newConfigs,
					lowerBoundednessGroundTruth,
					upperBoundednessGroundTruth
				)
			); 
	}
	
	public void loadGroundTruths(List<TestConfiguration> newConfigs, boolean lowerBoundednessGroundTruth, boolean upperBoundednessGroundTruth) {
		ExperimentSettings settings = experimentSettings.get(0);
		
		experimentSettings = List.of(
				new ExperimentSettings(
					createAutomaton(false),
					name,
					settings.numWarmupRuns(),
					settings.numRealRuns(),
					settings.numPathsCombinations(),
					settings.convergenceThreshold(),
					settings.convergenceStepSize(),
					newConfigs,
					lowerBoundednessGroundTruth,
					upperBoundednessGroundTruth
				)
			); 
	}
	
	/**
	 * creates Automaton out of the WeightedAutomaton for evaluation
	 * @param rootNode
	 * @return
	 */
	private Automaton<String, Map<String, Integer>> createAutomaton(boolean reversePathOrder) {
		
		Set<String> featureSet = getFeatures();
				

		@SuppressWarnings("unchecked")
		MultisetSemiring<String, Integer> semiring = (MultisetSemiring<String, Integer>) createSemiring(semiringType, featureSet, reversePathOrder);
		
		
		EditableAutomaton<String, Map<String, Integer>> automaton =
			new EditableAutomaton<>(
				semiring
			);
		
		var one = semiring.one();
		var zero = semiring.zero();
		
		Map<String, Integer> statesMap = new HashMap<>();
		
			
		for (State state : states) {
    		boolean hasInitial = state.isInitialState();
    		boolean hasFinal = state.isFinalState();
		
    		int id = automaton.addState(hasInitial ? one : zero, hasFinal ? one : zero);

    		statesMap.put(state.getId(), id);
		}
				
		for (Transition transition : transitions) {    		  	
    		automaton.addTransition(
    				statesMap.get(transition.getFromState().getId()), 
    				statesMap.get(transition.getToState().getId()), 
    				transition.getAction(), 
    				createWeightInt(transition.getWeight(), featureSet));
		}		
		
		return automaton;			
	}
	
	public MultisetSemiring<?, ?> createSemiring(String semiringType, Set<String> featureSet, boolean reversePathOrder) {
		switch (semiringType) {
			case "MaxTropicalInteger":
				return new MultisetSemiring<>(featureSet, new MaxTropicalIntegerSemiring());
			case "MinTropicalInteger":
				return new MultisetSemiring<>(featureSet, new MinTropicalIntegerSemiring());
			case "MaxMaxInteger":
				return new MultisetSemiring<>(featureSet, new MaxMaxIntegerSemiring());
			case "MinMinInteger":
				return new MultisetSemiring<>(featureSet, new MinMinIntegerSemiring());
			case "MaxTropicalDouble":
				return new MultisetSemiring<>(featureSet, new MaxTropicalDoubleSemiring());
			case "MinTropicalDouble":
				return new MultisetSemiring<>(featureSet, new MinTropicalDoubleSemiring());
			case "MaxMaxDouble":
				return new MultisetSemiring<>(featureSet, new MaxMaxDoubleSemiring());
			case "MinMinDouble":
				return new MultisetSemiring<>(featureSet, new MinMinDoubleSemiring());
			default:
				throw new IllegalArgumentException("Unknown semiring type: " + semiringType);
		}
	}

	
	private static Map<String, Integer> createWeightInt(Map<String, Integer> weight, Set<String> featureSet) {
		return MultisetSemiring.expandMap(weight, featureSet, 0);
	}
	
	/**
	 * Returns a comparator defining the ordering path exploration.
	 *
	 * @return comparator for multiset semiring
	 
	private static Comparator<Map<String, Integer>> createSemiringComparator(Set<String> featureSet, boolean reversePathOrder) {		
		List<String> featureList = new ArrayList<>(featureSet);
		
		if (reversePathOrder) {
			Collections.reverse(featureList);
		}
			
	    return (o1, o2) -> {
	        for (String feature : featureList) {
	            Integer value1 = o1.getOrDefault(feature, 0);
	            Integer value2 = o2.getOrDefault(feature, 0);

	            int result = value1.compareTo(value2);
	            if (result != 0) {
	                return result;
	            }
	        }
	        return 0;
	    };	    
	}*/
	
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
	
	public boolean applyMutation(String mutation) {
		boolean mutationSucceded = false;
		
		switch (mutation) {
		case "None":
			mutationSucceded = true;
			break;
		case "AddState":
			mutationSucceded = this.addRandomState();
			break;
		case "RemoveState":
			mutationSucceded = this.reduceSafelyStates(1);
			break;
		case "ChangeInitialState": 
			mutationSucceded = this.changeRandomInitialState();
			break;
		case "ChangeFinalState":
			mutationSucceded = this.changeRandomFinalState();
			break;
		case "MergeTwoStates":			
			mutationSucceded = this.mergeRandomStates();
			break;
		case "AddTransition":
			mutationSucceded = this.addRandomTransition();
			break;
		case "RemoveTransition":
			mutationSucceded = this.removeRandomTransition();
			break;
		case "AddFeature":
			mutationSucceded = this.addRandomFeature();
			break;
		case "RemoveFeature":
			mutationSucceded = this.removeRandomFeature();
			break;
		case "NormalizeWeight":
			mutationSucceded = this.normalizeWeights(150);
			break;
		default:
			throw new RuntimeException("Mutation " + mutation + " does not exist!");
		}
		
		if (!this.isDeterministic()) {
        	throw new IllegalStateException("Weighted Automaton not determinstic!");
        }
        
        if (!this.isAccepting()) {
        	throw new IllegalStateException("Weighted Automaton is not accepting!");
        }   
				
		this.updateName();	
		
		return mutationSucceded;
	}
}

package jautomata.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Bastian Polewka
*/
public class Transition {
    private State fromState;
    private State toState;
    private String action;
    private Map<String, Integer> weights;

    public Transition(State fromState, State toState, String action, Map<String, Integer> weights) {
        this.fromState = fromState;
        this.toState = toState;
        this.action = action;
        this.weights = weights;
    }
    
    public Transition(Transition another, Map<State, State> stateMapping) {
        this.fromState = stateMapping.get(another.fromState);
        this.toState = stateMapping.get(another.toState);
        this.action = another.action;
        this.weights = new HashMap<>(another.weights);
    }

    public State getFromState() {
        return fromState;
    }
    
    public void setFromState(State fromState) {
        this.fromState = fromState;
    }

    public State getToState() {
        return toState;
    }
    
    public void setToState(State toState) {
        this.toState = toState;
    }

    public String getAction() {
        return action;
    }

    public Map<String, Integer> getWeight() {
        return weights;
    }
    
    public String getWeightAsString() {
    	StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Integer> entry : weights.entrySet()) {
            sb.append(entry.getKey())
              .append(":")
              .append(entry.getValue())
              .append(", ");
        }

        // Remove the last comma and space if there are entries
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }

        return sb.toString();
    }
    
    public void setWeight(Map<String, Integer> weights) {
        this.weights = weights;
    }

    @Override
    public String toString() {
        return "Transition{" +
                "from=" + fromState.getId() +
                ", to=" + toState.getId() +
                ", action='" + action + '\'' +
                ", weights=" + weights +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false; 
        }
        Transition other = (Transition) obj;
        return Objects.equals(fromState, other.fromState) &&
               Objects.equals(toState, other.toState) &&
               Objects.equals(action, other.action) &&
               Objects.equals(weights, other.weights);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromState, toState, action, weights);
    }
}

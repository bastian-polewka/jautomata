package jautomata.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * @author Bastian Polewka
*/
public class Action {
	private String name;
	private Map<String, Integer> weights;
	
	public Action(String name) {
		this.name = name;
		weights = new HashMap<>();
	}
	

	public String getName() {
		return name;
	}
	
	public boolean addWeight(String feature, int weight) {	
		if (weights.containsKey(feature)) {
            return true;
        } 
		else {
        	weights.put(feature, weight);
            return false;
        }
	}
	
	public void addWeightMap(Map<String, Integer> weightMap) {
		weights = weightMap;
	}
	
	public Map<String, Integer> getWeights() {
		return weights;
	}
	
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Action action = (Action) o;
        return Objects.equals(name, action.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
    	String text = "Action{" + "name='" + name + "'";
    	    	
    	text += "}";
    	
        return text;
    }
}

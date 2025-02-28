package jautomata.generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import jautomata.extensions.analysis.LowerBoundednessProperty;
import jautomata.extensions.analysis.NonEmptinessProperty;
import jautomata.extensions.analysis.UniversalityProperty;
import jautomata.extensions.analysis.UpperBoundednessProperty;
import jautomata.extensions.util.CSVWriter;

/**
 * @author Bastian Polewka
 * @author Robert Müller
*/
public class Evaluate {

	public static void main(String[] args) throws Exception {	
		
		int seed;
		int numStates;
		int numTransitions;
		int minWeight;
		int maxWeight;
		int numActions;
		int numFeatures;
		boolean selfLoop;
		int maxCombinationSize;
		int[] multiplyWeight; 
		String path = "output";
		String automatonName = "";
		String mutation = "None";
		 
		
		if (args.length == 0) {
			seed = 42;
			numStates = 4;
			numTransitions = numStates + 2;
			minWeight = 1;
			maxWeight = 4;
			numActions = 3;
			numFeatures = 5;
			selfLoop = true;
			maxCombinationSize = 2;
			multiplyWeight = new int[]{1, 4, 7};
			path = "output";
			automatonName = "testName";
		} 
		else {
			System.out.println("Command-line arguments passed");
			seed = Integer.parseInt(args[0]);
			numStates = Integer.parseInt(args[1]);
			numTransitions = Integer.parseInt(args[2]) + numStates;
			minWeight = Integer.parseInt(args[3]);
			maxWeight = Integer.parseInt(args[4]);
			numActions = Integer.parseInt(args[5]);
			numFeatures = Integer.parseInt(args[6]);
			selfLoop = Boolean.parseBoolean(args[7]);
			maxCombinationSize = Integer.parseInt(args[8]);				
			multiplyWeight = parseToIntArray(args[9]);
			if (!args[10].isEmpty()) {
				mutation = args[10];
			}
				
			path += "/Seed" + seed +"/";
			for (int i = 1; i <= 7; i++) {
                path += args[i] + "_";
                automatonName += args[i] + "_";
            }	
			path = path.substring(0, path.length() - 1);
			automatonName = automatonName.substring(0, automatonName.length() - 1);		
		}
		
		try {
			WAGenerator waGenerator = new WAGenerator(seed, numStates, numTransitions, minWeight, maxWeight, numActions, numFeatures, selfLoop);
		
			WeightedAutomaton weightedAutomaton = waGenerator.getWeightedAutomaton();
			weightedAutomaton.setName(automatonName);
			
//			GraphVisualizer gv = new GraphVisualizer();
//			gv.visualizeGraph(weightedAutomaton);
					
				
			final Path outputDir = Path.of(path);
			
			try {
				if (!Files.isDirectory(outputDir)) {
					Files.createDirectory(outputDir);
				}
			} catch (IOException e) {
				throw new RuntimeException("Failed to create output folder", e);
			}
			
			
			if(!weightedAutomaton.applyMutation(mutation)) {
				System.out.println("Mutation could either not been applied or mutation does not exist.");
				return;
			}
			
			weightedAutomaton.generateTestConyfigurations(maxCombinationSize, multiplyWeight);	
			weightedAutomaton.loadExperimentSettings("automata/experiment_settings.json");
					
			String experimentType = "";
			
			if (weightedAutomaton.getExperimentSettings().get(0).numRealRuns() == 1) {
				experimentType = "effectiveness";
			}
			else {
				experimentType = "efficiency";
			}
					
			final File csvFile = outputDir.resolve("results-" + experimentType + mutation +".csv").toFile();

			if (csvFile.exists()) {
				throw new RuntimeException("Results file '" + csvFile.getPath() + "' already exists. Delete or rename it first.");
			}
			
			System.out.println("#".repeat(60));
			System.out.println("Evaluating " + experimentType);
			System.out.println("#".repeat(60));
			System.out.println();
			
			final long startTime = System.nanoTime();
			
			evaluateExperiments(csvFile, weightedAutomaton);
			
			System.out.println();
			System.out.println();
			System.out.println("Done. Total runtime: " + Duration.ofNanos(System.nanoTime() - startTime).toSeconds() + " s");
			
			weightedAutomaton.saveJson(path + "/automaton" + mutation + ".json");

		} catch (TransitionActionException e) {
			System.out.println(e);
			System.exit(0);
		}	
	}
	
	
	private static void evaluateExperiments(File csvFile, WeightedAutomaton weightedAutomaton) {
		List<ExperimentSettings> experiments = weightedAutomaton.getExperimentSettings();
		
		System.out.println("Total number of experiment runs: " + experiments.stream().mapToLong(ExperimentSettings::totalNumExperiments).sum());

		try (CSVWriter csvWriter = new CSVWriter(new BufferedWriter(new FileWriter(csvFile)))) {
			csvWriter.setToStringFunction(object -> {
				if (object instanceof Boolean) {
					return Integer.toString((Boolean)object ? 1 : 0);
				}
				return Objects.toString(object);
			});
			csvWriter.writeColumnSeparatorHint();
			csvWriter.write("Automaton", "Property", "Property parameter", "k", "Type", "Run",
					"Satisfied", "Satisfied (ground truth)", "Correct", "Runtime (ms)");

			for (ExperimentSettings settings : experiments) {			
				evaluateExperiment(settings, csvWriter, weightedAutomaton, csvFile);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to write results to CSV file", e);
		}
	}
	
	
	private static void evaluateExperiment(ExperimentSettings settings, CSVWriter csvWriter, WeightedAutomaton weightedAutomaton, File csvFile) throws IOException {
		System.out.println("=".repeat(60));
		System.out.println("Evaluating automaton: " + settings.automatonName());
		System.out.println("=".repeat(60));
		System.out.println();
		System.out.println("Number of experiment runs: " + settings.totalNumExperiments());
		System.out.println();

		List<Boolean> nonEmptiness = new ArrayList<>();
		List<Boolean> universality = new ArrayList<>();
		boolean lowerBoundedness = false;
		boolean upperBoundedness = false;
		
		try {
			nonEmptiness = evaluateNonEmptiness(settings, csvWriter, weightedAutomaton, nonEmptiness);
        } catch (StackOverflowError e) {
        	System.out.println("Caught StackOverflowError possibly because of PathOrder."); 	
        	return;    	     	
        }
		
		try {
			universality = evaluateUniversality(settings, csvWriter, weightedAutomaton, universality);
        } catch (StackOverflowError e) {
        	System.out.println("Caught StackOverflowError possibly because of PathOrder.");
        	return; 
        }
		
		try {
			lowerBoundedness = evaluateLowerBoundedness(settings, csvWriter);
        } catch (StackOverflowError e) {
        	System.out.println("Caught StackOverflowError possibly because of PathOrder.");
        	return;
        }
		
		try {
			upperBoundedness = evaluateUpperBoundedness(settings, csvWriter);
        } catch (StackOverflowError e) {
        	System.out.println("Caught StackOverflowError possibly because of PathOrder.");
        	return; 	
        }
		
		List<TestConfiguration> updatedTestConfigurations = new ArrayList<>();
		List<TestConfiguration> testConfigurations = new ArrayList<>(settings.testConfigurations());
		
		if (weightedAutomaton.getExperimentSettings().get(0).numRealRuns() == 1) {		
			for (int i = 0; i < testConfigurations.size(); i++) {
				TestConfiguration t = new TestConfiguration(testConfigurations.get(i).configuration(), nonEmptiness.get(i), universality.get(i));
				updatedTestConfigurations.add(t);
			}	
			weightedAutomaton.updateGroundTruths(updatedTestConfigurations, lowerBoundedness, upperBoundedness);
		}
		
		System.out.println();	
	}

	
	private static List<Boolean> evaluateNonEmptiness(ExperimentSettings settings, CSVWriter csvWriter, WeightedAutomaton weightedAutomaton, List<Boolean> groundTruths) throws IOException {
		Set<String> featureSet = weightedAutomaton.getFeatures();
		for (TestConfiguration testConfiguration : settings.testConfigurations()) {
			final String propertyName = "Non-emptiness";
			System.out.println("Evaluating: " + propertyName + " " + testConfiguration.configuration());
				
			if (settings.convergenceThreshold() == 0) {
				// GroundTruth is available
				for (int k : settings.numPathsCombinations()) {
					for (int warmupRun = 1; warmupRun <= settings.numWarmupRuns(); warmupRun++) {
						var result = NonEmptinessProperty.evaluate(settings.automaton(), k, testConfiguration.expandedConfiguration(featureSet));
						csvWriter.write(settings.automatonName(), propertyName, testConfiguration.configuration(), k, "warmup",
								warmupRun, result.satisfied(), testConfiguration.nonEmptinessGroundTruth(),
								result.satisfied() == testConfiguration.nonEmptinessGroundTruth(), result.runtime().toMillis());
						System.out.print(".");
					}
					for (int realRun = 1; realRun <= settings.numRealRuns(); realRun++) {
						var result = NonEmptinessProperty.evaluate(settings.automaton(), k, testConfiguration.expandedConfiguration(featureSet));
						csvWriter.write(settings.automatonName(), propertyName, testConfiguration.configuration(), k, "real",
								realRun, result.satisfied(), testConfiguration.nonEmptinessGroundTruth(),
								result.satisfied() == testConfiguration.nonEmptinessGroundTruth(), result.runtime().toMillis());
						System.out.print(".");
					}
				}
			}
			else {
				// GroundTruth needs to be determined
				int convergence = 0;
				int k = settings.convergenceStepSize();		
				List<List<Object>> dataLines = new ArrayList<>();
				
				var prevResult = NonEmptinessProperty.evaluate(settings.automaton(), k, testConfiguration.expandedConfiguration(featureSet));
						
				while (convergence <= settings.convergenceThreshold()) {
					for (int warmupRun = 1; warmupRun <= settings.numWarmupRuns(); warmupRun++) {					
						var result = NonEmptinessProperty.evaluate(settings.automaton(), k, testConfiguration.expandedConfiguration(featureSet));
						
						// save result to later add to CSV
						List<Object> line = createDataLine(settings.automatonName(), propertyName, testConfiguration.configuration(), k, "warmup", warmupRun, result.satisfied(), result.runtime().toMillis());
						dataLines.add(line);
						
						System.out.print(".");
					}
					
					boolean sameResult = true;	
									
					for (int realRun = 1; realRun <= settings.numRealRuns(); realRun++) {				
						var result = NonEmptinessProperty.evaluate(settings.automaton(), k, testConfiguration.expandedConfiguration(featureSet));
						
						// save result to later add to CSV
						List<Object> line = createDataLine(settings.automatonName(), propertyName, testConfiguration.configuration(), k, "real", realRun, result.satisfied(), result.runtime().toMillis());						
						dataLines.add(line);
								
						// check for convergence
						if (prevResult.satisfied() != result.satisfied()) {
							convergence = 0;
							sameResult = false;
							System.out.print("!");
						}					
						else {
							System.out.print(".");
						}	
						prevResult = result;
					}
					
					if (sameResult) {
						convergence++;
					}
					
					k += settings.convergenceStepSize();
				}
				
				// write GroundTruth and satisfied down for all runs	
				List<Object> lastLine = dataLines.get(dataLines.size() - 1);
				boolean satisfiedGroundTruth = (boolean) lastLine.get(6);
				groundTruths.add(satisfiedGroundTruth);
				
				
				for (List<Object> line : dataLines) {
					boolean sameSatisfied = false;
					if ((boolean) line.get(6) == satisfiedGroundTruth)
						sameSatisfied = true;
					
					csvWriter.write(
							line.get(0),
							line.get(1),
							line.get(2),
							line.get(3), 
							line.get(4),
							line.get(5),
							line.get(6),
							satisfiedGroundTruth,
							sameSatisfied,
							line.get(7));
				}					
			}				
			System.out.println();
		}
		return groundTruths;
	}


	private static List<Boolean> evaluateUniversality(ExperimentSettings settings, CSVWriter csvWriter, WeightedAutomaton weightedAutomaton, List<Boolean> groundTruths) throws IOException {
		Set<String> featureSet = weightedAutomaton.getFeatures();
		for (TestConfiguration testConfiguration : settings.testConfigurations()) {
			final String propertyName = "Universality";
			System.out.println("Evaluating: " + propertyName + " " + testConfiguration.configuration());
			
			if (settings.convergenceThreshold() == 0) {
				// GroundTruth is available	
				for (int k : settings.numPathsCombinations()) {
					for (int warmupRun = 1; warmupRun <= settings.numWarmupRuns(); warmupRun++) {
						var result = UniversalityProperty.evaluate(settings.automaton(), k, testConfiguration.expandedConfiguration(featureSet));
						csvWriter.write(settings.automatonName(), propertyName, testConfiguration.configuration(), k, "warmup",
								warmupRun, result.satisfied(), testConfiguration.universalityGroundTruth(),
								result.satisfied() == testConfiguration.universalityGroundTruth(), result.runtime().toMillis());
						System.out.print(".");
					}
					for (int realRun = 1; realRun <= settings.numRealRuns(); realRun++) {
						var result = UniversalityProperty.evaluate(settings.automaton(), k, testConfiguration.expandedConfiguration(featureSet));
						csvWriter.write(settings.automatonName(), propertyName, testConfiguration.configuration(), k, "real",
								realRun, result.satisfied(), testConfiguration.universalityGroundTruth(),
								result.satisfied() == testConfiguration.universalityGroundTruth(), result.runtime().toMillis());
						System.out.print(".");
					}
				}
			}
			else {
				// GroundTruth needs to be determined
				int convergence = 0;
				int k = settings.convergenceStepSize();
				List<List<Object>> dataLines = new ArrayList<>();
				
				var prevResult = UniversalityProperty.evaluate(settings.automaton(), k, testConfiguration.expandedConfiguration(featureSet));
				
				while (convergence <= settings.convergenceThreshold()) {		
					for (int warmupRun = 1; warmupRun <= settings.numWarmupRuns(); warmupRun++) {					
						var result = UniversalityProperty.evaluate(settings.automaton(), k, testConfiguration.expandedConfiguration(featureSet));
						
						// save result to later add to CSV
						List<Object> line = createDataLine(settings.automatonName(), propertyName, testConfiguration.configuration(), k, "warmup", warmupRun, result.satisfied(), result.runtime().toMillis());			
						dataLines.add(line);
						
						System.out.print(".");
					}
					
					boolean sameResult = true;
								
					for (int realRun = 1; realRun <= settings.numRealRuns(); realRun++) {				
						var result = UniversalityProperty.evaluate(settings.automaton(), k, testConfiguration.expandedConfiguration(featureSet));
						
						// save result to later add to CSV
						List<Object> line = createDataLine(settings.automatonName(), propertyName, testConfiguration.configuration(), k, "real", realRun, result.satisfied(), result.runtime().toMillis());						
						dataLines.add(line);
										
						// check for convergence
						if (prevResult.satisfied() != result.satisfied()) {
							convergence = 0;
							sameResult = false;
							System.out.print("!");
						}					
						else {
							System.out.print(".");
						}	
						prevResult = result;
					}
					
					if (sameResult) {
						convergence++;
					}
					
					k += settings.convergenceStepSize();
				}
				
				// write GroundTruth and satisfied down for all runs	
				List<Object> lastLine = dataLines.get(dataLines.size() - 1);				
				boolean satisfiedGroundTruth = (boolean) lastLine.get(6);
				groundTruths.add(satisfiedGroundTruth);				
				
				for (List<Object> line : dataLines) {
					boolean sameSatisfied = false;				
					if ((boolean) line.get(6) == satisfiedGroundTruth)
						sameSatisfied = true;
					
					csvWriter.write(
							line.get(0),
							line.get(1),
							line.get(2),
							line.get(3), 
							line.get(4),
							line.get(5),
							line.get(6),
							satisfiedGroundTruth,
							sameSatisfied,
							line.get(7));
				}					
			}
			System.out.println();
		}
		return groundTruths;
	}

	
	private static boolean evaluateLowerBoundedness(ExperimentSettings settings, CSVWriter csvWriter) throws IOException {
		final String propertyName = "Lower boundedness";
		System.out.println("Evaluating: " + propertyName);
		boolean groundTruth = false;
		
		if (settings.convergenceThreshold() == 0) {
			// GroundTruth is available	
			for (int k : settings.numPathsCombinations()) {
				for (int warmupRun = 1; warmupRun <= settings.numWarmupRuns(); warmupRun++) {
					var result = LowerBoundednessProperty.evaluate(settings.automaton(), k);
					csvWriter.write(settings.automatonName(), propertyName, "", k, "warmup",
							warmupRun, result.satisfied(), settings.lowerBoundednessGroundTruth(),
							result.satisfied() == settings.lowerBoundednessGroundTruth(), result.runtime().toMillis());
					System.out.print(".");
				}
				for (int realRun = 1; realRun <= settings.numRealRuns(); realRun++) {
					var result = LowerBoundednessProperty.evaluate(settings.automaton(), k);
					csvWriter.write(settings.automatonName(), propertyName, "", k, "real",
							realRun, result.satisfied(), settings.lowerBoundednessGroundTruth(),
							result.satisfied() == settings.lowerBoundednessGroundTruth(), result.runtime().toMillis());
					System.out.print(".");
				}
			}
		}
		else {
			// GroundTruth needs to be determined
			int convergence = 0;
			int k = settings.convergenceStepSize();
			List<List<Object>> dataLines = new ArrayList<>();
			
			var prevResult = LowerBoundednessProperty.evaluate(settings.automaton(), k);
			
			while (convergence <= settings.convergenceThreshold()) {		
				for (int warmupRun = 1; warmupRun <= settings.numWarmupRuns(); warmupRun++) {					
					var result = LowerBoundednessProperty.evaluate(settings.automaton(), k);
					
					// save result to later add to CSV
					List<Object> line = createDataLine(settings.automatonName(), propertyName, null, k, "warmup", warmupRun, result.satisfied(), result.runtime().toMillis());			
					dataLines.add(line);
					
					System.out.print(".");
				}
				
				boolean sameResult = true;
				
				for (int realRun = 1; realRun <= settings.numRealRuns(); realRun++) {				
					var result = LowerBoundednessProperty.evaluate(settings.automaton(), k);
					
					// save result to later add to CSV
					List<Object> line = createDataLine(settings.automatonName(), propertyName, null, k, "real", realRun, result.satisfied(), result.runtime().toMillis());			
					dataLines.add(line);
									
					// check for convergence
					if (prevResult.satisfied() != result.satisfied()) {
						convergence = 0;
						sameResult = false;					
						System.out.print("!");
					}					
					else {
						System.out.print(".");
					}	
					prevResult = result;
				}
				
				if (sameResult) {
					convergence++;
				}
				
				k += settings.convergenceStepSize();
			}
			
			// write GroundTruth and satisfied down for all runs	
			List<Object> lastLine = dataLines.get(dataLines.size() - 1);				
			boolean satisfiedGroundTruth = (boolean) lastLine.get(6);
			groundTruth = satisfiedGroundTruth;

			for (List<Object> line : dataLines) {
				boolean sameSatisfied = false;				
				if ((boolean) line.get(6) == satisfiedGroundTruth)
					sameSatisfied = true;
				
				csvWriter.write(
						line.get(0),
						line.get(1),
						line.get(2),
						line.get(3), 
						line.get(4),
						line.get(5),
						line.get(6),
						satisfiedGroundTruth,
						sameSatisfied,
						line.get(7));
			}					
		}			
		System.out.println();
		return groundTruth;
	}

	
	private static boolean evaluateUpperBoundedness(ExperimentSettings settings, CSVWriter csvWriter) throws IOException {
		final String propertyName = "Upper boundedness";
		System.out.println("Evaluating: " + propertyName);
		boolean groundTruth = false;
		
		if (settings.convergenceThreshold() == 0) {
			// GroundTruth is available	
			for (int k : settings.numPathsCombinations()) {
				for (int warmupRun = 1; warmupRun <= settings.numWarmupRuns(); warmupRun++) {
					var result = UpperBoundednessProperty.evaluate(settings.automaton(), k);
					csvWriter.write(settings.automatonName(), propertyName, "", k, "warmup",
							warmupRun, result.satisfied(), settings.upperBoundednessGroundTruth(),
							result.satisfied() == settings.upperBoundednessGroundTruth(), result.runtime().toMillis());
					System.out.print(".");
				}
				for (int realRun = 1; realRun <= settings.numRealRuns(); realRun++) {
					var result = UpperBoundednessProperty.evaluate(settings.automaton(), k);
					csvWriter.write(settings.automatonName(), propertyName, "", k, "real",
							realRun, result.satisfied(), settings.upperBoundednessGroundTruth(),
							result.satisfied() == settings.upperBoundednessGroundTruth(), result.runtime().toMillis());
					System.out.print(".");
				}
			}
		}
		else {
			// GroundTruth needs to be determined
			int convergence = 0;
			int k = settings.convergenceStepSize();
			List<List<Object>> dataLines = new ArrayList<>();
			
			var prevResult = UpperBoundednessProperty.evaluate(settings.automaton(), k);
			
			while (convergence <= settings.convergenceThreshold()) {		
				for (int warmupRun = 1; warmupRun <= settings.numWarmupRuns(); warmupRun++) {					
					var result = UpperBoundednessProperty.evaluate(settings.automaton(), k);
					
					// save result to later add to CSV
					List<Object> line = createDataLine(settings.automatonName(), propertyName, null, k, "warmup", warmupRun, result.satisfied(), result.runtime().toMillis());	
					line.add(settings.automatonName());
					line.add(propertyName);
					line.add("");
					line.add(k);
					line.add("warmup");
					line.add(warmupRun);
					line.add(result.satisfied());
					line.add(result.runtime().toMillis());				
					dataLines.add(line);
					
					System.out.print(".");
				}
				
				boolean sameResult = true;
				
				for (int realRun = 1; realRun <= settings.numRealRuns(); realRun++) {				
					var result = UpperBoundednessProperty.evaluate(settings.automaton(), k);
					
					// save result to later add to CSV
					List<Object> line = createDataLine(settings.automatonName(), propertyName, null, k, "real", realRun, result.satisfied(), result.runtime().toMillis());					
					dataLines.add(line);
									
					// check for convergence
					if (prevResult.satisfied() != result.satisfied()) {
						convergence = 0;
						sameResult = false;						
						System.out.print("!");
					}
					else {
						System.out.print(".");
					}
					prevResult = result;
				}
				
				if (sameResult) {
					convergence++;
				}
				
				k += settings.convergenceStepSize();
			}
			
			// write GroundTruth and satisfied down for all runs	
			List<Object> lastLine = dataLines.get(dataLines.size() - 1);				
			boolean satisfiedGroundTruth = (boolean) lastLine.get(6);
			groundTruth = satisfiedGroundTruth;

			for (List<Object> line : dataLines) {
				boolean sameSatisfied = false;				
				if ((boolean) line.get(6) == satisfiedGroundTruth)
					sameSatisfied = true;
				
				csvWriter.write(
						line.get(0),
						line.get(1),
						line.get(2),
						line.get(3), 
						line.get(4),
						line.get(5),
						line.get(6),
						satisfiedGroundTruth,
						sameSatisfied,
						line.get(7));
			}					
		}
		System.out.println();
		return groundTruth;
	}	
	
	
	private static List<Object> createDataLine(String automatonName, String propertyName, Map<String, Integer> testConfiguration, int k, String runType, int runNumber, boolean satisfied, long runtime) {
		List<Object> line = new ArrayList<>();
		
		line.add(automatonName);
		line.add(propertyName);
		
		if (testConfiguration == null) {
			line.add("");
		}
		else {
			line.add(testConfiguration);
		}
		
		line.add(k);
		line.add(runType);
		line.add(runNumber);
		line.add(satisfied);
		line.add(runtime);						
		
		return line;				
	}
	
	
	private static int[] parseToIntArray(String input) {
		input = input.replaceAll("[\\[\\] ]", "");
        String[] stringNumbers = input.split(",");    

        int[] numbers = new int[stringNumbers.length];
        for (int i = 0; i < stringNumbers.length; i++) {
            numbers[i] = Integer.parseInt(stringNumbers[i].trim());
        }      
		return numbers;
	}
}

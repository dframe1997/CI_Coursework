package coursework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import model.Fitness;
import model.Individual;
import model.LunarParameters.DataSet;
import model.NeuralNetwork;

/**
 * Implements a basic Evolutionary Algorithm to train a Neural Network
 * 
 * You Can Use This Class to implement your EA or implement your own class that extends {@link NeuralNetwork} 
 * 
 */
public class ExampleEvolutionaryAlgorithm extends NeuralNetwork {
	

	/**
	 * The Main Evolutionary Loop
	 */
	@Override
	public void run() {		
		//Initialise a population of Individuals with random weights
		population = initialise();

		//Record a copy of the best Individual in the population
		best = getBest();
		System.out.println("Best From Initialisation " + best);

		/**
		 * main EA processing loop
		 */		
		
		while (evaluations < Parameters.maxEvaluations) {

			/**
			 * this is a skeleton EA - you need to add the methods.
			 * You can also change the EA if you want 
			 * You must set the best Individual at the end of a run
			 * 
			 */

			// Select parents from the current population.
			ArrayList<Individual> parents = new ArrayList<Individual>();
			int parentNum = 2;
			int replacements = 1; //The number of bad ones to replace
			for(int i = 0; i < parentNum; i++) {
				Individual newParent = select();
				boolean duplicate = false;
				do {
					duplicate = false;
					for(Individual parent : parents) {
						if(Arrays.equals(newParent.chromosome,parent.chromosome)) {
							duplicate = true;
							newParent = select();
						}
					}
				} while(duplicate);
				parents.add(newParent);
			}
			
			// Generate a child by crossover.		
			ArrayList<Individual> children = reproduce(parents, replacements);		
			children.add(population.get(getWorstIndex()).copy());
			
			//mutate the offspring
			mutate(children, false);
			
			ArrayList<Individual> worst = new ArrayList<Individual>();
			worst.add(population.get(getWorstIndex()).copy());
			mutate(worst, true);
			
			// Evaluate the children
			evaluateIndividuals(children);
			evaluateIndividuals(worst);

			// Replace old individuals in population with new children
			replace(children);
			replace(worst);
			

			// check to see if the best has improved
			best = getBest();
			
			// Implemented in NN class. 
			outputStats();
			double totalFitness = 0;
			for(Individual person: population) {
				totalFitness += person.fitness;
			}
			double average = totalFitness/Parameters.popSize;
			System.out.println("Average fitness: " + average);
			//Increment number of completed generations			
		}

		//save the trained network to disk
		saveNeuralNetwork();
	}

	

	/**
	 * Sets the fitness of the individuals passed as parameters (whole population)
	 * 
	 */
	private void evaluateIndividuals(ArrayList<Individual> individuals) {
		for (Individual individual : individuals) {
			individual.fitness = Fitness.evaluate(individual, this);
		}
	}


	/**
	 * Returns a copy of the best individual in the population
	 * 
	 */
	private Individual getBest() {
		best = null;;
		for (Individual individual : population) {
			if (best == null) {
				best = individual.copy();
			} else if (individual.fitness < best.fitness) {
				best = individual.copy();
			}
		}
		return best;
	}
	
	/**
	 * Returns a copy of the best individual in the selection
	 * 
	 */
	private Individual getBestFromSelection(ArrayList<Individual> selection) {
		best = null;
		//99% chance to replace the worst one, 1% chance of replacing a random one
		Random rand = new Random();
		// Obtain a number between [0 - 100].
		int chance = rand.nextInt(100);
		
		if(chance <= 80){
			for (Individual individual : selection) {
				if (best == null) {
					best = individual.copy();
				} else if (individual.fitness < best.fitness) {
					best = individual.copy();
				}
			}
		}
		else {
			best = selection.get(rand.nextInt(selection.size()));
			//best = getBest();
		}
		return best;
	}

	/**
	 * Generates a randomly initialised population
	 * 
	 */
	private ArrayList<Individual> initialise() {
		population = new ArrayList<>();
		for (int i = 0; i < Parameters.popSize; ++i) {
			//chromosome weights are initialised randomly in the constructor
			Individual individual = new Individual();
			population.add(individual);
		}
		evaluateIndividuals(population);
		return population;
	}

	/**
	 * Selection --
	 * 
	 * EDITED
	 * 
	 */
	private Individual select() {	
		//Occasionally pick random to allow escape from local maximum
		//Another Option: Roulette Wheel - dosen't work for negative fitness
		
		//Tournament
		//Have to be in the top 75%

		Individual tournamentWinner = tournament(8);
		
		//Individual parent = population.get(Parameters.random.nextInt(Parameters.popSize));
		return tournamentWinner.copy();
	}
	
	//Tournament to pick the best from a random selection of size == tournamentSize
	private Individual tournament(int tournamentSize){
		ArrayList<Individual> selection = new ArrayList<Individual>();
		Random rand = new Random();
		for(int i = 0; i < tournamentSize; i++){
			Individual newPerson = population.get(Parameters.random.nextInt(Parameters.popSize));
			
			
			Collections.sort(population, Collections.reverseOrder());
			
			int splitIndex = Parameters.popSize/4;
			//int chance = rand.nextInt(1);
			while(newPerson.fitness > population.get(splitIndex).fitness)
			{
				newPerson = population.get(Parameters.random.nextInt(Parameters.popSize));
			}
			selection.add(newPerson);
		}
		return getBestFromSelection(selection);
	}

	/**
	 * Crossover / Reproduction
	 * 
	 * EDITED
	 * 
	 */
	private ArrayList<Individual> reproduce(ArrayList<Individual> parents, int numOfChildren) {
		ArrayList<Individual> children = new ArrayList<>();
		Random rand = new Random();
		
		for(int childCount = 0; childCount < numOfChildren; childCount++){
			Individual newChild = new Individual();
			
			ArrayList<Double> crossoverPoints = new ArrayList<Double>();
			
			if(parents.size() >= 2) {
				//Generate crossover points
				int numCrossoverPoints = parents.size() - 1;
				double crossoverInterval = Math.floor(parents.get(0).chromosome.length/numCrossoverPoints);
				for(int i = 1; i <= numCrossoverPoints; i++) {
					crossoverPoints.add(crossoverInterval*i);
				}
			}
			
			for (int i = 0; i < parents.get(0).chromosome.length; i++) {				
				// Either add or take away the chromosome values
				/*int chance = rand.nextInt(1);
				if(chance == 0){
					newChild.chromosome[i] = parent1.chromosome[i] + parent2.chromosome[i];
				}
				else{
					newChild.chromosome[i] = parent1.chromosome[i] - parent2.chromosome[i];
				}*/
				
				//Largest chromosome
				//Single point crossover
				for(int j = 0; j < crossoverPoints.size(); j++) {
					if(j == 0) {
						if(i <= crossoverPoints.get(j)){
							newChild.chromosome[i] = parents.get(j).chromosome[i];
						}
					}
					else if(j < crossoverPoints.size()-1) {
						if(i <= crossoverPoints.get(j) && i > crossoverPoints.get(j-1)){
							newChild.chromosome[i] = parents.get(j).chromosome[i];
						}
					}
					
					else if (i > crossoverPoints.get(j)){
						newChild.chromosome[i] = parents.get(j).chromosome[i];
					}
				}
			}
			children.add(newChild);
		}
		return children;
	} 
	
	/**
	 * Mutation
	 * 
	 * 
	 */
	private void mutate(ArrayList<Individual> individuals, boolean alwaysMutate) {		
		for(Individual individual : individuals) {
			for (int i = 0; i < individual.chromosome.length; i++) {
				if (Parameters.random.nextDouble() < Parameters.mutateRate || alwaysMutate == true) {
					if (Parameters.random.nextBoolean() && individual.chromosome[i] + (Parameters.mutateChange) <= Parameters.maxGene) {
						individual.chromosome[i] += (Parameters.mutateChange);
					} else if (individual.chromosome[i] + (Parameters.mutateChange) >= Parameters.minGene) {
						individual.chromosome[i] -= (Parameters.mutateChange);
					}
				}
			}
		}		
	}

	/**
	 * NEEDS REPLACING (I added this as it is required in the spec)
	 * Replaces the worst member of the population 
	 * (regardless of fitness)
	 * 
	 */
	private void replace(ArrayList<Individual> children) {
		for(Individual child : children) {
			int index = getWorstIndex();
			Individual deadManStanding = population.get(index);
			if(deadManStanding.fitness > child.fitness) {
				population.set(index, child);
			}	
		}		
	}

	

	/**
	 * Returns the index of the worst member of the population
	 * @return
	 */
	private int getWorstIndex() {
		Individual worst = null;
		int idx = -1;
		for (int i = 0; i < population.size(); i++) {
			Individual individual = population.get(i);
			if (worst == null) {
				worst = individual;
				idx = i;
			} else if (individual.fitness > worst.fitness) {
				worst = individual;
				idx = i; 
			}
		}
		return idx;
	}	

	@Override
	public double activationFunction(double x) {
		if (x < -20.0) {
			return -1.0;
		} else if (x > 20.0) {
			return 1.0;
		}
		return Math.tanh(x);
	}
}

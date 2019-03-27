package coursework;

import java.util.ArrayList;
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

			// Select 2 Individuals from the current population.
			Individual parent1 = select(); 
			Individual parent2 = select();
			while(parent2 == parent1){
				parent2 = select();
				System.out.println("Same parents");
			}

			// Generate a child by crossover.		
			ArrayList<Individual> children = reproduce(parent1, parent2, 1);			
			
			//mutate the offspring
			mutate(children);
			
			// Evaluate the children
			evaluateIndividuals(children);			

			// Replace old individuals in population with new children
			replace(children);

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
		

		Individual tournamentWinner = tournament(8);
		
		//Individual parent = population.get(Parameters.random.nextInt(Parameters.popSize));
		return tournamentWinner.copy();
	}
	
	//Tournament to pick the best from a random selection of size == tournamentSize
	private Individual tournament(int tournamentSize){
		ArrayList<Individual> selection = new ArrayList<Individual>();
		for(int i = 0; i < tournamentSize; i++){
			selection.add(population.get(Parameters.random.nextInt(Parameters.popSize)));
		}
		return getBestFromSelection(selection);
	}

	/**
	 * Crossover / Reproduction
	 * 
	 * EDITED
	 * 
	 */
	private ArrayList<Individual> reproduce(Individual parent1, Individual parent2, int numOfChildren) {
		ArrayList<Individual> children = new ArrayList<>();
		Random rand = new Random();
		
		for(int childCount = 0; childCount < numOfChildren; childCount++){
			Individual newChild = new Individual();
			double crossoverPoint = Math.floor(parent1.chromosome.length/2);
			for (int i = 0; i < parent1.chromosome.length; i++) {				
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
				if(i >= crossoverPoint){
					newChild.chromosome[i] = parent1.chromosome[i];
				}
				else{
					newChild.chromosome[i] = parent2.chromosome[i];
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
	private void mutate(ArrayList<Individual> individuals) {		
		for(Individual individual : individuals) {
			for (int i = 0; i < individual.chromosome.length; i++) {
				if (Parameters.random.nextDouble() < Parameters.mutateRate) {
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
			//if(deadManStanding.fitness > child.fitness) {
				population.set(index, child);
			//}	
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

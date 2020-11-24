package com.mugunga.musicmodels;

/**
 * Mathmatical operations are used mostly to determine metrics and statistics surrounding the melodies generates by
 * the algorimth, this will be used to populate the CounterPointStats class as the algorthim runs. 
 * @author laurencemarrin
 *
 */
public final class MathUtility {
	/**
	 * Return the standard deviation of an array of ints, ie the standard deviation of amelody around it's mean
	 * @param numbers
	 * @return
	 */
	public static double standardDeviation(int[] numbers) {
		double mean = mean(numbers);
		double standard_deviation;
		double accumulator = 0;
		for(int i = 0; i < numbers.length; i++) {
			accumulator += Math.pow((numbers[i] - mean), 2);
		}
		return Math.sqrt(accumulator/numbers.length);
		
	}
	
	/**
	 * Return the mean of an array of integers, ie the mean index of a melody. 
	 * @param numbers
	 * @return
	 */
	public static double mean(int[] numbers) {
		double sum = 0;
		for(int i = 0; i < numbers.length; i++) {
			sum += numbers[i];
			//System.out.println(sum);
		}
		return (sum/numbers.length);
	}
	
	/**
	 * When wanting to assume the tonic or starting note as a mean, we calculate the standard deviation around that, 
	 * rather than around the mathematical mean of the melody. 
	 * @param numbers
	 * @return
	 */
	public static double stdFromTonic(int[] numbers) {
		return 0;
	}

	
}

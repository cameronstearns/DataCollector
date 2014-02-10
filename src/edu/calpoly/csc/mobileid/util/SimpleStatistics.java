package edu.calpoly.csc.mobileid.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleStatistics {

   public static double calculateAverage(List<Double> list) {
      double total = 0;
      for (Double d : list) {
         total += d;
      }

      double mean = total / list.size();
      return mean;
   }

   public static double standardDev(List<Double> list) {
      double mean = calculateAverage(list);
      double dev = 0;
      for (Double d : list) {
         dev += (d - mean) * (d - mean);
      }
      dev /= (list.size() - 1);

      return Math.sqrt(dev);
   }

   public static double averageAbsoluteDifference(List<Double> list) {
      double avgAbsDiff = 0;
      for (Double d1 : list) {
         for (Double d2 : list) {
            avgAbsDiff += Math.abs(d1 - d2);
         }
      }
      avgAbsDiff /= list.size() * list.size();
      return avgAbsDiff;
   }

   public static double averageResultantAccl(List<Double> x, List<Double> y,
         List<Double> z) {
      double result = 0;
      for (int i = 0; i < x.size(); i++) {
         result += Math.sqrt(x.get(i) * x.get(i) + y.get(i) * y.get(i)
               + z.get(i) * z.get(i));
      }
      return result / x.size();
   }

   /**
    * Binned Distribution[30]: We determine the range of values for each axis
    * (maximum minimum), divide this range into 10 equal sized bins, and then
    * record the fraction of the 200 values that fall within each of the bins.
    *
    * @param list
    * @return
    */
   public static double[] bins(List<Double> list) {
      Double[] arr = new Double[list.size()];

      arr = list.toArray(arr);
      Arrays.sort(arr);

      double min = arr[0];
      double max = arr[arr.length - 1];
      double[] bins = new double[10];
      double range = max - min;
      range /= 10;
      for (Double d : arr) {
         int index = Double.valueOf(Math.floor((d - min) / range))
            .intValue();
         // If the final value = max... it ends up at 10. We don't want 10.
         if (index > 9) {
            index = 9;
         }
         bins[index]++;
      }
      for(int i = 0; i < bins.length; i++) {
         bins[i] /= arr.length;
      }
      return bins;
   }

   
   /**
    * A low pass filter implementation I took from some sketchy website.
    * I looked at data, and it actually wasn't as noisy as I had thought.
    * 
    * We may change our minds.
    * @param values
    */
   private static void smoothArray(List<Double> values, int filterStrength){
	   double value = values.get(0); // start with the first input
	   for (int i = 1; i < values.size(); i++){
	     double currentValue = values.get(i);
	     value += (currentValue - value) / filterStrength;
	     values.set(i, value);
	   }
	 }

	/**
	 * Time Between Peaks[3]: Time in milliseconds between peaks in the
	 * sinusoidal waves associated with most activities (for each axis)
	 * 
	 * My attempt at replicating their work. This data may be irrelevant.
	 */
	public static double timeBtwnPks(List<Double> firstList) {
		
		// Clone list... kind of.
		List<Double> list = new ArrayList<Double>(firstList);


		// Low pass filter
		smoothArray(list, 20);

		double threshold = .9;
		double max = list.get(0);
		double min = list.get(0);
		for (int i = 0; i < list.size(); i++) {
			if (max < list.get(i)) {
				max = list.get(i);
			}
			if (min > list.get(i)) {
				min = list.get(i);
			}
		}
		double range = max - min;

		List<Integer> peakIndices = new ArrayList<Integer>();
		
		// If we don't find at least 3 peaks, increase our thresh-hold
		while (peakIndices.size() < 3) {
			peakIndices = new ArrayList<Integer>();
			for(int i = 1; i < list.size() - 1; i++) {
				// Value is a local maximum AND in the threshold range
				// Does not account for noise. Try low pass filter?
				if (list.get(i) - min >= threshold * range
						&& list.get(i+1) < list.get(i)
						&& list.get(i - 1) < list.get(i)) {
					peakIndices.add(i);
				}
			}
			threshold -= .1;
			if(threshold < 0) {
				// Failure.
				System.out.println("Failure");
				return -1;
			}
		}

		double peakTotal = 0;

		for (int i = 0; i < peakIndices.size() - 1; i++) {
			peakTotal += peakIndices.get(i + 1) - peakIndices.get(i);
		}

		// Fencepost problem.
		peakTotal /= peakIndices.size() - 1;

		// Multiply the index differences times the time between each reading.
		// Milliseconds.
		peakTotal *= 50;
		System.out.println("averageTimeBetweenPeaks: " +  peakTotal);
		return peakTotal;
	}
}

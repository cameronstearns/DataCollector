package edu.calpoly.csc.mobileid.datatypes;

public class AccelerometerFeatureSet {
	private double averageX;
	private double averageY;
	private double averageZ;

	private double stdDevX;
	private double stdDevY;
	private double stdDevZ;

	// absolute mean difference
	private double avgAbsDistX;
	private double avgAbsDistY;
	private double avgAbsDistZ;

	private double avgResAccl;

	private double timeBtwnPkX;
	private double timeBtwnPkY;
	private double timeBtwnPkZ;

	private double[] binX;
	private double[] binY;
	private double[] binZ;

	public double getAverageX() {
		return averageX;
	}

	public void setAverageX(double averageX) {
		this.averageX = averageX;
	}

	public double getAverageY() {
		return averageY;
	}

	public void setAverageY(double averageY) {
		this.averageY = averageY;
	}

	public double getAverageZ() {
		return averageZ;
	}

	public void setAverageZ(double averagyZ) {
		this.averageZ = averagyZ;
	}

	public double getStdDevX() {
		return stdDevX;
	}

	public void setStdDevX(double stdDevX) {
		this.stdDevX = stdDevX;
	}

	public double getStdDevY() {
		return stdDevY;
	}

	public void setStdDevY(double stdDevY) {
		this.stdDevY = stdDevY;
	}

	public double getStdDevZ() {
		return stdDevZ;
	}

	public void setStdDevZ(double stdDevZ) {
		this.stdDevZ = stdDevZ;
	}

	// Lazily format a string.
	public String arrToStr(double arr[]) {
		String returnStr = "";
		for (Double d : arr) {
			returnStr += d + ", ";
		}
		return returnStr;
	}

	public String toString() {
		return "Average x: " + averageX + ", Average y:" + averageY
				+ ", Average z:" + averageZ + "\nStdDev x: " + stdDevX
				+ ", StdDev y: " + stdDevY + ", StdDev z: " + stdDevZ
				+ "\navgAbsDistX: " + avgAbsDistX + ", avgAbsDistY: "
				+ avgAbsDistY + ", avgAbsDistZ: " + avgAbsDistZ
				+ "\navgResAccl: " + avgResAccl + "\ntimeBtwnPkX: "
				+ timeBtwnPkX + ", timeBtwnPkY: " + timeBtwnPkY + ", timeBtwnPkZ: "
				+ timeBtwnPkZ + "\nbinX" + arrToStr(binX) + "\nbinY"
				+ arrToStr(binY) + "\nbinZ" + arrToStr(binZ);
	}

	public double getAvgAbsDistX() {
		return avgAbsDistX;
	}

	public void setAvgAbsDistX(double avgAbsDistX) {
		this.avgAbsDistX = avgAbsDistX;
	}

	public double getAvgAbsDistY() {
		return avgAbsDistY;
	}

	public void setAvgAbsDistY(double avgAbsDistY) {
		this.avgAbsDistY = avgAbsDistY;
	}

	public double getAvgAbsDistZ() {
		return avgAbsDistZ;
	}

	public void setAvgAbsDistZ(double avgAbsDistZ) {
		this.avgAbsDistZ = avgAbsDistZ;
	}

	public double getAvgResAccl() {
		return avgResAccl;
	}

	public void setAvgResAccl(double avgResAccl) {
		this.avgResAccl = avgResAccl;
	}

	public double getTimeBtwnPkX() {
		return timeBtwnPkX;
	}

	public void setTimeBtwnPkX(double timeBtwnPkX) {
		this.timeBtwnPkX = timeBtwnPkX;
	}

	public double getTimeBtwnPkY() {
		return timeBtwnPkY;
	}

	public void setTimeBtwnPkY(double timeBtwnPkY) {
		this.timeBtwnPkY = timeBtwnPkY;
	}

	public double getTimeBtwnPkZ() {
		return timeBtwnPkZ;
	}

	public void setTimeBtwnPkZ(double timeBtwnPkZ) {
		this.timeBtwnPkZ = timeBtwnPkZ;
	}

	public double[] getBinX() {
		return binX;
	}

	public void setBinX(double[] binX) {
		this.binX = binX;
	}

	public double[] getBinY() {
		return binY;
	}

	public void setBinY(double[] binY) {
		this.binY = binY;
	}

	public double[] getBinZ() {
		return binZ;
	}

	public void setBinZ(double[] binZ) {
		this.binZ = binZ;
	}
	public String toRecord() {
      String record = averageX + "," + averageY + "," + averageZ + ","
            + stdDevX + "," + stdDevY + "," + stdDevZ + "," + avgAbsDistX + ","
            + avgAbsDistY + "," + avgAbsDistZ + "," + avgResAccl + ","
            + timeBtwnPkX + "," + timeBtwnPkY + "," + timeBtwnPkZ;
      for(Double d : binX) {
         record += "," + d;
      }
      for(Double d : binY) {
         record += "," + d;
      }
      for(Double d : binZ) {
         record += "," + d;
      }
      
      return record;
	}
}

package edu.stanford.hci.r3.pen.gesture;

import java.util.ArrayList;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.colt.matrix.linalg.SingularValueDecomposition;


/**
 * <p>
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>.</span>
 * </p>
 * 
 * @author Avi Robinson-Mosher
 * 
 * 
 */
public class ShapeHistogram {
	int[] data;
	int[] bins;
	double[] mins;
	double[] maxes;
	int bands;
	
	public ShapeHistogram(int[] bins, double[] mins, double[] maxes, int bands)
	{
		// old-fashioned it. More convenient for ANN anyway
		int size = 1;
		for(int i = 0; i < bands; i++)
			size *= bins[i];
		data = new int[size];
		this.bands = bands;
		this.bins = bins;
		this.mins = mins;
		this.maxes = maxes;
	}
	
	public void addPoint(double[] input)
	{
		assert(input.length == bands);
		int multiplier = 1;
		int index = 0;
		for (int i = 0; i < bands; i++) {
			int subindex = Math.min((int)(((input[i] - mins[i]) / (maxes[i] - mins[i])) * bins[i]), bins[i] - 1);
			index += multiplier * subindex;
			multiplier *= bins[i];
		}
		data[index]++;
	}
	
	static public double[][] computeCostMatrix(ArrayList<ShapeHistogram> first, ArrayList<ShapeHistogram> second)
	{
		assert(first.size() == second.size()); // perhaps I should do those dummy points
		int points = first.size();
		double[][] cost = new double[points][points];
		for(int i = 0; i < points; i++) {
			ShapeHistogram s1 = first.get(i);
			for(int j = 0; j < points; j++) {
				ShapeHistogram s2 = second.get(j);
				double sum = 0;
				for (int k = 0; k < s1.data.length; k++) {
					int s1k = s1.data[k];
					int s2k = s2.data[k];
					if (s1k == s2k) continue;
					sum += Math.pow(s1k - s2k, 2) / (double)(s1k + s2k);
				}
				cost[i][j] = .5 * sum + Math.random() * .1;
			}
		}
		return cost;
	}
	
	static public class Pair{
		public int row;
		public int column;
		
		public Pair(int row, int column)
		{
			this.row = row;
			this.column = column;
		}
	}
	
	static public double distance(double[][] costMatrix, int[] mapping, int firstSize, int secondSize)
	{
		// leave out dummy points, scale
		int points = mapping.length;
		double cost = 0;
		int count = Math.min(firstSize, secondSize);
		for (int i = 0; i < points; i++) {
			int first = i;
			int second = mapping[i];
			if (first >= firstSize || second >= secondSize) continue;
			cost += costMatrix[i][mapping[i]];
		}
		return cost/count;
	}
	
	// http://www.public.iastate.edu/~ddoty/HungarianAlgorithm.html
	static public int[] munkres(int n, double[][] costs)
	{
		// n : constant integer := 20;
		// C : is array(1..n,1..n) of float;
		// M : is array(1..n,1..n) of integer;
		// Row,Col : is array(1..n) of integer;
		int stepnum;
		boolean done;
		double[][] C = new double[n][n];
		int[][] M = new int[n][n];
		int[] Row = new int[n];
		int[] Col = new int[n];

		for (int j = 0; j < n; j++) for (int i = 0; i < n; i++)
			C[j][i] = costs[j][i];

	    done = false;
	    stepnum = 1;
	    while(!done) {
	    	System.out.println("Step " + stepnum);
	    	switch(stepnum) {
		    	case 1: stepnum = step1(n, C, M, Row, Col);break;
		    	case 2: stepnum = step2(n, C, M, Row, Col);break;
		    	case 3: stepnum = step3(n, C, M, Row, Col);break;
		    	case 4: stepnum = step4(n, C, M, Row, Col);break;
		    	case 5: stepnum = step5(n, C, M, Row, Col);break;
		    	case 6: stepnum = step6(n, C, M, Row, Col);break;
		    	default: done = true;
	    	}
	    }
	    int[] matching = new int[n];
	    for(int i = 0; i < n; i++)
		    for(int j = 0; j < n; j++)
		    	if (M[i][j] == 1) {
		    		matching[i] = j;
		    		continue;
		    	}
	    return matching;
	}

	static public int step1(int n, double[][] C, int[][] M, int[] Row, int[] Col)
	{
		double minval;
		for (int i = 0; i < n; i++) {
			minval = C[i][0];
			for (int j = 1; j < n; j++) {
				if (minval > C[i][j])
					minval = C[i][j];
			}
			for (int j = 0; j < n; j++) {
				C[i][j] -= minval;
			}
		}
		return 2;
	}

	static public int step2(int n, double[][] C, int[][] M, int[] R_cov, int[] C_cov)
	{
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (C[i][j] == 0 && C_cov[j] == 0 && R_cov[i] == 0) {
					M[i][j] = 1;
					C_cov[j] = 1;
					R_cov[i] = 1;
				}
			}
		}
		for (int i = 0; i < n; i++) {
			C_cov[i] = 0;
			R_cov[i] = 0;
		}
		return 3;
	}

	static public int step3(int n, double[][] C, int[][] M, int[] R_cov, int[] C_cov)
	{
		int count;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (M[i][j] == 1) {
					C_cov[j] = 1; 
				}
			}
		}
		count = 0;
		for (int j = 0; j < n; j++)
			count = count + C_cov[j];
		int step;
		if (count >= n) step = 7;
		else step = 4;
		return step;
	}
	
	static public Pair find_a_zero(int n, double[][] C, int[][] M, int[] R_cov, int[] C_cov)
	{
		int row = -1;
		int col = -1;
		boolean done = false;
		for (int i = 0; i < n && !done; i++)
			for (int j = 0; j < n; j++) {
				if (C[i][j] == 0 && R_cov[i] == 0 && C_cov[j] == 0) {
					row = i;
					col = j;
					done = true;
				}
			}
		return new Pair(row,col);
	}

	static public boolean star_in_row(int n, double[][] C, int[][] M, int[] R_cov, int[] C_cov, int row)
	{
		for (int j = 0; j < n; j++)
			if (M[row][j] == 1) return true;
		return false;
	}
	
	static public int find_star_in_row(int n, double[][] C, int[][] M, int[] R_cov, int[] C_cov, int row)
	{
		int col = -1;
		for (int j = 0; j < n; j++)
			if (M[row][j] == 1) col = j;
		return col;
	}
	
	static Pair Z0;
	
	static public int step4(int n, double[][] C, int[][] M, int[] R_cov, int[] C_cov)
	{
		while (true) {
			Pair pair = find_a_zero(n, C, M, R_cov, C_cov);
			if (pair.row == -1) return 6;
			M[pair.row][pair.column] = 2;
			if (star_in_row(n, C, M, R_cov, C_cov, pair.row)) {
				int col = find_star_in_row(n, C, M, R_cov, C_cov, pair.row);
				R_cov[pair.row] = 1;
				C_cov[col] = 0;
			}
			else {
				Z0 = pair;
				return 5;
			}
		}
	}

	static public Pair find_star_in_col(int n, double[][] C, int[][] M, int[] R_cov, int[] C_cov, int column)
	{
		int row = -1;
		for (int i = 0; i < n; i++)
			if (M[i][column] == 1) row = i;
		return new Pair(row, column);
	}

	static public Pair find_prime_in_row(int n, double[][] C, int[][] M, int[] R_cov, int[] C_cov, int row)
	{
		int column = -1;
		for (int j = 0; j < n; j++)
			if (M[row][j] == 2) column = j;
		return new Pair(row, column);
	}
	
	static public void convert_path(int n, double[][] C, int[][] M, int[] R_cov, int[] C_cov, ArrayList<Pair> path)
	{
		for (int i = 0; i < path.size(); i++) {
			Pair point = path.get(i);
			if (M[point.row][point.column] == 1)
				M[point.row][point.column] = 0;
			else
				M[point.row][point.column] = 1;
		}
	}
	
	static public void clear_covers(int n, double[][] C, int[][] M, int[] R_cov, int[] C_cov)
	{
		for (int i = 0; i < n; i++) {
			R_cov[i] = 0;
			C_cov[i] = 0;
		}
	}
	
	static public void erase_primes(int n, double[][] C, int[][] M, int[] R_cov, int[] C_cov)
	{
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (M[i][j] == 2)
					M[i][j] = 0;
			}
		}
	}
	
	static public int step5(int n, double[][] C, int[][] M, int[] R_cov, int[] C_cov)
	{
		int count = 1;
		ArrayList<Pair> path = new ArrayList<Pair>();
		path.add(Z0);
		boolean done = false;
		while (!done) {
			Pair last = path.get(path.size() - 1);
			Pair star = find_star_in_col(n, C, M, R_cov, C_cov, last.column);
			if (star.row > -1) {
				path.add(new Pair(star.row, last.column));
			}
			else
				done = true;
			if (!done) {
				Pair prime = find_prime_in_row(n, C, M, R_cov, C_cov, star.row);
				path.add(new Pair(star.row, prime.column));
			}
		}
		convert_path(n, C, M, R_cov, C_cov, path);
	    clear_covers(n, C, M, R_cov, C_cov);
	    erase_primes(n, C, M, R_cov, C_cov);
	    return 3;
	}

	static public double find_smallest(int n, double[][] C, int[][] M, int[] R_cov, int[] C_cov)
	{
		double minval = Double.MAX_VALUE;
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				if (R_cov[i] == 0 && C_cov[j] == 0 && minval > C[i][j])
					minval = C[i][j];
		return minval;
	}
	
	static public int step6(int n, double[][] C, int[][] M, int[] R_cov, int[] C_cov)
	{
		double minval = find_smallest(n, C, M, R_cov, C_cov);
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++) {
				if (R_cov[i] == 1) C[i][j] += minval;
				if (C_cov[j] == 0) C[i][j] -= minval;
			}
		return 4;
	}

	// ah, point arrays
	static public DoubleMatrix2D bookstein(double[][] X, double[][] X2, double beta_k, double[] E)
	{
		int N = X.length;
		int Nb = X2.length;
		assert(N == Nb);
		double[][] r2 = new double[N+3][N+3];
		for(int i=0;i<N;i++) for(int j=i;j<N;j++)
			r2[i][j] = r2[j][i] = Math.pow(X[i][0]-X[j][0], 2) + Math.pow(X[i][1]-X[j][1], 2);
		for(int i=0;i<N;i++)
			r2[i][i] *= Math.log(r2[i][i] + 1);
		for(int i=0;i<N;i++) for(int j=i+1;j<N;j++)
			r2[i][j] = r2[j][i] = r2[j][i] * Math.log(r2[j][i]);
		DoubleMatrix2D K = new DenseDoubleMatrix2D(r2); // too large, but I'm lazy
		for(int i=0;i<N;i++) {
			r2[i][N] = r2[N][i] = 1;
			r2[i][N+1] = r2[N+1][i] = X[i][0];
			r2[i][N+2] = r2[N+2][i] = X[i][1];
		}
		for(int i=0;i<N;i++) for(int j=0;j<N;j++)
			r2[i][j]+=beta_k;
		Algebra algebra = new Algebra();
		DoubleMatrix2D L = new DenseDoubleMatrix2D(r2);
		DoubleMatrix2D invL = algebra.inverse(L);
		double[][] v = new double[N+3][2];
		for(int i=0;i<N;i++) {
			v[i][0] = X2[i][0];
			v[i][1] = X2[i][1];
		}
		DoubleMatrix2D V = new DenseDoubleMatrix2D(v);
		DoubleMatrix2D c = algebra.mult(invL, V);
		DoubleMatrix2D Qpart = algebra.mult(K,c);
		DoubleMatrix2D Q = algebra.mult(algebra.transpose(c), Qpart);
		// extra bits are all zero, so it's cool
		E[0] = algebra.trace(Q) / N; // bending energy
		// affine cost
		double[][] a = new double[2][2];
		a[0][0] = c.getQuick(N+1, 0);
		a[0][1] = c.getQuick(N+1, 1);
		a[1][0] = c.getQuick(N+2, 0);
		a[1][1] = c.getQuick(N+2, 1);
		SingularValueDecomposition s = new SingularValueDecomposition(new DenseDoubleMatrix2D(a));
		E[1] = Math.log(s.cond());
		DoubleMatrix2D result = new DenseDoubleMatrix2D(N, 2);
		for(int i=0;i<N;i++) {
			result.setQuick(i, 0, c.getQuick(i, 0));
			result.setQuick(i, 1, c.getQuick(i, 1));
		}
		return result;
	}
	
	static public double shapeContextCost(int N, double[][] costs)
	{
		double rowMin = 0, colMin = 0;
		for(int i=0;i<N;i++) {
			double m = Double.MAX_VALUE;
			for(int j=0;j<N;j++)
				if (costs[i][j] < m) m = costs[i][j];
			colMin += m;
		}
		colMin /= N;
		for(int j=0;j<N;j++) {
			double m = Double.MAX_VALUE;
			for(int i=0;i<N;i++)
				if (costs[i][j] < m) m = costs[i][j];
			rowMin += m;
		}
		rowMin /= N;
		return Math.max(rowMin, colMin);
	}

	static public double[][] warp(double[][] X, double[][] X2, DoubleMatrix2D c, int N)
	{
		Algebra algebra = new Algebra();
		double[][] d2 = new double[N][N];
		final double eps = 1e-10;
		for(int i=0;i<N;i++) for(int j=i;j<N;j++) {
			double tmp = Math.pow(X[i][0]-X2[j][0], 2) + Math.pow(X[i][1]-X2[j][1], 2);
			d2[i][j] = d2[j][i] = tmp * Math.log(tmp + eps);
		}
		DoubleMatrix2D warped = algebra.mult(algebra.transpose(c), new DenseDoubleMatrix2D(d2));
		
		DoubleMatrix2D partial = new DenseDoubleMatrix2D(2, 3);
		for(int i=0;i<3;i++) {
			partial.setQuick(0, i, c.getQuick(i, 0));
			partial.setQuick(1, i, c.getQuick(i, 1));
		}
		double[][] paddedX = new double[3][N];
		for(int i=0;i<N;i++) {
			paddedX[0][i] = 1;
			paddedX[1][i] = X[i][0];
			paddedX[2][i] = X[i][1];
		}
		DoubleMatrix2D affine = algebra.mult(partial, new DenseDoubleMatrix2D(paddedX));
		double[][] result = new double[N][2];
		for(int i=0;i<N;i++) {
			result[i][0] = affine.getQuick(0,i) + warped.getQuick(0,i);
			result[i][1] = affine.getQuick(1,i) + warped.getQuick(1,i);
		}
		return result;
	}

	public static double shapeContextMetric(ShapeContext shape1, ShapeContext shape2)
	{
		int N = Math.max(shape1.size(), shape2.size());
		int n = Math.min(shape1.size(), shape2.size());
		ArrayList<ShapeHistogram> histogram1 = shape1.generateShapeHistogram(N);
		ArrayList<ShapeHistogram> histogram2 = shape2.generateShapeHistogram(N);
		double[][] costs = computeCostMatrix(histogram1, histogram2);
		int[] matching = munkres(N, costs);
		double[][] X1 = shape1.points();
		double[][] X2 = shape2.points();
		// take the NON-dummy points from both
		int count=0;
		if (X1.length > X2.length) {
			// X2 stays normal;
			double[][] X1_new = new double[X2.length][2];
			for(int i=0;i<X1.length;i++)
				if(matching[i] < X2.length) {
					X1_new[count][0] = X1[i][0];
					X1_new[count][1] = X1[i][1];
					count++;
				}
			X1 = X1_new;
		}
		else if (X1.length < X2.length) {
			double[][] X2_new = new double[X1.length][2];
			for(int i=0;i<X1.length;i++) {
				X2_new[count][0] = X2[matching[i]][0];
				X2_new[count][1] = X2[matching[i]][1];
			}
			X2 = X2_new;
		}
		double[] E = new double[2];
		double dist = 0;
		for(int i=0;i<X1.length;i++) for(int j=i+1;j<X1.length;j++)
			dist += Math.sqrt(Math.pow(X1[i][0]-X1[j][0],2)+Math.pow(X1[i][1]-X1[j][1],2));
		dist /= (n*(n-1))/2;
		double beta_k = Math.pow(dist,2);
		DoubleMatrix2D c = bookstein(X1, X2, beta_k, E);
		double sc_cost = shapeContextCost(N, costs);
		double[][] Xwarped = warp(X1, X2, c, n);
		double mean_squared_error = 0;
		for(int i=0;i<n;i++) {
			mean_squared_error += Math.sqrt(Math.pow(Xwarped[i][0] - X2[i][0],2) + Math.pow(Xwarped[i][1] - X2[i][1],2));
		}
		mean_squared_error /= n;
		// using digit distance function from paper
		return 1.6 * E[0] + sc_cost + .3 * E[1];
	}
/*
 * 
 * Pseudocode
 * 
 * constructor(bins[],mins[],maxes[],bands(=length bins))
 * 
 * add(double values[]) {increment all relevant bins}
 * 
 */
}
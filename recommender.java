import java.util.*;
import java.io.*;
import java.lang.Math;
import java.io.PrintWriter;

class recommender
{
	// Initialize the comma delimiter and new line separator.
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";

	// Method to create a matrix.
	public static int[][] CreateMatrix()throws Exception
	{
		// Initialize the matrix with -1 for all elements
		int[][] matrix = new int[6040][3952];
		for (int i = 0; i<matrix.length; ++i)
		{
			for (int j = 0; j<matrix[0].length; ++j)
			{
				matrix[i][j] = -1;
			}
		}

		// Read the input values and form the full matrix 
		BufferedReader br = new BufferedReader(new FileReader("ratings.csv"));
		StringTokenizer st = null;
		String row;
		while ((row = br.readLine()) != null)
		{
			st = new StringTokenizer(row, ",");
			while(st.hasMoreTokens())
                	{
				int user = Integer.parseInt(st.nextToken());
				int movie = Integer.parseInt(st.nextToken());
				int rating = Integer.parseInt(st.nextToken());
				matrix[user-1][movie-1] = rating; 
				st.nextToken();
			}		
		}
		return matrix;
	}

	public static int[][] testData()throws FileNotFoundException, IOException
	{
		BufferedReader br = new BufferedReader(new FileReader("toBeRated.csv"));
		StringTokenizer st = null;
		String row;
		int[][] data = new int[200000][2];
		int i = 0;
		while ((row = br.readLine()) != null)
		{
			st = new StringTokenizer(row, ",");
			while(st.hasMoreTokens())
                	{
				data[i][0] = Integer.parseInt(st.nextToken());
				data[i][1] = Integer.parseInt(st.nextToken());
			}
			i += 1;
		}
		return data;
	}

	public static String CrossValidation(String UserItem)throws FileNotFoundException, IOException
	{
		// Initialize the matrix with -1 for all elements;
		int[][] matrix = new int[6040][3952];
		for (int i = 0; i<matrix.length; ++i)
		{
			for (int j = 0; j<matrix[0].length; ++j)
			{
				matrix[i][j] = -1;
			}
		}

		// Read the input values and form the full matrix
		int[][] rateValue = new int[800000][3];
		int num = 0;
		BufferedReader br = new BufferedReader(new FileReader("ratings.csv"));
		StringTokenizer st = null;
		String row;
		while ((row = br.readLine()) != null)
		{
			st = new StringTokenizer(row, ",");
			while(st.hasMoreTokens())
                	{
				rateValue[num][0] = Integer.parseInt(st.nextToken());
				rateValue[num][1] = Integer.parseInt(st.nextToken());
				rateValue[num][2] = Integer.parseInt(st.nextToken());
				num++;
				st.nextToken();
			}		
		}
		
		int kfold = 2;
		int[][] traindata = new int[rateValue.length/kfold][2];
		int[] testdata = new int[rateValue.length/kfold];
		double[] KrmseP = new double[kfold];
		double[] KrmseC = new double[kfold];
		double[] KrmseJ = new double[kfold];
		for (int k = 0; k < kfold; k++)
		{
			num = 0;
			int foldsize = rateValue.length/kfold;
			for (int i = k*foldsize; i < (k+1)*foldsize; ++i)
			{	
				int user = rateValue[i][0];
				int movie = rateValue[i][1];
				int rating = rateValue[i][2];
				matrix[user-1][movie-1] = rating; 
			}

			for (int i = (kfold-(k+1))*foldsize; i < (kfold-k)*foldsize; ++i)
			{
				int user = rateValue[i][0];
				int movie = rateValue[i][1];
				traindata[num][0] = user;
				traindata[num][1] = movie;
				testdata[num] = rateValue[i][2];
				num++;
			}

			float[] opRatingP = new float[rateValue.length/kfold];
			float[] opRatingC = new float[rateValue.length/kfold];
			float[] opRatingJ = new float[rateValue.length/kfold];
			
			if (UserItem.equals("User"))
			{
				opRatingP = PearsonSimilarityUser(matrix,traindata);
				opRatingC = CosineSimilarityUser(matrix,traindata);
				opRatingJ = JaccardSimilarityUser(matrix,traindata);
			}
			else if (UserItem.equals("Item"))
			{
				opRatingP = PearsonSimilarityItem(matrix,traindata);
				opRatingC = CosineSimilarityItem(matrix,traindata);
				opRatingJ = JaccardSimilarityItem(matrix,traindata);
			}

			double  rmseP = 0.0;
			double  rmseC = 0.0;
			double  rmseJ = 0.0;

			for (int i = 0; i < opRatingC.length; ++i)
			{
				rmseP += (double)(opRatingP[i] - testdata[i])*(opRatingP[i] - testdata[i]);
				rmseC += (double)(opRatingC[i] - testdata[i])*(opRatingC[i] - testdata[i]);
				rmseJ += (double)(opRatingJ[i] - testdata[i])*(opRatingJ[i] - testdata[i]);	
			}

			KrmseP[k] = Math.sqrt(rmseP/opRatingP.length);
			KrmseC[k] = Math.sqrt(rmseC/opRatingC.length);
			KrmseJ[k] = Math.sqrt(rmseJ/opRatingJ.length);

			for (int i = 0; i<matrix.length; ++i)
			{
				for (int j = 0; j<matrix[0].length; ++j)
				{
					matrix[i][j] = -1;
				}
			}
		}

		double TrmseP = 0;
		double TrmseC = 0;
		double TrmseJ = 0;

		for (int k = 0; k < kfold; k++)
		{
			TrmseP += KrmseP[k];
			TrmseC += KrmseC[k];
			TrmseJ += KrmseJ[k];
		}
		
		TrmseP /= kfold;
		TrmseC /= kfold;
		TrmseJ /= kfold;

		System.out.println(TrmseP + " " + TrmseC + " " + TrmseJ);
		if (TrmseP > TrmseC)
		{
			if (TrmseC > TrmseJ)
			{
				return "Jaccard";
			}
			else
			{
				return "Cosine";
			}
		}
		else
		{
			if (TrmseP > TrmseJ)
			{
				return "Jaccard";
			}
			else
			{
				return "Pearson";
			}
		}
	}
	
	public static void CrossValidationCombined()throws FileNotFoundException, IOException
	{
		int[][] matrix = new int[6040][3952];
		for (int i = 0; i<matrix.length; ++i)
		{
			for (int j = 0; j<matrix[0].length; ++j)
			{
				matrix[i][j] = -1;
			}
		}

		int[][] rateValue = new int[800000][3];
		int num = 0;
		BufferedReader br = new BufferedReader(new FileReader("ratings.csv"));
		StringTokenizer st = null;
		String row;
		while ((row = br.readLine()) != null)
		{
			st = new StringTokenizer(row, ",");
			while(st.hasMoreTokens())
                	{
				rateValue[num][0] = Integer.parseInt(st.nextToken());
				rateValue[num][1] = Integer.parseInt(st.nextToken());
				rateValue[num][2] = Integer.parseInt(st.nextToken());
				num++;
				st.nextToken();
			}		
		}

		int kfold = 2;
		int[][] traindata = new int[rateValue.length/kfold][2];
		int[] testdata = new int[rateValue.length/kfold];
		double[] KrmseJ = new double[kfold];
		for (int k = 0; k < kfold; k++)
		{
			num = 0;
			int foldsize = rateValue.length/kfold;
			for (int i = k*foldsize; i < (k+1)*foldsize; ++i)
			{	
				int user = rateValue[i][0];
				int movie = rateValue[i][1];
				int rating = rateValue[i][2];
				matrix[user-1][movie-1] = rating; 
			}

			for (int i = (kfold-(k+1))*foldsize; i < (kfold-k)*foldsize; ++i)
			{
				int user = rateValue[i][0];
				int movie = rateValue[i][1];
				traindata[num][0] = user;
				traindata[num][1] = movie;
				testdata[num] = rateValue[i][2];
				num++;
			}

			float[] opRatingJ = new float[rateValue.length/kfold];
			
			opRatingJ = CombinedBestRating(matrix,traindata);

			double  rmseJ = 0.0;

			for (int i = 0; i < opRatingJ.length; ++i)
			{
				rmseJ += (double)(opRatingJ[i] - testdata[i])*(opRatingJ[i] - testdata[i]);	
			}

			KrmseJ[k] = Math.sqrt(rmseJ/opRatingJ.length);

			for (int i = 0; i<matrix.length; ++i)
			{
				for (int j = 0; j<matrix[0].length; ++j)
				{
					matrix[i][j] = -1;
				}
			}
		}

		double TrmseJ = 0;

		for (int k = 0; k < kfold; k++)
		{
			TrmseJ += KrmseJ[k];
		}

		TrmseJ /= kfold;

		System.out.println(TrmseJ);
	}
	
	public static float[] PearsonSimilarityUser(int[][] matrix,int[][] test)throws FileNotFoundException, IOException
	{
		int len = test.length;
		int lenUsers = matrix.length;
		int lenMovies = matrix[0].length;
		float[] opRating = new float[len];
		int user = 0;
		int movie = 0;
		for (int i = 0; i < len; ++i)
		{
			user = test[i][0];
			movie = test[i][1];
			float upperNum = 0;
			float upperDenom = 0;
			for (int j = 0; j < lenUsers; ++j)
			{
				int userthis = 0;
				int userother = 0;
				int count = 0;
				if(matrix[j][movie-1] != -1)
				{
					for (int k = 0; k < lenMovies; ++k)
					{
						if ((matrix[user-1][k] != -1) && (matrix[j][k] != -1))
						{
							userthis += matrix[user-1][k];
							userother += matrix[j][k];
							count++;		
						}
					}
					if (count>0)
					{
						float thisavg = (float)userthis/(float)count;
						float otheravg = (float)userother/(float)count;
						float num = 0;
						float denom1 = 0;
						float denom2 = 0;
						for (int k = 0; k < lenMovies; ++k)
						{
							if ((matrix[user-1][k] != -1) && (matrix[j][k] != -1))
							{
								num += (matrix[user-1][k] - thisavg)*(matrix[j][k] - otheravg);
								denom1 += (matrix[user-1][k] - thisavg)*(matrix[user-1][k] - thisavg);
								denom2 += (matrix[j][k] - otheravg)*(matrix[j][k] - otheravg);		
							}
						}
						if (num > 0)
						{
							if (denom1*denom2>0)
							{
								upperDenom += num/(Math.sqrt(denom1*denom2));
								upperNum += matrix[j][movie-1]*num/(Math.sqrt(denom1*denom2));
							}
						}	
					}				
				}
			}
			float predrating = 0;
			if(upperDenom == 0)
			{
				predrating = upperNum;
			}
			else
			{
				predrating = upperNum/upperDenom;
			}		
			opRating[i] = predrating;
		}
		return opRating;
	}


	public static float[] CosineSimilarityUser(int[][] matrix,int[][] test)throws FileNotFoundException, IOException
	{
		int len = test.length;
		int lenUsers = matrix.length;
		int lenMovies = matrix[0].length;
		int user = 0;
		int movie = 0;
		float[] opRating = new float[len];
		for (int i = 0; i < len; ++i)
		{
			user = test[i][0];
			movie = test[i][1];
			float upperNum = 0;
			float upperDenom = 0;
			for (int j = 0; j < lenUsers; ++j)
			{
				if(matrix[j][movie-1] != -1)
				{
					float num = 0;
					float denom1 = 0;
					float denom2 = 0;
					boolean flag = false;
					for (int k = 0; k < lenMovies; ++k)
					{
						if ((matrix[user-1][k] != -1) && (matrix[j][k] != -1))
						{
							flag = true;
							num += (float)matrix[user-1][k]*matrix[j][k];
							denom1 += (float)matrix[user-1][k]*matrix[user-1][k];
							denom2 += (float)matrix[j][k]*matrix[j][k];
						}
					}
					if (flag)
					{
						upperDenom += num/(Math.sqrt(denom1*denom2));
						upperNum += matrix[j][movie-1]*num/(Math.sqrt(denom1*denom2));	
					}
				}
			}

			float predrating = 0;

			if (upperDenom > 0)
			{
				predrating = upperNum/upperDenom;		
			}
			else
			{
				predrating = upperNum;
			}
			opRating[i] = predrating;
		}
		return opRating;
	}

	public static float[] JaccardSimilarityUser(int[][] matrix,int[][] test)throws FileNotFoundException, IOException
	{
		int len = test.length;
		int lenUsers = matrix.length;
		int lenMovies = matrix[0].length;
		float[] opRating = new float[len];
		int user = 0;
		int movie = 0;
		for (int i = 0; i < len; ++i)
		{
			user = test[i][0];
			movie = test[i][1];
			float upperNum = 0;
			float upperDenom = 0;
			for (int j = 0; j < lenUsers; ++j)
			{
				if(matrix[j][movie-1] != -1)
				{
					int first = 0;
					int second = 0;
					int common = 0;
					for (int k = 0; k < lenMovies; ++k)
					{
						if ((matrix[user-1][k] != -1) && (matrix[j][k] != -1))
						{
							common++;
							first++;
							second++;
						}
						else if (matrix[user-1][k] != -1) { first++; }
						else if (matrix[j][k] != -1) { second++; }
					}
					if (common>0)
					{
						upperDenom += (float)(common)/(float)(first+second-common);
						upperNum += matrix[j][movie-1]*((float)(common)/(float)(first+second-common));	
					}
				}
			}

			float predrating = 0;

			if (upperDenom > 0)
			{
				predrating = upperNum/upperDenom;		
			}
			else
			{
				predrating = upperNum;
			}		
			opRating[i] = predrating;
		}
		return opRating;
	}

	public static float[] PearsonSimilarityItem(int[][] matrix,int[][] test)throws FileNotFoundException, IOException
	{
		int len = test.length;
		int lenUsers = matrix.length;
		int lenMovies = matrix[0].length;
		float[] opRating = new float[len];
		int user = 0;
		int movie = 0;
		for (int i = 0; i < len; ++i)
		{
			user = test[i][0];
			movie = test[i][1];
			float upperNum = 0;
			float upperDenom = 0;
			for (int j = 0; j < lenMovies; ++j)
			{
				int itemthis = 0;
				int itemother = 0;
				int count = 0;
				if(matrix[user-1][j] != -1)
				{
					for (int k = 0; k < lenUsers; ++k)
					{
						if ((matrix[k][movie-1] != -1) && (matrix[k][j] != -1))
						{
							itemthis += matrix[k][movie-1];
							itemother += matrix[k][j];
							count++;		
						}
					}
					if (count>0)
					{
						float thisavg = (float)itemthis/(float)count;
						float otheravg = (float)itemother/(float)count;
						float num = 0;
						float denom1 = 0;
						float denom2 = 0;
						for (int k = 0; k < lenUsers; ++k)
						{
							if ((matrix[k][movie-1] != -1) && (matrix[k][j] != -1))
							{
								num += (matrix[k][movie-1] - thisavg)*(matrix[k][j] - otheravg);
								denom1 += (matrix[k][movie-1] - thisavg)*(matrix[k][movie-1] - thisavg);
								denom2 += (matrix[k][j] - otheravg)*(matrix[k][j] - otheravg);
							}
						}

						if (num>0)
						{
							if (denom1*denom2>0)
							{
								upperDenom += num/(Math.sqrt(denom1*denom2));
								upperNum += matrix[user-1][j]*num/(Math.sqrt(denom1)*Math.sqrt(denom2));
							}
						}	
					}				
				}
			}
			float predrating = 0;
			if(upperDenom == 0)
			{
				predrating = upperNum;
			}
			else
			{
				predrating = upperNum/upperDenom;
			}		
			opRating[i] = predrating;
		}
		return opRating;
	}

	public static float[] CosineSimilarityItem(int[][] matrix,int[][] test)throws FileNotFoundException, IOException
	{
		int len = test.length;
		int lenUsers = matrix.length;
		int lenMovies = matrix[0].length;
		float[] opRating = new float[len];
		int user = 0;
		int movie = 0;
		for (int i = 0; i < len; ++i)
		{
			user = test[i][0];
			movie = test[i][1];
			float upperNum = 0;
			float upperDenom = 0;
			for (int j = 0; j < lenMovies; ++j)
			{
				if(matrix[user-1][j] != -1)
				{
					float num = 0;
					float denom1 = 0;
					float denom2 = 0;
					boolean flag = false;
					for (int k = 0; k < lenUsers; ++k)
					{
						if ((matrix[k][movie-1] != -1) && (matrix[k][j] != -1))
						{
							flag = true;
							num += (float)matrix[k][movie-1]*matrix[k][j];
							denom1 += (float)matrix[k][movie-1]*matrix[k][movie-1];
							denom2 += (float)matrix[k][j]*matrix[k][j];
						}
					}
					if (flag)
					{
						upperDenom += num/(Math.sqrt(denom1*denom2));
						upperNum += matrix[user-1][j]*num/(Math.sqrt(denom1*denom2));	
					}
				}
			}

			float predrating = 0;

			if (upperDenom > 0)
			{
				predrating = upperNum/upperDenom;		
			}
			else
			{
				predrating = upperNum;
			}
			opRating[i] = predrating;
		}
		return opRating;
	}

	public static float[] JaccardSimilarityItem(int[][] matrix,int[][] test)throws FileNotFoundException, IOException
	{
		int len = test.length;
		int lenUsers = matrix.length;
		int lenMovies = matrix[0].length;
		float[] opRating = new float[len];
		int user = 0;
		int movie = 0;
		for (int i = 0; i < len; ++i)
		{
			user = test[i][0];
			movie = test[i][1];
			float upperNum = 0;
			float upperDenom = 0;
			for (int j = 0; j < lenMovies; ++j)
			{
				if(matrix[user-1][j] != -1)
				{
					int first = 0;
					int second = 0;
					int common = 0;
					for (int k = 0; k < lenUsers; ++k)
					{
						if ((matrix[k][movie-1] != -1) && (matrix[k][j] != -1))
						{
							common++;
							first++;
							second++;
						}
						else if (matrix[k][movie-1] != -1) { first++; }
						else if (matrix[k][j] != -1) { second++; }
					}
					if (common>0)
					{
						upperDenom += (float)(common)/(float)(first+second-common);
						upperNum += matrix[user-1][j]*((float)(common)/(float)(first+second-common));	
					}
				}
			}
			
			float predrating = 0;

			if (upperDenom > 0)
			{
				predrating = upperNum/upperDenom;		
			}
			else
			{
				predrating = upperNum;
			}
			opRating[i] = predrating;
		}
		return opRating;
	}	

	public static float[] CombinedBestRating(int[][] matrix,int[][] test)throws FileNotFoundException, IOException
	{
		BufferedReader br = new BufferedReader(new FileReader("users.csv"));
		StringTokenizer st = null;
		String row;
		Map<Integer,String> uidgender = new HashMap<Integer,String>();
		Map<Integer,Integer> uidage = new HashMap<Integer,Integer>();
		while ((row = br.readLine()) != null)
		{
			st = new StringTokenizer(row, ",");
			while(st.hasMoreTokens())
                	{
				int userid = Integer.parseInt(st.nextToken());
				String gender = st.nextToken();
				int age = Integer.parseInt(st.nextToken());
				uidgender.put(userid,gender);
				uidage.put(userid,age);
				st.nextToken();
				st.nextToken();
			}		
		}

		int len = test.length;
		int lenUsers = matrix.length;
		int lenMovies = matrix[0].length;
		float[] opRatingU = new float[len];
		int user = 0;
		int movie = 0;
		for (int i = 0; i < len; ++i)
		{
			user = test[i][0];
			movie = test[i][1];
			float upperNum = 0;
			float upperDenom = 0;
			for (int j = 0; j < lenUsers; ++j)
			{
				if(matrix[j][movie-1] != -1 && (uidage.get(user) == uidage.get(j+1)) && (uidgender.get(user).equals(uidgender.get(j+1))))
				{
					int first = 0;
					int second = 0;
					int common = 0;
					for (int k = 0; k < lenMovies; ++k)
					{
						if ((matrix[user-1][k] != -1) && (matrix[j][k] != -1))
						{
							common++;
							first++;
							second++;
						}
						else if (matrix[user-1][k] != -1) { first++; }
						else if (matrix[j][k] != -1) { second++; }
					}
					if (common>0)
					{
						upperDenom += (float)(common)/(float)(first+second-common);
						upperNum += matrix[j][movie-1]*((float)(common)/(float)(first+second-common));	
					}
				}
			}

			float predrating = 0;

			if (upperDenom > 0)
			{
				predrating = upperNum/upperDenom;		
			}
			else
			{
				predrating = upperNum;
			}		
			opRatingU[i] = predrating;
		}

		float[] opRatingI = new float[len];
		user = 0;
		movie = 0;
		for (int i = 0; i < len; ++i)
		{
			user = test[i][0];
			movie = test[i][1];
			float upperNum = 0;
			float upperDenom = 0;
			for (int j = 0; j < lenMovies; ++j)
			{
				if(matrix[user-1][j] != -1)
				{
					int first = 0;
					int second = 0;
					int common = 0;
					for (int k = 0; k < lenUsers; ++k)
					{
						if ((matrix[k][movie-1] != -1) && (matrix[k][j] != -1))
						{
							common++;
							first++;
							second++;
						}
						else if (matrix[k][movie-1] != -1) { first++; }
						else if (matrix[k][j] != -1) { second++; }
					}
					if (common>0)
					{
						upperDenom += (float)(common)/(float)(first+second-common);
						upperNum += matrix[user-1][j]*((float)(common)/(float)(first+second-common));	
					}
				}
			}
			
			float predrating = 0;

			if (upperDenom > 0)
			{
				predrating = upperNum/upperDenom;		
			}
			else
			{
				predrating = upperNum;
			}
			opRatingI[i] = predrating;
		}

		float[] opRating = new float[len];
		for (int i = 0; i < len; ++i)
		{
			opRating[i] = (opRatingU[i] + opRatingI[i])/2;
		}
		return opRating;
	}

	public static void main(String args[])throws IOException
	{
		System.out.println("Recommendation System Ratings!!!");
		String UserItem = args[0];
		String SimPCJ = args[1];
		int[][] matrix = CreateMatrix();
		int[][] test = testData();
		
		// CrossValidationCombined();
		
		if (SimPCJ.equals("Best"))
		{
			SimPCJ = CrossValidation(UserItem);
		}

		float[] opRating = new float[test.length];

		if (UserItem.equals("User"))
		{
			switch(SimPCJ)
			{
				case "Pearson":
					System.out.println("User Pearson");
					opRating = PearsonSimilarityUser(matrix,test);
					break;
				case "Cosine":
					System.out.println("User Cosine");
					opRating = CosineSimilarityUser(matrix,test);
					break;
				case "Jaccard":
					System.out.println("User Jaccard");
					opRating = JaccardSimilarityUser(matrix,test);
			}
		}
		else if (UserItem.equals("Item"))
		{
			switch(SimPCJ)
			{
				case "Pearson":
					System.out.println("Item Pearson");
					opRating = PearsonSimilarityItem(matrix,test);
					break;
				case "Cosine":
					System.out.println("Item Cosine");
					opRating = CosineSimilarityItem(matrix,test);
					break;
				case "Jaccard":
					System.out.println("Item Jaccard");
					opRating = JaccardSimilarityItem(matrix,test);
			}
		}
		else if (UserItem.equals("Combined"))
		{
			System.out.println("Combined");
			opRating = CombinedBestRating(matrix,test);
		}

		if (UserItem.equals("User"))
		{	
			PrintWriter writer = new PrintWriter("result1.csv", "UTF-8");
			for (int i = 0; i < opRating.length; ++i)
			{
				writer.println(opRating[i]);
			}
			writer.close();
		}
		else if (UserItem.equals("Item"))
		{	
			PrintWriter writer = new PrintWriter("result2.csv", "UTF-8");
			for (int i = 0; i < opRating.length; ++i)
			{
				writer.println(opRating[i]);
			}
			writer.close();
		} 
		else if (UserItem.equals("Combined"))
		{	
			PrintWriter writer = new PrintWriter("result3.csv", "UTF-8");
			for (int i = 0; i < opRating.length; ++i)
			{
				writer.println(opRating[i]);
			}
			writer.close();
		}
	}
}

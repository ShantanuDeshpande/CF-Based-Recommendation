# CF-Based-Recommendation
Recommendation System based on Collaborative Filtering

README

1) To compile the program you need java. To create class file, execute the command

>> javac recommender.java

2) Once the class file is formed, different configuration of recommender system can be run. If you want to run cross-validation you will have to mention second 
argument as "Best" as undermentioned, else you can specify any one type of similarity type as second argument. Also since java is row-major, both Item-based and
Combined-based program became slow to ensure generality. User-based predictions with predefined similarity could take upto 45-mins on quad-core i7 system, whereas 
Item-based and Combined-based could take upto 2 hours. If you run cross-validation, then the code will use the entire ratings.csv file, for the entire 8,00,000 
entiries and Item-based system could take upto 12 hours to run.

	2.1) User-Based
	I) To run user based recommender system with Pearson similarity execute -
	>> java recommender User Pearson 

	II) To run user based recommender system with Cosine similarity execute -
	>> java recommender User Cosine

	III) To run user based recommender system with Jaccard similarity execute -
	>> java recommender User Jaccard

	IV) To run user based recommender system with Best similarity, using K-fold cross-validation execute -
	>> java recommender User Best

	
	2.2) Item-Based
	I) To run item based recommender system with Pearson similarity execute -
	>> java recommender Item Pearson 

	II) To run item based recommender system with Cosine similarity execute -
	>> java recommender Item Cosine

	III) To run item based recommender system with Jaccard similarity execute -
	>> java recommender Item Jaccard

	IV) To run item based recommender system with Best similarity, using K-fold cross-validation execute -
	>> java recommender Item Best

	2.3) Combined User-Item Based
	I) To run the combined user-item based recommender system with Jaccard similarity execute -
	>> java recommender Combined None

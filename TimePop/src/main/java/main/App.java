package main;

import org.apache.commons.cli.*;
import recommenders.*;


public class App {

	//Training set file
	private static final String TRAINFILE = "trainfile";;
	
	//Number of items to recommend
	private static final String NITEMSRECOMMENDED = "nItemsRecommended";
	
	//Output file with recommendation
	private static final String RESULTSFILE = "resultFile";
	
	//Recommender
	private static final String RECOMMENDER = "recommender";
	
	//Beta value for temporal decay
	private static final String BETA = "beta";
	
	//Last rating timestamp in the training set;
	private static final String REFERRINGTIMESTAMP = "referringTimestamp";
	
	//First timestamp in the training set;
	private static final String STARTINGTIMESTAMP = "startingTimestamp";

	public static void main(String[] args) throws Exception {
		
        CommandLine cl = getCommandLineOptions(args);
        if (cl == null) {
            System.out.println("Error in arguments");
            return;
        } 
        
        // Mandatory arguments for each approach
        String trainfile = cl.getOptionValue(TRAINFILE);
        String recommender = cl.getOptionValue(RECOMMENDER);
        String reccFilePath = cl.getOptionValue(RESULTSFILE);

        
        
        
        switch(recommender) {
        	case "TimePop":
        		System.out.println("TimePop launched");
                String betaS = cl.getOptionValue(BETA);
                String startingTimestampS = cl.getOptionValue(STARTINGTIMESTAMP);
                String referringTimestampS = cl.getOptionValue(REFERRINGTIMESTAMP);
                String nReccS = cl.getOptionValue(NITEMSRECOMMENDED);
                
                
        		double beta = 1.0/200.0;
        		int nRecc = 300;
        		long startingTimestamp;
        		long referringTimestamp = 0;


                if (referringTimestampS == null){
                    System.out.println("Referring Timestamp is missing");
                    return;
                }
        			 		
        		if(nReccS != null) {
        			nRecc = Integer.parseInt(nReccS);
        		}
        		
        		if(betaS != null) {
        			beta = Double.parseDouble(betaS);
        		}else {
                    if (startingTimestampS == null) {
                        System.out.println("Starting timestamp is needed to compute beta automatically. Default value of 1/200 will be used for beta");
                        beta = 1.0/200.0;
                    }else {
                        startingTimestamp = Long.parseLong(startingTimestampS);
                        referringTimestamp = Long.parseLong(referringTimestampS);
                        beta = beta *(((double)(referringTimestamp - startingTimestamp))/(60*60*24*365));
                    }

        		}
        		
        		TimePop timepop = new TimePop(beta, referringTimestamp, nRecc, trainfile, reccFilePath);
        		timepop.launchTimePop();
        		
        		break;
		}
		
	}
	
	
	private static CommandLine getCommandLineOptions(String[] args) {
        Options options = new Options();

        // Train file
        Option trainFile = new Option("tf",TRAINFILE, true, "Training set path");
        trainFile.setRequired(true);
        options.addOption(trainFile);


        // NumberItemsRecommended
        Option numberItemsRecommended = new Option("N", NITEMSRECOMMENDED, true, "Number of items recommended");
        numberItemsRecommended.setRequired(false);
        options.addOption(numberItemsRecommended);

        // OutResultfile
        Option outfile = new Option("rf", RESULTSFILE, true, "Output result file with recommendations");
        outfile.setRequired(true);
        options.addOption(outfile);

        // Resultsfile
        Option recommender = new Option("rec", RECOMMENDER, true, "Name of recommender");
        recommender.setRequired(true);
        options.addOption(recommender);
        
        // Beta
        Option beta = new Option("b", BETA, true, "Beta value for temporal decay");
        beta.setRequired(false);
        options.addOption(beta);
        
        // startingTimestamp
        Option startingTimestamp = new Option("st", STARTINGTIMESTAMP, true, "first rating timestamp in the training set");
        startingTimestamp.setRequired(false);
        options.addOption(startingTimestamp);
        
        
        // referringTimestamp
        Option referringTimestamp = new Option("rt", REFERRINGTIMESTAMP, true, "Last rating timestamp in the training set");
        referringTimestamp.setRequired(false);
        options.addOption(referringTimestamp);
        

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            return null;
        }
        return cmd;

    }
	
}
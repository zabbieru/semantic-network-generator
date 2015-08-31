package netgen.NetworkGeneration.NetworkTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import netgen.NetworkGeneration.Network;
import netgen.NetworkGeneration.TokenPair;
import netgen.Preprocessing.Corpus;
import netgen.Preprocessing.Token;

public class SentencewiseNetwork extends Network
{
	public SentencewiseNetwork(Corpus inCorpus, int inMaxWindowSentences, int inMaxWindowTokens)
	{
		super(inCorpus);
		if(inMaxWindowSentences == 0 && inMaxWindowTokens == 0)
		{
			generateBySingleSentenceWindow(corpus.getProcessedText());
		}
		else
		{
			generateByMultiSentenceSlidingWindow(corpus.getProcessedText(), inMaxWindowSentences, inMaxWindowTokens);
		}
	}
	public SentencewiseNetwork(Corpus inCorpus)
	{
		super(inCorpus);
		generateBySingleSentenceWindow(corpus.getProcessedText());
	}
	
	
	//Combine these two network generation methods. Single sentence edge window is 
    //		a just a simple case of the multisentence window method. Or is it...
    /*
    Forms a complete graph of every line and returns the sum of all of these graphs
    Tokens will never be linked to themselves.
    Tokens occurring more than once in a line will be weighted proportionally to the number of times they appear
    */
   private void generateBySingleSentenceWindow(ArrayList<ArrayList<Token>> lines) {

//       HashMap<TokenPair, Double> edgeset = new HashMap<>();

       for (ArrayList<Token> line : lines) {									// For each line in the corpus
           for (int i = 0; i < line.size() - 1; i++) {							// For each Token in the line
               for (int j = i + 1; j < line.size(); j++) {						// For each Token further on in the line 
                   if (!line.get(i).equals(line.get(j))) {						// If the Tokens aren't the same word
                       TokenPair pair = new TokenPair(line.get(i).getSignature(), line.get(j).getSignature());
                       if (edgeSet.containsKey(pair)) {
                           edgeSet.put(pair, (double)edgeSet.get(pair) + 1.0);	// Increment the edge weight if it exists in the EdgeSet
                       } else {
                           edgeSet.put(pair, 1.0);								// Or create a new entry set at weight 1.0
                       }
                   }
               }
           }
       }
   }
    //Multi-sentence-complete sliding window
    public void generateByMultiSentenceSlidingWindow(ArrayList<ArrayList<Token>> lines, 
    		int maxWindowSentences, int maxWindowTokens) 
    {

        //HashMap<TokenPair, Double> edgeset = new HashMap<>();

        int minWindowTokens = 1 + maxWindowTokens / 5;

        //For each window...
        //(includes smaller-size windows towards the beginning and end of the lines)
        for (int i = 1 - maxWindowSentences; i < lines.size(); i++) {
            ArrayList<Token> windowTokens = new ArrayList<>();
            //Add sentences to the current window until the sentence or token maximum is met
            for (int j = i; j < i + maxWindowSentences; j++) {
                if (j >= 0 && j < lines.size() //Don't add out-of-bounds sentences
                        && (windowTokens.size() + lines.get(j).size() < maxWindowTokens //Don't exceed max tokens
                        || windowTokens.size() < minWindowTokens)) {    //Unless necessary to reach min tokens
                    windowTokens.addAll(lines.get(j));
                } else {
                    break;
                }
            }

            //Generate and attenuate the window-level network based on window size
            HashMap<TokenPair, Double> windowNetwork = networkSentence(windowTokens);
            for (Entry edge : windowNetwork.entrySet()) {
                edge.setValue((double)edge.getValue() / (1 + windowNetwork.size()));
            }

            //Add the window-level network to the main network
            for (Entry entry : windowNetwork.entrySet()) {
                edgeSet = sum(edgeSet, windowNetwork);
            }

        }

    }

    //Forms a complete graph of a single sentence
    private HashMap<TokenPair, Double> networkSentence(ArrayList<Token> line) {
        HashMap<TokenPair, Double> edgeset = new HashMap<>();

        for (int i = 0; i < line.size() - 1; i++) {
            for (int j = i + 1; j < line.size(); j++) {
                if (!line.get(i).equals(line.get(j))) {
                    TokenPair pair = new TokenPair(line.get(i).getSignature(), line.get(j).getSignature());
                    if (edgeset.containsKey(pair)) {
                        edgeset.put(pair, (double)edgeset.get(pair) + 1.0);
                    } else {
                        edgeset.put(pair, 1.0);
                    }
                }
            }
        }
        return edgeset;
    }
	
	
	
	
}
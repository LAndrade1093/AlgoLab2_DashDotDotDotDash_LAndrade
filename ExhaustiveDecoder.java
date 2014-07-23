import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import edu.neumont.nlp.*;
import org.apache.commons.codec.*;

public class ExhaustiveDecoder 
{
	private DecodingDictionary diction;
	private float possibilitiesEstimate;
	private ArrayList<MessageBigramResult> possibilities;
	private static final int TOP_RANKING = 20;
	private static int num; 
	
	public ExhaustiveDecoder()
	{
		diction = new DecodingDictionary();
		possibilitiesEstimate = 10000.0f;
	}
	
	public ExhaustiveDecoder(DecodingDictionary dd)
	{
		diction = dd;
		possibilitiesEstimate = 10000.0f;
	}
	
	public ExhaustiveDecoder(int num)
	{
		diction = new DecodingDictionary();
		possibilitiesEstimate = (float)num;
	}
	
	public ExhaustiveDecoder(DecodingDictionary dd, int num)
	{
		diction = dd;
		possibilitiesEstimate = (float)num;
	}
	
	
	/*
	 * Public method that makes the initial call to recurse and decode the message
	 * Returns a list of the top 20 possibilites from the decoding
	 */
	public List<String> decode(String message)
	{
		num = 0;
		possibilities = new ArrayList<>();
		decodingHelper(message, new ArrayList<String>());
		quickSortArray(possibilities);
		List<String> bestPossibilities = new ArrayList<String>();		
		for(int i = 0; i < possibilities.size() && i < TOP_RANKING; i++)
		{
			bestPossibilities.add(possibilities.get(i).getPossibleMessage());
		}
		
		return bestPossibilities;
	}
	
	/*
	 * The recursive method that decodes the message using backtracking
	 */
	private void decodingHelper(String morseCodeToSearch, ArrayList<String> decodedMessage)
	{
		//Base Case
		if(morseCodeToSearch.isEmpty())
		{
			float currentSentenceFrequency = getMessageBigramFrequency(decodedMessage);
			if(isMessageValid(currentSentenceFrequency))
			{
				possibilities.add(new MessageBigramResult(decodedMessage, currentSentenceFrequency));
			}
			return;
		}
		else
		{
			//Break away from search branch if the possible sentence has a low frequency, therefore not a valid sentence
			float frequency = getMessageBigramFrequency(decodedMessage);
			if(!isMessageValid(frequency))
			{
				return;
			}
		
			//Continue searching through the rest of the morse code message
			else
			{
				for(int i = 0; i < morseCodeToSearch.length(); i++)
				{
					String code = morseCodeToSearch.substring(0, i + 1);
					Set<String> posWords = diction.getWordsForCode(code);
					if(posWords != null)
					{
						String currentMessage = morseCodeToSearch.substring(i+1);
						for(String word : posWords)
						{
							ArrayList<String> sentence = new ArrayList<String>(decodedMessage);
							sentence.add(word);
							decodingHelper(currentMessage, sentence);
						}
					}
				}
			}
		}
	}
	
	
	
	/*
	 * Calculates the overall frequency of the string passed in
	 */
	private float getMessageBigramFrequency(ArrayList<String> decodedMessage)
	{
		float frequencyResult = 1.0f;
		for(int i = 1; i < decodedMessage.size(); i++)
		{
			int frequency = diction.frequencyOfFollowingWord(decodedMessage.get(i-1), decodedMessage.get(i));
			frequencyResult *= (frequency/possibilitiesEstimate);
		}
		
		return frequencyResult;
	}
	
	/*
	 * Determines if a message's overall bigram frequency is above a certain threshold
	 * If true, the message is valid and we can continue searching
	 * If false, the message is invalid, indicating a bad branch in the search
	 */
	private boolean isMessageValid(float frequency)
	{
		return frequency > (1.0f/possibilitiesEstimate);
	}
	
	
	
	private void quickSortArray(List<MessageBigramResult> array)
	{
		if(array.size() > 1)
		{
			float pivotValue = array.get(array.size()/2).getOverallFrequency();
			int leftIndex = 0;
			int rightIndex = array.size() - 1;
			while (leftIndex < rightIndex)
			{
				while (array.get(leftIndex).getOverallFrequency() > pivotValue)
				{
					leftIndex++;
				}
				while (array.get(rightIndex).getOverallFrequency() < pivotValue)
				{
					rightIndex--;
				}
				if(leftIndex <= rightIndex)
				{
					MessageBigramResult temp = array.get(leftIndex);
					array.set(leftIndex, array.get(rightIndex));
					array.set(rightIndex, temp);
				}
				quickSortArray(array.subList(0, rightIndex));
				quickSortArray(array.subList(leftIndex + 1, array.size()));
			}
		}
	}
	
	
	/*
	 * An inner class (or struct) that holds a valid version of the decoded message and it's 
	 * frequency result from the bigram test
	 */
	private class MessageBigramResult
	{
		private String decodedMessage;
		private float overallFrequencyValue;
		
		public MessageBigramResult(ArrayList<String> sentence, float frequency)
		{
			decodedMessage = "";
			for(String s : sentence)
			{
				decodedMessage += s + " ";
			}
			overallFrequencyValue = frequency;
		}
		
		public String getPossibleMessage()
		{
			return decodedMessage;
		}
		
		public float getOverallFrequency()
		{
			return overallFrequencyValue;
		}
	}
}
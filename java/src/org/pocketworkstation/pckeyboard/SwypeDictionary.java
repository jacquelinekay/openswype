package org.pocketworkstation.pckeyboard;

import java.util.ArrayList;

import org.pocketworkstation.pckeyboard.Dictionary.WordCallback;
import org.pocketworkstation.pckeyboard.OpenSwypeProvider.Constants;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class SwypeDictionary extends Dictionary {

    public static final String RTAG = "RESULTS";
    public static final String TAG = "SwypeDictionary";
	public static final int maxLengthDiff = 5;
	public static final int maxGuesses = 10;
	public boolean bigramsOnly = false;
	
	public static ContentResolver mContentResolver;
	private ArrayList<String> guesses;
	public static final class Unigrams {
		public static final Uri CONTENT_URI = Uri.parse("content://" + Constants.AUTHORITY + "/unigrams");
		public static final String TABLE_NAME = "unigrams";
		public static final String ID = "rowid";
		public static final String TOKEN = "word";
		public static final String FREQ = "freq";
		public static final String LENGTH = "length";
		
	}
	
	public static final class Bigrams {
		public static final Uri CONTENT_URI = Uri.parse("content://" + Constants.AUTHORITY + "/bigrams");
		public static final String TABLE_NAME = "bigrams";
		public static final String ID = "rowid";
		public static final String WORD1 = "word1";
		public static final String WORD2 = "word2";
		public static final String LENGTH1 = "length1";
		public static final String LENGTH2 = "length2";
		public static final String FREQ = "frequency";
	}
	
	public int getTableLength(String tableName){
		String[] projection = {"COUNT(*)"};
		Uri contentUri;
		if(tableName.equals(Unigrams.TABLE_NAME)){
			contentUri = Unigrams.CONTENT_URI;
		} else {
			contentUri = Bigrams.CONTENT_URI;
		}
		Cursor c = mContentResolver.query(contentUri, projection, null, null, null);
		c.moveToFirst();
		int i = c.getInt(0);
		c.close();
		return i;
	}
	
	public SwypeDictionary(Context c){
		mContentResolver = c.getContentResolver();
	}
	
	public SwypeDictionary(Context c, boolean b){
		mContentResolver = c.getContentResolver();
		bigramsOnly = b;
	}
	
	public ArrayList<String> getGuesses(){
		return guesses;
	}
	
	public void clearGuesses(){
		guesses = new ArrayList<String>(maxGuesses);
	}

	@Override
	public void getBigrams(final WordComposer composer, final CharSequence previousWord,
            final WordCallback callback, int[] nextLettersFrequencies){
		CharSequence typed = composer.getTypedWord();
		if(guesses == null){
			guesses = new ArrayList<String>(maxGuesses);
		}

		if(isValidWord(typed) && !guesses.contains(typed)){
			guesses.add(0, typed.toString());
		}
		
		if(typed == null ||  previousWord == null){
			return;
		}
		if(typed.length()==0 || previousWord.length() == 0){
			return;
		}
		int minDist = Integer.MAX_VALUE;
		//int maxScore = 0;
		String firstChar = String.valueOf(typed.charAt(0));
		String[] projection = {Bigrams.WORD1, Bigrams.WORD2, Bigrams.FREQ};
		String selection = "SUBSTR(" + Bigrams.WORD2 + ", 1, 1)=? AND " + Bigrams.LENGTH1 +
				"<? AND " + Bigrams.LENGTH2 + "<?";
		String[] selectionArgs = {firstChar,
				String.valueOf(previousWord.length() + maxLengthDiff), String.valueOf(typed.length() + maxLengthDiff)};
		String sortOrder = Bigrams.FREQ + " DESC";
		Cursor c = mContentResolver.query(Bigrams.CONTENT_URI, projection, selection,
				selectionArgs, sortOrder);
		while(c.moveToNext()){
			String[] guessed = {c.getString(0), c.getString(1)};
			if(!guesses.contains(guessed[1])){
				int distance = levenshteinDistance(typed, guessed[0]);
				distance += levenshteinDistance(previousWord.toString(), guessed[1]);
				if(distance <= minDist && !guesses.contains(guessed[1]) ){
					minDist = distance;
					int i = guesses.contains(typed) ? 1 : 0;
					guesses.add(i, guessed[1]);
				}

				if(guesses.size() == maxGuesses){
					break;
				}
			}
		}
		c.close();
	}

	public void getWordsFromSwype(WordComposer composer){
		CharSequence typed = composer.getTypedWord();
		if(typed == null || typed.length()==0){
			return;
		}
		int minDist = Integer.MAX_VALUE;
		if(isValidWord(typed) && !guesses.contains(typed)){
			guesses.add(0, typed.toString());
		}
		String firstChar = String.valueOf(typed.charAt(0));
		String[] projection = {Unigrams.TOKEN, Unigrams.FREQ};
		String selection = Unigrams.LENGTH + "<? AND SUBSTR(" + Unigrams.TOKEN + ", 1, 1)=?";
		String[] selectionArgs = {String.valueOf(typed.length() + maxLengthDiff), firstChar};
		Cursor c = mContentResolver.query(Unigrams.CONTENT_URI, projection, selection,
				selectionArgs, Unigrams.FREQ + " DESC");
		while(c.moveToNext()){
			String curString = c.getString(0);

			int distance = levenshteinDistance(typed, curString);
			if(distance <= minDist && !guesses.contains(curString)){
				minDist = distance;
				int i = guesses.contains(typed) ? 1 : 0;
				guesses.add(i, curString);
			}
			if(guesses.size() == maxGuesses){
				break;
			}
		}
		c.close();
	}
	
	@Override
	public void getWords(WordComposer composer, WordCallback callback,
			int[] nextLettersFrequencies) {
		CharSequence typed = composer.getTypedWord();
		//int maxScore = 0;
		int minDist = Integer.MAX_VALUE;
		if(typed == null || typed.length()==0){
			return;
		}
		String firstChar = String.valueOf(typed.charAt(0));
		String[] projection = {Unigrams.TOKEN, Unigrams.FREQ};
		String selection = Unigrams.LENGTH + "<? AND SUBSTR(" + Unigrams.TOKEN + ", 1, 1)=?";
		String[] selectionArgs = {String.valueOf(typed.length() + maxLengthDiff), firstChar};
		Cursor c = mContentResolver.query(Unigrams.CONTENT_URI, projection, selection,
				selectionArgs, Unigrams.FREQ + " DESC");
		while(c.moveToNext()){
			String curString = c.getString(0);
			int distance = levenshteinDistance(typed, curString);
			if(distance <= minDist && !guesses.contains(curString)){
				minDist = distance;
				guesses.add(0, curString);
			}
			/*int score = calculateSimilarity(composer, curString);
			//This is mad sketchy
//			if(score >= maxScore && !guesses.contains(curString)){
				maxScore = score;
				guesses.add(0, curString);
			}*/
			
			if(guesses.size() == maxGuesses){
				break;
			}
		}
		c.close();
		//TODO: callback business?
	}
	
	private static int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
	}
	
	private int levenshteinDistance(CharSequence word1, CharSequence word2){
		int[][] distance = new int[word1.length() + 1][word2.length() + 1];
		 
        for (int i = 0; i <= word1.length(); i++)
                distance[i][0] = i;
        for (int j = 1; j <= word2.length(); j++)
                distance[0][j] = j;

        for (int i = 1; i <= word1.length(); i++)
                for (int j = 1; j <= word2.length(); j++)
                        distance[i][j] = minimum(
                                        distance[i - 1][j] + 1,
                                        distance[i][j - 1] + 1,
                                        distance[i - 1][j - 1]
                                                        + ((word1.charAt(i - 1) == word2.charAt(j - 1)) ? 0
                                                                        : 1));
        return distance[word1.length()][word2.length()];
	}
	
	//here codes represents codes for word1
	private int calculateSimilarity(WordComposer composer, CharSequence word2){
		int score = 0;
		CharSequence word1 = composer.getTypedWord();
		int length = word1.length() < word2.length() ? word1.length() : word2.length();
		for(int i = 0; i < length; i++){
			char c1 = word1.charAt(i);
			char c2 = word2.charAt(i);
			if (c1 == c2){
				score++;
			} else {
				//check code similarity?
				int[] codes = composer.getCodesAt(i);
				for(int code : codes){
					if((char) code == c2){
						score++;
					}
				}
			}
		}
		if(word1.charAt(word1.length()-1) == word2.charAt(word2.length()-1)){
			score += 3;
		}
		score -= Math.abs(word2.length() - word1.length())/2;
		return score;
	}
	
	private int calculateSimilarity(CharSequence composer, CharSequence word2){
		int score = 0;
		CharSequence word1 = composer;
		int length = word1.length() < word2.length() ? word1.length() : word2.length();
		for(int i = 0; i < length; i++){
			char c1 = word1.charAt(i);
			char c2 = word2.charAt(i);
			if (c1 == c2){
				score++;
			}
		}
		if(word1.charAt(word1.length()-1) == word2.charAt(word2.length()-1)){
			score += 3;
		}
		// penalize length difference
		score -= Math.abs(word2.length() - word1.length())/2;
		return score;
	}
	
	@Override
	public boolean isValidWord(CharSequence word) {
		String[] projection;
		String selection;
		Uri contentUri;
		if(bigramsOnly){
			projection = new String[]{Bigrams.WORD1, Bigrams.WORD2};
			selection = Bigrams.WORD1 + "=? OR " + Bigrams.WORD2 + "=?";
			contentUri = Bigrams.CONTENT_URI;
		} else{

			projection = new String[]{Unigrams.TOKEN};
			selection = Unigrams.TOKEN + "=?";
			contentUri = Unigrams.CONTENT_URI;
		}
		String selectionArgs[] = {word.toString()};
		Cursor c = mContentResolver.query(contentUri, projection, selection, selectionArgs, null);
		boolean b = c.getCount() != 0;
		c.close();
		return b;
	}

}

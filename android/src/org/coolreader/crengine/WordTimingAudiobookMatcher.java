package org.coolreader.crengine;

import android.media.MediaMetadataRetriever;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WordTimingAudiobookMatcher {
	public static final Logger log = L.create("wordtiming");

	private static class WordTiming {
		String word;
		Double startTime;
		File audioFile;

		public WordTiming(String word, Double startTime, File audioFile){
			this.word = word;
			this.startTime = startTime;
			this.audioFile = audioFile;
		}
	}

	private final File wordTimingsFile;
	private final String audioFileRelativeDir;
	private final List<SentenceInfo> allSentences;
	private final Map<String, SentenceInfo> sentencesByStartPos = new HashMap<>();
	private final Map<String, File> audioFilesByAudioFileName = new HashMap<>();
	private final Map<File, String> audioFileNamesByAudioFile = new HashMap<>();

	private boolean sentenceTimingReady = false;

	public WordTimingAudiobookMatcher(File wordTimingsFile, List<SentenceInfo> allSentences) {
		this.wordTimingsFile = wordTimingsFile;
		this.audioFileRelativeDir = wordTimingsFile.getAbsoluteFile().getParent();
		this.allSentences = allSentences;
		for(SentenceInfo s : allSentences){
			sentencesByStartPos.put(s.startPos, s);
		}
	}

	public void parseWordTimingsFile(){
		List<WordTiming> wordTimings;
		try {
			BufferedReader br = new BufferedReader(new FileReader(wordTimingsFile));
			String line;
			wordTimings = new ArrayList<>();
			while ((line = br.readLine()) != null) {
				WordTiming wordTiming = parseWordTimingsLine(line);
				if(wordTiming == null){
					log.d("ERROR: could not parse word timings line: " + line);
				}else{
					wordTimings.add(wordTiming);
				}
			}
			br.close();
		} catch(Exception e) {
			log.d("ERROR: could not read word timings file: " + wordTimingsFile + " " + e);
			wordTimings = new ArrayList<>();
		}

		updateSentenceInfoNextSentence();

		Map<String, List<String>> wordsBySentencePos = new HashMap<>();
		for(SentenceInfo s : allSentences){
			wordsBySentencePos.put(s.startPos, splitSentenceIntoWords(s.text));
		}

		if(wordTimings.size() == 0){
			return;
		}

		int wtIndex = 0;
		double prevStartTime = 0;
		File prevAudioFile = wordTimings.get(0).audioFile;
		for(SentenceInfo s : allSentences){
			SentenceTiming t = new SentenceTiming();
			s.sentenceTiming = t;

			List<String> words = wordsBySentencePos.get(s.startPos);
			if(words.size() == 0){
				t.startTime = prevStartTime;
				t.audioFile = prevAudioFile;
				continue;
			}
			boolean matchFailed = false;
			WordTiming firstWordTiming = null;
			int sentenceWtIndex = wtIndex;
			for(String wordInSentence : words){
				int wordWtIndex = sentenceWtIndex;
				boolean wordFound = false;
				while(wordWtIndex <= wordTimings.size()){
					if(wordsMatch(wordInSentence, wordTimings.get(wordWtIndex).word)){
						wordFound = true;
						break;
					}else if(wordWtIndex - sentenceWtIndex > 20){
						break;
					}else{
						wordWtIndex++;
					}
				}
				if(wordFound){
					if(firstWordTiming == null){
						firstWordTiming = wordTimings.get(wordWtIndex);
					}
					sentenceWtIndex = wordWtIndex + 1;
				}else{
					matchFailed = true;
					break;
				}
			}
			if(matchFailed){
				t.startTime = prevStartTime;
				t.audioFile = prevAudioFile;
			}else{
				wtIndex = sentenceWtIndex;
				t.startTime = firstWordTiming.startTime;
				t.audioFile = firstWordTiming.audioFile;
				prevStartTime = t.startTime;
				prevAudioFile = t.audioFile;
			}
		}

		//start first sentence of all audio files at 0.0
		// prevents skipping intros
		File curAudioFile = null;
		double prevTotalAudioFileDurations = 0;
		for(SentenceInfo s : allSentences){
			SentenceTiming t = s.sentenceTiming;
			if(curAudioFile == null || t.audioFile != curAudioFile){
				t.isFirstSentenceInAudioFile = true;
				t.startTime = 0;
				if(curAudioFile != null){
					prevTotalAudioFileDurations += getAudioFileDuration(curAudioFile);
				}
				curAudioFile = t.audioFile;
			}
			t.startTimeInBook = t.startTime + prevTotalAudioFileDurations;
		}

		double totalBookDuration = prevTotalAudioFileDurations;
		if(curAudioFile != null){
			totalBookDuration += getAudioFileDuration(curAudioFile);
		}

		for(SentenceInfo s : allSentences){
			s.sentenceTiming.totalBookDuration = totalBookDuration;
		}

		this.sentenceTimingReady = true;
	}

	public void maybeReadSentenceTimingCache(File sentenceTimingCacheFile){
		try {
			if(sentenceTimingCacheFile == null || !sentenceTimingCacheFile.exists()){
				return;
			}

			BufferedReader br = new BufferedReader(new FileReader(sentenceTimingCacheFile));
			String line;
			while ((line = br.readLine()) != null) {
				Map.Entry<String, SentenceTiming> sentenceTimingRes = parseSentenceTimingLine(line);
				if(sentenceTimingRes == null){
					log.d("ERROR: could not parse sentence timing line: " + line);
				}else{
					String startPos = sentenceTimingRes.getKey();
					SentenceTiming t = sentenceTimingRes.getValue();
					SentenceInfo s = sentencesByStartPos.get(startPos);
					s.sentenceTiming = t;
				}
			}
			br.close();

			updateSentenceInfoNextSentence();

			this.sentenceTimingReady = true;
		} catch(Exception e) {
			log.d("ERROR: could not read timing cache file: " + sentenceTimingCacheFile, e);
		}
	}

	public void maybeWriteSentenceTimingCache(File sentenceTimingCacheFile){
		try{
			FileWriter fw = new FileWriter(sentenceTimingCacheFile);
			for(SentenceInfo s : allSentences){
				SentenceTiming t = s.sentenceTiming;
				fw.write(""
					+ ""  + s.startPos
					+ "," + t.startTime
					+ "," + t.startTimeInBook
					+ "," + t.totalBookDuration
					+ "," + t.isFirstSentenceInAudioFile
					+ "," + audioFileNamesByAudioFile.get(t.audioFile)
					+ "\n"
				);
			}
			fw.close();
		} catch(Exception e) {
			log.d("ERROR: could not write timing cache file: " + sentenceTimingCacheFile, e);
		}
	}

	public boolean isSentenceTimingReady(){
		return this.sentenceTimingReady;
	}

	public SentenceInfo getSentence(String startPos){
		return sentencesByStartPos.get(startPos);
	}

	private void updateSentenceInfoNextSentence(){
		for(int i=0; i<allSentences.size(); i++){
			SentenceInfo s = allSentences.get(i);
			SentenceInfo nextSentence;
			if(i+1<allSentences.size()){
				nextSentence = allSentences.get(i+1);
			}else{
				nextSentence = null;
			}
			s.nextSentence = nextSentence;
		}
	}

	private Double getAudioFileDuration(File file){
		try{
			MediaMetadataRetriever m = new MediaMetadataRetriever();
			m.setDataSource(file.getAbsolutePath());
			String durationStr = m.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			return Long.parseLong(durationStr) / 1000.0;
		}catch(Exception e){
			log.d("ERROR: could not get audio file duration for " + file, e);
			return 0.0;
		}
	}

	private WordTiming parseWordTimingsLine(String line){
		int sep1 = line.indexOf(',');
		int sep2 = line.indexOf(',', sep1+1);
		if(sep1 < 0 || sep2 < 0 || sep1 >= line.length() || sep2 >= line.length()){
			return null;
		}
		String word = line.substring(sep1+1, sep2);
		Double startTime = Double.parseDouble(line.substring(0, sep1));
		String audioFileName = line.substring(sep2+1);
		File audioFile = audioFilesByAudioFileName.get(audioFileName);
		if(audioFile == null){
			audioFile = new File(audioFileRelativeDir + "/" + audioFileName);
			audioFilesByAudioFileName.put(audioFileName, audioFile);
			audioFileNamesByAudioFile.put(audioFile, audioFileName);
		}
		return new WordTiming(word, startTime, audioFile);
	}

	private Map.Entry<String, SentenceTiming> parseSentenceTimingLine(String line){
		String[] cols = line.split(",", 6);
		if(cols.length != 6){
			return null;
		}
		SentenceTiming t = new SentenceTiming();
		String startPos = cols[0];
		t.startTime = Double.parseDouble(cols[1]);
		t.startTimeInBook = Double.parseDouble(cols[2]);
		t.totalBookDuration = Double.parseDouble(cols[3]);
		t.isFirstSentenceInAudioFile = Boolean.parseBoolean(cols[4]);
		String audioFileName = cols[5];
		File audioFile = audioFilesByAudioFileName.get(audioFileName);
		if(audioFile == null){
			audioFile = new File(audioFileRelativeDir + "/" + audioFileName);
			audioFilesByAudioFileName.put(audioFileName, audioFile);
			audioFileNamesByAudioFile.put(audioFile, audioFileName);
		}
		t.audioFile = audioFile;
		return new AbstractMap.SimpleEntry<>(startPos, t);
	}

	private boolean wordsMatch(String word1, String word2){
		if(word1 == null && word2 == null) {
			return true;
		} else if(word1 == null || word2 == null) {
			return false;
		} else if(word1.equals(word2)) {
			return true;
		} else {
			//expensive calculation, but relatively rarely performed
			String word1Letters = "";
			String word2Letters = "";
			String word1Digits = "";
			String word2Digits = "";
			for(int i=0; i<word1.length(); i++){
				char ch = word1.charAt(i);
				if(Character.isLetter(ch)){
					word1Letters += ch;
				}else if(Character.isDigit(ch)){
					word1Digits += ch;
				}
			}
			for(int i=0; i<word2.length(); i++){
				char ch = word2.charAt(i);
				if(Character.isLetter(ch)) {
					word2Letters += ch;
				}else if(Character.isDigit(ch)){
					word2Digits += ch;
				}
			}
			if(word1Letters.length() > 0 && word2Letters.length() > 0) {
					//if there is at least one letter in each word: compare only letters
					return word1Letters.equals(word2Letters);
			}else if(word1Digits.length() > 0 && word2Digits.length() > 0) {
					//if there is at least one number in each word: compare only numbers
					return word1Digits.equals(word2Digits);
			}else{
					return word1.equals(word2);
			}
		}
	}

	private List<String> splitSentenceIntoWords(String sentence){
		List<String> words = new ArrayList<String>();

		StringBuilder str = null;
		boolean wordContainsLetterOrNumber = false;
		for(int i=0; i<sentence.length(); i++){
			char ch = sentence.charAt(i);
			if(ch == '’'){
				ch = '\'';
			}
			ch = Character.toLowerCase(ch);

			boolean isWordChar;
			if(Character.isLetter(ch)){
				isWordChar = true;
				wordContainsLetterOrNumber = true;
			}else if(Character.isDigit(ch)){
				isWordChar = true;
				wordContainsLetterOrNumber = true;
			}else if(ch == '\''){
				isWordChar = true;
			}else{
				isWordChar = false;
			}

			if(isWordChar){
				if(str == null){
					str = new StringBuilder();
				}
				str.append(ch);
			}

			if(str != null && (!isWordChar || i == sentence.length()-1)){
				if(wordContainsLetterOrNumber){
					words.add(str.toString());
				}
				str = null;
				wordContainsLetterOrNumber = false;
			}
		}

		return words;
	}

	public static void main(String[] args){
		if(args.length != 2){
			System.out.println("USAGE: SENTENCE_INFO_FILE WORDTIMING_FILE");
		}
		List<SentenceInfo> sentences = SentenceInfoCache.maybeReadCache(new File(args[0]));
		new WordTimingAudiobookMatcher(new File(args[1]), sentences).parseWordTimingsFile();
	}
}

package com.interview.servicenow;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PatternParser {

    private static final String RESPONSE_TEMPLATE = "src/main/resources/responseTemplate.txt";
    public static final String SPACE_SEPARATOR = " ";
    public static final String ONLY_ALPHA_REG = "[^a-zA-Z\\s]";
    public static final String EMPTY_STRING = "";

    private List<String> sentences;

    public PatternParser(List<String> sentences){
        this.sentences = sentences;
    }

    public Map<String,String> parseSentences(){
        Integer sentenceIndex = 0;
        Map<String, Set<Integer>> wordsToSentencesIndexMap = new HashMap<>();
        Map<String,String> responseTemplateMap = new HashMap<>();
        Map<String,String> sentenceToFullSentence = new HashMap<>();
        List<String> filterSentenses = updateListOfSentencesWithoutTimestamp(sentenceToFullSentence);

        for(String filteredSentence : filterSentenses) {
            List<String> wordsInSentence = Arrays.asList(filteredSentence.split(SPACE_SEPARATOR));
            Map<Integer, Integer> commonWordsToPreviousLinesMap = new HashMap<>();
            Integer desiredMatchLength = wordsInSentence.size()-1;
            processWordsInSentence(sentenceIndex, wordsToSentencesIndexMap, wordsInSentence, commonWordsToPreviousLinesMap);
            List<Integer> desiredMatchLenghtSentences = getListOfLineIndexesHavingDesiredMatchLenght(commonWordsToPreviousLinesMap, desiredMatchLength);
            Map<String,Set<String>> templatesToChangedWordsMap = getTemplatesToChangedWordsMap(desiredMatchLenghtSentences, filterSentenses, sentenceIndex);
            buildResponseTemplate(templatesToChangedWordsMap,responseTemplateMap,sentenceToFullSentence);

            sentenceIndex++;
        }
        return responseTemplateMap;
    }

    private void processWordsInSentence(Integer sentenceSequence, Map<String, Set<Integer>> wordsToSentencesIndexMap, List<String> wordsInSentence, Map<Integer, Integer> commonWordsToPreviousLinesMap) {
        for (String word : wordsInSentence) {
            if (wordsToSentencesIndexMap.containsKey(word)) {
                updateNumberOfUsageOfTheWord(commonWordsToPreviousLinesMap, wordsToSentencesIndexMap.get(word));
                wordsToSentencesIndexMap.get(word).add(sentenceSequence);
            } else {
                Set<Integer> wordRefSet = new HashSet<>();
                wordRefSet.add(sentenceSequence);
                wordsToSentencesIndexMap.put(word, wordRefSet);
            }
        }
    }

    private List<String> updateListOfSentencesWithoutTimestamp(Map<String,String> sentenceToFullSentence) {
        List<String> filterSentenses = new ArrayList<>();
        String filteredSentence = EMPTY_STRING;
        for(String sentence : sentences){
            filteredSentence = sentence.replaceAll(ONLY_ALPHA_REG, EMPTY_STRING).trim();
            sentenceToFullSentence.put(filteredSentence,sentence);
            filterSentenses.add(filteredSentence);
        }
        return filterSentenses;
    }

    private void buildResponseTemplate(Map<String,Set<String>> templatesToChangedWordsMap,Map<String,String> responseTemplateMap,Map<String,String> sentenceToFullSentence) {
        String responseTemplate = EMPTY_STRING;
        try {
            responseTemplate = new String(Files.readAllBytes(Paths.get(RESPONSE_TEMPLATE)), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Map.Entry<String,Set<String>> templateEntry : templatesToChangedWordsMap.entrySet()){
            StringBuilder sentencesMatch = new StringBuilder();
            for(String placeHolder : templateEntry.getValue()){
                String message = sentenceToFullSentence.get(MessageFormat.format(templateEntry.getKey(),placeHolder));
                sentencesMatch.append(message);
                sentencesMatch.append("\n");
            }

            responseTemplateMap.put(templateEntry.getKey(),MessageFormat.format(responseTemplate,sentencesMatch.toString().trim(),templateEntry.getValue()));
        }
    }

    private Map<String,Set<String>> getTemplatesToChangedWordsMap(List<Integer> matchedPatternSequences,List<String> filterSentenses,Integer currentSentenceSequence) {
        Map<String,Set<String>> templateToListOfChangingWordsMap = new HashMap<>();
        if(matchedPatternSequences.isEmpty()){
            return templateToListOfChangingWordsMap;
        }

        for(Integer seq : matchedPatternSequences){
            Set<String> listOfDiffrentWords = new HashSet<>();
            String template = buildSentenceTemplate(filterSentenses, currentSentenceSequence, listOfDiffrentWords, seq);
            updateTemplatesMap(listOfDiffrentWords, templateToListOfChangingWordsMap, template);
            String currentLineIndexTemplate = buildSentenceTemplate(filterSentenses, seq, listOfDiffrentWords, currentSentenceSequence);
            updateTemplatesMap(listOfDiffrentWords, templateToListOfChangingWordsMap, currentLineIndexTemplate);
        }
        return templateToListOfChangingWordsMap;
    }

    private String buildSentenceTemplate(List<String> filterSentenses, Integer currentSentenceSequence, Set<String> listOfDiffrentWords, Integer seq) {
        StringBuilder templateBuilder = new StringBuilder();
        for (String word : filterSentenses.get(seq).split(SPACE_SEPARATOR)) {
            if (!filterSentenses.get(currentSentenceSequence).contains(word)) {
                listOfDiffrentWords.add(word);
                templateBuilder.append("{0}").append(SPACE_SEPARATOR);
            } else {
                templateBuilder.append(word).append(SPACE_SEPARATOR);
            }
        }
        return templateBuilder.toString().trim();
    }

    private void updateTemplatesMap(Set<String> listOfDiffrentWords, Map<String, Set<String>> templateToListOfChangingWordsMap, String template) {
        if(templateToListOfChangingWordsMap.containsKey(template)){
            templateToListOfChangingWordsMap.get(template).addAll(listOfDiffrentWords);
        }else {
            templateToListOfChangingWordsMap.put(template, listOfDiffrentWords);
        }
    }

    private List<Integer> getListOfLineIndexesHavingDesiredMatchLenght(Map<Integer, Integer> commonWordsToPreviousLinesMap, Integer desiredMatchLength) {
        List<Integer> sequences = new ArrayList<>();
        for(Map.Entry<Integer,Integer> commonWordsEntry : commonWordsToPreviousLinesMap.entrySet()){
            if(desiredMatchLength.equals(commonWordsEntry.getValue())){
                sequences.add(commonWordsEntry.getKey());
            }
        }
        return sequences;
    }

    private void updateNumberOfUsageOfTheWord(Map<Integer, Integer> commonWordsMap, Set<Integer> sentenceSequences) {
        for(Integer sentenceSequence : sentenceSequences){
            if(commonWordsMap.containsKey(sentenceSequence)){
                Integer value = commonWordsMap.get(sentenceSequence) + 1;
                commonWordsMap.put(sentenceSequence,value);
            }else{
                commonWordsMap.put(sentenceSequence,1);
            }
        }
    }

    public List<String> getSentences() {
        return sentences;
    }

    public void setSentences(List<String> sentences) {
        this.sentences = sentences;
    }
}

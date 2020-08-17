package com.interview.servicenow;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LogParesrMain {

    public static void main(String[] args) {

        String inputFileName = "src/main/resources/privateInvLog.txt";
        String outputFileName = "target/templateResponse.txt";

        try {
            List list = getLinesFromFileAsList(inputFileName);
            PatternParser patternParser = new PatternParser(list);
            Map<String,String> responseTemplateMap = patternParser.parseSentences();
            StringBuilder responseBuilder = new StringBuilder();
            for(String val : responseTemplateMap.values()){
                responseBuilder.append(val);
            }
            writeResponseToFile(responseBuilder.toString(),outputFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void writeResponseToFile(String responseTemplateMap,String fileName)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        writer.write(responseTemplateMap);

        writer.close();
    }

    private static List getLinesFromFileAsList(String fileName) throws IOException {
        List<String> result;
        try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
            result = lines.collect(Collectors.toList());
        }
        return result;

    }

}

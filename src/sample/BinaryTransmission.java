package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinaryTransmission {
    private List<TramaItem> word;
    private Integer counter=0;
    private Integer wordCounter=1;
    private Integer letterCounter=0;
    private List<List<String>> operationsArray= Arrays.asList(
            Arrays.asList("Inicio","SKAB1"),
            Arrays.asList("Fin","SKYZ10"),
            Arrays.asList("Si","#DH1"),
            Arrays.asList("No","*DH0"),
            Arrays.asList(" ","!DH3")
    );
    private Map<String,String> specialOperations=new HashMap<>(operationsArray.size());

    BinaryTransmission() {
        for(List<String> item:operationsArray){
            specialOperations.put(item.get(0),item.get(1));
            specialOperations.put(item.get(1),item.get(0));
        }
    }

    boolean doTransformation(String wordToSend){
        if(String.valueOf(wordToSend.charAt(counter)).matches("^[\\s]$")){
            letterCounter=0;
            wordCounter++;
            word.add(new TramaItem(
                new SimpleStringProperty(" "),
                new SimpleStringProperty(specialOperations.get(String.valueOf(wordToSend.charAt(counter)))),
                new SimpleStringProperty(String.valueOf(wordToSend.charAt(counter))),
                new SimpleStringProperty(" ")
            ));
        }else{
            letterCounter++;
            word.add(new TramaItem(
                new SimpleStringProperty("P"+wordCounter.toString()),
                new SimpleStringProperty(Integer.toBinaryString(String.valueOf(wordToSend.charAt(counter)).codePointAt(0))),
                new SimpleStringProperty(String.valueOf(wordToSend.charAt(counter))),
                new SimpleStringProperty("T"+letterCounter.toString())
            ));
        }

        counter++;

        if(counter==wordToSend.length()){
            return true;
        }else{
            return doTransformation(wordToSend);
        }
    }

    void resetAll(){
        if(word!=null){
            word.clear();
        }
        wordCounter=1;
        counter=0;
        letterCounter=0;
    }

    public List<TramaItem> getWord() {
        return word;
    }

    void setWord(List<TramaItem> word) {
        this.word = word;
    }

    public Map<String, String> getSpecialOperations(){
        return specialOperations;
    }
}

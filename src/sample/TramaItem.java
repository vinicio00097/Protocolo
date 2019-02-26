package sample;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.SimpleStringProperty;

public class TramaItem extends RecursiveTreeObject<TramaItem> {
    private SimpleStringProperty wordIndex;
    private SimpleStringProperty binaryLetter;
    private SimpleStringProperty letter;
    private SimpleStringProperty complementoCrc=new SimpleStringProperty();
    private SimpleStringProperty tramaIndex;

    public TramaItem(SimpleStringProperty wordIndex, SimpleStringProperty binaryLetter,SimpleStringProperty letter,SimpleStringProperty tramaIndex) {
        this.wordIndex = wordIndex;
        this.binaryLetter = binaryLetter;
        this.letter=letter;
        this.tramaIndex = tramaIndex;
    }

    public String getWordIndex() {
        return wordIndex.get();
    }

    public SimpleStringProperty wordIndexProperty() {
        return wordIndex;
    }

    public void setWordIndex(String wordIndex) {
        this.wordIndex.set(wordIndex);
    }

    public String getBinaryLetter() {
        return binaryLetter.get();
    }

    public SimpleStringProperty binaryLetterProperty() {
        return binaryLetter;
    }

    public void setBinaryLetter(String binaryLetter) {
        this.binaryLetter.set(binaryLetter);
    }

    public String getLetter() {
        return letter.get();
    }

    public SimpleStringProperty letterProperty() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter.set(letter);
    }

    public String getComplementoCrc() {
        return complementoCrc.get();
    }

    public SimpleStringProperty complementoCrcProperty() {
        return complementoCrc;
    }

    public void setComplementoCrc(String complementoCrc) {
        this.complementoCrc.set(complementoCrc);
    }

    public String getTramaIndex() {
        return tramaIndex.get();
    }

    public SimpleStringProperty tramaIndexProperty() {
        return tramaIndex;
    }

    public void setTramaIndex(String tramaIndex) {
        this.tramaIndex.set(tramaIndex);
    }
}

package ch.petrce.triolingo;

public class Vocab {
    private String value;
    private String translation;
    private Integer correct;

    public String getValue() {
        return value;
    }

    public String getTranslation() {
        return translation;
    }

    public Integer getCorrect() {
        return correct;
    }

    public void setCorrect(Integer correct) {
        this.correct = correct;
    }
}

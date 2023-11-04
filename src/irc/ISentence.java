package irc;

import jvn.ReadAnnotation;
import jvn.WriteAnnotation;

public interface ISentence {
    @WriteAnnotation
    void write(String text);

    @ReadAnnotation
    String read();
}

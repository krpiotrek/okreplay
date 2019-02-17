package okreplay;

import java.util.List;

public interface InteractionsMode {

    int size();

    List<YamlRecordedInteraction> getInteractions();

    void setInteractions(List<YamlRecordedInteraction> interactions);

    boolean seek(Request request);

    Response play(Request request);

    void record(RecordedInteraction interaction);
}

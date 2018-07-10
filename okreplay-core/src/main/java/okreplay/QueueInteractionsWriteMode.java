package okreplay;

import java.util.ArrayList;
import java.util.List;

class QueueInteractionsWriteMode implements InteractionsMode {

    private List<YamlRecordedInteraction> interactions = new ArrayList<>();

    @Override
    public int size() {
        return interactions.size();
    }

    @Override
    public List<YamlRecordedInteraction> getInteractions() {
        return interactions;
    }

    @Override
    public void setInteractions(List<YamlRecordedInteraction> interactions) {
        this.interactions = interactions;
    }

    @Override
    public boolean seek(Request request) {
        throw new RuntimeException("no seek in WRITE mode");
    }

    @Override
    public Response play(Request request) {
        throw new RuntimeException("no playing in WRITE mode");
    }

    @Override
    public void record(RecordedInteraction interaction) {
        interactions.add(interaction.toYaml());
    }
}

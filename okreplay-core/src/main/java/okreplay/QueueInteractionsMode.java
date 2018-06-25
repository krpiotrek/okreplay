package okreplay;

import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.annotation.Nullable;

import static okreplay.Util.stringify;

class QueueInteractionsMode implements InteractionsMode {

    private List<YamlRecordedInteraction> interactions;
    private final MatchRule matchRule;
    private final LinkedHashMap<Request, Deque<RecordedInteraction>> map = new LinkedHashMap<>();

    QueueInteractionsMode(List<YamlRecordedInteraction> interactions, MatchRule matchRule) {
        this.interactions = interactions;
        this.matchRule = matchRule;
        createMap();
    }

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
        map.clear();
        createMap();
    }

    private void createMap() {
        List<YamlRecordedInteraction> list = new ArrayList<>(interactions);

        while (!list.isEmpty()) {
            Request request = list.get(0).toImmutable().request();
            Deque<RecordedInteraction> queue = new LinkedList<>();

            for (Iterator<YamlRecordedInteraction> it = list.iterator(); it.hasNext(); ) {
                YamlRecordedInteraction interaction = it.next();
                Request testedRequest = interaction.toImmutable().request();
                if (matchRule.isMatch(request, testedRequest)) {
                    queue.add(interaction.toImmutable());
                    it.remove();
                }
            }

            map.put(request, queue);
        }
    }

    @Override
    public boolean seek(Request request) {
        Queue<RecordedInteraction> yamlRecordedInteractions = findQueueForRequest(request);
        return yamlRecordedInteractions != null && yamlRecordedInteractions.peek() != null;
    }

    @Override
    public Response play(Request request) {
        Queue<RecordedInteraction> queueForRequest = findQueueForRequest(request);
        if(queueForRequest == null) throw new RuntimeException("request has not been recorded " + stringify(request));

        RecordedInteraction yamlRecordedInteraction = queueForRequest.poll();
        if (yamlRecordedInteraction == null)
            throw new IndexOutOfBoundsException("all requests have been played already");

        return yamlRecordedInteraction.response();
    }

    @Nullable
    private Queue<RecordedInteraction> findQueueForRequest(Request request) {
        for (Map.Entry<Request, Deque<RecordedInteraction>> entry : map.entrySet()) {
            if (matchRule.isMatch(entry.getKey(), request)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public void record(RecordedInteraction interaction) {
        interactions.add(interaction.toYaml());
    }
}

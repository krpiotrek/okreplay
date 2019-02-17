package okreplay;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static okreplay.Util.stringify;

public class SequentialInteractionsMode implements InteractionsMode {

  private final transient AtomicInteger orderedIndex = new AtomicInteger();

  private final MatchRule matchRule;

  private List<YamlRecordedInteraction> interactions;

  SequentialInteractionsMode(List<YamlRecordedInteraction> interactions, MatchRule matchRule) {
    this.matchRule = matchRule;
    this.interactions = interactions;
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
  }

  @Override
  public boolean seek(Request request) {
    try {
      // TODO: it's a complete waste of time using an AtomicInteger when this method is called
      // before play in a non-transactional way
      Integer index = orderedIndex.get();
      RecordedInteraction interaction = interactions.get(index).toImmutable();
      Request nextRequest = interaction == null ? null : interaction.request();
      return nextRequest != null && matchRule.isMatch(request, nextRequest);
    } catch (IndexOutOfBoundsException e) {
      throw new NonWritableTapeException();
    }
  }

  @Override
  public Response play(Request request) {
    Integer nextIndex = orderedIndex.getAndIncrement();
    RecordedInteraction nextInteraction = interactions.get(nextIndex).toImmutable();
    if (nextInteraction == null) {
      throw new IllegalStateException(String.format("No recording found at position %s",
          nextIndex));
    }

    if (!matchRule.isMatch(request, nextInteraction.request())) {
      throw new IllegalStateException(String.format("Request %s does not match recorded " +
          "request" + " %s", stringify(request), stringify(nextInteraction.request())));
    }

    return nextInteraction.response();
  }

  @Override
  public void record(RecordedInteraction interaction) {
    interactions.add(interaction.toYaml());
  }
}

package okreplay;

import java.util.List;

public class SingleInteractionsMode implements InteractionsMode {

  private List<YamlRecordedInteraction> interactions;
  private final MatchRule matchRule;

  SingleInteractionsMode(List<YamlRecordedInteraction> interactions, MatchRule matchRule) {
    this.interactions = interactions;
    this.matchRule = matchRule;
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
    return findMatch(request) >= 0;
  }

  @Override
  public Response play(Request request) {
    int position = findMatch(request);
    if (position < 0) {
      throw new IllegalStateException("no matching recording found");
    } else {
      return interactions.get(position).toImmutable().response();
    }
  }

  @Override
  public void record(RecordedInteraction interaction) {
    int position = findMatch(interaction.request());
    if (position >= 0) {
      interactions.set(position, interaction.toYaml());
    } else {
      interactions.add(interaction.toYaml());
    }
  }

  private synchronized int findMatch(final Request request) {
    return Util.indexOf(interactions.iterator(), new Predicate<YamlRecordedInteraction>() {
      @Override
      public boolean apply(YamlRecordedInteraction input) {
        return matchRule.isMatch(request, input.toImmutable().request());
      }
    });
  }
}

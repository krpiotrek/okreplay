package okreplay;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static okreplay.Util.VIA;

/**
 * Represents a set of recorded HTTP interactions that can be played back or
 * appended to.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
abstract class MemoryTape implements Tape {
  private String name;
  private List<YamlRecordedInteraction> interactions = new ArrayList<>();
  private transient TapeMode mode = OkReplayConfig.DEFAULT_MODE;
  private transient MatchRule matchRule = OkReplayConfig.DEFAULT_MATCH_RULE;
  private transient InteractionsMode interactionsMode = new SingleInteractionsMode(interactions, matchRule);

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public TapeMode getMode() {
    return mode;
  }

  @Override
  public void setMode(TapeMode mode) {
    this.mode = mode;
  }

  @Override
  public MatchRule getMatchRule() {
    return this.matchRule;
  }

  @Override
  public void setMatchRule(MatchRule matchRule) {
    this.matchRule = matchRule;
  }

  @Override
  public boolean isReadable() {
    return mode.isReadable();
  }

  @Override
  public boolean isWritable() {
    return mode.isWritable();
  }

  @Override
  public boolean isSequential() {
    return mode.isSequential();
  }

  @Override
  public int size() {
    return interactionsMode.size();
  }

  @Override
  public void start() {
    if (mode.isSequential()) {
      interactionsMode = new SequentialInteractionsMode(interactions, matchRule);
    } else if (mode.isQueued()) {
      if (mode.isWritable()) {
        interactionsMode = new QueueInteractionsWriteMode(interactions);
      } else {
        interactionsMode = new QueueInteractionsReadMode(interactions, matchRule);
      }
    } else {
      interactionsMode = new SingleInteractionsMode(interactions, matchRule);
    }
  }

  public List<YamlRecordedInteraction> getInteractions() {
    return interactionsMode.getInteractions();
  }

  public void setInteractions(List<YamlRecordedInteraction> interactions) {
    interactionsMode.setInteractions(interactions);
  }

  @Override
  public boolean seek(Request request) {
    return interactionsMode.seek(request);
  }

  @Override
  public Response play(final Request request) {
    if (!mode.isReadable()) {
      throw new IllegalStateException("the tape is not readable");
    }

    return interactionsMode.play(request);
  }

  @Override
  public synchronized void record(Request request, Response response) {
    if (!mode.isWritable()) {
      throw new IllegalStateException("the tape is not writable");
    }

    RecordedInteraction interaction = new RecordedInteraction(new Date(), recordRequest(request),
        recordResponse(response));

    interactionsMode.record(interaction);
  }

  @Override
  public String toString() {
    return String.format("Tape[%s]", name);
  }

  private Request recordRequest(Request request) {
    return request.newBuilder()
        .removeHeader(VIA)
        .build();
  }

  private Response recordResponse(Response response) {
    return response.newBuilder()
        .removeHeader(VIA)
        .removeHeader(Headers.X_OKREPLAY)
        .build();
  }
}

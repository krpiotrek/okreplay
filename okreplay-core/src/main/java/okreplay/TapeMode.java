package okreplay;

public enum TapeMode {

  UNDEFINED(false, false, OrderingMode.SINGLE), READ_WRITE(true, true, OrderingMode.SINGLE), READ_ONLY(true, false, OrderingMode.SINGLE),
  READ_ONLY_QUIET(true, false, OrderingMode.SINGLE), READ_SEQUENTIAL(true, false, OrderingMode.SEQUENTIAL),
  WRITE_ONLY(false, true, OrderingMode.SINGLE), WRITE_SEQUENTIAL(false, true, OrderingMode.SEQUENTIAL),
  WRITE_QUEUE(false, true, OrderingMode.QUEUE), READ_QUEUE(true, false, OrderingMode.QUEUE);

  private final boolean readable;
  private final boolean writable;
  private final OrderingMode orderingMode;

  TapeMode(boolean readable, boolean writable, OrderingMode orderingMode) {
    this.readable = readable;
    this.writable = writable;
    this.orderingMode = orderingMode;
  }

  public boolean isReadable() {
    return readable;
  }

  public boolean isWritable() {
    return writable;
  }

  public OrderingMode getOrderingMode() {
    return orderingMode;
  }

  /**
   * For compatibility with Groovy truth.
   */
  public boolean asBoolean() {
    return readable || writable;
  }

  public Optional<TapeMode> toOptional() {
    if (this.equals(TapeMode.UNDEFINED)) {
      return Optional.absent();
    } else {
      return Optional.of(this);
    }
  }

  public boolean isSequential() {
    return orderingMode == OrderingMode.SEQUENTIAL;
  }

  public boolean isQueued() {
    return orderingMode == OrderingMode.QUEUE;
  }

  public enum OrderingMode {
    SINGLE, SEQUENTIAL, QUEUE
  }
}

package com.suwan.qworker.model;

public record NumberTask(int taskId, int number) {
  @Override
  public String toString() {
    return String.format("Task[id=%d, number=%d]", taskId, number);
  }
}

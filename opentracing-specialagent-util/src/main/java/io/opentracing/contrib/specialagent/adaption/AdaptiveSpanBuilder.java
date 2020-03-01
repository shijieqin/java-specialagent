package io.opentracing.contrib.specialagent.adaption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.tag.Tag;

public class AdaptiveSpanBuilder extends Adapter implements Tracer.SpanBuilder {
  private final Tracer.SpanBuilder target;

  AdaptiveSpanBuilder(final String operationName, final Tracer tracer, final AdaptionRules rules) {
    super(rules);
    this.target = tracer.buildSpan(operationName);
    processOperationName(operationName);
    processStart();
  }

  @Override
  public Tracer.SpanBuilder asChildOf(final SpanContext parent) {
    target.asChildOf(parent);
    return this;
  }

  @Override
  public Tracer.SpanBuilder asChildOf(final Span parent) {
    target.asChildOf(parent);
    return this;
  }

  @Override
  public Tracer.SpanBuilder addReference(final String referenceType, final SpanContext referencedContext) {
    target.addReference(referenceType, referencedContext);
    return this;
  }

  @Override
  public Tracer.SpanBuilder ignoreActiveSpan() {
    target.ignoreActiveSpan();
    return this;
  }

  @Override
  public Tracer.SpanBuilder withTag(final String key, final String value) {
    processTag(key, value);
    return this;
  }

  @Override
  public Tracer.SpanBuilder withTag(final String key, final boolean value) {
    processTag(key, value);
    return this;
  }

  @Override
  public Tracer.SpanBuilder withTag(final String key, final Number value) {
    processTag(key, value);
    return this;
  }

  @Override
  public <T>Tracer.SpanBuilder withTag(final Tag<T> tag, final T value) {
    processTag(tag.getKey(), value);
    return this;
  }

  @Override
  public Tracer.SpanBuilder withStartTimestamp(final long microseconds) {
    target.withStartTimestamp(microseconds);
    return this;
  }

  @Override
  @Deprecated
  public Span startManual() {
    return target.startManual();
  }

  @Override
  public Span start() {
    final Span span = target.start();
    if (log != null)
      for (final Map<String,Object> fields : log)
        span.log(fields);

    if (operationName != null)
      span.setOperationName(operationName);

    return newAdaptiveSpan(span);
  }

  AdaptiveSpan newAdaptiveSpan(final Span span) {
    return new AdaptiveSpan(span, rules);
  }

  @Override
  @Deprecated
  public Scope startActive(final boolean finishSpanOnClose) {
    return target.startActive(finishSpanOnClose);
  }

  private String operationName;
  private List<Map<String,Object>> log;

  @Override
  void adaptTag(final String key, final Object value) {
    if (value == null)
      target.withTag(key, (String)null);
    else if (value instanceof Number)
      target.withTag(key, (Number)value);
    else if (value instanceof Boolean)
      target.withTag(key, (Boolean)value);
    else
      target.withTag(key, value.toString());
  }

  @Override
  void adaptLog(final long timestampMicroseconds, final String key, final Object value) {
    if (log == null)
      log = new ArrayList<>();

    log.add(Collections.singletonMap(key, value));
  }

  @Override
  void adaptOperationName(final String name) {
    operationName = name;
  }

  List<Map<String,Object>> getLog() {
    return log;
  }
}

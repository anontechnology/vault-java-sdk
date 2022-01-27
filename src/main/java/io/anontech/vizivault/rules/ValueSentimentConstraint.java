package io.anontech.vizivault.rules;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class ValueSentimentConstraint extends RuleConstraint {

  private double threshold;

  public ValueSentimentConstraint(){
    super("sentiment");
  }

  public ValueSentimentConstraint(double threshold) {
    this();
    this.threshold = threshold;
  }
}

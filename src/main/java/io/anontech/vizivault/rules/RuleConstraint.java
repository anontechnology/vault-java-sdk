package io.anontech.vizivault.rules;

import lombok.Getter;

public abstract class RuleConstraint {
  
  @Getter
  private String type;

  protected RuleConstraint(String type) {
    this.type = type;
  }
}

package io.anontech.vizivault.tagging;

import lombok.Getter;

public abstract class RegulationRule {
  
  @Getter
  private String type;

  protected RegulationRule(String type) {
    this.type = type;
  }
}

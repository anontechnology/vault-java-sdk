package io.anontech.vizivault.rules;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class RegulationAction extends RuleAction {
  private String regulation;

  public RegulationAction() {
    super("regulation");
  }

  public RegulationAction(String regulation) {
    this();
    this.regulation = regulation;
  }
}

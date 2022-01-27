package io.anontech.vizivault.rules;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class LegalHoldAction extends RuleAction {
  public LegalHoldAction() {
    super("legalHold");
  }
}

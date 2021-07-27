package io.anontech.vizivault.rules;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class BlockAction extends RuleAction {
  private String message;
  public BlockAction() {
    super("block");
  }
}

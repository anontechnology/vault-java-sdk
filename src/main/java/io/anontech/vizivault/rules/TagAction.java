package io.anontech.vizivault.rules;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class TagAction extends RuleAction {
  private String tag;

  public TagAction() {
    super("tag");
  }
}

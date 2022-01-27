package io.anontech.vizivault.rules;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class EntityTagAction extends RuleAction {
  private String tag;
  private boolean status;

  public EntityTagAction() {
    super("entityTag");
  }

  public EntityTagAction(String tag, boolean status) {
    this();
    this.tag = tag;
    this.status = status;
  }
}

package io.anontech.vizivault.rules;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class AlertAction extends RuleAction {
  public static enum AlertLevel { INFO, WARNING, DANGER, SUCCESS, NEUTRAL };
  private String message;
  private AlertLevel alertLevel;
  public AlertAction() {
    super("alert");
  }
}

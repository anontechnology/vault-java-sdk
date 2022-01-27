package io.anontech.vizivault.rules;

import java.time.Instant;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RetentionPolicyAction extends RuleAction {
  private Instant expirationDate;
  private Integer daysSinceStore;
  private Integer daysSinceAccess;
  private boolean autoDelete;

  public RetentionPolicyAction() {
    super("retention");
  }
}
package io.anontech.vizivault;

import java.util.Date;

import io.anontech.vizivault.rules.RuleAction;
import io.anontech.vizivault.rules.RuleConstraint;
import lombok.Data;

@Data
public class Rule {
  private RuleConstraint constraint;
  private RuleAction action;

  private String name;
  private int executionOrder;
  
  private Date createdDate;
  private Date modifiedDate;
}

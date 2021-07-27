package io.anontech.vizivault.rules;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ConjunctiveConstraint extends RuleConstraint {
  private List<RuleConstraint> constraints;
  public ConjunctiveConstraint(){
    super("all");
    constraints = new ArrayList<>();
  }

  public void addRule(RuleConstraint rule) {
    constraints.add(rule);
  }
}

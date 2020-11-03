package io.anontech.vizivault.tagging;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DisjunctiveRule extends RegulationRule {
  private List<RegulationRule> constraints;
  public DisjunctiveRule(){
    super("any");
    constraints = new ArrayList<>();
  }

  public void addRule(RegulationRule rule) {
    constraints.add(rule);
  }
}

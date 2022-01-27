package io.anontech.vizivault.rules;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class EntityTypeConstraint extends RuleConstraint {

  private List<String> entityTypes;
  public EntityTypeConstraint(){
    super("entityType");
    entityTypes = new ArrayList<>();
  }

  public EntityTypeConstraint(List<String> entityTypes) {
    super("entityType");
    this.entityTypes = entityTypes;
  }
}

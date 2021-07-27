package io.anontech.vizivault.rules;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class RegulationConstraint extends RuleConstraint {

  public static enum RegulationListOperator {
    @SerializedName("any")
    ANY,
    
    @SerializedName("none")
    NONE,
    
    @SerializedName("all")
    ALL;
  }

  private RegulationListOperator operator;
  private List<String> regulations;
  public RegulationConstraint(){
    super("regulation");
    regulations = new ArrayList<>();
  }
  
  public RegulationConstraint(List<String> regulations, RegulationListOperator operator) {
    super("regulation");
    this.regulations = regulations;
    this.operator = operator;
  }
}

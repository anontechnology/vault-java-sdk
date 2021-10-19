package io.anontech.vizivault.rules;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class TagConstraint extends RuleConstraint {

  public static enum TagListOperator {
    @SerializedName("any")
    ANY,
    
    @SerializedName("none")
    NONE,
    
    @SerializedName("all")
    ALL;
  }

  private TagListOperator operator;
  private List<String> tags;
  public TagConstraint(){
    super("tag");
    tags = new ArrayList<>();
  }
  
  public TagConstraint(List<String> tags, TagListOperator operator) {
    super("tag");
    this.tags = tags;
    this.operator = operator;
  }
}

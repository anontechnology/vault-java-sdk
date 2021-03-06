package io.anontech.vizivault.tagging;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class TagRule extends RegulationRule {

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
  public TagRule(){
    super("tag");
    tags = new ArrayList<>();
  }
  
  public TagRule(List<String> tags, TagListOperator operator) {
    super("tag");
    this.tags = tags;
    this.operator = operator;
  }
}

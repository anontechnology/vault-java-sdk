package io.anontech.vizivault;

import java.util.Date;

import io.anontech.vizivault.tagging.RegulationRule;
import lombok.Data;

@Data
public class Regulation {
  private String key;
  private String name;
  private String url;
  private RegulationRule rule;

  private Date createdDate;
  private Date modifiedDate;
}

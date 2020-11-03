package io.anontech.vizivault;

import java.util.Date;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Tag {
  private String name;

  private Date createdDate;
  private Date modifiedDate;

  public Tag(String name) {
    this.name = name;
  }
}

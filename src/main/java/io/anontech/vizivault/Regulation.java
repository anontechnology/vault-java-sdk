package io.anontech.vizivault;

import java.util.Date;

import lombok.Data;

@Data
public class Regulation {
  private String key;
  private String name;
  private String url;

  private Date createdDate;
  private Date modifiedDate;
}

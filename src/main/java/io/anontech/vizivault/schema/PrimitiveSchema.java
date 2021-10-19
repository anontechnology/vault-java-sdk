package io.anontech.vizivault.schema;

import com.google.gson.annotations.SerializedName;

public enum PrimitiveSchema {
  @SerializedName("string") STRING("string"),
  @SerializedName("integer") INTEGER("int"),
  @SerializedName("boolean") BOOLEAN("boolean"),
  @SerializedName("file") FILE("file"),
  @SerializedName("float") FLOAT("float"),
  @SerializedName("date") DATE("date");

  public final String string;

  private PrimitiveSchema(String string) {
    this.string = string;
  }

  @Override
  public String toString() {
    return string;
  }



}

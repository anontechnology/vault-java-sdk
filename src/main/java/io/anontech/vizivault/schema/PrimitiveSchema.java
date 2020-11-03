package io.anontech.vizivault.schema;

public enum PrimitiveSchema {
  STRING("string"), INTEGER("int"), BOOLEAN("boolean"), FILE("file"), FLOAT("float"), DATE("date");

  public final String string;

  private PrimitiveSchema(String string) {
    this.string = string;
  }

  @Override
  public String toString() {
    return string;
  }
}

package io.anontech.vizivault.schema;

public class InvalidSchemaException extends RuntimeException {

  private static final long serialVersionUID = 1L;
  
  public InvalidSchemaException(String message) {
    super(message);
  }
}

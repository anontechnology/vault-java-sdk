package io.anontech.vizivault;

/**
 * Indicates that the vault server responded with an error message.
 */
public class MissingKeyException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public MissingKeyException(String message) {
    super(message);
  }

}

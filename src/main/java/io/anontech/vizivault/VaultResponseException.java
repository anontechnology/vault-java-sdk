package io.anontech.vizivault;

/**
 * Indicates that the vault server responded with an error message.
 */
public class VaultResponseException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  private final String message;
  private final int statusCode;

  public VaultResponseException(String message, int status) {
    this.message = message;
    this.statusCode = status;
  }

  @Override
  public String getMessage() {
    return String.format("Error %d: %s", statusCode, message);
  }

  public int getStatus() {
    return statusCode;
  }

}

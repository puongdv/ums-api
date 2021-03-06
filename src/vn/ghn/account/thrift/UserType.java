/**
 * Autogenerated by Thrift Compiler (0.9.2)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package vn.ghn.account.thrift;


import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;

public enum UserType implements org.apache.thrift.TEnum {
  DRIVER(1),
  STAFF(2),
  MANAGER(3),
  ADMIN(4);

  private final int value;

  private UserType(int value) {
    this.value = value;
  }

  /**
   * Get the integer value of this enum value, as defined in the Thrift IDL.
   */
  public int getValue() {
    return value;
  }

  /**
   * Find a the enum type by its integer value, as defined in the Thrift IDL.
   * @return null if the value is not found.
   */
  public static UserType findByValue(int value) { 
    switch (value) {
      case 1:
        return DRIVER;
      case 2:
        return STAFF;
      case 3:
        return MANAGER;
      case 4:
        return ADMIN;
      default:
        return null;
    }
  }
}

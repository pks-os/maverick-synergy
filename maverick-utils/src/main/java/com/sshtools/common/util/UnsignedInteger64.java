/*
 *    _           _             _   _
 *   (_) __ _  __| | __ _ _ __ | |_(_)_   _____
 *   | |/ _` |/ _` |/ _` | '_ \| __| \ \ / / _ \
 *   | | (_| | (_| | (_| | |_) | |_| |\ V /  __/
 *  _/ |\__,_|\__,_|\__,_| .__/ \__|_| \_/ \___|
 * |__/                  |_|
 *
 * This file is part of the Maverick Synergy Hotfixes Java SSH API
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 *
 * Copyright (C) 2002-2021 JADAPTIVE Limited - All Rights Reserved
 *
 * Use of this software may also be covered by third-party licenses depending on the choices you make about what features to use.
 *
 * Please visit the link below to see additional third-party licenses and copyrights
 *
 * https://www.jadaptive.com/app/manpage/en/article/1565029/What-third-party-dependencies-does-the-Maverick-Synergy-API-have
 */


package com.sshtools.common.util;

import java.math.BigInteger;

/**
 * This class provides a 64bit unsigned integer.
 *
 * @author Lee David Painter
 */
public class UnsignedInteger64 {

  /**  */
  public final static BigInteger MAX_VALUE = new BigInteger(
      "18446744073709551615");
       
  /**  */
  public final static BigInteger MIN_VALUE = new BigInteger("0");
  private BigInteger bigInt;

  /**
   * Creates a new UnsignedInteger64 object.
   *
   * @param sval
   *
   * @throws NumberFormatException
   */
  public UnsignedInteger64(String sval) throws NumberFormatException {
    bigInt = new BigInteger(sval);

    if ( (bigInt.compareTo(MIN_VALUE) < 0)
        || (bigInt.compareTo(MAX_VALUE) > 0)) {
      throw new NumberFormatException();
    }
  }

  /**
   * Creates a new UnsignedInteger64 object.
   *
   * @param bval
   *
   * @throws NumberFormatException
   */
  public UnsignedInteger64(byte[] bval) throws NumberFormatException {
    bigInt = new BigInteger(bval);

    if ( (bigInt.compareTo(MIN_VALUE) < 0)
        || (bigInt.compareTo(MAX_VALUE) > 0)) {
      throw new NumberFormatException();
    }
  }

  public UnsignedInteger64(long value) {
    bigInt = BigInteger.valueOf(value);
  }

  /**
   * Creates a new UnsignedInteger64 object.
   *
   * @param input
   *
   * @throws NumberFormatException
   */
  public UnsignedInteger64(BigInteger input) {
    bigInt = new BigInteger(input.toString());

    if ( (bigInt.compareTo(MIN_VALUE) < 0)
        || (bigInt.compareTo(MAX_VALUE) > 0)) {
      throw new NumberFormatException();
    }
  }

  /**
   *
   * Compares this unsigned integer to an object.
   *
   * @param o
   *
   * @return
   */
  public boolean equals(Object o) {
    if(o==null){
    	return false;
    }
	try {
      UnsignedInteger64 u = (UnsignedInteger64) o;

      return u.bigInt.equals(this.bigInt);
    }
    catch (ClassCastException ce) {
      // This was not an UnsignedInt64, so equals should fail.
      return false;
    }
  }

  /**
   * Return a BigInteger value of the unsigned integer.
   *
   * @return
   */
  public BigInteger bigIntValue() {
    return bigInt;
  }

  /**
   * Return a long value of the unsigned integer.
   *
   * @return
   */
  public long longValue() {
    return bigInt.longValue();
  }


  /**
   * Return a String representation of the unsigned integer
   *
   * @return
   */
  public String toString() {
    return bigInt.toString(10);
  }

  /**
   * Return the objects hash code.
   *
   * @return
   */
  public int hashCode() {
    return bigInt.hashCode();
  }

  /**
   * Add an unsigned integer to another unsigned integer.
   *
   * @param x
   * @param y
   *
   * @return
   */
  public static UnsignedInteger64 add(UnsignedInteger64 x, UnsignedInteger64 y) {
    return new UnsignedInteger64(x.bigInt.add(y.bigInt));
  }

  /**
   * Add an unsigned integer to an int.
   *
   * @param x
   * @param y
   *
   * @return
   */
  public static UnsignedInteger64 add(UnsignedInteger64 x, int y) {
    return new UnsignedInteger64(x.bigInt.add(BigInteger.valueOf(y)));
  }

  /**
   * Returns a byte array encoded with the unsigned integer.
   * @return
   */
  public byte[] toByteArray() {
    byte[] raw = new byte[8];
    byte[] bi = bigIntValue().toByteArray();
    System.arraycopy(bi, 0, raw, raw.length - bi.length, bi.length);
    return raw;
  }
}

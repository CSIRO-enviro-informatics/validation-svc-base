/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.csiro.wdts.validation.utils;

/**
 *
 * <p>The
 * <a href="http://www.unicode.org/unicode/faq/utf_bom.html">Unicode FAQ</a>
 * defines 5 types of BOMs:<ul>
 * <li><pre>00 00 FE FF  = UTF-32, big-endian</pre></li>
 * <li><pre>FF FE 00 00  = UTF-32, little-endian</pre></li>
 * <li><pre>FE FF        = UTF-16, big-endian</pre></li>
 * <li><pre>FF FE        = UTF-16, little-endian</pre></li>
 * <li><pre>EF BB BF     = UTF-8</pre></li>
 * </ul></p>
 *
 * @author yu021
 */
public final class BOM
  {
    /**
     * NONE.
     */
    public static final BOM NONE = new BOM(new byte[]{},"NONE");

    /**
     * UTF-8 BOM (EF BB BF).
     */
    public static final BOM UTF_8 = new BOM(new byte[]{(byte)0xEF,
                                                       (byte)0xBB,
                                                       (byte)0xBF},
                                            "UTF-8");

    /**
     * UTF-16, little-endian (FF FE).
     */
    public static final BOM UTF_16_LE = new BOM(new byte[]{ (byte)0xFF,
                                                            (byte)0xFE},
                                                "UTF-16 little-endian");

    /**
     * UTF-16, big-endian (FE FF).
     */
    public static final BOM UTF_16_BE = new BOM(new byte[]{ (byte)0xFE,
                                                            (byte)0xFF},
                                                "UTF-16 big-endian");

    /**
     * UTF-32, little-endian (FF FE 00 00).
     */
    public static final BOM UTF_32_LE = new BOM(new byte[]{ (byte)0xFF,
                                                            (byte)0xFE,
                                                            (byte)0x00,
                                                            (byte)0x00},
                                                "UTF-32 little-endian");

    /**
     * UTF-32, big-endian (00 00 FE FF).
     */
    public static final BOM UTF_32_BE = new BOM(new byte[]{ (byte)0x00,
                                                            (byte)0x00,
                                                            (byte)0xFE,
                                                            (byte)0xFF},
                                                "UTF-32 big-endian");

    /**
     * Returns a <code>String</code> representation of this <code>BOM</code>
     * value.
     */
    public final String toString()
    {
      return description;
    }

    /**
     * Returns the bytes corresponding to this <code>BOM</code> value.
     */
    public final byte[] getBytes()
    {
      final int     length = bytes.length;
      final byte[]  result = new byte[length];

      // Make a defensive copy
      System.arraycopy(bytes,0,result,0,length);

      return result;
    }

    private BOM(final byte bom[], final String description)
    {
      assert(bom != null)               : "invalid BOM: null is not allowed";
      assert(description != null)       : "invalid description: null is not allowed";
      assert(description.length() != 0) : "invalid description: empty string is not allowed";

      this.bytes          = bom;
      this.description  = description;
    }

            final byte    bytes[];
    private final String  description;

  } // BOM

package com.example;

/**
 * Describe class <code>Hello</code> here.
 *
 * @author <a href="mailto:zlz.3907@gmail.com">Zhong Lizhi</a>
 * @version 1.0
 */
public class Hello {

  /**
   * The name will be print.
   *
   */
  private String name = "Lion";

  /**
   * Describe <code>getName</code> method here.
   *
   * @return a <code>String</code> value
   */
  public final String getName() {
    return this.name;
  }

  /**
   * main.
   *
   * @param args String[] input args.
   */
  public static void main(final String[] args) {
    String name = new Hello().getName();
    System.out.println("Hello! " + name + " "  + args);
  }
}

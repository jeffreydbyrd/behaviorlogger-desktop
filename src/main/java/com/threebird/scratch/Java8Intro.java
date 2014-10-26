package com.threebird.scratch;

import java.util.function.Supplier;

public class Java8Intro
{

  /**
   * Pretty simple static method. Give me a Runnable, and I'll call run
   */
  public static void callRun( Runnable r )
  {
    r.run();
  }

  /**
   * Think of a Supplier as a function that takes 0 args and generates a value
   */
  public static < T > void callSupplier( Supplier< T > f )
  {
    System.out.println( f.get() );
  }

  /**
   * A lambda is really just syntactic sugar that implements an anonymous class
   */
  public static void anonmyousFunctionsDemo()
  {

    /**
     * Lets start with some bullshit class that prints a message when i say
     * printer.print()
     */
    class Printer
    {
      private String message;

      Printer( String message )
      {
        this.message = message;
      }

      public void print()
      {
        System.out.println( message );
      }
    }

    Printer p = new Printer( "hello world" );

    // passing around existing function
    callRun( p::print );

    // actually that expands into this lambda syntax
    callRun( ( ) -> p.print() );

    // actually THAT expands into anonymous class syntax
    callRun( new Runnable() {
      @Override public void run()
      {
        p.print();
      }
    } );

    // Lambdas will auto-implement any interface with ONE abstract method
    // (eg: Runnable, Callable, Function, Supplier, Consumer)
    callSupplier( ( ) -> "hello supplier" );

    // Same thing as typing:
    callSupplier( new Supplier< String >() {
      @Override public String get()
      {
        return "hello supplier";
      }
    } );

    // I think this a good compromise that makes Java 8 backwards compatible
    // with other versions, but also gives it the power of clean lambda syntax
  }

  public static void main( String[] args )
  {
    anonmyousFunctionsDemo();
  }
}


package ru.dz;

import java.util.concurrent.TimeUnit;


public final class MyLogger {

  public static void elapsedTimeInfo(final String message, long elapsedTime) {
    MyLogger.info("%s It took %d minutes %d seconds %d milliseconds",
            message,
            TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
            TimeUnit.MILLISECONDS.toSeconds(elapsedTime)
                    - TimeUnit.MINUTES.toSeconds(
                    TimeUnit.MILLISECONDS.toMinutes(elapsedTime)),
            elapsedTime - TimeUnit.SECONDS.toMillis(
                    TimeUnit.MILLISECONDS.toSeconds(elapsedTime)));
  }


  public static void info(final String string, final Object... args) {
    if (string == null) {
      return;
    }
    System.out.printf((string) + "%n", args);
  }

  public static void err(final String string, final Object... args) {
    if (string == null) {
      return;
    }
    System.err.printf((string) + "%n", args);
  }

  public static void logException(Throwable throwable) {
    System.err.println("Exception happened!\n" + throwable.toString());
  }

}

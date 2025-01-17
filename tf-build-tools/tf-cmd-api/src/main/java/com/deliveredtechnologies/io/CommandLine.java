package com.deliveredtechnologies.io;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Command line abstraction; executes any command via the command line (cmd.exe or bash based on OS).
 */
public class CommandLine implements Executable {

  private static int DEFAULT_TIMEOUT = 600000;

  private Path directory;
  private Optional<Logger> logger;

  public CommandLine(Path directory) {
    this(directory, null);
  }

  public CommandLine(Path directory, Logger logger) {
    this.logger = Optional.ofNullable(logger);
    this.directory = directory;
  }

  /**
   * Runs the command specified on the command line (cmd.exe or bash based on OS).
   * @param command the command to be run on the command line
   * @param timeout the max amount of time in milliseconds the command is allowed to run before interruption
   * @return        the output from the command line
   * @throws IOException
   * @throws InterruptedException
   */
  @Override
  public String execute(String command, int timeout) throws IOException, InterruptedException {
    boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

    Process process;
    if (isWindows) {
      String[] cmd = new String[] {
        "cmd.exe",
        "/c",
        command
      };
      ProcessBuilder processBuilder = new ProcessBuilder(cmd);
      processBuilder.directory(directory.toFile());
      process = processBuilder.inheritIO().start();
    } else {
      String[] cmd = new String[] {
        "bash",
        "-c",
        command
      };
      logger.ifPresent(log -> log.debug(Arrays.stream(cmd).reduce("", (stringA, stringB) -> stringA + stringB)));
      ProcessBuilder processBuilder = new ProcessBuilder(cmd);
      processBuilder.directory(directory.toFile());
      process = processBuilder.start();
    }

    String output = IOUtils.toString(new InputStreamReader(process.getInputStream()));
    String error = IOUtils.toString(new InputStreamReader(process.getErrorStream()));
    process.waitFor(timeout, TimeUnit.MILLISECONDS);

    if (process.exitValue() > 0) {
      throw new IOException("Exit value was greater than zero!\n" + error);
    }
    return output;
  }

  @Override
  public String execute(String command) throws IOException, InterruptedException {
    return this.execute(command, DEFAULT_TIMEOUT);
  }

  public Path getDirectory() {
    return this.directory;
  }
}

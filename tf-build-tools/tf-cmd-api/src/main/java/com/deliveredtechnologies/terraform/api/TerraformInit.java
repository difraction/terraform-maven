package com.deliveredtechnologies.terraform.api;

import com.deliveredtechnologies.io.Executable;
import com.deliveredtechnologies.terraform.TerraformCommand;
import com.deliveredtechnologies.terraform.TerraformCommandLineDecorator;
import com.deliveredtechnologies.terraform.TerraformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * API for terraform init.
 * <br>
 * See <a href="https://www.terraform.io/docs/commands/init.html">https://www.terraform.io/docs/commands/init.html</a>
 */
public class TerraformInit implements TerraformOperation<String> {

  private Executable terraform;
  private Logger log;

  enum TerraformInitParam {
    pluginDir("plugin-dir"),
    verifyPlugins("verify-plugins"),
    getPlugins("get-plugins"),
    backendConfig("backend-config");

    Optional<String> name = Optional.empty();
    String property;

    TerraformInitParam(String name) {
      this.property = this.toString();
      this.name = Optional.of(name);
    }

    @Override
    public String toString() {
      return name.orElse(super.toString());
    }
  }

  public TerraformInit() throws IOException {
    this(LoggerFactory.getLogger(TerraformInit.class), new TerraformCommandLineDecorator(TerraformCommand.INIT));
  }

  public TerraformInit(String tfRootDir) throws IOException, TerraformException {
    this(LoggerFactory.getLogger(TerraformInit.class), new TerraformCommandLineDecorator(TerraformCommand.INIT, tfRootDir));
  }

  public TerraformInit(Logger log) throws IOException {
    this(log, new TerraformCommandLineDecorator(TerraformCommand.INIT));
  }

  public TerraformInit(Logger log, String tfRootDir) throws IOException, TerraformException {
    this(log, new TerraformCommandLineDecorator(TerraformCommand.INIT, tfRootDir));
  }

  TerraformInit(Executable terraform) {
    this(LoggerFactory.getLogger(TerraformInit.class), terraform);
  }

  TerraformInit(Logger log, Executable terraform) {
    this.log = log;
    this.terraform = terraform;
  }

  /**
   * Executes terraform init. <br>
   * <p>
   *   Valid Properties: <br>
   *   pluginDir - skips plugin installation and loads plugins only from the specified directory <br>
   *   verifyPlugins - skips release signature validation when installing downloaded plugins (not recommended) <br>
   *   getPlugins - skips plugin installation when false <br>
   * </p>
   * @param properties  paramter options and properties for terraform init
   * @return            the output of terraform init
   * @throws TerraformException
   */
  @Override
  public String execute(Properties properties) throws TerraformException {
    try {
      StringBuilder options = new StringBuilder();

      for (TerraformInitParam param : TerraformInitParam.values()) {
        if (properties.containsKey(param.property)) {

          if (param == TerraformInitParam.backendConfig) {
            for (String file : (properties.getProperty(param.property)).split(",")) {
              options.append(String.format("-%1$s=\"%2$s\" ", param, file.trim()));
            }
          }

          switch (param) {
            case pluginDir:
            case getPlugins:
            case verifyPlugins:
              options.append(String.format("-%1$s=%2$s ", param.toString(), properties.getProperty(param.property)));
              break;
            default:
              break;
          }
        }
      }

      options.append("-no-color ");
      return terraform.execute(options.toString());
    } catch (InterruptedException | IOException e) {
      throw new TerraformException(e);
    }
  }
}

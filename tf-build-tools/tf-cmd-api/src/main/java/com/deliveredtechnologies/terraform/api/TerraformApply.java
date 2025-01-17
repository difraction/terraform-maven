package com.deliveredtechnologies.terraform.api;

import com.deliveredtechnologies.io.Executable;
import com.deliveredtechnologies.terraform.TerraformCommand;
import com.deliveredtechnologies.terraform.TerraformCommandLineDecorator;
import com.deliveredtechnologies.terraform.TerraformException;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * API for terraform apply.
 */
public class TerraformApply implements TerraformOperation<String> {
  private Executable terraform;

  enum TerraformApplyParam {
    tfVars("var"),
    varFiles("var_file"),
    lockTimeout("lock-timeout"),
    target("target"),
    plan("plan"),
    noColor("no-color"),
    timeout("timeout");

    Optional<String> name = Optional.empty();
    String property;

    TerraformApplyParam(String name) {
      this.property = this.toString();
      this.name = Optional.of(name);
    }

    @Override
    public String toString() {
      return name.orElse(super.toString());
    }
  }

  TerraformApply(Executable terraform) {
    this.terraform = terraform;
  }

  public TerraformApply() throws IOException {
    this(new TerraformCommandLineDecorator(TerraformCommand.APPLY));
  }

  public TerraformApply(String tfRootDir) throws IOException, TerraformException {
    this(new TerraformCommandLineDecorator(TerraformCommand.APPLY, tfRootDir));
  }

  /**
   * Executes terraform apply.
   * <p>
   *   Valid Properties: <br>
   *   tfVars - a comma delimited list of terraform variables<br>
   *   varFiles - a comma delimited list of terraform vars files<br>
   *   lockTimeout - state file lock timeout<br>
   *   target - resource target<br>
   *   autoApprove - approve without prompt<br>
   *   plan - the plan file to run the apply against<br>
   *   noColor - remove color encoding from output<br>
   *   timeout - how long in milliseconds the terraform apply command can run<br>
   * </p>
   * @param properties  parameter options and properties for terraform apply
   * @return            the output of terraform apply
   * @throws TerraformException
   */
  @Override
  public String execute(Properties properties) throws TerraformException {
    StringBuilder options = new StringBuilder();

    for (TerraformApplyParam param : TerraformApplyParam.values()) {
      if (properties.containsKey(param.property)) {
        if (param == TerraformApplyParam.varFiles) {
          for (String file : (properties.getProperty(param.property)).split(",")) {
            options.append(String.format("-%1$s=%2$s ", param, file.trim()));
          }
          continue;
        }
        if (param == TerraformApplyParam.tfVars) {
          for (String var : ((String)properties.get(param.property)).split(",")) {
            options.append(String.format("-%1$s '%2$s' ", param, var.trim()));
          }
          continue;
        }
        switch (param) {
          case noColor:
            options.append(String.format("-%1$s ", param));
            break;
          case timeout:
          case plan:
            break;
          default:
            options.append(String.format("-%1$s=%2$s ", param, properties.getProperty(param.property)));
        }
      }
    }

    options.append("-auto-approve ");

    try {
      if (properties.containsKey(TerraformApplyParam.plan.property)) {
        options.append(properties.getProperty(TerraformApplyParam.plan.property));
      }

      if (properties.containsKey("timeout")) {
        return terraform.execute(options.toString(), Integer.parseInt(properties.getProperty("timeout")));
      } else {
        return terraform.execute(options.toString());
      }
    } catch (InterruptedException | IOException e) {
      throw new TerraformException(e.getMessage(), e);
    }
  }
}


package com.deliveredtechnologies.maven.terraform.mojo;

import com.deliveredtechnologies.terraform.TerraformException;
import com.deliveredtechnologies.terraform.api.TerraformPlan;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;

/**
 * Mojo terraform plan goal.
 * <br>
 * Runs 'terraform plan'
 */
@Mojo(name = "plan")
public class Plan extends TerraformMojo<String> {
  @Parameter(property = "tfRootDir")
  String tfRootDir;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    try {
      execute(new TerraformPlan(tfRootDir), System.getProperties());
    } catch (IOException | TerraformException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }
}

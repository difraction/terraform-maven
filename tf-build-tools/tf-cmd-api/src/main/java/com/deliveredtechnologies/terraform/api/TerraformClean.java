package com.deliveredtechnologies.terraform.api;

import com.deliveredtechnologies.terraform.TerraformException;
import com.deliveredtechnologies.terraform.TerraformUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * API for cleaning up 'terraform' files.
 */
public class TerraformClean implements TerraformOperation<String> {

  private Path tfRootModulePath;
  private Path tfModulesPath;

  /**
   * Constructor instantiates TerraformClean.
   * @param tfModules     the tfModules directory; defaults to src/main/.tfmodules if empty
   * @param tfRootModule  the Terraform root module directory; defaults to src/main/terraform/{first dir} if empty
   * @throws IOException
   */
  public TerraformClean(String tfModules, String tfRootModule) throws IOException, TerraformException {
    this.tfModulesPath = StringUtils.isEmpty(tfModules)
      ? TerraformUtils.getDefaultTfModulesDir()
      : Paths.get(tfModules);
    this.tfRootModulePath = (StringUtils.isEmpty(tfRootModule)
      ? TerraformUtils.getDefaultTerraformRootModuleDir()
      : TerraformUtils.getTerraformRootModuleDir(tfRootModule)).getParent();
  }

  @Override
  public String execute(Properties properties) throws TerraformException {
    try {
      if (!this.tfRootModulePath.toFile().exists()) {
        throw new IOException(this.tfRootModulePath.toString() + " does not exist!");
      }

      //delete .tfmodules
      StringBuilder response = new StringBuilder("Deleting...\n");
      if (this.tfModulesPath.toFile().exists()) {
        response.append(this.tfModulesPath.toString()).append('\n');
        FileUtils.forceDelete(this.tfModulesPath.toFile());
      }

      //delete terraform files in terraform root module
      List<Path> terraformFiles = Files.walk(this.tfRootModulePath.getParent())
          .filter(path -> path.getFileName().toString().contains(".terraform") || path.getFileName().toString().contains(".tfstate"))
          .collect(Collectors.toList());
      for (Path file : terraformFiles) {
        if (file.toFile().exists()) {
          response.append(file.toString()).append('\n');
          FileUtils.forceDelete(file.toFile());
        }
      }
      return response.toString();
    } catch (IOException e) {
      throw new TerraformException(e);
    }
  }
}

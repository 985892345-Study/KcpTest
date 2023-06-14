package com.ndhzs.plugin.kotlin.ir.findpackage

import com.ndhzs.plugin.kotlin.LogUtils
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

/**
 * .
 *
 * @author 985892345
 * 2023/6/13 18:54
 */
class FindPackageExtension(
  override val messageCollector: MessageCollector
) : IrGenerationExtension, LogUtils {
  override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
    pluginContext.moduleDescriptor
      .getPackage(FqName("com.a"))
      .memberScope
      .getContributedDescriptors()
      .filterIsInstance<ClassDescriptor>()
      .forEach {
        log(it.fqNameSafe)
      }
  }
}
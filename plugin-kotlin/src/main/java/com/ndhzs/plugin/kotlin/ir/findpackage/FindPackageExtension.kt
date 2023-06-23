package com.ndhzs.plugin.kotlin.ir.findpackage

import com.ndhzs.plugin.kotlin.LogUtils
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.components.hasDefaultValue
import org.jetbrains.kotlin.resolve.constants.KClassValue
import org.jetbrains.kotlin.resolve.constants.KClassValue.Value.NormalClass

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
    val testAnnotation = FqName("TestAnnotation")
    pluginContext.moduleDescriptor
      .getPackage(FqName("com.a"))
      .memberScope
      .getContributedDescriptors {
        true
      }
      .filter {
        log("${it.name}   qualifiedName = " + it::class.qualifiedName)
        it is ClassDescriptor || it is PackageViewDescriptor
      }
      .forEach {
        it.accept(
          object : DeclarationDescriptorVisitorImpl() {
            override fun visitClassDescriptor(descriptor: ClassDescriptor, data: Unit) {
              val annotation = descriptor.annotations.findAnnotation(testAnnotation)
              if (annotation != null) {
                val hasEmptyConstructor = descriptor.constructors.any { it.valueParameters.isEmpty() }
                log("hasEmptyConstructor = $hasEmptyConstructor")
                val kClass = annotation.allValueArguments[Name.identifier("clazz")]?.value as NormalClass?
                val name = annotation.allValueArguments[Name.identifier("name")]?.value as String?
                log("name = $name")
                val key = getKey(kClass?.classId, name ?: "")
                log("key = $key")
              }
            }
          },
          Unit
        )
      }
  }
  
  private fun getKey(classId: ClassId?, name: String): String {
    if (classId == null && name.isEmpty()) throw IllegalArgumentException("must set clazz or name!")
    return if (classId == null) name else {
      if (name.isEmpty()) classId.asFqNameString() else classId.asFqNameString() + "-" + name
    }
  }
}
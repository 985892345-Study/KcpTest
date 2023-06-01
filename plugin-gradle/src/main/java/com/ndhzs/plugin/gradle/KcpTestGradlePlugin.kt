package com.ndhzs.plugin.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption

/**
 * .
 *
 * @author 985892345
 * 2023/5/26 12:10
 */
class KcpTestGradlePlugin : KotlinCompilerPluginSupportPlugin {
  
  override fun apply(target: Project) {
  }
  
  override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
    val project = kotlinCompilation.target.project
    return project.provider { emptyList() }
  }
  
  override fun getCompilerPluginId(): String {
    return "kcpTest"
  }
  
  override fun getPluginArtifact(): SubpluginArtifact {
    return SubpluginArtifact(
      groupId = "com.g985892345.kcptest",
      artifactId = "KcpTest",
      version = "0.0.10",
    )
  }
  
  override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
    return true
  }
}
package com.ndhzs.kcptest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ndhzs.kcptest.service.ServiceProviderImpl

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    android.util.Log.d("ggg", "(${Exception().stackTrace[0].run { "$fileName:$lineNumber" }}) -> " +
      "annotations = ${ServiceProviderImpl::class.java.annotations.contentToString()}")
  }
}
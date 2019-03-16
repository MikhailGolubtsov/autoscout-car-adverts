package com.github.mikhailgolubtsov.autoscout.caradverts

import java.time.Clock

import com.google.inject.AbstractModule

/**
  * This class is a Guice module that tells Guice how to bind several
  * different types. This Guice module is created when the Play
  * application starts.
  */
class ApplicationModule extends AbstractModule {

  override def configure() = {
    // Use the system clock as the default implementation of Clock
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
  }

}

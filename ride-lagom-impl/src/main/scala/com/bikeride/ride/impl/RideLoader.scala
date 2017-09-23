package com.bikeride.ride.impl

import com.bikeride.ride.api.RideService
import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.softwaremill.macwire._
import org.slf4j.{Logger, LoggerFactory}

class RideLoader extends LagomApplicationLoader{

  override def load(context: LagomApplicationContext): LagomApplication =
    new RideApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new RideApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[RideService])
}

abstract class RideApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  val log: Logger = LoggerFactory.getLogger(getClass)
  log.debug("Loading...")

  override lazy val lagomServer = serverFor[RideService](wire[RideServiceImpl])
  override lazy val jsonSerializerRegistry = RideSerializerRegistry

  lazy val trackService = serviceClient.implement[RideService]

  persistentEntityRegistry.register(wire[RideEntity])
  //readSide.register(wire[RideEventProcessor])
}
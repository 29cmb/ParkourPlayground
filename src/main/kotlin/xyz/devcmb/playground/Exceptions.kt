package xyz.devcmb.playground

class WorldSetupException(override val message: String = "An error occurred trying to setup the world") : Exception()
class ObstacleStepException(override val message: String = "An error occurred trying to step the obstacle loop") : Exception()
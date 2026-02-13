package xyz.devcmb.playground.annotations

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Configurable(val path: String)

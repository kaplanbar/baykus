package com.baykus

object Main {
  def main(args: Array[String]): Unit = {
    val modules = new ApplicationModule {}
    modules.application.start()
  }
}

package org.bblf.client

import gopkg.in.bblfsh.sdk.v1.protocol.generated.SupportedLanguagesResponse
import org.bblfsh.client.BblfshClient
import org.scalatest.{BeforeAndAfter, FunSuite}

class BblfshClientSupportedLanguagesTest extends FunSuite with BeforeAndAfter {
  val client = BblfshClient("0.0.0.0", 9432)
  var resp: SupportedLanguagesResponse = _

  before {
    resp = client.supportedLanguages()
  }

  test("Check languages") {
    assert(!resp.languages.isEmpty)
  }
}

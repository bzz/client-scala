package org.bblfsh.client.v2

import java.nio.ByteBuffer

import gopkg.in.bblfsh.sdk.v2.protocol.driver.Mode

import scala.io.Source

class BblfshClientParseTest extends BblfshClientBaseTest {

  import BblfshClient._ // enables uast.* methods

  "Parsed UAST for .java file" should "not be empty" in {
    assert(resp != null)
    assert(resp.errors.isEmpty)
  }

  "Filtering UAST" should "work in Native mode" in {
    val fileContent = Source.fromFile(fileName).getLines.mkString("\n")
    val resp = client.parse(fileName, fileContent, Mode.NATIVE)
    val node = resp.get

    val iter = BblfshClient.filter(node, "//SimpleName")
    iter.toList should have size (10) // number of Identifiers in the file
    iter.close()
  }

// TODO(#110) implement value type returns
//  "Filtering UAST" should "work for Value types" in {
//    val iter = BblfshClient.filterNumber(resp.get, "count(//*)")
//    iter.toList should have size (517) // total number of nodes (not the number of results which is 1)
//  }

  "Filtering UAST" should "work in Annotated mode" in {
    val fileContent = Source.fromFile(fileName).getLines.mkString("\n")
    val resp = client.parse(fileName, fileContent, Mode.ANNOTATED)
    val node = resp.get

    val iter = BblfshClient.filter(node, "//SimpleName[@role='Call']")
    iter.toList should have size (1) // number of function called in the file
    iter.close()
  }

  "Filtering UAST" should "work in Semantic mode" in {
    val fileContent = Source.fromFile(fileName).getLines.mkString("\n")
    val resp = client.parse(fileName, fileContent, Mode.SEMANTIC)
    val node = resp.get

    val iter = BblfshClient.filter(node, "//uast:Identifier[@role='Call']")
    iter.toList should have size (1) // number of function called in the file
    iter.close()
  }

  "Decoded UAST after parsing" should "not be NULL" in {
    val uast = resp.uast.decode()

    assert(uast != null)
    assert(uast.nativeContext != 0)

    uast.dispose()
  }

  "Decoded UAST RootNode" should "not be NULL" in {
    val uast = resp.uast.decode()
    val rootNode: NodeExt = uast.root()

    rootNode should not be null
    rootNode.ctx should not be (0)
    rootNode.handle should not be (0)

    uast.dispose()
  }

  "Encoding UAST to the same ContextExt" should "produce the same bytes" in {
    val uastCtx: ContextExt = resp.uast.decode()
    val rootNode: NodeExt = uastCtx.root()

    val encodedBytes: ByteBuffer = uastCtx.encode(rootNode)

    encodedBytes.capacity should be(resp.uast.asReadOnlyByteBuffer.capacity)
    encodedBytes shouldEqual resp.uast.asReadOnlyByteBuffer
  }

  "Encoding java UAST to a new Context" should "produce the same bytes" in {
    val node = resp.get

    val encodedBytes = node.toByteBuffer

    val nodeEncodedDecoded = JNode.parseFrom(encodedBytes)
    nodeEncodedDecoded shouldEqual node

    encodedBytes.capacity should be(resp.uast.asReadOnlyByteBuffer.capacity)
    encodedBytes shouldEqual resp.uast.asReadOnlyByteBuffer
  }


  "Encoding python UAST to a new Context" should "produce the same bytes" in {
    val fileName = "src/test/resources/python_file.py"
    val fileContent = Source.fromFile(fileName).getLines.mkString("\n")
    val resp = client.parse(fileName, fileContent)
    val node = resp.get

    // when
    val encodedBytes = node.toByteBuffer

    val nodeEncodedDecoded = JNode.parseFrom(encodedBytes)
    nodeEncodedDecoded shouldEqual node

    encodedBytes.capacity should be(resp.uast.asReadOnlyByteBuffer.capacity)
    encodedBytes shouldEqual resp.uast.asReadOnlyByteBuffer
  }


}

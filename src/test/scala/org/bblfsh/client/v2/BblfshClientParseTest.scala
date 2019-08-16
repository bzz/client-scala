package org.bblfsh.client.v2

import java.nio.ByteBuffer

import scala.io.Source

class BblfshClientParseTest extends BblfshClientBaseTest {

  import BblfshClient._ // enables uast.* methods

  "Parsed UAST for .java file" should "not be empty" in {
    assert(resp != null)
    assert(resp.errors.isEmpty)
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

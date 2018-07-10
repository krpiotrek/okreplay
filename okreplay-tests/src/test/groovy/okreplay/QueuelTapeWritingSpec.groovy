package okreplay

import com.google.common.io.Files
import okhttp3.MediaType
import okhttp3.ResponseBody
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import static java.net.HttpURLConnection.HTTP_OK
import static okreplay.TapeMode.WRITE_QUEUE

class QueuelTapeWritingSpec extends Specification {
  @Shared @AutoCleanup("deleteDir") def tapeRoot = Files.createTempDir()
  @Shared def tapeLoader = new YamlTapeLoader(tapeRoot)
  MemoryTape tape

  void setup() {
    def fileName = "queue_tape.yaml"
    Files.copy(new File(getClass().classLoader.getResource("okreplay/tapes/" + fileName).getFile()), new File(tapeRoot.getAbsolutePath() + "/" + fileName))
    tape = tapeLoader.loadTape("queue tape")
    tape.mode = WRITE_QUEUE
    tape.start()
  }

  void "write queue tapes record multiple matching responses"() {
    when: "multiple responses are captured from the same endpoint"
    (1..n).each {
      def response = new RecordedResponse.Builder()
          .code(HTTP_OK)
          .body(ResponseBody.create(MediaType.parse("text/plain"), "count: $it".bytes))
          .build()
      tape.record(request, response)
    }

    then: "multiple recordings are added to the tape"
    tape.size() == old(tape.size()) + n

    and: "each has different content"
    with(tape.interactions) {
      response.collect { it.body() } == (1..n).collect {
        "count: $it"
      }
    }

    where:
    n = 2
    request = new RecordedRequest.Builder()
        .url("http://freeside.co/betamax")
        .build()
  }
}

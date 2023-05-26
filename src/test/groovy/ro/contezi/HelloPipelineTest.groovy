package ro.contezi

import spock.lang.Specification

class HelloPipelineTest extends Specification {
    def "HelloPipeline says hello"() {
        given:
            def context = Mock(PipelineContext.class)
            def helloPipeline = new HelloPipeline(context)
        when:
            helloPipeline.sayHello()
        then:
            1 * context.sh ({ it.contains('Hello') })
    }
}

package org.wso2.extensions.siddhi.io.file;

import org.apache.log4j.Logger;
import org.wso2.carbon.messaging.exceptions.ServerConnectorException;
import org.wso2.carbon.transport.file.connector.server.FileServerConnector;
import org.wso2.carbon.transport.file.connector.server.util.Constants;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.OptionHolder;


import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by minudika on 18/5/17.
 */
@Extension(
        name = "file2",
        namespace = "source",
        description = "File Source",
        parameters = {
                @Parameter(name = "enclosing.element",
                        description =
                                "Used to specify the enclosing element in case of sending multiple events in same "
                                        + "JSON message. WSO2 DAS will treat the child element of given enclosing "
                                        + "element as events"
                                        + " and execute json path expressions on child elements. If enclosing.element "
                                        + "is not provided "
                                        + "multiple event scenario is disregarded and json path will be evaluated "
                                        + "with respect to "
                                        + "root element.",
                        type = {DataType.STRING}),
                @Parameter(name = "fail.on.missing.attribute",
                        description = "This can either have value true or false. By default it will be true. This "
                                + "attribute allows user to handle unknown attributes. By default if an json "
                                + "execution "
                                + "fails or returns null DAS will drop that message. However setting this property"
                                + " to "
                                + "false will prompt DAS to send and event with null value to Siddhi where user "
                                + "can handle"
                                + " it accordingly(ie. Assign a default value)",
                        type = {DataType.BOOL})
        },
        examples = {
                @Example(
                        syntax = "@source(type='inMemory', topic='stock', @map(type='json'))\n"
                                + "define stream FooStream (symbol string, price float, volume long);\n",
                        description =  "Above configuration will do a default JSON input mapping. Expected "
                                + "input will look like below."
                                + "{\n"
                                + "    \"event\":{\n"
                                + "        \"symbol\":\"WSO2\",\n"
                                + "        \"price\":55.6,\n"
                                + "        \"volume\":100\n"
                                + "    }\n"
                                + "}\n"),
                @Example(
                        syntax = "@source(type='inMemory', topic='stock', @map(type='json', "
                                + "enclosing.element=\"$.portfolio\", "
                                + "@attributes(symbol = \"company.symbol\", price = \"price\", volume = \"volume\")))",
                        description =  "Above configuration will perform a custom JSON mapping. Expected input will "
                                + "look like below."
                                + "{"
                                + " \"portfolio\":{\n"
                                + "     \"stock\":{"
                                + "        \"volume\":100,\n"
                                + "        \"company\":{\n"
                                + "           \"symbol\":\"WSO2\"\n"
                                + "       },\n"
                                + "        \"price\":55.6\n"
                                + "    }\n"
                                + "}")
        }
)
public class FileSource extends Source{
    private static final Logger log = Logger.getLogger(FileSource.class);

    private SourceEventListener sourceEventListener;
    private static String URI_IDENTIFIER = "uri";
    private String fileURI = null;
    private FileServerConnector fileServerConnector = null;
    private FileMessageProcessor fileMessageProcessor = null;

    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder, ConfigReader configReader, ExecutionPlanContext executionPlanContext) {
        this.sourceEventListener = sourceEventListener;
        fileURI = optionHolder.validateAndGetStaticValue(URI_IDENTIFIER,null);
    }

    public void connect() throws ConnectionUnavailableException {
        Map<String, String> parameters = new HashMap<String,String>();
        parameters.put(Constants.TRANSPORT_FILE_FILE_URI, fileURI);
        parameters.put(org.wso2.carbon.connector.framework.server.polling.Constants.POLLING_INTERVAL, "1000");
        fileServerConnector = new FileServerConnector("siddhi.io.file",parameters);
       // fileMessageProcessor = new FileMessageProcessor();
        fileServerConnector.setMessageProcessor(fileMessageProcessor);
        try{
            fileServerConnector.start();
            fileMessageProcessor.waitTillDone();
        } catch (ServerConnectorException e) {
            log.error("Exception in starting the JMS receiver for stream: "
                    + sourceEventListener.getStreamDefinition().getId(), e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fileMessageProcessor.getFileContent();
    }

    public void disconnect() {

    }

    public void destroy() {

    }

    public void pause() {

    }

    public void resume() {

    }

    public Map<String, Object> currentState() {
        return null;
    }

    public void restoreState(Map<String, Object> map) {

    }
}

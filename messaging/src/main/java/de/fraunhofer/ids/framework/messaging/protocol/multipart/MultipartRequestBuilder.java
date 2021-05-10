package de.fraunhofer.ids.framework.messaging.protocol.multipart;

import java.io.IOException;
import java.net.URI;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.ids.framework.messaging.protocol.RequestBuilder;
import de.fraunhofer.ids.framework.util.MultipartDatapart;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class MultipartRequestBuilder implements RequestBuilder {

    private static final Serializer serializer = new Serializer();



    /**
     * {@inheritDoc}
     */
    @Override
    public Request build( Message message, URI target ) throws IOException {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(MultipartDatapart.HEADER.toString(), serializer.serialize(message))
                .build();
        return new Request.Builder()
                .url(target.toURL())
                .post(body)
                .build();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Request build( Message message, URI target, String payload ) throws IOException {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(MultipartDatapart.HEADER.toString(), serializer.serialize(message))
                .addFormDataPart(MultipartDatapart.PAYLOAD.toString(), payload)
                .build();
        return new Request.Builder()
                .url(target.toURL())
                .post(body)
                .build();
    }

}

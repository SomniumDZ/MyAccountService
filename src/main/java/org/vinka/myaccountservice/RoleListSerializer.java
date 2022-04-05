package org.vinka.myaccountservice;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.vinka.myaccountservice.business.RoleEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RoleListSerializer extends StdSerializer<List<RoleEntity>> {

    public RoleListSerializer() {
        this(null);
    }

    protected RoleListSerializer(Class<List<RoleEntity>> t) {
        super(t);
    }

    @Override
    public void serialize(List<RoleEntity> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartArray();
        var list = new ArrayList<>(value);
        Collections.reverse(list);
        for (RoleEntity roleEntity : list) {
            gen.writeString(roleEntity.getName());
        }
        gen.writeEndArray();
    }
}

/*
 * Copyright 1999-2101 Alibaba Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author wenshao<szujobs@hotmail.com>
 */
public class DoubleArraySerializer implements ObjectSerializer {

    public static final DoubleArraySerializer instance = new DoubleArraySerializer();

    public DoubleArraySerializer(){
    }

    public final void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType) throws IOException {
        SerializeWriter out = serializer.getWriter();
        
        if (object == null) {
            if (out.isEnabled(SerializerFeature.WriteNullListAsEmpty)) {
                out.write("[]");
            } else {
                out.writeNull();
            }
            return;
        }

        double[] array = (double[]) object;
        int size = array.length;

        int end = size - 1;

        if (end == -1) {
            out.append("[]");
            return;
        }

        out.append('[');
        for (int i = 0; i < end; ++i) {
            double item = array[i];

            if (Double.isNaN(item)) {
                out.writeNull();
            } else {
                out.append(Double.toString(item));
            }

            out.append(',');
        }

        double item = array[end];

        if (Double.isNaN(item)) {
            out.writeNull();
        } else {
            out.append(Double.toString(item));
        }

        out.append(']');
    }
}

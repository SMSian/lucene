/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.lucene.util.fst;

import java.io.IOException;
import org.apache.lucene.store.IndexInput;

/** Implements reverse read from an index input. */
final class ReverseIndexInputReader extends FST.BytesReader {
    private final IndexInput in;
    private final long startFP;

    public ReverseIndexInputReader(IndexInput in, long startFP) {
        this.in = in;
        this.startFP = startFP;
    }

    @Override
    public byte readByte() throws IOException {
        final byte b = this.in.readByte();
        this.skipBytes(2);
        return b;
    }

    @Override
    public void readBytes(byte[] b, int offset, int len) throws IOException {
        this.skipBytes(len - 1);
        this.in.readBytes(b, offset, len);
        int i = offset, j = offset + len - 1;
        byte tmp;
        while (j > i) {
            tmp = b[j];
            b[j--] = b[i];
            b[i++] = tmp;
        }
        this.skipBytes(len + 1);
    }

    @Override
    public void skipBytes(long count) {
        this.setPosition(this.getPosition() - count);
    }

    @Override
    public long getPosition() {
        final long position = this.in.getFilePointer() - startFP;
        return position;
    }

    @Override
    public void setPosition(long pos) {
        try {
            this.in.seek(startFP + pos);
        } catch (IOException ex) {
            System.out.println(String.format("Unreported exception in set position at %d - %s", pos, ex.getMessage()));
        }
    }

    @Override
    public boolean reversed() {
        return true;
    }
}


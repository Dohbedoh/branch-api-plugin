/*
 * The MIT License
 *
 * Copyright (c) 2016, CloudBees, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jenkins.branch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FileUtils;

/**
 * Copy of hudson.util.io.RewindableFileOutputStream that supports an initial append mode.
 */
// TODO replace with hudson.util.io.RewindableFileOutputStream once baseline core has version supporting initial append
class RewindableFileOutputStream extends OutputStream {
    protected final File out;
    private boolean closed;
    private boolean initialAppend;

    private OutputStream current;

    public RewindableFileOutputStream(File out) {
        this(out, false);
    }

    public RewindableFileOutputStream(File out, boolean initialAppend) {
        this.out = out;
        this.initialAppend = initialAppend;
    }

    private synchronized OutputStream current() throws IOException {
        if (current == null) {
            if (!closed) {
                FileUtils.forceMkdir(out.getParentFile());
                try {
                    current = new FileOutputStream(out, initialAppend);
                    initialAppend = false;
                } catch (FileNotFoundException e) {
                    throw new IOException("Failed to open " + out, e);
                }
            } else {
                throw new IOException(out.getName() + " stream is closed");
            }
        }
        return current;
    }

    @Override
    public void write(int b) throws IOException {
        current().write(b);
    }

    @Override
    public void write(@NonNull byte[] b) throws IOException {
        current().write(b);
    }

    @Override
    public void write(@NonNull byte[] b, int off, int len) throws IOException {
        current().write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        current().flush();
    }

    @Override
    public synchronized void close() throws IOException {
        closeCurrent();
        closed = true;
    }

    /**
     * In addition to close, ensure that the next "open" would truncate the file.
     */
    public synchronized void rewind() throws IOException {
        closeCurrent();
    }

    private void closeCurrent() throws IOException {
        if (current != null) {
            current.close();
            current = null;
        }
    }
}

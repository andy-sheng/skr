//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.cache.disc.impl.ext;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DiskLruCache implements Closeable {
  static final String JOURNAL_FILE = "journal";
  static final String JOURNAL_FILE_TEMP = "journal.tmp";
  static final String JOURNAL_FILE_BACKUP = "journal.bkp";
  static final String MAGIC = "libcore.io.DiskLruCache";
  static final String VERSION_1 = "1";
  static final long ANY_SEQUENCE_NUMBER = -1L;
  static final Pattern LEGAL_KEY_PATTERN = Pattern.compile("[a-z0-9_-]{1,64}");
  private static final String CLEAN = "CLEAN";
  private static final String DIRTY = "DIRTY";
  private static final String REMOVE = "REMOVE";
  private static final String READ = "READ";
  private final File directory;
  private final File journalFile;
  private final File journalFileTmp;
  private final File journalFileBackup;
  private final int appVersion;
  private long maxSize;
  private int maxFileCount;
  private final int valueCount;
  private long size = 0L;
  private int fileCount = 0;
  private Writer journalWriter;
  private final LinkedHashMap<String, io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry> lruEntries = new LinkedHashMap(0, 0.75F, true);
  private int redundantOpCount;
  private long nextSequenceNumber = 0L;
  final ThreadPoolExecutor executorService;
  private final Callable<Void> cleanupCallable;
  private static final OutputStream NULL_OUTPUT_STREAM = new OutputStream() {
    public void write(int b) throws IOException {
    }
  };

  private DiskLruCache(File directory, int appVersion, int valueCount, long maxSize, int maxFileCount) {
    this.executorService = new ThreadPoolExecutor(0, 1, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue());
    this.cleanupCallable = new Callable<Void>() {
      public Void call() throws Exception {
        io.rong.imageloader.cache.disc.impl.ext.DiskLruCache var1 = io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this;
        synchronized(io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this) {
          if (io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.journalWriter == null) {
            return null;
          } else {
            io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.trimToSize();
            io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.trimToFileCount();
            if (io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.journalRebuildRequired()) {
              io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.rebuildJournal();
              io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.redundantOpCount = 0;
            }

            return null;
          }
        }
      }
    };
    this.directory = directory;
    this.appVersion = appVersion;
    this.journalFile = new File(directory, "journal");
    this.journalFileTmp = new File(directory, "journal.tmp");
    this.journalFileBackup = new File(directory, "journal.bkp");
    this.valueCount = valueCount;
    this.maxSize = maxSize;
    this.maxFileCount = maxFileCount;
  }

  public static io.rong.imageloader.cache.disc.impl.ext.DiskLruCache open(File directory, int appVersion, int valueCount, long maxSize, int maxFileCount) throws IOException {
    if (maxSize <= 0L) {
      throw new IllegalArgumentException("maxSize <= 0");
    } else if (maxFileCount <= 0) {
      throw new IllegalArgumentException("maxFileCount <= 0");
    } else if (valueCount <= 0) {
      throw new IllegalArgumentException("valueCount <= 0");
    } else {
      File backupFile = new File(directory, "journal.bkp");
      if (backupFile.exists()) {
        File journalFile = new File(directory, "journal");
        if (journalFile.exists()) {
          backupFile.delete();
        } else {
          renameTo(backupFile, journalFile, false);
        }
      }

      io.rong.imageloader.cache.disc.impl.ext.DiskLruCache cache = new io.rong.imageloader.cache.disc.impl.ext.DiskLruCache(directory, appVersion, valueCount, maxSize, maxFileCount);
      if (cache.journalFile.exists()) {
        try {
          cache.readJournal();
          cache.processJournal();
          cache.journalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(cache.journalFile, true), Util.US_ASCII));
          return cache;
        } catch (IOException var9) {
          System.out.println("DiskLruCache " + directory + " is corrupt: " + var9.getMessage() + ", removing");
          cache.delete();
        }
      }

      directory.mkdirs();
      cache = new io.rong.imageloader.cache.disc.impl.ext.DiskLruCache(directory, appVersion, valueCount, maxSize, maxFileCount);
      cache.rebuildJournal();
      return cache;
    }
  }

  private void readJournal() throws IOException {
    io.rong.imageloader.cache.disc.impl.ext.StrictLineReader reader = new io.rong.imageloader.cache.disc.impl.ext.StrictLineReader(new FileInputStream(this.journalFile), Util.US_ASCII);

    try {
      String magic = reader.readLine();
      String version = reader.readLine();
      String appVersionString = reader.readLine();
      String valueCountString = reader.readLine();
      String blank = reader.readLine();
      if ("libcore.io.DiskLruCache".equals(magic) && "1".equals(version) && Integer.toString(this.appVersion).equals(appVersionString) && Integer.toString(this.valueCount).equals(valueCountString) && "".equals(blank)) {
        int lineCount = 0;

        while(true) {
          try {
            this.readJournalLine(reader.readLine());
            ++lineCount;
          } catch (EOFException var12) {
            this.redundantOpCount = lineCount - this.lruEntries.size();
            return;
          }
        }
      } else {
        throw new IOException("unexpected journal header: [" + magic + ", " + version + ", " + valueCountString + ", " + blank + "]");
      }
    } finally {
      Util.closeQuietly(reader);
    }
  }

  private void readJournalLine(String line) throws IOException {
    int firstSpace = line.indexOf(32);
    if (firstSpace == -1) {
      throw new IOException("unexpected journal line: " + line);
    } else {
      int keyBegin = firstSpace + 1;
      int secondSpace = line.indexOf(32, keyBegin);
      String key;
      if (secondSpace == -1) {
        key = line.substring(keyBegin);
        if (firstSpace == "REMOVE".length() && line.startsWith("REMOVE")) {
          this.lruEntries.remove(key);
          return;
        }
      } else {
        key = line.substring(keyBegin, secondSpace);
      }

      io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry entry = (io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry)this.lruEntries.get(key);
      if (entry == null) {
        entry = new io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry(key);
        this.lruEntries.put(key, entry);
      }

      if (secondSpace != -1 && firstSpace == "CLEAN".length() && line.startsWith("CLEAN")) {
        String[] parts = line.substring(secondSpace + 1).split(" ");
        entry.readable = true;
        entry.currentEditor = null;
        entry.setLengths(parts);
      } else if (secondSpace == -1 && firstSpace == "DIRTY".length() && line.startsWith("DIRTY")) {
        entry.currentEditor = new io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Editor(entry);
      } else if (secondSpace != -1 || firstSpace != "READ".length() || !line.startsWith("READ")) {
        throw new IOException("unexpected journal line: " + line);
      }

    }
  }

  private void processJournal() throws IOException {
    deleteIfExists(this.journalFileTmp);
    Iterator i = this.lruEntries.values().iterator();

    while(true) {
      while(i.hasNext()) {
        io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry entry = (io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry)i.next();
        int t;
        if (entry.currentEditor == null) {
          for(t = 0; t < this.valueCount; ++t) {
            this.size += entry.lengths[t];
            ++this.fileCount;
          }
        } else {
          entry.currentEditor = null;

          for(t = 0; t < this.valueCount; ++t) {
            deleteIfExists(entry.getCleanFile(t));
            deleteIfExists(entry.getDirtyFile(t));
          }

          i.remove();
        }
      }

      return;
    }
  }

  private synchronized void rebuildJournal() throws IOException {
    if (this.journalWriter != null) {
      this.journalWriter.close();
    }

    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.journalFileTmp), Util.US_ASCII));

    try {
      writer.write("libcore.io.DiskLruCache");
      writer.write("\n");
      writer.write("1");
      writer.write("\n");
      writer.write(Integer.toString(this.appVersion));
      writer.write("\n");
      writer.write(Integer.toString(this.valueCount));
      writer.write("\n");
      writer.write("\n");
      Iterator var2 = this.lruEntries.values().iterator();

      while(var2.hasNext()) {
        io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry entry = (io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry)var2.next();
        if (entry.currentEditor != null) {
          writer.write("DIRTY " + entry.key + '\n');
        } else {
          writer.write("CLEAN " + entry.key + entry.getLengths() + '\n');
        }
      }
    } finally {
      writer.close();
    }

    if (this.journalFile.exists()) {
      renameTo(this.journalFile, this.journalFileBackup, true);
    }

    renameTo(this.journalFileTmp, this.journalFile, false);
    this.journalFileBackup.delete();
    this.journalWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.journalFile, true), Util.US_ASCII));
  }

  private static void deleteIfExists(File file) throws IOException {
    if (file.exists() && !file.delete()) {
      throw new IOException();
    }
  }

  private static void renameTo(File from, File to, boolean deleteDestination) throws IOException {
    if (deleteDestination) {
      deleteIfExists(to);
    }

    if (!from.renameTo(to)) {
      throw new IOException();
    }
  }

  public synchronized io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Snapshot get(String key) throws IOException {
    this.checkNotClosed();
    this.validateKey(key);
    io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry entry = (io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry)this.lruEntries.get(key);
    if (entry == null) {
      return null;
    } else if (!entry.readable) {
      return null;
    } else {
      File[] files = new File[this.valueCount];
      InputStream[] ins = new InputStream[this.valueCount];

      int i;
      try {
        for(i = 0; i < this.valueCount; ++i) {
          File file = entry.getCleanFile(i);
          files[i] = file;
          ins[i] = new FileInputStream(file);
        }
      } catch (FileNotFoundException var7) {
        for(i = 0; i < this.valueCount && ins[i] != null; ++i) {
          Util.closeQuietly(ins[i]);
        }

        return null;
      }

      ++this.redundantOpCount;
      this.journalWriter.append("READ " + key + '\n');
      if (this.journalRebuildRequired()) {
        this.executorService.submit(this.cleanupCallable);
      }

      return new io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Snapshot(key, entry.sequenceNumber, files, ins, entry.lengths);
    }
  }

  public io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Editor edit(String key) throws IOException {
    return this.edit(key, -1L);
  }

  private synchronized io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Editor edit(String key, long expectedSequenceNumber) throws IOException {
    this.checkNotClosed();
    this.validateKey(key);
    io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry entry = (io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry)this.lruEntries.get(key);
    if (expectedSequenceNumber == -1L || entry != null && entry.sequenceNumber == expectedSequenceNumber) {
      if (entry == null) {
        entry = new io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry(key);
        this.lruEntries.put(key, entry);
      } else if (entry.currentEditor != null) {
        return null;
      }

      io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Editor editor = new io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Editor(entry);
      entry.currentEditor = editor;
      this.journalWriter.write("DIRTY " + key + '\n');
      this.journalWriter.flush();
      return editor;
    } else {
      return null;
    }
  }

  public File getDirectory() {
    return this.directory;
  }

  public synchronized long getMaxSize() {
    return this.maxSize;
  }

  public synchronized int getMaxFileCount() {
    return this.maxFileCount;
  }

  public synchronized void setMaxSize(long maxSize) {
    this.maxSize = maxSize;
    this.executorService.submit(this.cleanupCallable);
  }

  public synchronized long size() {
    return this.size;
  }

  public synchronized long fileCount() {
    return (long)this.fileCount;
  }

  private synchronized void completeEdit(io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Editor editor, boolean success) throws IOException {
    io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry entry = editor.entry;
    if (entry.currentEditor != editor) {
      throw new IllegalStateException();
    } else {
      int i;
      if (success && !entry.readable) {
        for(i = 0; i < this.valueCount; ++i) {
          if (!editor.written[i]) {
            editor.abort();
            throw new IllegalStateException("Newly created entry didn't create value for index " + i);
          }

          if (!entry.getDirtyFile(i).exists()) {
            editor.abort();
            return;
          }
        }
      }

      for(i = 0; i < this.valueCount; ++i) {
        File dirty = entry.getDirtyFile(i);
        if (success) {
          if (dirty.exists()) {
            File clean = entry.getCleanFile(i);
            dirty.renameTo(clean);
            long oldLength = entry.lengths[i];
            long newLength = clean.length();
            entry.lengths[i] = newLength;
            this.size = this.size - oldLength + newLength;
            ++this.fileCount;
          }
        } else {
          deleteIfExists(dirty);
        }
      }

      ++this.redundantOpCount;
      entry.currentEditor = null;
      if (entry.readable | success) {
        entry.readable = true;
        this.journalWriter.write("CLEAN " + entry.key + entry.getLengths() + '\n');
        if (success) {
          entry.sequenceNumber = (long)(this.nextSequenceNumber++);
        }
      } else {
        this.lruEntries.remove(entry.key);
        this.journalWriter.write("REMOVE " + entry.key + '\n');
      }

      this.journalWriter.flush();
      if (this.size > this.maxSize || this.fileCount > this.maxFileCount || this.journalRebuildRequired()) {
        this.executorService.submit(this.cleanupCallable);
      }

    }
  }

  private boolean journalRebuildRequired() {
    return this.redundantOpCount >= 2000 && this.redundantOpCount >= this.lruEntries.size();
  }

  public synchronized boolean remove(String key) throws IOException {
    this.checkNotClosed();
    this.validateKey(key);
    io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry entry = (io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry)this.lruEntries.get(key);
    if (entry != null && entry.currentEditor == null) {
      for(int i = 0; i < this.valueCount; ++i) {
        File file = entry.getCleanFile(i);
        if (file.exists() && !file.delete()) {
          throw new IOException("failed to delete " + file);
        }

        this.size -= entry.lengths[i];
        --this.fileCount;
        entry.lengths[i] = 0L;
      }

      ++this.redundantOpCount;
      this.journalWriter.append("REMOVE " + key + '\n');
      this.lruEntries.remove(key);
      if (this.journalRebuildRequired()) {
        this.executorService.submit(this.cleanupCallable);
      }

      return true;
    } else {
      return false;
    }
  }

  public synchronized boolean isClosed() {
    return this.journalWriter == null;
  }

  private void checkNotClosed() {
    if (this.journalWriter == null) {
      throw new IllegalStateException("cache is closed");
    }
  }

  public synchronized void flush() throws IOException {
    this.checkNotClosed();
    this.trimToSize();
    this.trimToFileCount();
    this.journalWriter.flush();
  }

  public synchronized void close() throws IOException {
    if (this.journalWriter != null) {
      Iterator var1 = (new ArrayList(this.lruEntries.values())).iterator();

      while(var1.hasNext()) {
        io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry entry = (io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry)var1.next();
        if (entry.currentEditor != null) {
          entry.currentEditor.abort();
        }
      }

      this.trimToSize();
      this.trimToFileCount();
      this.journalWriter.close();
      this.journalWriter = null;
    }
  }

  private void trimToSize() throws IOException {
    while(this.size > this.maxSize) {
      java.util.Map.Entry<String, io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry> toEvict = (java.util.Map.Entry)this.lruEntries.entrySet().iterator().next();
      this.remove((String)toEvict.getKey());
    }

  }

  private void trimToFileCount() throws IOException {
    while(this.fileCount > this.maxFileCount) {
      java.util.Map.Entry<String, io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry> toEvict = (java.util.Map.Entry)this.lruEntries.entrySet().iterator().next();
      this.remove((String)toEvict.getKey());
    }

  }

  public void delete() throws IOException {
    this.close();
    Util.deleteContents(this.directory);
  }

  private void validateKey(String key) {
    Matcher matcher = LEGAL_KEY_PATTERN.matcher(key);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("keys must match regex [a-z0-9_-]{1,64}: \"" + key + "\"");
    }
  }

  private static String inputStreamToString(InputStream in) throws IOException {
    return Util.readFully(new InputStreamReader(in, Util.UTF_8));
  }

  private final class Entry {
    private final String key;
    private final long[] lengths;
    private boolean readable;
    private io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Editor currentEditor;
    private long sequenceNumber;

    private Entry(String key) {
      this.key = key;
      this.lengths = new long[io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.valueCount];
    }

    public String getLengths() throws IOException {
      StringBuilder result = new StringBuilder();
      long[] var2 = this.lengths;
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
        long size = var2[var4];
        result.append(' ').append(size);
      }

      return result.toString();
    }

    private void setLengths(String[] strings) throws IOException {
      if (strings.length != io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.valueCount) {
        throw this.invalidLengths(strings);
      } else {
        try {
          for(int i = 0; i < strings.length; ++i) {
            this.lengths[i] = Long.parseLong(strings[i]);
          }

        } catch (NumberFormatException var3) {
          throw this.invalidLengths(strings);
        }
      }
    }

    private IOException invalidLengths(String[] strings) throws IOException {
      throw new IOException("unexpected journal line: " + Arrays.toString(strings));
    }

    public File getCleanFile(int i) {
      return new File(io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.directory, this.key + "." + i);
    }

    public File getDirtyFile(int i) {
      return new File(io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.directory, this.key + "." + i + ".tmp");
    }
  }

  public final class Editor {
    private final io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry entry;
    private final boolean[] written;
    private boolean hasErrors;
    private boolean committed;

    private Editor(io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Entry entry) {
      this.entry = entry;
      this.written = entry.readable ? null : new boolean[io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.valueCount];
    }

    public InputStream newInputStream(int index) throws IOException {
      io.rong.imageloader.cache.disc.impl.ext.DiskLruCache var2 = io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this;
      synchronized(io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this) {
        if (this.entry.currentEditor != this) {
          throw new IllegalStateException();
        } else if (!this.entry.readable) {
          return null;
        } else {
          FileInputStream var10000;
          try {
            var10000 = new FileInputStream(this.entry.getCleanFile(index));
          } catch (FileNotFoundException var5) {
            return null;
          }

          return var10000;
        }
      }
    }

    public String getString(int index) throws IOException {
      InputStream in = this.newInputStream(index);
      return in != null ? io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.inputStreamToString(in) : null;
    }

    public OutputStream newOutputStream(int index) throws IOException {
      io.rong.imageloader.cache.disc.impl.ext.DiskLruCache var2 = io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this;
      synchronized(io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this) {
        if (this.entry.currentEditor != this) {
          throw new IllegalStateException();
        } else {
          if (!this.entry.readable) {
            this.written[index] = true;
          }

          File dirtyFile = this.entry.getDirtyFile(index);

          FileOutputStream outputStream;
          try {
            outputStream = new FileOutputStream(dirtyFile);
          } catch (FileNotFoundException var9) {
            io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.directory.mkdirs();

            try {
              outputStream = new FileOutputStream(dirtyFile);
            } catch (FileNotFoundException var8) {
              return io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.NULL_OUTPUT_STREAM;
            }
          }

          return new io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Editor.FaultHidingOutputStream(outputStream);
        }
      }
    }

    public void set(int index, String value) throws IOException {
      OutputStreamWriter writer = null;

      try {
        writer = new OutputStreamWriter(this.newOutputStream(index), Util.UTF_8);
        writer.write(value);
      } finally {
        Util.closeQuietly(writer);
      }

    }

    public void commit() throws IOException {
      if (this.hasErrors) {
        io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.completeEdit(this, false);
        io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.remove(this.entry.key);
      } else {
        io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.completeEdit(this, true);
      }

      this.committed = true;
    }

    public void abort() throws IOException {
      io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.completeEdit(this, false);
    }

    public void abortUnlessCommitted() {
      if (!this.committed) {
        try {
          this.abort();
        } catch (IOException var2) {
          ;
        }
      }

    }

    private class FaultHidingOutputStream extends FilterOutputStream {
      private FaultHidingOutputStream(OutputStream out) {
        super(out);
      }

      public void write(int oneByte) {
        try {
          this.out.write(oneByte);
        } catch (IOException var3) {
          Editor.this.hasErrors = true;
        }

      }

      public void write(byte[] buffer, int offset, int length) {
        try {
          this.out.write(buffer, offset, length);
        } catch (IOException var5) {
          Editor.this.hasErrors = true;
        }

      }

      public void close() {
        try {
          this.out.close();
        } catch (IOException var2) {
          Editor.this.hasErrors = true;
        }

      }

      public void flush() {
        try {
          this.out.flush();
        } catch (IOException var2) {
          Editor.this.hasErrors = true;
        }

      }
    }
  }

  public final class Snapshot implements Closeable {
    private final String key;
    private final long sequenceNumber;
    private File[] files;
    private final InputStream[] ins;
    private final long[] lengths;

    private Snapshot(String key, long sequenceNumber, File[] files, InputStream[] ins, long[] lengths) {
      this.key = key;
      this.sequenceNumber = sequenceNumber;
      this.files = files;
      this.ins = ins;
      this.lengths = lengths;
    }

    public io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.Editor edit() throws IOException {
      return io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.this.edit(this.key, this.sequenceNumber);
    }

    public File getFile(int index) {
      return this.files[index];
    }

    public InputStream getInputStream(int index) {
      return this.ins[index];
    }

    public String getString(int index) throws IOException {
      return io.rong.imageloader.cache.disc.impl.ext.DiskLruCache.inputStreamToString(this.getInputStream(index));
    }

    public long getLength(int index) {
      return this.lengths[index];
    }

    public void close() {
      InputStream[] var1 = this.ins;
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
        InputStream in = var1[var3];
        Util.closeQuietly(in);
      }

    }
  }
}

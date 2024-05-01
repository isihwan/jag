package org.sean.kim.jag.util;

import android.content.ContentValues;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

final public class Logger {
  private static final int SECOND_TAG_LEN = 10;
  private final String TAG;
  private final String secondTag;
  private final boolean debugLine;
  private final int LEVEL;

  public Logger(String tag) {
    this(tag, null);
  }

  public Logger(String tag, String secondTag) { this(tag, secondTag, org.sean.kim.jag.BuildConfig.LOG_LIMIT); }

  public Logger(String tag, String secondTag, int level) { this(tag, secondTag, org.sean.kim.jag.BuildConfig.LOG_LIMIT, false); }

  public Logger(String tag, String secondTag, int level, boolean debugLine) {
    TAG = tag;
    if (secondTag != null)
      this.secondTag = secondTag.length() >= SECOND_TAG_LEN ? secondTag.substring(0, SECOND_TAG_LEN) : appendSpace(secondTag, SECOND_TAG_LEN);
    else
      this.secondTag = null;
    this.debugLine = debugLine;
    LEVEL = level;
  }

  private static String appendSpace(final String tag, final int tagLength) {
    final int appends = tagLength - tag.length();
    StringBuilder sb = new StringBuilder(tag);
    for (int i = 0; i < appends; ++i) {
      sb.append(' ');
    }
    return sb.toString();
  }

  private String createLog(StackTraceElement[] sElements, final String msgFormat, Object... args) {
    return (secondTag == null ? "" : (secondTag + ": ")) + "[" + sElements[1].getFileName() + ':' + sElements[1].getLineNumber() + "] : " + (args.length == 0 ? msgFormat : String.format(msgFormat, args));
  }

  public final boolean isEnableV() {
    return LEVEL <= Log.VERBOSE;
  }

  public final void v(final String msgFormat, Object... args) {
    if (LEVEL <= Log.VERBOSE) {
      if (debugLine) {
        Log.v(TAG, createLog(new Throwable().getStackTrace(), msgFormat, args));
      } else {
        Log.v(TAG, (secondTag == null ? "" : secondTag+": ") + (args.length == 0 ? msgFormat : String.format(msgFormat, args)));
      }
    }
  }

  public final boolean isEnableD() {
    return LEVEL <= Log.DEBUG;
  }

  public final void d(final String msgFormat, Object... args) {
    if (LEVEL <= Log.DEBUG) {
      if (debugLine) {
        Log.d(TAG, createLog(new Throwable().getStackTrace(), msgFormat, args));
      } else {
        Log.d(TAG, (secondTag == null ? "" : secondTag+": ") + (args.length == 0 ? msgFormat : String.format(msgFormat, args)));
      }
    }
  }

  public final boolean isEnableI() {
    return LEVEL <= Log.INFO;
  }
  
  public final void i(final String msgFormat, Object... args) {
    if (LEVEL <= Log.INFO) {
      if (debugLine) {
        Log.i(TAG, createLog(new Throwable().getStackTrace(), msgFormat, args));
      } else {
        Log.i(TAG, (secondTag == null ? "" : secondTag+": ") + (args.length == 0 ? msgFormat : String.format(msgFormat, args)));
      }
    }
  }

  public final void w(final String msgFormat, Object... args) {
    if (LEVEL <= Log.WARN) {
      if (debugLine) {
        Log.w(TAG, createLog(new Throwable().getStackTrace(), msgFormat, args));
      } else {
        Log.w(TAG, (secondTag == null ? "" : secondTag+": ") + (args.length == 0 ? msgFormat : String.format(msgFormat, args)));
      }
    }
  }

  public final void e(final String msgFormat, Object... args) {
    Log.e(TAG, createLog(new Throwable().getStackTrace(), msgFormat, args));
  }

  public static String toString(Iterable<String> iterable) {
    StringBuilder result = new StringBuilder();
    if (iterable == null)
      return "null";
    Iterator<String> iter;
    for (iter = iterable.iterator(); iter.hasNext();) {
      String s = iter.next();
      result.append(", ").append(s);
    }
    return result.toString();
  }

  public static String toString(ContentValues values) {
    StringBuilder result = new StringBuilder();
    if (values == null)
      return "null";
    Set<Map.Entry<String, Object>> keys = values.valueSet();
    for (Map.Entry<String, Object> e : keys) {
      result.append(" ,").append(e.getKey()).append(":").append(e.getValue().toString());
    }

    return result.toString();
  }

  public static String toString(String [] values) {
    StringBuilder result = new StringBuilder();
    if (values == null)
      return "null";

    for (String s : values) {
      result.append(" ,").append(s);
    }

    return result.toString();
  }

  public static String toString(ByteBuffer buffer) {
    StringBuilder result = new StringBuilder();
    if (buffer == null)
      return "null";
    int n = 0;
    for (int i = 0; i < buffer.limit(); ++i) {
      if (n%16 == 0) {
          result.append(String.format("%04x: ", n));
      }
      if ((buffer.get(i) & 0xFF) > 0x0f)
          result.append("0x").append(Integer.toHexString(buffer.get(i) & 0xFF)).append(",");
      else
          result.append("0x0").append(Integer.toHexString(buffer.get(i) & 0xFF)).append(",");
      ++n;
      if (n%16 == 0) {
          result.append("\n");
      }
    }
    return result.toString();
  }

  public final String memDPrint(String string, byte[] value, int offset, int length) {
    if (LEVEL <= Log.DEBUG) {
      return memPrint(string, value, offset, length);
    }
    return "";
  }

  public static String memPrint(String string, byte[] value, int offset, int length) {
    StringBuilder sbuilder = new StringBuilder();
    sbuilder.append(string);
    if (length > 16) {
      sbuilder.append('\n');
    }
    int n = 0;
    for (int i = offset; i < (offset + length); ++i) {
      if (n%16 == 0) {
        sbuilder.append(String.format("%04x: ", n));
      }
      if ((value[i] & 0xFF) > 0x0f)
        sbuilder.append("0x").append(Integer.toHexString(value[i] & 0xFF)).append(",");
      else
        sbuilder.append("0x0").append(Integer.toHexString(value[i] & 0xFF)).append(",");
      ++n;
      if (n%16 == 0) {
        sbuilder.append("\n");
      }
    }
    return sbuilder.toString();
  }

  public static<T> String collectionPrint(Collection<T> list) {
    StringBuilder sbuilder = new StringBuilder();
    for (T o : list) {
      sbuilder.append(o.toString());
      sbuilder.append(',');
    }
    return sbuilder.toString();
  }

  public final void printStack(int cnt) {
    if (LEVEL <= Log.DEBUG) {
      StringBuilder sb = new StringBuilder("======print stack ====\n");
      StackTraceElement[] elem = new Throwable().getStackTrace();
      for (int i = 1; i < elem.length && cnt > 0; ++i, --cnt) {
        sb.append('\t');
        sb.append(elem[i].getClassName());
        sb.append(':');
        sb.append(elem[i].getMethodName());
        sb.append('\n');
      }
      Log.d(TAG, sb.toString());
    }
  }
}

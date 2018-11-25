//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit;

public class InputBar {
  public InputBar() {
  }

  public static enum Type {
    TYPE_DEFAULT,
    TYPE_CS_ROBOT,
    TYPE_CS_HUMAN,
    TYPE_CS_ROBOT_FIRST,
    TYPE_CS_HUMAN_FIRST;

    private Type() {
    }
  }

  public static enum Style {
    STYLE_SWITCH_CONTAINER_EXTENSION(291),
    STYLE_SWITCH_CONTAINER(288),
    STYLE_CONTAINER_EXTENSION(35),
    STYLE_EXTENSION_CONTAINER(800),
    STYLE_CONTAINER(32);

    int v;

    private Style(int v) {
      this.v = v;
    }

    public static io.rong.imkit.InputBar.Style getStyle(int v) {
      io.rong.imkit.InputBar.Style result = null;
      io.rong.imkit.InputBar.Style[] var2 = values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
        io.rong.imkit.InputBar.Style style = var2[var4];
        if (style.v == v) {
          result = style;
          break;
        }
      }

      return result;
    }
  }
}
